package me.alpha432.oxevy.features.modules.hud;

import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.event.impl.render.Render2DEvent;
import me.alpha432.oxevy.features.modules.client.HudModule;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.manager.ServerManager;
import me.alpha432.oxevy.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.Connection;

import java.util.Locale;

public class ServerInfoHudModule extends HudModule {
    private static final String[] MOON_PHASES = {"\uD83C\uDF11", "\uD83C\uDF12", "\uD83C\uDF13", "\uD83C\uDF14", "\uD83C\uDF15", "\uD83C\uDF16", "\uD83C\uDF17", "\uD83C\uDF18"};

    public final Setting<Boolean> showPing = bool("Ping", true);
    public final Setting<Boolean> showTps = bool("TPS", true);
    public final Setting<Boolean> showStability = bool("Stability", true);
    public final Setting<Boolean> showPlayerCount = bool("PlayerCount", true);
    public final Setting<Boolean> showWorldInfo = bool("WorldInfo", true);
    public final Setting<Boolean> showTime = bool("TimeOfDay", true);
    public final Setting<Boolean> showWeather = bool("Weather", true);
    public final Setting<Boolean> showMoonPhase = bool("MoonPhase", true);
    public final Setting<Boolean> background = bool("Background", true);
    public final Setting<Integer> backgroundOpacity = num("BackgroundOpacity", 120, 0, 255);

    public ServerInfoHudModule() {
        super("ServerInfo", "Server and world information", 160, 70);
        register(backgroundOpacity);
        backgroundOpacity.setVisibility((Integer i) -> background.getValue());
    }

    @Override
    protected void render(Render2DEvent e) {
        super.render(e);
        if (nullCheck()) return;

        ServerManager sm = Oxevy.serverManager;
        float x = getX();
        float y = getY();
        GuiGraphics ctx = e.getContext();
        int lineHeight = mc.font.lineHeight;
        float drawY = y;
        int maxWidth = 0;

        // Server name / IP
        String serverName = getServerName();
        String serverLine = "§7\uD83C\uDF10 §f" + serverName;
        ctx.drawString(mc.font, serverLine, (int) x, (int) drawY, 0xFFFFFFFF);
        maxWidth = Math.max(maxWidth, mc.font.width(serverLine));
        drawY += lineHeight;

        if (mc.getConnection() != null) {
            if (showPing.getValue()) {
                int ping = sm.getPing();
                String pingStr = "§7Ping: §f" + ping + " ms";
                ctx.drawString(mc.font, pingStr, (int) x, (int) drawY, pingColor(ping));
                maxWidth = Math.max(maxWidth, mc.font.width(pingStr));
                drawY += lineHeight;
            }

            if (showTps.getValue()) {
                float tps = sm.getTps();
                String tpsStr = "§7TPS: §f" + String.format(Locale.US, "%.1f", tps);
                ctx.drawString(mc.font, tpsStr, (int) x, (int) drawY, tpsColor(tps));
                maxWidth = Math.max(maxWidth, mc.font.width(tpsStr));
                drawY += lineHeight;
            }

            if (showStability.getValue()) {
                boolean notResponding = sm.isServerNotResponding();
                String stabStr = "§7Stability: " + (notResponding ? "§cUnstable" : "§aStable");
                ctx.drawString(mc.font, stabStr, (int) x, (int) drawY, notResponding ? 0xFFFF5555 : 0xFF55FF55);
                maxWidth = Math.max(maxWidth, mc.font.width(stabStr));
                drawY += lineHeight;
            }

            if (showPlayerCount.getValue()) {
                int online = mc.getConnection().getOnlinePlayers().size();
                String countStr = "§7Players: §f" + online;
                ctx.drawString(mc.font, countStr, (int) x, (int) drawY, 0xFF_CC_CC_CC);
                maxWidth = Math.max(maxWidth, mc.font.width(countStr));
                drawY += lineHeight;
            }
        }

        if (showWorldInfo.getValue()) {
            String dim = getDimensionPath();
            String dimLine = "§7World: §f" + dim;
            ctx.drawString(mc.font, dimLine, (int) x, (int) drawY, 0xFF_CC_CC_CC);
            maxWidth = Math.max(maxWidth, mc.font.width(dimLine));
            drawY += lineHeight;
        }

        if (showTime.getValue()) {
            long dayTime = mc.level.getDayTime() % 24000;
            int hours = (int) (dayTime / 1000);
            int mins = (int) ((dayTime % 1000) * 60 / 1000);
            String timeStr = "§7Time: §f" + String.format("%02d:%02d", hours, mins);
            ctx.drawString(mc.font, timeStr, (int) x, (int) drawY, 0xFF_CC_CC_CC);
            maxWidth = Math.max(maxWidth, mc.font.width(timeStr));
            drawY += lineHeight;
        }

        if (showWeather.getValue()) {
            String weather = mc.level.isThundering() ? "Thunder" : mc.level.isRaining() ? "Rain" : "Clear";
            String weatherStr = "§7Weather: §f" + weather;
            ctx.drawString(mc.font, weatherStr, (int) x, (int) drawY, 0xFF_CC_CC_CC);
            maxWidth = Math.max(maxWidth, mc.font.width(weatherStr));
            drawY += lineHeight;
        }

        if (showMoonPhase.getValue()) {
            int phase = getMoonPhase();
            String moonStr = "§7Moon: §f" + MOON_PHASES[phase % MOON_PHASES.length] + " §7(" + (phase + 1) + "/8)";
            ctx.drawString(mc.font, moonStr, (int) x, (int) drawY, 0xFF_CC_CC_CC);
            maxWidth = Math.max(maxWidth, mc.font.width(moonStr));
            drawY += lineHeight;
        }

        setWidth(Math.max(getWidth(), maxWidth + 4));
        setHeight(drawY - y);

        if (background.getValue()) {
            int alpha = backgroundOpacity.getValue();
            RenderUtil.rect(ctx, x - 2, y - 2, x + getWidth() + 2, y + getHeight() + 2, (alpha << 24) | 0x11_11_11);
        }
    }

    private String getServerName() {
        Minecraft mc = Minecraft.getInstance();
        ServerData data = mc.getCurrentServer();
        if (data != null) {
            String name = data.name != null && !data.name.isEmpty() ? data.name : null;
            String ip = data.ip != null && !data.ip.isEmpty() ? data.ip : null;
            return name != null ? name : (ip != null ? ip : "Multiplayer");
        }
        if (mc.getSingleplayerServer() != null) {
            return "Singleplayer";
        }
        Connection conn = mc.getConnection() != null ? mc.getConnection().getConnection() : null;
        if (conn != null) {
            String addr = conn.getRemoteAddress() != null ? conn.getRemoteAddress().toString() : "";
            if (!addr.isEmpty()) return addr;
        }
        return "Unknown";
    }

    private String getDimensionPath() {
        String s = mc.level.dimension().toString();
        if (s.contains("the_nether")) return "the_nether";
        if (s.contains("overworld")) return "overworld";
        if (s.contains("the_end")) return "the_end";
        return s;
    }

    private int getMoonPhase() {
        return (int) ((mc.level.getDayTime() / 24000L) % 8);
    }

    private int pingColor(int ping) {
        if (ping < 50) return 0xFF_44_FF_44;
        if (ping < 100) return 0xFF_FF_FF_44;
        return 0xFF_FF_44_44;
    }

    private int tpsColor(float tps) {
        if (tps >= 18) return 0xFF_44_FF_44;
        if (tps >= 15) return 0xFF_FF_FF_44;
        return 0xFF_FF_44_44;
    }
}
