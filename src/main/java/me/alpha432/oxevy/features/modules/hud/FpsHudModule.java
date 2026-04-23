package me.alpha432.oxevy.features.modules.hud;

import me.alpha432.oxevy.event.impl.render.Render2DEvent;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.render.RenderUtil;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Locale;

public class FpsHudModule extends HudModule {
    private static final int HISTORY_SIZE = 60;
    private static final int GRAPH_HEIGHT = 24;

    public final Setting<Boolean> showMinMax = bool("MinMax", true);
    public final Setting<Boolean> showAverage = bool("Average", true);
    public final Setting<Boolean> showFrameTime = bool("FrameTime", true);
    public final Setting<Boolean> showGraph = bool("Graph", true);
    public final Setting<Boolean> performanceWarnings = bool("Warnings", true);

    private final float[] fpsHistory = new float[HISTORY_SIZE];
    private int historyIndex;
    private long lastFrameTime = -1;
    private float currentFps;
    private float minFps = 999;
    private float maxFps;
    private float sumFps;
    private int samples;

    public FpsHudModule() {
        super("FPS", "FPS counter with graph and stats", 120, 60);
    }

    @Override
    public void drawContent(Render2DEvent e) {
        long now = System.currentTimeMillis();
        if (lastFrameTime > 0) {
            float dt = (now - lastFrameTime) / 1000f;
            if (dt > 0 && dt < 1f) {
                currentFps = 1f / dt;
                if (currentFps > 0 && currentFps < 10000) {
                    fpsHistory[historyIndex % HISTORY_SIZE] = currentFps;
                    historyIndex++;
                    minFps = Math.min(minFps, currentFps);
                    maxFps = Math.max(maxFps, currentFps);
                    sumFps += currentFps;
                    samples++;
                }
            }
        }
        lastFrameTime = now;

        float x = getX();
        float y = getY();
        GuiGraphics ctx = e.getContext();
        int lineHeight = mc.font.lineHeight;
        float drawY = y;
        int maxWidth = 0;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        boolean isLeft = x < screenWidth / 2.0f;

        String fpsColorCode = currentFps >= 60 ? "§a" : currentFps >= 30 ? "§e" : "§c";
        String fpsStr = "§fFPS: " + fpsColorCode + (int) currentFps;
        int fpsWidth = mc.font.width(fpsStr);
        int fpsTextX;
        if (isLeft) {
            fpsTextX = (int) (x + 2);
        } else {
            fpsTextX = (int) (x + getWidth() - 2 - fpsWidth);
        }
        ctx.drawString(mc.font, fpsStr, fpsTextX, (int) drawY, getFpsColor(currentFps));
        maxWidth = Math.max(maxWidth, fpsWidth);
        drawY += lineHeight;

        if (showMinMax.getValue()) {
            String minMaxStr = "§7Min: §f" + (int) minFps + " §7Max: §f" + (int) maxFps;
            int minMaxWidth = mc.font.width(minMaxStr);
            int minMaxTextX;
            if (isLeft) {
                minMaxTextX = (int) (x + 2);
            } else {
                minMaxTextX = (int) (x + getWidth() - 2 - minMaxWidth);
            }
            ctx.drawString(mc.font, minMaxStr, minMaxTextX, (int) drawY, 0xFF_CC_CC_CC);
            maxWidth = Math.max(maxWidth, minMaxWidth);
            drawY += lineHeight;
        }

        if (showAverage.getValue() && samples > 0) {
            String avgStr = "§7Avg: §f" + String.format(Locale.US, "%.1f", sumFps / samples);
            int avgWidth = mc.font.width(avgStr);
            int avgTextX;
            if (isLeft) {
                avgTextX = (int) (x + 2);
            } else {
                avgTextX = (int) (x + getWidth() - 2 - avgWidth);
            }
            ctx.drawString(mc.font, avgStr, avgTextX, (int) drawY, 0xFF_CC_CC_CC);
            maxWidth = Math.max(maxWidth, avgWidth);
            drawY += lineHeight;
        }

        if (showFrameTime.getValue()) {
            String msStr = "§7Frame: §f" + String.format(Locale.US, "%.1f", currentFps > 0 ? 1000f / currentFps : 0) + " ms";
            int msWidth = mc.font.width(msStr);
            int msTextX;
            if (isLeft) {
                msTextX = (int) (x + 2);
            } else {
                msTextX = (int) (x + getWidth() - 2 - msWidth);
            }
            ctx.drawString(mc.font, msStr, msTextX, (int) drawY, 0xFF_CC_CC_CC);
            maxWidth = Math.max(maxWidth, msWidth);
            drawY += lineHeight;
        }

        if (showGraph.getValue() && historyIndex > 0) {
            int graphW = Math.min(HISTORY_SIZE * 2, 100);
            float gx = x, gy = drawY, maxVal = 1f;
            for (int i = 0; i < HISTORY_SIZE; i++) if (fpsHistory[i] > maxVal) maxVal = fpsHistory[i];
            for (int i = 0; i < Math.min(historyIndex, HISTORY_SIZE); i++) {
                int idx = (historyIndex - 1 - i + HISTORY_SIZE) % HISTORY_SIZE;
                float f = fpsHistory[idx];
                int barH = (int) ((f / maxVal) * (GRAPH_HEIGHT - 2));
                if (barH > 0) RenderUtil.rect(ctx, gx + graphW - 2 - i * (graphW / (float) HISTORY_SIZE), gy + GRAPH_HEIGHT - barH - 1, gx + graphW - i * (graphW / (float) HISTORY_SIZE), gy + GRAPH_HEIGHT - 1, getFpsColor(f));
            }
            drawY += GRAPH_HEIGHT + 2;
            maxWidth = Math.max(maxWidth, graphW);
        }

        if (performanceWarnings.getValue()) {
            String warning = getPerformanceWarning();
            if (warning != null) {
                String warningStr = "§c⚠ " + warning;
                int warningWidth = mc.font.width("⚠ " + warning);
                int warningTextX;
                if (isLeft) {
                    warningTextX = (int) (x + 2);
                } else {
                    warningTextX = (int) (x + getWidth() - 2 - warningWidth);
                }
                ctx.drawString(mc.font, warningStr, warningTextX, (int) drawY, 0xFFFF5555);
                maxWidth = Math.max(maxWidth, warningWidth);
                drawY += lineHeight;
            }
        }

        setWidth(Math.max(80, maxWidth + 4));
        setHeight(drawY - y);
    }

    private int getFpsColor(float fps) { return fps >= 60 ? 0xFF_44FF44 : fps >= 30 ? 0xFF_FFFF44 : 0xFF_FF4444; }
    private String getPerformanceWarning() { if (currentFps > 0 && currentFps < 25) return "Low FPS"; long usedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(); if (usedMem > Runtime.getRuntime().maxMemory() * 0.9) return "High memory usage"; return null; }

    @Override
    public void onDisable() { historyIndex = 0; minFps = 999; maxFps = sumFps = samples = 0; lastFrameTime = -1; }
}
