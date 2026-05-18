package me.alpha432.oxevy.features.modules.hud;

import me.alpha432.oxevy.event.impl.render.Render2DEvent;
import me.alpha432.oxevy.features.settings.Setting;
import net.minecraft.client.gui.GuiGraphics;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MusicPlayerHudModule extends HudModule {
    public final Setting<Float> pollRate = num("Poll Rate", 2.0f, 0.5f, 10.0f);

    private final AtomicReference<String> currentTrack = new AtomicReference<>("");
    private final AtomicReference<String> currentArtist = new AtomicReference<>("");
    private final AtomicReference<String> currentStatus = new AtomicReference<>("");
    private final AtomicReference<String> lastError = new AtomicReference<>("");
    private Thread pollThread;
    private volatile boolean running;
    private volatile String[] playerctlCommand;
    private volatile String playerctlDisplay = "playerctl";
    private volatile boolean useFlatpakHost;
    private volatile long retryAfterMs;

    private static final String OS = System.getProperty("os.name").toLowerCase(Locale.ROOT);
    private static final boolean IS_WINDOWS = OS.contains("win");
    private static final boolean IS_LINUX = OS.contains("nix") || OS.contains("nux") || OS.contains("aix");
    private static final boolean IS_MAC = OS.contains("mac");
    private static final String PLAYERCTL_FORMAT = "{{artist}}|{{title}}|{{status}}";
    private static final String DEFAULT_PATH = "/usr/local/bin:/usr/bin:/bin:/usr/local/sbin:/usr/sbin";
    private static final long GENERIC_RETRY_DELAY_MS = 5_000L;
    private static final long SANDBOX_RETRY_DELAY_MS = 30_000L;
    private static final String[] PLAYERCTL_PATHS = {
            "/usr/bin/playerctl",
            "/usr/local/bin/playerctl",
            "/bin/playerctl",
            "/opt/homebrew/bin/playerctl"
    };
    private static final String[] FLATPAK_SPAWN_PATHS = {
            "/usr/bin/flatpak-spawn",
            "/app/bin/flatpak-spawn",
            "/bin/flatpak-spawn"
    };

    public MusicPlayerHudModule() {
        super("MusicPlayer", "Shows system audio (Spotify, YouTube, etc.) via MPRIS", 200, 50);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        lastError.set("");
        playerctlCommand = null;
        playerctlDisplay = "playerctl";
        useFlatpakHost = false;
        retryAfterMs = 0L;
        running = true;
        pollThread = new Thread(this::pollLoop, "MusicPlayer-Poller");
        pollThread.setDaemon(true);
        pollThread.start();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        running = false;
        if (pollThread != null) {
            pollThread.interrupt();
        }
        pollThread = null;
        currentTrack.set("");
        currentArtist.set("");
        currentStatus.set("");
        lastError.set("");
        retryAfterMs = 0L;
    }

    private void pollLoop() {
        while (running) {
            updateTrack();
            try {
                Thread.sleep((long) (pollRate.getValue() * 1000));
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private String resolvePlayerctl() {
        if (isFlatpakSandbox()) {
            String flatpakSpawn = resolveBinary("flatpak-spawn", FLATPAK_SPAWN_PATHS);
            if (flatpakSpawn != null) {
                useFlatpakHost = true;
                playerctlDisplay = flatpakSpawn + " --host playerctl";
                return flatpakSpawn;
            }
        }
        useFlatpakHost = false;
        String playerctl = resolveBinary("playerctl", mergeCandidates(
                PLAYERCTL_PATHS,
                flatpakExportPath(),
                "/var/lib/flatpak/exports/bin/playerctl"));
        if (playerctl != null) {
            playerctlDisplay = playerctl;
            return playerctl;
        }
        playerctlDisplay = "playerctl";
        return "playerctl";
    }

    private void updateTrack() {
        if (isRetryWindowActive()) {
            return;
        }
        try {
            if (IS_LINUX) {
                if (playerctlCommand == null) {
                    String resolved = resolvePlayerctl();
                    if (useFlatpakHost) {
                        playerctlCommand = new String[]{resolved, "--host", "playerctl"};
                    } else {
                        playerctlCommand = new String[]{resolved};
                    }
                }
                updateTrackLinux();
            } else if (IS_WINDOWS) {
                updateTrackWindows();
                lastError.set("");
                retryAfterMs = 0L;
            } else if (IS_MAC) {
                updateTrackMac();
                lastError.set("");
                retryAfterMs = 0L;
            } else {
                clearTrack();
                lastError.set("");
            }
        } catch (Exception e) {
            if (IS_LINUX) {
                handleLinuxException(e);
            } else {
                String message = e.getMessage();
                if (message != null && message.length() > 120) {
                    message = message.substring(0, 117) + "...";
                }
                lastError.set(message == null || message.isBlank() ? e.getClass().getSimpleName() : message);
                retryAfterMs = System.currentTimeMillis() + GENERIC_RETRY_DELAY_MS;
            }
            clearTrack();
        }
    }

    private void updateTrackLinux() throws Exception {
        CommandResult result = runLinuxCommand("metadata", "--format", PLAYERCTL_FORMAT);
        if (result.exitCode != 0) {
            if (isNoPlayerMessage(result.output)) {
                clearTrack();
                lastError.set("");
                retryAfterMs = 0L;
                return;
            }
            if (isSandboxBlockedMessage(result.output)) {
                setRetryError(sandboxErrorMessage(result.output), SANDBOX_RETRY_DELAY_MS);
            } else {
                setRetryError(formatError("playerctl exit " + result.exitCode, result.output), GENERIC_RETRY_DELAY_MS);
            }
            clearTrack();
            return;
        }
        parseLine(firstNonEmptyLine(result.output), "\\|");
        lastError.set("");
        retryAfterMs = 0L;
    }

    private void updateTrackWindows() throws Exception {
        Process process = new ProcessBuilder("powershell", "-NoProfile", "-Command",
                "try{$m=[Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager,Windows.Media.Control,ContentType=WindowsRuntime];" +
                "$s=[Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager]::RequestAsync().GetResults().GetCurrentSession();" +
                "if($s){$p=$s.TryGetMediaPropertiesAsync().GetResults();" +
                "Write-Output ($p.Artist + '|' + $p.Title + '|' + $s.GetPlaybackInfo().PlaybackStatus)}}catch{}")
                .redirectErrorStream(true).start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line = reader.readLine();
            process.waitFor();
            parseLine(line, "\\|");
        }
    }

    private void updateTrackMac() throws Exception {
        String[] apps = {"Spotify", "Music", "VLC", "IINA"};
        for (String app : apps) {
            Process process = new ProcessBuilder("osascript", "-e",
                    "tell application \"" + app + "\" to if player state is playing then " +
                    "return (artist of current track) & \"|\" & (name of current track) & \"|\" & player state")
                    .redirectErrorStream(true).start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                process.waitFor();
                parseLine(line, "\\|");
                if (!currentTrack.get().isEmpty()) return;
            }
        }
        clearTrack();
    }

    private void parseLine(String line, String separator) {
        if (line != null && !line.isEmpty()) {
            String[] parts = line.split(separator, 3);
            if (parts.length >= 2 && !parts[1].isEmpty()) {
                currentArtist.set(parts[0].isEmpty() ? "Unknown" : parts[0]);
                currentTrack.set(parts[1]);
                currentStatus.set(parts.length >= 3 ? parts[2] : "");
                return;
            }
        }
        clearTrack();
    }

    private void clearTrack() {
        currentTrack.set("");
        currentArtist.set("");
        currentStatus.set("");
    }

    private boolean isFlatpakSandbox() {
        return new File("/.flatpak-info").exists() || System.getenv("FLATPAK_ID") != null;
    }

    private boolean isSnapSandbox() {
        return System.getenv("SNAP") != null || System.getenv("SNAP_NAME") != null;
    }

    private String flatpakExportPath() {
        String home = System.getProperty("user.home");
        if (home == null || home.isEmpty()) {
            return null;
        }
        return new File(home, ".local/share/flatpak/exports/bin/playerctl").getAbsolutePath();
    }

    private String[] mergeCandidates(String[] base, String... extras) {
        List<String> candidates = new ArrayList<>(base.length + extras.length);
        for (String candidate : base) {
            candidates.add(candidate);
        }
        for (String extra : extras) {
            if (extra != null && !extra.isEmpty()) {
                candidates.add(extra);
            }
        }
        return candidates.toArray(new String[0]);
    }

    private String resolveBinary(String binaryName, String... candidates) {
        for (String candidate : candidates) {
            if (candidate == null || candidate.isEmpty()) {
                continue;
            }
            File file = new File(candidate);
            if (file.exists() && file.canExecute()) {
                return file.getAbsolutePath();
            }
        }
        try {
            Process which = new ProcessBuilder("/usr/bin/which", binaryName)
                    .redirectErrorStream(true)
                    .start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(which.getInputStream()))) {
                String line = reader.readLine();
                which.waitFor();
                if (line != null && !line.isEmpty() && new File(line).exists()) {
                    return line;
                }
            }
        } catch (Exception ignored) {
        }
        try {
            ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", "command -v " + binaryName);
            pb.environment().put("PATH", DEFAULT_PATH);
            Process sh = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(sh.getInputStream()))) {
                String line = reader.readLine();
                sh.waitFor();
                if (line != null && !line.isEmpty() && new File(line).exists()) {
                    return line;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private boolean isRetryWindowActive() {
        return retryAfterMs > System.currentTimeMillis();
    }

    private void setRetryError(String error, long retryDelayMs) {
        lastError.set(error);
        retryAfterMs = System.currentTimeMillis() + retryDelayMs;
    }

    private void handleLinuxException(Exception e) {
        String message = e.getMessage() == null ? "" : e.getMessage().trim();
        if (message.length() > 120) {
            message = message.substring(0, 117) + "...";
        }
        if (isSnapSandbox()) {
            setRetryError(sandboxErrorMessage(message), SANDBOX_RETRY_DELAY_MS);
            return;
        }
        if (isSandboxBlockedMessage(message)) {
            setRetryError(formatError("playerctl=" + playerctlDisplay, message), SANDBOX_RETRY_DELAY_MS);
            return;
        }
        setRetryError("playerctl=" + playerctlDisplay + (message.isEmpty() ? "" : " " + message), GENERIC_RETRY_DELAY_MS);
    }

    private CommandResult runLinuxCommand(String... args) throws Exception {
        List<String> command = new ArrayList<>(playerctlCommand.length + args.length);
        for (String part : playerctlCommand) {
            command.add(part);
        }
        for (String arg : args) {
            command.add(arg);
        }
        ProcessBuilder pb = new ProcessBuilder(command);
        String currentPath = System.getenv("PATH");
        pb.environment().put("PATH",
                currentPath == null || currentPath.isBlank() ? DEFAULT_PATH : currentPath + ":" + DEFAULT_PATH);
        pb.redirectErrorStream(true);

        Process process = pb.start();
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (output.length() > 0) {
                    output.append('\n');
                }
                output.append(line);
            }
        }
        process.waitFor();
        return new CommandResult(process.exitValue(), output.toString().trim());
    }

    private boolean isNoPlayerMessage(String output) {
        String normalized = output == null ? "" : output.toLowerCase(Locale.ROOT);
        return normalized.contains("no players found")
                || normalized.contains("no player could handle this command");
    }

    private boolean isSandboxBlockedMessage(String output) {
        String normalized = output == null ? "" : output.toLowerCase(Locale.ROOT);
        return normalized.contains("operation not permitted")
                || normalized.contains("permission denied")
                || normalized.contains("access denied")
                || normalized.contains("could not connect")
                || normalized.contains("dbus")
                || normalized.contains("not allowed");
    }

    private String firstNonEmptyLine(String output) {
        if (output == null || output.isEmpty()) {
            return null;
        }
        for (String line : output.split("\\R")) {
            if (!line.isBlank()) {
                return line;
            }
        }
        return null;
    }

    private String formatError(String prefix, String output) {
        if (output == null || output.isBlank()) {
            return prefix + " (no output)";
        }
        String singleLine = output.replace('\n', ' ').trim();
        if (singleLine.length() > 120) {
            singleLine = singleLine.substring(0, 117) + "...";
        }
        return prefix + ": " + singleLine;
    }

    private String sandboxErrorMessage(String detail) {
        String prefix;
        if (isSnapSandbox()) {
            prefix = "Snap sandbox blocked playerctl/MPRIS; use AppImage or native Prism";
        } else if (isFlatpakSandbox()) {
            prefix = "Flatpak sandbox blocked playerctl/MPRIS";
        } else {
            prefix = "playerctl could not reach MPRIS";
        }
        if (detail == null || detail.isBlank()) {
            return prefix;
        }
        return formatError(prefix, detail);
    }

    private record CommandResult(int exitCode, String output) {}

    @Override
    public void drawContent(Render2DEvent e) {
        if (nullCheck()) return;

        float x = getX();
        float y = getY();
        GuiGraphics ctx = e.getContext();
        int lineHeight = mc.font.lineHeight;
        float drawY = y;
        int maxWidth = 0;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        boolean isLeft = x < screenWidth / 2.0f;

        String track = currentTrack.get();
        String artist = currentArtist.get();
        String status = currentStatus.get();
        String error = lastError.get();
        boolean hasTrack = !track.isEmpty();
        boolean isPaused = "Paused".equalsIgnoreCase(status);

        String header;
        int headerColor;
        if (hasTrack && isPaused) {
            header = "§7\u266B Paused";
            headerColor = 0xFFAAAAAA;
        } else if (hasTrack) {
            header = "§b\u266B Now Playing";
            headerColor = 0xFF55FFFF;
        } else {
            header = "§7\u266B No media";
            headerColor = 0xFFAAAAAA;
        }

        int headerWidth = mc.font.width(header);
        int headerX = isLeft ? (int) (x + 2) : (int) (x + getWidth() - 2 - headerWidth);
        ctx.drawString(mc.font, header, headerX, (int) drawY, headerColor);
        maxWidth = Math.max(maxWidth, headerWidth);
        drawY += lineHeight;

        if (hasTrack) {
            String trackStr = (isPaused ? "§7" : "§f") + track;
            int trackWidth = mc.font.width(trackStr);
            int trackX = isLeft ? (int) (x + 2) : (int) (x + getWidth() - 2 - trackWidth);
            ctx.drawString(mc.font, trackStr, trackX, (int) drawY, isPaused ? 0xFF777777 : 0xFFFFFFFF);
            maxWidth = Math.max(maxWidth, trackWidth);
            drawY += lineHeight;

            String artistStr = "§7by §f" + artist;
            int artistWidth = mc.font.width(artistStr);
            int artistX = isLeft ? (int) (x + 2) : (int) (x + getWidth() - 2 - artistWidth);
            ctx.drawString(mc.font, artistStr, artistX, (int) drawY, 0xFF999999);
            maxWidth = Math.max(maxWidth, artistWidth);
            drawY += lineHeight;
        } else {
            String trackStr = "§7Nothing playing";
            int trackWidth = mc.font.width(trackStr);
            int trackX = isLeft ? (int) (x + 2) : (int) (x + getWidth() - 2 - trackWidth);
            ctx.drawString(mc.font, trackStr, trackX, (int) drawY, 0xFF777777);
            maxWidth = Math.max(maxWidth, trackWidth);
            drawY += lineHeight;
        }

        if (!error.isEmpty()) {
            String errorStr = "§c" + error;
            int errorWidth = mc.font.width(errorStr);
            int errorX = isLeft ? (int) (x + 2) : (int) (x + getWidth() - 2 - errorWidth);
            ctx.drawString(mc.font, errorStr, errorX, (int) drawY, 0xFFFF5555);
            maxWidth = Math.max(maxWidth, errorWidth);
            drawY += lineHeight;
        }

        setWidth(Math.max(140, maxWidth + 4));
        setHeight(drawY - y);
    }
}
