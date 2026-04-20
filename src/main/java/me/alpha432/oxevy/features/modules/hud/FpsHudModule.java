package me.alpha432.oxevy.features.modules.hud;

import me.alpha432.oxevy.event.impl.render.Render2DEvent;
import me.alpha432.oxevy.features.modules.client.HudModule;
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
    public final Setting<Boolean> background = bool("Background", true);
    public final Setting<Integer> backgroundOpacity = num("BackgroundOpacity", 100, 0, 255);

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
        register(backgroundOpacity);
        backgroundOpacity.setVisibility((Integer i) -> background.getValue());
    }

    @Override
    protected void render(Render2DEvent e) {
        super.render(e);

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

        // Main FPS line with color (green >60, yellow >30, red <30)
        String fpsColorCode = currentFps >= 60 ? "§a" : currentFps >= 30 ? "§e" : "§c";
        String fpsStr = "§fFPS: " + fpsColorCode + (int) currentFps;
        ctx.drawString(mc.font, fpsStr, (int) x, (int) drawY, getFpsColor(currentFps));
        maxWidth = Math.max(maxWidth, mc.font.width(fpsStr));
        drawY += lineHeight;

        if (showMinMax.getValue()) {
            String minMaxStr = "§7Min: §f" + (int) minFps + " §7Max: §f" + (int) maxFps;
            ctx.drawString(mc.font, minMaxStr, (int) x, (int) drawY, 0xFF_CC_CC_CC);
            maxWidth = Math.max(maxWidth, mc.font.width(minMaxStr));
            drawY += lineHeight;
        }

        if (showAverage.getValue() && samples > 0) {
            float avg = sumFps / samples;
            String avgStr = "§7Avg: §f" + String.format(Locale.US, "%.1f", avg);
            ctx.drawString(mc.font, avgStr, (int) x, (int) drawY, 0xFF_CC_CC_CC);
            maxWidth = Math.max(maxWidth, mc.font.width(avgStr));
            drawY += lineHeight;
        }

        if (showFrameTime.getValue()) {
            float ms = currentFps > 0 ? 1000f / currentFps : 0;
            String msStr = "§7Frame: §f" + String.format(Locale.US, "%.1f", ms) + " ms";
            ctx.drawString(mc.font, msStr, (int) x, (int) drawY, 0xFF_CC_CC_CC);
            maxWidth = Math.max(maxWidth, mc.font.width(msStr));
            drawY += lineHeight;
        }

        if (showGraph.getValue() && historyIndex > 0) {
            int graphW = Math.min(HISTORY_SIZE * 2, 100);
            float gx = x;
            float gy = drawY;
            if (background.getValue()) {
                int alpha = backgroundOpacity.getValue();
                RenderUtil.rect(ctx, gx, gy, gx + graphW, gy + GRAPH_HEIGHT, (alpha << 24) | 0x11_11_11);
            }
            float maxVal = 1f;
            for (int i = 0; i < HISTORY_SIZE; i++) {
                if (fpsHistory[i] > maxVal) maxVal = fpsHistory[i];
            }
            if (maxVal < 1) maxVal = 1;
            for (int i = 0; i < Math.min(historyIndex, HISTORY_SIZE); i++) {
                int idx = (historyIndex - 1 - i + HISTORY_SIZE) % HISTORY_SIZE;
                float f = fpsHistory[idx];
                int barH = (int) ((f / maxVal) * (GRAPH_HEIGHT - 2));
                if (barH > 0) {
                    int barColor = getFpsColor(f);
                    int barX = (int) (gx + graphW - 2 - i * (graphW / (float) HISTORY_SIZE));
                    RenderUtil.rect(ctx, barX, gy + GRAPH_HEIGHT - barH - 1, barX + 2, gy + GRAPH_HEIGHT - 1, barColor);
                }
            }
            drawY += GRAPH_HEIGHT + 2;
            maxWidth = Math.max(maxWidth, graphW);
        }

        if (performanceWarnings.getValue()) {
            String warning = getPerformanceWarning();
            if (warning != null) {
                ctx.drawString(mc.font, "§c⚠ " + warning, (int) x, (int) drawY, 0xFFFF5555);
                maxWidth = Math.max(maxWidth, mc.font.width(warning) + mc.font.width("⚠ "));
                drawY += lineHeight;
            }
        }

        setWidth(Math.max(getWidth(), maxWidth + 4));
        setHeight(drawY - y);

        if (background.getValue() && !showGraph.getValue()) {
            int alpha = backgroundOpacity.getValue();
            RenderUtil.rect(ctx, x - 2, y - 2, x + getWidth() + 2, y + getHeight() + 2, (alpha << 24) | 0x11_11_11);
        }
    }

    private int getFpsColor(float fps) {
        if (fps >= 60) return 0xFF_44_FF_44; // green
        if (fps >= 30) return 0xFF_FF_FF_44; // yellow
        return 0xFF_FF_44_44; // red
    }

    private String getPerformanceWarning() {
        if (currentFps > 0 && currentFps < 25) {
            return "Low FPS - try lowering render distance";
        }
        long usedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long maxMem = Runtime.getRuntime().maxMemory();
        if (maxMem != Long.MAX_VALUE && usedMem > maxMem * 0.9) {
            return "High memory usage";
        }
        return null;
    }

    @Override
    public void onDisable() {
        historyIndex = 0;
        minFps = 999;
        maxFps = 0;
        sumFps = 0;
        samples = 0;
        lastFrameTime = -1;
    }
}
