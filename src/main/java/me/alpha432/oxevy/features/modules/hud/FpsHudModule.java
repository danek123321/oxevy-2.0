package me.alpha432.oxevy.features.modules.hud;

import me.alpha432.oxevy.event.impl.render.Render2DEvent;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.render.RenderUtil;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Locale;

public class FpsHudModule extends HudModule {
    private static final int HISTORY_SIZE = 60;

    public final Setting<Boolean> showMinMax = bool("MinMax", true);
    public final Setting<Boolean> showAverage = bool("Average", true);
    public final Setting<Boolean> showFrameTime = bool("FrameTime", true);
    public final Setting<Boolean> showGraph = bool("Graph", true);
    public final Setting<Boolean> performanceWarnings = bool("Warnings", true);

    // Smoothing: alpha = 0.06 for ~16 frame window (heavily smooths jitter)
    private static final float SMOOTHING_ALPHA = 0.06f;

    private long lastFrameNanos;
    private float currentFps;
    private float smoothFps;
    private float minFps = Float.MAX_VALUE;
    private float maxFps;
    private double sumFps;
    private int samples;

    // Circular buffer for graph
    private final float[] fpsHistory = new float[HISTORY_SIZE];
    private int historyIndex;

    // Cached text to avoid per-frame formatting
    private String cachedFpsText = "";
    private String cachedMinMaxText = "";
    private String cachedAvgText = "";
    private String cachedFrameTimeText = "";
    private int cachedFpsWidth;
    private int cachedMinMaxWidth;
    private int cachedAvgWidth;
    private int cachedFrameTimeWidth;
    private int lastDisplayedFps = -1;
    private boolean textDirty = true;

    private int cachedFpsColor = 0xFF44FF44;

    public FpsHudModule() {
        super("FPS", "FPS counter with graph and stats", 120, 60);
    }

    @Override
    public void drawContent(Render2DEvent e) {
        measureFps();

        float x = getX();
        float y = getY();
        GuiGraphics ctx = e.getContext();
        int lineHeight = mc.font.lineHeight;
        float drawY = y;
        int maxWidth = 0;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        boolean isLeft = x < screenWidth / 2.0f;

        updateCachedText();
        updateCachedColors();

        // Main FPS line
        int textX = getTextX(x, isLeft, cachedFpsWidth);
        if (textX >= 0) {
            ctx.drawString(mc.font, cachedFpsText, textX, (int) drawY, cachedFpsColor);
        }
        maxWidth = Math.max(maxWidth, cachedFpsWidth);
        drawY += lineHeight;

        // Min/Max
        if (showMinMax.getValue() && samples > 0) {
            int mmX = getTextX(x, isLeft, cachedMinMaxWidth);
            if (mmX >= 0) {
                ctx.drawString(mc.font, cachedMinMaxText, mmX, (int) drawY, 0xFFCCCCCC);
            }
            maxWidth = Math.max(maxWidth, cachedMinMaxWidth);
            drawY += lineHeight;
        }

        // Average
        if (showAverage.getValue() && samples > 0) {
            int avgX = getTextX(x, isLeft, cachedAvgWidth);
            if (avgX >= 0) {
                ctx.drawString(mc.font, cachedAvgText, avgX, (int) drawY, 0xFFCCCCCC);
            }
            maxWidth = Math.max(maxWidth, cachedAvgWidth);
            drawY += lineHeight;
        }

        // Frame time
        if (showFrameTime.getValue()) {
            int ftX = getTextX(x, isLeft, cachedFrameTimeWidth);
            if (ftX >= 0) {
                ctx.drawString(mc.font, cachedFrameTimeText, ftX, (int) drawY, 0xFFCCCCCC);
            }
            maxWidth = Math.max(maxWidth, cachedFrameTimeWidth);
            drawY += lineHeight;
        }

        // Graph
        if (showGraph.getValue() && historyIndex > 0) {
            drawGraph(ctx, x, drawY);
            drawY += 26;
            maxWidth = Math.max(maxWidth, 100);
        }

        // Warning
        if (performanceWarnings.getValue()) {
            String warning = getPerformanceWarning();
            if (warning != null) {
                String warnStr = "§c⚠ " + warning;
                int warnWidth = mc.font.width(warnStr);
                int warnX = getTextX(x, isLeft, warnWidth);
                if (warnX >= 0) {
                    ctx.drawString(mc.font, warnStr, warnX, (int) drawY, 0xFFFF5555);
                }
                maxWidth = Math.max(maxWidth, warnWidth);
                drawY += lineHeight;
            }
        }

        setWidth(Math.max(80, maxWidth + 4));
        setHeight(drawY - y);
    }

    private void measureFps() {
        long now = System.nanoTime();
        if (lastFrameNanos > 0) {
            float dt = (now - lastFrameNanos) / 1_000_000_000f;
            if (dt > 0 && dt < 1f) {
                currentFps = 1f / dt;

                // Exponential moving average for stability
                if (samples == 0) {
                    smoothFps = currentFps;
                } else {
                    smoothFps = smoothFps * (1f - SMOOTHING_ALPHA) + currentFps * SMOOTHING_ALPHA;
                }

                if (currentFps > 0 && currentFps < 10000) {
                    fpsHistory[historyIndex % HISTORY_SIZE] = currentFps;
                    historyIndex++;
                    minFps = Math.min(minFps, currentFps);
                    maxFps = Math.max(maxFps, currentFps);
                    sumFps += currentFps;
                    samples++;
                }

                int displayFps = Math.round(smoothFps);
                if (displayFps != lastDisplayedFps) {
                    lastDisplayedFps = displayFps;
                    textDirty = true;
                }
            }
        }
        lastFrameNanos = now;
    }

    private void updateCachedText() {
        if (!textDirty && samples > 0) return;

        int display = Math.round(smoothFps);
        String colorCode = display >= 60 ? "§a" : display >= 30 ? "§e" : "§c";
        cachedFpsText = "§fFPS: " + colorCode + display;
        cachedFpsWidth = mc.font.width(cachedFpsText);

        if (showMinMax.getValue() && samples > 0) {
            cachedMinMaxText = "§7Min: §f" + (int) minFps + " §7Max: §f" + (int) maxFps;
            cachedMinMaxWidth = mc.font.width(cachedMinMaxText);
        }

        if (showAverage.getValue() && samples > 0) {
            cachedAvgText = "§7Avg: §f" + String.format(Locale.US, "%.1f", sumFps / samples);
            cachedAvgWidth = mc.font.width(cachedAvgText);
        }

        if (showFrameTime.getValue()) {
            float ms = smoothFps > 0 ? 1000f / smoothFps : 0;
            cachedFrameTimeText = "§7Frame: §f" + String.format(Locale.US, "%.1f", ms) + " ms";
            cachedFrameTimeWidth = mc.font.width(cachedFrameTimeText);
        }

        textDirty = false;
    }

    private void updateCachedColors() {
        if (smoothFps >= 60) cachedFpsColor = 0xFF44FF44;
        else if (smoothFps >= 30) cachedFpsColor = 0xFFFFFF44;
        else cachedFpsColor = 0xFFFF4444;
    }

    private int getTextX(float x, boolean isLeft, int textWidth) {
        if (isLeft) {
            return (int) (x + 2);
        } else {
            return (int) (x + getWidth() - 2 - textWidth);
        }
    }

    private void drawGraph(GuiGraphics ctx, float gx, float gy) {
        float maxVal = 1f;
        float step = 100f / HISTORY_SIZE;

        for (int i = 0; i < Math.min(historyIndex, HISTORY_SIZE); i++) {
            float f = fpsHistory[i];
            if (f > maxVal) maxVal = f;
        }

        for (int i = 0; i < Math.min(historyIndex, HISTORY_SIZE); i++) {
            int idx = (historyIndex - 1 - i + HISTORY_SIZE) % HISTORY_SIZE;
            float f = fpsHistory[idx];
            int barH = (int) ((f / maxVal) * 22);
            if (barH > 0) {
                int color = f >= 60 ? 0xFF44FF44 : f >= 30 ? 0xFFFFFF44 : 0xFFFF4444;
                float barX = gx + 100 - step * (i + 1);
                RenderUtil.rect(ctx, barX, gy + 24 - barH, barX + step - 1, gy + 24, color);
            }
        }
    }

    private int getFpsColor(float fps) {
        return fps >= 60 ? 0xFF44FF44 : fps >= 30 ? 0xFFFFFF44 : 0xFFFF4444;
    }

    private String getPerformanceWarning() {
        if (smoothFps > 0 && smoothFps < 25) return "Low FPS";
        long usedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        if (usedMem > Runtime.getRuntime().maxMemory() * 0.9) return "High memory usage";
        return null;
    }

    @Override
    public void onDisable() {
        historyIndex = 0;
        minFps = Float.MAX_VALUE;
        maxFps = 0;
        sumFps = 0;
        samples = 0;
        lastFrameNanos = 0;
        smoothFps = 0;
        textDirty = true;
        lastDisplayedFps = -1;
    }
}
