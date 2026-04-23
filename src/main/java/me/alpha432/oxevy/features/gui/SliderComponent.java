package me.alpha432.oxevy.features.gui;

import me.alpha432.oxevy.features.modules.client.ClickGuiModule;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.awt.*;

public class SliderComponent extends SettingComponent {
    private static final Minecraft mc = Minecraft.getInstance();
    private boolean dragging = false;
    
    public SliderComponent(Setting<Number> setting, int x, int y, int width, int height) {
        super(setting, x, y, width, height);
    }
    
    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        float min = setting.getMin() != null ? ((Number) setting.getMin()).floatValue() : 0f;
        float max = setting.getMax() != null ? ((Number) setting.getMax()).floatValue() : 100f;
        float value = ((Number) setting.getValue()).floatValue();
        
        float percentage = (value - min) / (max - min);
        int fillWidth = (int) (percentage * width);
        
        // Background
        RenderUtil.rect(context, x, y, x + width, y + height, 0x11FFFFFF);
        
        // Bar
        int barHeight = 4;
        int barY = y + height - barHeight - 2;
        RenderUtil.rect(context, x + 5, barY, x + width - 5, barY + barHeight, 0xFF333333);
        
        int fillX2 = x + 5 + (int) (percentage * (width - 10));
        RenderUtil.rect(context, x + 5, barY, fillX2, barY + barHeight, ClickGuiModule.getInstance().color.getValue().getRGB());
        
        // Label and value
        String labelText = setting.getName();
        String valueText = String.valueOf(setting.getValue());
        context.drawString(mc.font, labelText, x + 5, y + 2, 0xFFBBBBBB);
        context.drawString(mc.font, valueText, x + width - 5 - mc.font.width(valueText), y + 2, 0xFFFFFFFF);
        
        if (isHovered(mouseX, mouseY)) {
            RenderUtil.rect(context, x, y, x + width, y + height, 0x11FFFFFF);
        }
        
        if (dragging) {
            updateValue(mouseX);
        }
    }
    
    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            if (button == 0) {
                dragging = true;
                updateValue(mouseX);
            } else if (button == 1) {
                setting.reset();
            }
        }
    }
    
    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
    }
    
    private void updateValue(double mouseX) {
        float min = setting.getMin() != null ? ((Number) setting.getMin()).floatValue() : 0f;
        float max = setting.getMax() != null ? ((Number) setting.getMax()).floatValue() : 100f;
        
        double percentage = Math.max(0, Math.min(1, (mouseX - (x + 5)) / (width - 10)));
        float rawValue = (float) (min + percentage * (max - min));
        rawValue = Math.max(min, Math.min(max, rawValue));
        
        if (setting.getValue() instanceof Integer) {
            int snapped = Math.round(rawValue);
            setting.setValue(snapped);
        } else if (setting.getValue() instanceof Float) {
            float snapped = snap(rawValue, 0.1f);
            setting.setValue(Math.max(min, Math.min(max, snapped)));
        } else if (setting.getValue() instanceof Double) {
            double snapped = snap(rawValue, 0.1d);
            double clamped = Math.max(min, Math.min(max, snapped));
            setting.setValue(clamped);
        }
    }

    private static float snap(float v, float step) {
        if (step <= 0) return v;
        return Math.round(v / step) * step;
    }

    private static double snap(double v, double step) {
        if (step <= 0) return v;
        return Math.round(v / step) * step;
    }
}

