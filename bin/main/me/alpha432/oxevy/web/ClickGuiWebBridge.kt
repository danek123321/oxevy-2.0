package me.alpha432.oxevy.web

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import me.alpha432.oxevy.Oxevy
import me.alpha432.oxevy.features.modules.Module
import me.alpha432.oxevy.features.settings.Bind
import me.alpha432.oxevy.features.settings.Setting
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW
import java.awt.Color
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * ClickGuiWebBridge  –  Fabric 1.21 edition
 *
 * Uses a raw ServerSocket instead of com.sun.net.httpserver (which is
 * unavailable / restricted on the JVM bundled with modern Minecraft launchers).
 *
 * Endpoints:
 *   GET  /              → index.html
 *   GET  /style.css     → style.css
 *   GET  /app.js        → app.js
 *   GET  /api/state     → full module state JSON
 *   POST /api/module/toggle  body: {"module":"KillAura"}
 *   POST /api/setting        body: {"module":"X","setting":"Y","value":...}
 *   OPTIONS *           → CORS preflight
 */
object ClickGuiWebBridge {

    private val gson = GsonBuilder().disableHtmlEscaping().create()
    private val running = AtomicBoolean(false)
    private val executor = Executors.newCachedThreadPool { r ->
        Thread(r, "WebGui-Worker").also { it.isDaemon = true }
    }

    @Volatile private var serverSocket: ServerSocket? = null
    @Volatile private var activePort: Int = 8765

    // ── Public API ──────────────────────────────────────────────────────────

    @JvmStatic
    fun ensureStarted(): String {
        if (running.get()) return baseUrl()
        synchronized(this) {
            if (running.get()) return baseUrl()

            var bound: ServerSocket? = null
            for (port in 8765..8775) {
                try {
                    bound = ServerSocket(port).also { it.reuseAddress = true }
                    activePort = port
                    break
                } catch (_: Exception) {}
            }
            val ss = bound ?: error("Could not bind Web ClickGui on ports 8765-8775")
            serverSocket = ss
            running.set(true)

            Thread({
                Oxevy.LOGGER.info("[WebGui] Listening on {}", baseUrl())
                while (running.get()) {
                    val client = try { ss.accept() } catch (_: SocketException) { break } catch (_: Exception) { continue }
                    executor.submit { handleClient(client) }
                }
            }, "WebGui-Accept").also { it.isDaemon = true }.start()
        }
        return baseUrl()
    }

    @JvmStatic
    fun stop() {
        running.set(false)
        try { serverSocket?.close() } catch (_: Exception) {}
        serverSocket = null
    }

    @JvmStatic
    fun launch(openBrowser: Boolean): String {
        val url = ensureStarted()
        if (openBrowser) {
            try {
                val desktop = Class.forName("java.awt.Desktop")
                val inst = desktop.getMethod("getDesktop").invoke(null)
                desktop.getMethod("browse", java.net.URI::class.java).invoke(inst, java.net.URI(url))
            } catch (_: Exception) {
                Oxevy.LOGGER.warn("[WebGui] Could not open browser automatically. Visit: {}", url)
            }
        }
        return url
    }

    // ── Raw HTTP server ─────────────────────────────────────────────────────

    private fun handleClient(socket: Socket) {
        try {
            socket.soTimeout = 5_000
            socket.use {
                val reader = BufferedReader(InputStreamReader(it.getInputStream(), StandardCharsets.UTF_8))
                val out    = it.getOutputStream()

                // Parse request line
                val requestLine = reader.readLine() ?: return
                val parts = requestLine.split(" ")
                if (parts.size < 2) return
                val method = parts[0].uppercase()
                val fullPath = parts[1]
                val path = fullPath.substringBefore("?")

                // Parse headers
                val headers = mutableMapOf<String, String>()
                var line = reader.readLine()
                while (!line.isNullOrBlank()) {
                    val colon = line.indexOf(':')
                    if (colon != -1) {
                        headers[line.substring(0, colon).trim().lowercase()] = line.substring(colon + 1).trim()
                    }
                    line = reader.readLine()
                }

                // Read body for POST
                val body: String = if (method == "POST") {
                    val len = headers["content-length"]?.toIntOrNull() ?: 0
                    val buf = CharArray(len)
                    var read = 0
                    while (read < len) {
                        val n = reader.read(buf, read, len - read)
                        if (n == -1) break
                        read += n
                    }
                    String(buf, 0, read)
                } else ""

                // Route
                when {
                    method == "OPTIONS"                          -> sendCors(out)
                    method == "GET" && path == "/api/state"      -> handleState(out)
                    method == "POST" && path == "/api/module/toggle" -> handleToggle(out, body)
                    method == "POST" && path == "/api/setting"   -> handleSetting(out, body)
                    method == "GET"                              -> handleStatic(out, path)
                    else -> sendText(out, 405, "Method Not Allowed")
                }
            }
        } catch (e: Exception) {
            Oxevy.LOGGER.debug("[WebGui] Client error: {}", e.message)
        }
    }

    // ── Route handlers ──────────────────────────────────────────────────────

    private fun handleState(out: OutputStream) {
        val state = onClientThread { buildState() }
        sendJson(out, 200, state)
    }

    private fun handleToggle(out: OutputStream, body: String) {
        val json = parseJson(body) ?: run { sendText(out, 400, "Bad JSON"); return }
        val moduleId = json.get("module")?.asString
        if (moduleId.isNullOrBlank()) { sendText(out, 400, "Missing module id"); return }

        val error = onClientThread {
            val m = Oxevy.moduleManager.getModuleByName(moduleId)
                ?: return@onClientThread "Unknown module: $moduleId"
            m.toggle()
            null
        }
        if (error != null) { sendText(out, 404, error); return }
        sendJson(out, 200, onClientThread { buildState() })
    }

    private fun handleSetting(out: OutputStream, body: String) {
        val json = parseJson(body) ?: run { sendText(out, 400, "Bad JSON"); return }
        val moduleId    = json.get("module")?.asString
        val settingName = json.get("setting")?.asString
        val value       = json.get("value")
        if (moduleId.isNullOrBlank() || settingName.isNullOrBlank() || value == null) {
            sendText(out, 400, "Missing module, setting, or value"); return
        }
        val error = onClientThread { applySettingUpdate(moduleId, settingName, value) }
        if (error != null) { sendText(out, 400, error); return }
        sendJson(out, 200, onClientThread { buildState() })
    }

    private fun handleStatic(out: OutputStream, path: String) {
        val rawPath = path.removePrefix("/")
        if (rawPath.contains("..")) { sendText(out, 403, "Forbidden"); return }

        val resourcePath = if (rawPath.isBlank()) "webclickgui/index.html" else "webclickgui/$rawPath"
        val stream = javaClass.classLoader.getResourceAsStream(resourcePath)
            ?: run { sendText(out, 404, "Not found: $resourcePath"); return }

        val bytes = stream.use { s ->
            val buf = ByteArrayOutputStream()
            s.copyTo(buf)
            buf.toByteArray()
        }

        val mime = contentType(resourcePath)
        val header = buildString {
            append("HTTP/1.1 200 OK\r\n")
            append("Content-Type: $mime\r\n")
            append("Content-Length: ${bytes.size}\r\n")
            appendCors()
            append("Cache-Control: no-store\r\n")
            append("Connection: close\r\n\r\n")
        }
        out.write(header.toByteArray(StandardCharsets.UTF_8))
        out.write(bytes)
        out.flush()
    }

    // ── State builder ───────────────────────────────────────────────────────

    private fun buildState(): JsonObject {
        val root = JsonObject()
        root.addProperty("name", "Oxevy")
        root.addProperty("version", "1.0")
        root.addProperty("url", baseUrl())
        root.addProperty("activeCount",
            Oxevy.moduleManager.modules.count { !it.hidden && it.isEnabled })

        val categoriesArray = JsonArray()
        for (category in Module.Category.values()) {
            val catObj = JsonObject()
            val modules = Oxevy.moduleManager.getModulesByCategory(category).filter { !it.hidden }
            catObj.addProperty("key", category.name)
            catObj.addProperty("name", category.name
                .lowercase()
                .replaceFirstChar { it.uppercaseChar() })
            catObj.addProperty("enabledCount", modules.count { it.isEnabled })

            val moduleArray = JsonArray()
            for (module in modules) moduleArray.add(buildModuleJson(module))
            catObj.add("modules", moduleArray)
            categoriesArray.add(catObj)
        }
        root.add("categories", categoriesArray)
        return root
    }

    private fun buildModuleJson(module: Module): JsonObject {
        val obj = JsonObject()
        obj.addProperty("id",          module.name)
        obj.addProperty("name",        module.name)
        obj.addProperty("description", module.description ?: "")
        obj.addProperty("category",    module.category.name)
        obj.addProperty("enabled",     module.isEnabled)

        // Keybind
        val bindObj = JsonObject()
        try {
            val bind = module.getBind()
            bindObj.addProperty("key",   bind?.getKey() ?: -1)
            bindObj.addProperty("label", bind?.toString() ?: "None")
        } catch (_: Exception) {
            bindObj.addProperty("key", -1)
            bindObj.addProperty("label", "None")
        }
        obj.add("bind", bindObj)

        // Settings
        val settingsArray = JsonArray()
        for (s in module.settings) {
            if (s.name.equals("Enabled", true) || s.name.equals("DisplayName", true)) continue
            try { settingsArray.add(buildSettingJson(s)) } catch (_: Exception) {}
        }
        obj.add("settings", settingsArray)
        return obj
    }

    private fun buildSettingJson(setting: Setting<*>): JsonObject {
        val obj   = JsonObject()
        val value = setting.value
        val type  = settingType(setting)

        obj.addProperty("id",   setting.name)
        obj.addProperty("name", setting.name)
        obj.addProperty("type", type)
        obj.addProperty("description", setting.description ?: "")

        when (type) {
            "boolean" -> obj.addProperty("value", value as Boolean)
            "number"  -> {
                obj.addProperty("value", (value as Number).toDouble())
                setting.min?.let { obj.addProperty("min", (it as Number).toDouble()) }
                setting.max?.let { obj.addProperty("max", (it as Number).toDouble()) }
                obj.addProperty("step",
                    if (value is Int || value is Long || value is Short || value is Byte) 1 else 0.1)
            }
            "string"  -> obj.addProperty("value", value as String)
            "color"   -> obj.add("value", buildColorJson(value as Color))
            "bind"    -> obj.add("value", buildBindJson(value as Bind))
            "enum"    -> {
                val enumValue = value as Enum<*>
                obj.addProperty("value", enumValue.name)
                val options = JsonArray()
                enumValue.javaClass.enumConstants.forEach { options.add((it as Enum<*>).name) }
                obj.add("options", options)
            }
        }
        return obj
    }

    private fun buildColorJson(color: Color): JsonObject {
        val o = JsonObject()
        o.addProperty("r", color.red)
        o.addProperty("g", color.green)
        o.addProperty("b", color.blue)
        o.addProperty("a", color.alpha)
        return o
    }

    private fun buildBindJson(bind: Bind): JsonObject {
        val o = JsonObject()
        o.addProperty("key",   bind.key)
        o.addProperty("label", bind.toString())
        return o
    }

    private fun settingType(setting: Setting<*>): String = when (val v = setting.value) {
        is Boolean -> "boolean"
        is Number  -> if (setting.min != null) "number" else "unknown"
        is String  -> "string"
        is Color   -> "color"
        is Bind    -> "bind"
        is Enum<*> -> "enum"
        else       -> "unknown"
    }

    // ── Setting appliers ────────────────────────────────────────────────────

    private fun applySettingUpdate(moduleId: String, settingName: String, value: JsonElement): String? {
        val module = Oxevy.moduleManager.getModuleByName(moduleId)
            ?: return "Unknown module: $moduleId"
        val setting = module.settings.firstOrNull { it.name.equals(settingName, true) }
            ?: return "Unknown setting: $settingName in $moduleId"
        return try {
            when (settingType(setting)) {
                "boolean" -> @Suppress("UNCHECKED_CAST") (setting as Setting<Boolean>).setValue(value.asBoolean)
                "number"  -> applyNumericSetting(setting, value.asDouble)
                "string"  -> @Suppress("UNCHECKED_CAST") (setting as Setting<String>).setValue(value.asString)
                "color"   -> @Suppress("UNCHECKED_CAST") applyColorSetting(setting as Setting<Color>, value.asJsonObject)
                "bind"    -> @Suppress("UNCHECKED_CAST") applyBindSetting(setting as Setting<Bind>, value)
                "enum"    -> applyEnumSetting(setting, value.asString)
                else      -> return "Unsupported setting type"
            }
            null
        } catch (e: Exception) {
            "Failed to update ${module.name}.${setting.name}: ${e.message}"
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun applyEnumSetting(setting: Setting<*>, requestedValue: String) {
        val current = setting.value as Enum<*>
        val next = current.javaClass.enumConstants
            .map { it as Enum<*> }
            .firstOrNull { it.name.equals(requestedValue, true) }
            ?: throw IllegalArgumentException("Unknown enum value: $requestedValue")
        (setting as Setting<Enum<*>>).setValue(next)
    }

    private fun applyNumericSetting(setting: Setting<*>, raw: Double) {
        when (setting.value) {
            is Int    -> @Suppress("UNCHECKED_CAST") (setting as Setting<Int>).setValue(raw.roundToInt())
            is Float  -> @Suppress("UNCHECKED_CAST") (setting as Setting<Float>).setValue(raw.toFloat())
            is Double -> @Suppress("UNCHECKED_CAST") (setting as Setting<Double>).setValue(raw)
            is Long   -> @Suppress("UNCHECKED_CAST") (setting as Setting<Long>).setValue(raw.roundToLong())
            is Short  -> @Suppress("UNCHECKED_CAST") (setting as Setting<Short>).setValue(raw.roundToInt().toShort())
            is Byte   -> @Suppress("UNCHECKED_CAST") (setting as Setting<Byte>).setValue(raw.roundToInt().toByte())
            else      -> throw IllegalArgumentException("Unsupported number type")
        }
    }

    private fun applyColorSetting(setting: Setting<Color>, value: JsonObject) {
        setting.setValue(Color(
            value.get("r").asInt.coerceIn(0, 255),
            value.get("g").asInt.coerceIn(0, 255),
            value.get("b").asInt.coerceIn(0, 255),
            value.get("a").asInt.coerceIn(0, 255)
        ))
    }

    private fun applyBindSetting(setting: Setting<Bind>, value: JsonElement) {
        val key = when {
            value.isJsonPrimitive -> value.asInt
            value.isJsonObject && value.asJsonObject.has("key") -> value.asJsonObject["key"].asInt
            else -> throw IllegalArgumentException("Bind requires a key code")
        }
        setting.setValue(if (key < 0) Bind.none() else Bind(key))
    }

    // ── HTTP helpers ────────────────────────────────────────────────────────

    private fun sendJson(out: OutputStream, status: Int, body: JsonObject) {
        val bytes = gson.toJson(body).toByteArray(StandardCharsets.UTF_8)
        val header = buildString {
            append("HTTP/1.1 $status ${reasonPhrase(status)}\r\n")
            append("Content-Type: application/json; charset=utf-8\r\n")
            append("Content-Length: ${bytes.size}\r\n")
            appendCors()
            append("Cache-Control: no-store\r\nConnection: close\r\n\r\n")
        }
        out.write(header.toByteArray(StandardCharsets.UTF_8))
        out.write(bytes)
        out.flush()
    }

    private fun sendText(out: OutputStream, status: Int, message: String) {
        val bytes = message.toByteArray(StandardCharsets.UTF_8)
        val header = buildString {
            append("HTTP/1.1 $status ${reasonPhrase(status)}\r\n")
            append("Content-Type: text/plain; charset=utf-8\r\n")
            append("Content-Length: ${bytes.size}\r\n")
            appendCors()
            append("Connection: close\r\n\r\n")
        }
        out.write(header.toByteArray(StandardCharsets.UTF_8))
        out.write(bytes)
        out.flush()
    }

    private fun sendCors(out: OutputStream) {
        val header = buildString {
            append("HTTP/1.1 204 No Content\r\n")
            appendCors()
            append("Connection: close\r\n\r\n")
        }
        out.write(header.toByteArray(StandardCharsets.UTF_8))
        out.flush()
    }

    private fun StringBuilder.appendCors() {
        append("Access-Control-Allow-Origin: *\r\n")
        append("Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n")
        append("Access-Control-Allow-Headers: Content-Type\r\n")
    }

    private fun contentType(path: String): String = when {
        path.endsWith(".html") -> "text/html; charset=utf-8"
        path.endsWith(".js")   -> "application/javascript; charset=utf-8"
        path.endsWith(".css")  -> "text/css; charset=utf-8"
        path.endsWith(".png")  -> "image/png"
        path.endsWith(".ico")  -> "image/x-icon"
        else                   -> "application/octet-stream"
    }

    private fun reasonPhrase(status: Int) = when (status) {
        200 -> "OK"; 204 -> "No Content"; 400 -> "Bad Request"
        403 -> "Forbidden"; 404 -> "Not Found"; 405 -> "Method Not Allowed"
        else -> "Unknown"
    }

    private fun parseJson(text: String): JsonObject? {
        if (text.isBlank()) return null
        return try { JsonParser.parseString(text).asJsonObject } catch (_: Exception) { null }
    }

    private fun baseUrl() = "http://127.0.0.1:$activePort/"

    // ── MC thread dispatch ──────────────────────────────────────────────────

    private fun <T> onClientThread(action: () -> T): T {
        val mc = Minecraft.getInstance()
        if (mc.isSameThread()) return action()
        val future = CompletableFuture<T>()
        mc.execute {
            try { future.complete(action()) }
            catch (e: Throwable) { future.completeExceptionally(e) }
        }
        return future.get(5, TimeUnit.SECONDS)
    }

    // ── Key code helper (unchanged from original) ───────────────────────────

    fun glfwKeyFromCode(code: String): Int? {
        if (code.startsWith("Key") && code.length == 4) {
            val letter = code[3].uppercaseChar()
            return if (letter in 'A'..'Z') GLFW.GLFW_KEY_A + (letter.code - 'A'.code) else null
        }
        if (code.startsWith("Digit") && code.length == 6) {
            val digit = code[5]
            return if (digit in '0'..'9') GLFW.GLFW_KEY_0 + (digit.code - '0'.code) else null
        }
        return when (code.lowercase(Locale.ROOT)) {
            "space"        -> GLFW.GLFW_KEY_SPACE
            "escape"       -> GLFW.GLFW_KEY_ESCAPE
            "enter", "numpadenter" -> GLFW.GLFW_KEY_ENTER
            "tab"          -> GLFW.GLFW_KEY_TAB
            "backspace"    -> GLFW.GLFW_KEY_BACKSPACE
            "delete"       -> GLFW.GLFW_KEY_DELETE
            "insert"       -> GLFW.GLFW_KEY_INSERT
            "arrowleft"    -> GLFW.GLFW_KEY_LEFT
            "arrowright"   -> GLFW.GLFW_KEY_RIGHT
            "arrowup"      -> GLFW.GLFW_KEY_UP
            "arrowdown"    -> GLFW.GLFW_KEY_DOWN
            "shiftleft"    -> GLFW.GLFW_KEY_LEFT_SHIFT
            "shiftright"   -> GLFW.GLFW_KEY_RIGHT_SHIFT
            "controlleft"  -> GLFW.GLFW_KEY_LEFT_CONTROL
            "controlright" -> GLFW.GLFW_KEY_RIGHT_CONTROL
            "altleft"      -> GLFW.GLFW_KEY_LEFT_ALT
            "altright"     -> GLFW.GLFW_KEY_RIGHT_ALT
            "minus"        -> GLFW.GLFW_KEY_MINUS
            "equal"        -> GLFW.GLFW_KEY_EQUAL
            "bracketleft"  -> GLFW.GLFW_KEY_LEFT_BRACKET
            "bracketright" -> GLFW.GLFW_KEY_RIGHT_BRACKET
            "backslash"    -> GLFW.GLFW_KEY_BACKSLASH
            "semicolon"    -> GLFW.GLFW_KEY_SEMICOLON
            "quote"        -> GLFW.GLFW_KEY_APOSTROPHE
            "comma"        -> GLFW.GLFW_KEY_COMMA
            "period"       -> GLFW.GLFW_KEY_PERIOD
            "slash"        -> GLFW.GLFW_KEY_SLASH
            "backquote"    -> GLFW.GLFW_KEY_GRAVE_ACCENT
            else           -> null
        }
    }
}
