package me.alpha432.oxevy.features.gui

import me.alpha432.oxevy.Oxevy
import me.alpha432.oxevy.features.modules.Module
import me.alpha432.oxevy.features.modules.client.ClickGuiModule
import me.alpha432.oxevy.features.settings.Bind
import me.alpha432.oxevy.features.settings.Setting
import me.alpha432.oxevy.util.KeyboardUtil
import me.alpha432.oxevy.util.render.RenderUtil
import me.alpha432.oxevy.util.render.gl.ShaderRenderer
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import org.lwjgl.glfw.GLFW
import java.awt.Color

class OxevyGui : Screen(Component.literal("Oxevy")) {

    // ── state ─────────────────────────────────────────────────────
    private var selCategory = Module.Category.COMBAT
    private var selModule: Module? = null
    private var searchText = ""
    private var showDrawer = false
    private var scrollOffset = 0
    private var maxScroll = 0
    private val scrollSpeed = 16

    private val settingComponents = mutableMapOf<Setting<*>, SettingComponent>()
    private var focusedStringSetting: Setting<String>? = null
    private var listeningBindSetting: Setting<Bind>? = null
    private var draggingSlider: SettingComponent? = null

    private val modulesByCat: Map<Module.Category, List<Module>> by lazy {
        val map = mutableMapOf<Module.Category, List<Module>>()
        for (cat in Module.Category.values()) {
            map[cat] = Oxevy.moduleManager.modules.filter { it.category == cat && !it.hidden }
        }
        map
    }

    // ── helpers ───────────────────────────────────────────────────
    private val mc: Minecraft get() = Minecraft.getInstance()
    private val clickGui: ClickGuiModule get() = ClickGuiModule.getInstance()
    private val guiStyle: ClickGuiModule.GuiStyle get() = clickGui.style.value
    private val accent: Color get() = Color(0, 255, 102) // Green accent
    private val accentDark: Color get() {
        val base = accent
        return Color(
            (base.red * 0.5).toInt(),
            (base.green * 0.7).toInt(),
            (base.blue * 0.9).toInt(),
            base.alpha
        )
    }

    private fun guiLeft(): Int = (mc.window.guiScaledWidth - GUI_W) / 2
    private fun guiTop(): Int = (mc.window.guiScaledHeight - GUI_H) / 2

    private fun contentX() = guiLeft() + SIDEBAR_W
    private fun contentW() = GUI_W - SIDEBAR_W
    private fun cardAreaTop() = guiTop() + TOPBAR_H + SEARCH_H + 12
    private fun cardAreaBottom() = guiTop() + GUI_H - 8

    // ── render ────────────────────────────────────────────────────
    override fun render(ctx: GuiGraphics, mx: Int, my: Int, delta: Float) {
        val gx = guiLeft().toFloat()
        val gy = guiTop().toFloat()
        val gw = GUI_W.toFloat()
        val gh = GUI_H.toFloat()

        // Enhanced background with better visual hierarchy
        ctx.fill(0, 0, ctx.guiWidth(), ctx.guiHeight(), BG_DARK_OVERLAY)
        
        // Main GUI background with subtle gradient effect
        ShaderRenderer.drawRoundedRect(ctx, gx, gy, gw, gh, 12f, BG_DARK)
        
        // Top accent bar with gradient
        RenderUtil.rect(ctx, gx + 2f, gy, gx + gw - 2f, gy + 3f, ACCENT_PRIMARY)
        RenderUtil.rect(ctx, gx + 2f, gy + 3f, gx + gw - 2f, gy + 4f, ACCENT_SECONDARY)

        drawSidebar(ctx, gx, gy, mx, my)
        drawTopBar(ctx, gx, gy, mx, my)
        drawSearchBar(ctx, gx, gy, mx, my)
        drawCards(ctx, gx, gy, mx, my, delta)

        if (showDrawer) {
            drawDrawer(ctx, gx, gy, mx, my, delta)
        }

        super.render(ctx, mx, my, delta)
    }

    // ── SIDEBAR ───────────────────────────────────────────────────
    private val sideList = Module.Category.values().toList()

    private fun drawSidebar(ctx: GuiGraphics, gx: Float, gy: Float, mx: Int, my: Int) {
        RenderUtil.rect(ctx, gx, gy + 4f, gx + SIDEBAR_W, gy + GUI_H, BG_SIDEBAR)

        val tex = Identifier.fromNamespaceAndPath("oxevy", "textures/watermark.png")
        val texW = 48
        val texH = (texW * 400 / 800).toInt()
        val texX = gx.toInt() + (SIDEBAR_W - texW) / 2
        ctx.pose().pushMatrix()
        ctx.pose().translate(texX.toFloat(), gy + 10f)
        ctx.pose().scale(texW / 800f, texW / 800f)
        ctx.blit(RenderPipelines.GUI_TEXTURED, tex, 0, 0, 0f, 0f, 800, 400, 800, 400)
        ctx.pose().popMatrix()
        RenderUtil.rect(ctx, gx + 6f, gy + 30f, gx + SIDEBAR_W - 6f, gy + 32f, SEPARATOR)

        var yOff = (gy + 42f).toInt()
        val itemH = 38
        for (cat in sideList) {
            val hover = mx in gx.toInt()..<gx.toInt() + SIDEBAR_W && my in yOff..<yOff + itemH
            val active = cat == selCategory
            val enabledCount = modulesByCat[cat]?.count { it.isEnabled } ?: 0

            if (active) {
                RenderUtil.rect(ctx, gx, yOff.toFloat(), gx + 2f, (yOff + itemH).toFloat(), ACCENT_PRIMARY)
                RenderUtil.rect(ctx, gx + 2f, yOff.toFloat(), gx + SIDEBAR_W, (yOff + itemH).toFloat(), ACCENT_DIM)
            } else if (hover) {
                RenderUtil.rect(ctx, gx + 2f, yOff.toFloat(), gx + SIDEBAR_W, (yOff + itemH).toFloat(), ACCENT_HOVER)
            }

            val label = CATEGORY_LABELS[cat] ?: "?"
            val labelColor = if (active) ACCENT_PRIMARY else if (hover) TXT_PRIMARY else TXT_SECONDARY
            ctx.drawString(mc.font, label, gx.toInt() + (SIDEBAR_W - mc.font.width(label)) / 2, yOff + 8, labelColor)

            if (enabledCount > 0) {
                val countStr = enabledCount.toString()
                ctx.drawString(mc.font, countStr, gx.toInt() + (SIDEBAR_W - mc.font.width(countStr)) / 2, yOff + itemH - 12, TXT_MUTED)
            }

            yOff += itemH
        }
    }

    // ── TOP BAR ───────────────────────────────────────────────────
    private fun drawTopBar(ctx: GuiGraphics, gx: Float, gy: Float, mx: Int, my: Int) {
        val cx = contentX()
        val catName = selCategory.name
        val enabled = modulesByCat[selCategory]?.count { it.isEnabled } ?: 0
        val total = modulesByCat[selCategory]?.size ?: 0
        val info = "$enabled/$total"

        ctx.drawString(mc.font, catName, cx + 16, gy.toInt() + 12, ACCENT_PRIMARY)
        val infoW = mc.font.width(info)
        ctx.drawString(mc.font, info, cx + contentW() - 16 - infoW, gy.toInt() + 12, TXT_SECONDARY)
    }

    // ── SEARCH ────────────────────────────────────────────────────
    private var searchFocused = false

    private fun drawSearchBar(ctx: GuiGraphics, gx: Float, gy: Float, mx: Int, my: Int) {
        val cx = contentX().toFloat()
        val sy = (gy + TOPBAR_H + 6f).toInt()
        val sw = (contentW() - 24).toFloat()

        ShaderRenderer.drawRoundedRect(ctx, cx + 12f, sy.toFloat(), sw, SEARCH_H.toFloat(), 8f, BG_INPUT)
        val hover = mx in (cx + 12f).toInt()..<(cx + 12f + sw).toInt() && my in sy..<sy + SEARCH_H

        val displayText = if (searchText.isEmpty() && !searchFocused) "🔍 Search modules..." else searchText
        val textColor = if (searchText.isEmpty() && !searchFocused) TXT_MUTED else TXT_PRIMARY
        ctx.drawString(mc.font, displayText, (cx + 22f).toInt(), sy + 12, textColor)

        if (searchFocused || hover) {
            RenderUtil.rect(ctx, cx + 12f, sy.toFloat(), cx + 12f + sw, (sy + SEARCH_H).toFloat(), ACCENT_DIM)
        }
        
        // Border accent
        RenderUtil.rect(ctx, cx + 12f, sy.toFloat(), cx + 13f, (sy + SEARCH_H).toFloat(), ACCENT_SECONDARY)
    }

    // ── MODULE CARDS ──────────────────────────────────────────────
    private fun drawCards(ctx: GuiGraphics, gx: Float, gy: Float, mx: Int, my: Int, delta: Float) {
        val cx = contentX().toFloat()
        val cw = (contentW() - 24).toFloat()
        val startY = cardAreaTop()

        val modules = getFilteredModules()
        val clipTop = startY - 2
        val clipBottom = cardAreaBottom()

        ctx.enableScissor(gx.toInt(), clipTop, (gx + GUI_W).toInt(), clipBottom)
        var yOff = startY - scrollOffset

        for (mod in modules) {
            if (yOff + CARD_H < clipTop) { yOff += CARD_H + CARD_GAP; continue }
            if (yOff > clipBottom) break
            drawCard(ctx, mod, cx, cw, yOff, mx, my)
            yOff += CARD_H + CARD_GAP
        }

        maxScroll = maxOf(0, yOff - clipBottom + scrollOffset)
        ctx.disableScissor()

        if (maxScroll > 0) drawScrollbar(ctx, gx, gy, startY, clipTop, clipBottom, yOff)
    }

    private fun drawCard(ctx: GuiGraphics, mod: Module, cx: Float, cw: Float, yOff: Int, mx: Int, my: Int) {
        val hover = mx in (cx + 12f).toInt()..<(cx + 12f + cw).toInt() && my in yOff..<yOff + CARD_H
        val selected = mod == selModule
        val enabled = mod.isEnabled

        // Shadow effect
        if (hover || selected) {
            ctx.fill((cx + 12f).toInt(), yOff + 2, (cx + 12f + cw).toInt(), yOff + CARD_H + 2, 0x22000000)
        }

        val bg = when { selected -> BG_CARD_SEL; hover -> BG_CARD_HOVER; else -> BG_CARD }
        ShaderRenderer.drawRoundedRect(ctx, cx + 12f, yOff.toFloat(), cw, CARD_H.toFloat(), 8f, bg)
        
        // Left accent bar for enabled modules
        if (enabled) {
            RenderUtil.rect(ctx, cx + 12f, yOff.toFloat(), cx + 14f, (yOff + CARD_H).toFloat(), ACCENT_PRIMARY)
        }
        
        val nameColor = if (enabled) TXT_PRIMARY else TXT_SECONDARY
        ctx.drawString(mc.font, mod.getDisplayName(), (cx + 24f).toInt(), yOff + 12, nameColor)

        drawTogglePill(ctx, mod, cx, cw, yOff, enabled)
        drawKeybindBadge(ctx, mod, cx, cw, yOff)
        if (mod.getSettings().isNotEmpty()) {
            ctx.drawString(mc.font, "→", (cx + cw - 18f).toInt(), yOff + (CARD_H - 8) / 2, TXT_SECONDARY)
        }
    }

    private fun drawTogglePill(ctx: GuiGraphics, mod: Module, cx: Float, cw: Float, yOff: Int, enabled: Boolean) {
        val pillW = 32
        val pillH = 16
        val pX = (cx + cw - (if (mod.getSettings().isNotEmpty()) 60f else 32f))
        val pY = yOff + (CARD_H - pillH) / 2f

        val pillColor = if (enabled) ACCENT_PRIMARY else 0xFF2A2A35.toInt()
        RenderUtil.roundRect(ctx, pX, pY, pillW.toFloat(), pillH.toFloat(), 8f, pillColor)
        RenderUtil.roundRect(ctx,
            (if (enabled) pX + pillW - 13f else pX + 2f),
            pY + 2f,
            13f, 12f, 6f, 0xFFFFFFFF.toInt())
    }

    private fun drawKeybindBadge(ctx: GuiGraphics, mod: Module, cx: Float, cw: Float, yOff: Int) {
        val bind = mod.getBind()
        if (bind.isEmpty()) return
        val bindStr = KeyboardUtil.getKeyName(bind.key)
        val bindW = mc.font.width(bindStr) + 12
        val bindX = (cx + cw - 60f - bindW).toInt()
        val bindY = yOff + (CARD_H - 14) / 2
        RenderUtil.roundRect(ctx, bindX.toFloat(), bindY.toFloat(), bindW.toFloat(), 14f, 5f, 0xFF1A1A28.toInt())
        ctx.drawString(mc.font, bindStr, bindX + 6, bindY + 2, ACCENT_SECONDARY)
    }

    // ── SCROLLBAR ─────────────────────────────────────────────────
    private fun drawScrollbar(ctx: GuiGraphics, gx: Float, gy: Float, startY: Int, clipTop: Int, clipBottom: Int, yOff: Int) {
        val trackH = (clipBottom - clipTop).toFloat()
        val totalH = (yOff - startY + scrollOffset).toFloat()
        val barH = trackH * trackH / totalH
        val barY = clipTop.toFloat() + (scrollOffset.toFloat() / maxScroll.toFloat()) * (trackH - barH)
        RenderUtil.rect(ctx, gx + GUI_W - 6f, barY, gx + GUI_W - 2f, barY + barH, ACCENT_DIM)
    }

    // ── SETTINGS DRAWER ──────────────────────────────────────────
    private var drawerScroll = 0
    private var drawerMaxScroll = 0
    private var drawerOpenAnim = 0f

    private fun drawDrawer(ctx: GuiGraphics, gx: Float, gy: Float, mx: Int, my: Int, delta: Float) {
        drawerOpenAnim = (drawerOpenAnim + delta * 0.12f).coerceAtMost(1f)
        val drawerX = (gx + GUI_W - DRAWER_W * drawerOpenAnim).toInt()

        RenderUtil.rect(ctx, gx, gy, drawerX.toFloat(), gy + GUI_H, 0x55000000.toInt())
        RenderUtil.rect(ctx, drawerX.toFloat(), gy + 4f, (drawerX + DRAWER_W).toFloat(), gy + GUI_H, BG_DRAWER)

        val mod = selModule ?: return
        val header = "${mod.getDisplayName()} Settings"
        ctx.drawString(mc.font, header, drawerX + 16, gy.toInt() + 14, TXT_PRIMARY)

        val backX = drawerX + DRAWER_W - 20
        ctx.drawString(mc.font, "←", backX, gy.toInt() + 14, TXT_SECONDARY)

        RenderUtil.rect(ctx, (drawerX + 12).toFloat(), gy + 32f, (drawerX + DRAWER_W - 12).toFloat(), gy + 34f, SEPARATOR)

        val clipTop = gy.toInt() + 36
        val clipBottom = gy.toInt() + GUI_H - 8
        ctx.enableScissor(drawerX, clipTop, drawerX + DRAWER_W, clipBottom)

        var sy = clipTop - drawerScroll
        val settings = mod.getSettings().filter { it.name != "Enabled" && it.name != "DisplayName" && it.isVisible }

        for (setting in settings) {
            if (sy + 20 < clipTop) { sy += getComponentHeight(setting); continue }
            if (sy > clipBottom) break
            val comp = getOrCreateComponent(setting, drawerX + 16, sy, DRAWER_W - 32)
            comp.render(ctx, mx, my, delta)
            sy += comp.getHeight() + 6
        }

        drawerMaxScroll = maxOf(0, sy - clipBottom + drawerScroll)
        ctx.disableScissor()

        if (drawerMaxScroll > 0) {
            val trackH = (clipBottom - clipTop).toFloat()
            val totalH = (sy - clipTop + drawerScroll).toFloat()
            val barH = trackH * trackH / totalH
            val barY = clipTop.toFloat() + (drawerScroll.toFloat() / drawerMaxScroll.toFloat()) * (trackH - barH)
            RenderUtil.rect(ctx, (drawerX + DRAWER_W - 5).toFloat(), barY, (drawerX + DRAWER_W - 2).toFloat(), barY + barH, ACCENT_DIM)
        }
    }

    private fun getComponentHeight(setting: Setting<*>): Int {
        val comp = getOrCreateComponent(setting, 0, 0, DRAWER_W - 32)
        return comp.getHeight() + 4
    }

    private fun getOrCreateComponent(setting: Setting<*>, x: Int, y: Int, w: Int): SettingComponent {
        return settingComponents.getOrPut(setting) { createComponent(setting, x, y, w) }.also {
            it.x = x
            it.y = y
        }
    }

    private fun createComponent(setting: Setting<*>, x: Int, y: Int, w: Int): SettingComponent {
        return when (setting.value) {
            is Boolean -> BooleanComponent(setting as Setting<Boolean>, x, y, w, 16)
            is Number -> if (setting.min != null) SliderComponent(setting as Setting<Number>, x, y, w, 16)
                          else StringSettingComponent(setting, x, y, w, 16)
            is Bind -> BindComponent(setting as Setting<Bind>, x, y, w, 16)
            is Color -> ColorComponent(setting as Setting<Color>, x, y, w, 16)
            else -> if (setting.isEnumSetting) EnumComponent(setting, x, y, w, 16)
                    else if (setting.isStringSetting) StringComponent(setting as Setting<String>, x, y, w, 16)
                    else if (setting.isButtonSetting) ButtonComponent(setting as Setting<Runnable>, x, y, w, 16)
                    else StringSettingComponent(setting, x, y, w, 16)
        }
    }

    // ── MOUSE ─────────────────────────────────────────────────────
    override fun mouseClicked(click: MouseButtonEvent, doubled: Boolean): Boolean {
        val mx = click.x().toInt()
        val my = click.y().toInt()
        val btn = click.button()
        val gx = guiLeft()
        val gy = guiTop()

        focusedStringSetting = null
        listeningBindSetting = null
        for (comp in settingComponents.values) {
            if (comp is BindComponent) comp.stopListening()
        }

        if (showDrawer) {
            val drawerX = gx + GUI_W - DRAWER_W
            if (mx in drawerX..<gx + GUI_W && my in gy..<gy + GUI_H) {
                val backX = drawerX + DRAWER_W - 16
                if (mx in backX - 4..<backX + 12 && my in gy + 10..<gy + 24) {
                    closeDrawer()
                    return true
                }
                val clipTop = gy + 32
                var sy = clipTop - drawerScroll
                val mod = selModule ?: return true
                for (setting in mod.getSettings().filter { it.name != "Enabled" && it.name != "DisplayName" && it.isVisible }) {
                    val h = getComponentHeight(setting)
                    if (sy + h < clipTop) { sy += h; continue }
                    if (sy > gy + GUI_H - 4) break
                    val comp = getOrCreateComponent(setting, drawerX + 16, sy, DRAWER_W - 32)
                    if (comp.isHovered(mx.toDouble(), my.toDouble())) {
                        comp.mouseClicked(mx.toDouble(), my.toDouble(), btn)
                        if (comp is StringComponent && comp.isFocused) focusedStringSetting = setting as Setting<String>
                        if (comp is BindComponent && comp.isListening) listeningBindSetting = setting as Setting<Bind>
                        return true
                    }
                    sy += h
                }
                return true
            }
            closeDrawer()
            return true
        }

        // sidebar
        if (mx in gx..<gx + SIDEBAR_W && my in gy..<gy + GUI_H) {
            var yOff = (gy + 42f).toInt()
            val itemH = 38
            for (cat in sideList) {
                if (my in yOff..<yOff + itemH) {
                    if (selCategory != cat) {
                        selCategory = cat
                        selModule = null
                        scrollOffset = 0
                        drawerScroll = 0
                    }
                    return true
                }
                yOff += itemH
            }
            return true
        }

        val sy = gy + TOPBAR_H + 4
        if (mx in contentX() + 12..<contentX() + 12 + contentW() - 24 && my in sy..<sy + SEARCH_H) {
            searchFocused = true
            return true
        } else {
            searchFocused = false
        }

        // module cards
        if (mx in contentX() + 12..<contentX() + 12 + contentW() - 24 && my in cardAreaTop()..<cardAreaBottom()) {
            val cw = contentW() - 24
            val mods = getFilteredModules()
            var yOff = cardAreaTop() - scrollOffset
            for (mod in mods) {
                if (my in yOff..<yOff + CARD_H) {
                    val pillX = contentX() + cw - (if (mod.getSettings().isNotEmpty()) 56 else 28)
                    val arrowX = contentX() + cw - 16
                    if (btn == 0 && mx in arrowX - 4..<arrowX + 12 && mod.getSettings().isNotEmpty()) {
                        selModule = mod
                        showDrawer = true
                        drawerOpenAnim = 0f
                        settingComponents.clear()
                        drawerScroll = 0
                    } else if (btn == 0 && mx in pillX..<pillX + 28) {
                        mod.toggle()
                    } else if (btn == 0) {
                        selModule = mod
                        settingComponents.clear()
                        drawerScroll = 0
                    } else if (btn == 1) {
                        mod.toggle()
                    }
                    return true
                }
                yOff += CARD_H + CARD_GAP
            }
        }

        return super.mouseClicked(click, doubled)
    }

    override fun mouseReleased(click: MouseButtonEvent): Boolean {
        for (comp in settingComponents.values) {
            comp.mouseReleased(click.x(), click.y(), click.button())
        }
        return super.mouseReleased(click)
    }

    override fun mouseScrolled(mx: Double, my: Double, horizontal: Double, vertical: Double): Boolean {
        val gx = guiLeft()
        val gy = guiTop()

        if (showDrawer) {
            val drawerX = gx + GUI_W - DRAWER_W
            if (mx in drawerX.toDouble()..<(gx + GUI_W).toDouble() && my in gy.toDouble()..<(gy + GUI_H).toDouble()) {
                drawerScroll = (drawerScroll - (vertical * scrollSpeed).toInt()).coerceIn(0, drawerMaxScroll)
                return true
            }
        }

        if (mx in contentX().toDouble()..<(gx + GUI_W).toDouble() && my in gy.toDouble()..<(gy + GUI_H).toDouble()) {
            scrollOffset = (scrollOffset - (vertical * scrollSpeed).toInt()).coerceIn(0, maxScroll)
            return true
        }

        return super.mouseScrolled(mx, my, horizontal, vertical)
    }

    // ── KEYBOARD ──────────────────────────────────────────────────
    override fun keyPressed(input: KeyEvent): Boolean {
        val key = input.key()

        if (listeningBindSetting != null) {
            listeningBindSetting!!.setValue(if (key == GLFW.GLFW_KEY_ESCAPE) Bind.none() else Bind(key))
            listeningBindSetting = null
            for (comp in settingComponents.values) {
                if (comp is BindComponent) comp.stopListening()
            }
            return true
        }

        if (searchFocused) {
            when (key) {
                GLFW.GLFW_KEY_BACKSPACE -> {
                    if (searchText.isNotEmpty()) {
                        searchText = searchText.substring(0, searchText.length - 1)
                    }
                    scrollOffset = 0
                    return true
                }
                GLFW.GLFW_KEY_DELETE -> {
                    searchText = ""
                    scrollOffset = 0
                    return true
                }
                else -> {
                    // Let other keys be handled by charTyped
                }
            }
        }

        if (focusedStringSetting != null) {
            when (key) {
                GLFW.GLFW_KEY_ESCAPE, GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> focusedStringSetting = null
                GLFW.GLFW_KEY_BACKSPACE -> {
                    val s = focusedStringSetting!!.value
                    if (s.isNotEmpty()) focusedStringSetting!!.setValue(s.substring(0, s.length - 1))
                }
            }
            return true
        }

        if (key == GLFW.GLFW_KEY_ESCAPE) {
            if (showDrawer) closeDrawer()
            else onClose()
            return true
        }

        for (comp in settingComponents.values) {
            if (comp.isCapturingInput && comp.keyPressed(input)) return true
        }

        return super.keyPressed(input)
    }

    override fun charTyped(input: CharacterEvent): Boolean {
        if (focusedStringSetting != null) {
            val chr = input.codepointAsString()
            if (chr.isNotEmpty()) {
                val s = focusedStringSetting!!.value
                if (s.length < 64) focusedStringSetting!!.setValue(s + chr)
            }
            return true
        }
        if (searchFocused) {
            val chr = input.codepointAsString()
            if (chr.isNotEmpty() && searchText.length < 64) {
                searchText += chr
                scrollOffset = 0
                return true
            }
        }
        return super.charTyped(input)
    }

    // ── UTILITY ───────────────────────────────────────────────────
    private fun closeDrawer() {
        showDrawer = false
        selModule = null
        settingComponents.clear()
        drawerScroll = 0
        drawerOpenAnim = 0f
    }

    private fun getFilteredModules(): List<Module> {
        return if (searchText.isBlank()) {
            modulesByCat[selCategory] ?: emptyList()
        } else {
            val q = searchText.lowercase()
            Oxevy.moduleManager.modules.filter { !it.hidden && it.getDisplayName().lowercase().contains(q) }
        }
    }

    override fun isPauseScreen(): Boolean = false
    override fun renderBackground(ctx: GuiGraphics, mx: Int, my: Int, delta: Float) {}

    companion object {
        const val SIDEBAR_W = 60
        const val GUI_W = 600
        const val GUI_H = 420
        const val CARD_H = 34
        const val CARD_GAP = 4
        const val DRAWER_W = 240
        const val SEARCH_H = 28
        const val TOPBAR_H = 30

        // Modern dark theme with gradient support
        val BG_DARK = 0xFF0D0D12.toInt()
        val BG_DARK_OVERLAY = 0xAA000000.toInt()
        val BG_SIDEBAR = 0xFF10101A.toInt()
        val BG_CARD = 0xFF16161F.toInt()
        val BG_CARD_HOVER = 0xFF1E1E2B.toInt()
        val BG_CARD_SEL = 0xFF252535.toInt()
        val BG_DRAWER = 0xFF13131D.toInt()
        val BG_INPUT = 0xFF0F0F18.toInt()
        
        // Text colors with better contrast
        val TXT_PRIMARY = 0xFFFFFFFF.toInt()
        val TXT_SECONDARY = 0xFFB8B8C8.toInt()
        val TXT_MUTED = 0xFF6B6B7B.toInt()
        
        // Accent colors - green gradient
        val ACCENT_PRIMARY = 0xFF00FF66.toInt()
        val ACCENT_SECONDARY = 0xFF00CC44.toInt()
        val ACCENT_DIM = 0x1400FF66.toInt()
        val ACCENT_HOVER = 0x2200FF66.toInt()
        
        val SEPARATOR = 0xFF1A1A24.toInt()

        val CATEGORY_LABELS = mapOf(
            Module.Category.COMBAT to "CO",
            Module.Category.MISC to "MI",
            Module.Category.RENDER to "RE",
            Module.Category.MOVEMENT to "MO",
            Module.Category.PLAYER to "PL",
            Module.Category.CLIENT to "CL",
            Module.Category.HUD to "HD"
        )

        private var INSTANCE: OxevyGui? = null

        fun getClickGui(): OxevyGui {
            if (INSTANCE == null) INSTANCE = OxevyGui()
            return INSTANCE!!
        }

        private var colorClipboard: Color? = null
        fun getColorClipboard(): Color? = colorClipboard
        fun setColorClipboard(c: Color?) { colorClipboard = c }
    }
}
