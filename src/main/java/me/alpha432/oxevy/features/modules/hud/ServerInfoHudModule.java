package me.alpha432.oxevy.features.modules.hud;

import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.event.impl.render.Render2DEvent;
import me.alpha432.oxevy.features.settings.Setting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ServerData;

import java.util.Locale;

public class ServerInfoHudModule extends HudModule {
    public final Setting<Boolean> showPing = bool("Ping", true);
    public final Setting<Boolean> showTps = bool("TPS", true);
    public final Setting<Boolean> showPlayerCount = bool("PlayerCount", true);
    public final Setting<Boolean> showDimension = bool("Dimension", true);
    public final Setting<Boolean> showTime = bool("WorldTime", true);
    public final Setting<Boolean> showWeather = bool("Weather", true);

    public ServerInfoHudModule() {
        super("ServerInfo", "Server stats, TPS, and world info", 150, 80);
    }

    // Pose toggle button: flips the global Modify Pose flag to allow on-the-fly pose editing
    public final Setting<Runnable> togglePoseButton = button("Modify Pose (Toggle)", () -> {
        modifyPose.setValue(!modifyPose.getValue());
    });

    @Override
    public void drawContent(Render2DEvent e) {
        float x = getX();
        float y = getY();
        GuiGraphics ctx = e.getContext();
        int lineHeight = mc.font.lineHeight;
        float drawY = y;
        int maxWidth = 0;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        boolean isLeft = x < screenWidth / 2.0f;

        ServerData serverData = mc.getCurrentServer();
        String serverLine = serverData != null ? serverData.ip : "Integrated Server";
        int serverWidth = mc.font.width(serverLine);
        int textX;
        if (isLeft) {
            textX = (int) (x + 2);
        } else {
            textX = (int) (x + getWidth() - 2 - serverWidth);
        }
        ctx.drawString(mc.font, serverLine, textX, (int) drawY, 0xFFFFFFFF);
        maxWidth = Math.max(maxWidth, serverWidth);
        drawY += lineHeight;

        if (showPing.getValue()) {
            int ping = Oxevy.serverManager.getPing();
            String pingStr = "§7Ping: " + (ping < 0 ? "§c?" : (ping < 100 ? "§a" : "§e") + ping + "ms");
            int pingWidth = mc.font.width(pingStr);
            if (isLeft) {
                textX = (int) (x + 2);
            } else {
                textX = (int) (x + getWidth() - 2 - pingWidth);
            }
            ctx.drawString(mc.font, pingStr, textX, (int) drawY, 0xFFFFFFFF);
            maxWidth = Math.max(maxWidth, pingWidth);
            drawY += lineHeight;
        }

        if (showTps.getValue()) {
        // TPS retrieval may vary between versions; fall back to handling absence gracefully
        float tps = 0f;
        try {
            tps = Oxevy.serverManager.getTps();
        } catch (Throwable t) {
            // If getTPS() is not available, attempt to compute or leave as 0
            tps = 0f;
        }
            String tpsStr = "§7TPS: " + (tps > 18 ? "§a" : (tps > 15 ? "§e" : "§c")) + String.format(Locale.US, "%.2f", tps);
            int tpsWidth = mc.font.width(tpsStr);
            if (isLeft) {
                textX = (int) (x + 2);
            } else {
                textX = (int) (x + getWidth() - 2 - tpsWidth);
            }
            ctx.drawString(mc.font, tpsStr, textX, (int) drawY, 0xFFFFFFFF);
            maxWidth = Math.max(maxWidth, tpsWidth);
            drawY += lineHeight;
        }

        if (showPlayerCount.getValue() && mc.getConnection() != null) {
            int players = mc.getConnection().getOnlinePlayers().size();
            String countStr = "§7Players: §f" + players;
            int countWidth = mc.font.width(countStr);
            if (isLeft) {
                textX = (int) (x + 2);
            } else {
                textX = (int) (x + getWidth() - 2 - countWidth);
            }
            ctx.drawString(mc.font, countStr, textX, (int) drawY, 0xFFFFFFFF);
            maxWidth = Math.max(maxWidth, countWidth);
            drawY += lineHeight;
        }

        setWidth(Math.max(100, maxWidth + 4));
        setHeight(drawY - y);
    }
}
