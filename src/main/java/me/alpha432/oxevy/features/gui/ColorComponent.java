package me.alpha432.oxevy.features.gui;

import me.alpha432.oxevy.features.modules.client.ClickGuiModule;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.awt.*;

public class ColorComponent extends SettingComponent {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final int PICKER_HEIGHT = 80;
    
    private boolean pickerOpen = false;
    private float[] hsb;
    private int dragX = -1, dragY = -1;
    private boolean dragging = false;
    private boolean draggingAlpha = false;
    private int pickerOffsetY = 0;
    
    public ColorComponent(Setting<Color> setting, int x, int y, int width, int height) {
        super(setting, x, y, width, height);
        Color c = (Color) setting.getValue();
        hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
    }
    
    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        Color value = (Color) setting.getValue();
        boolean hovered = isHovered(mouseX, mouseY);
        
        RenderUtil.rect(context, x, y, x + width, y + height, 0x11FFFFFF);
        
        context.drawString(mc.font, setting.getName(), x + 5, y + (height - 8) / 2, 0xFFBBBBBB);
        
        RenderUtil.rect(context, x + width - 25, y + 3, x + width - 5, y + height - 3, value.getRGB());
        
        if (hovered) {
            RenderUtil.rect(context, x, y, x + width, y + height, 0x11FFFFFF);
        }
        
        if (pickerOpen) {
            renderPicker(context, mouseX, mouseY);
        }
    }
    
    private void renderPicker(GuiGraphics context, int mouseX, int mouseY) {
        int pickerWidth = width - 10;
        int pickerX = x + 5;
        int pickerY = y + height + 2;
        
        pickerOffsetY = pickerY;
        
        Color currentColor = (Color) setting.getValue();
        Color realColor = Color.getHSBColor(hsb[0], 1, 1);
        
        RenderUtil.horizontalGradient(context, pickerX, pickerY, pickerX + pickerWidth, pickerY + pickerWidth,
            Color.WHITE, realColor);
        RenderUtil.verticalGradient(context, pickerX, pickerY, pickerX + pickerWidth, pickerY + pickerWidth,
            new Color(0, 0, 0, 0), Color.BLACK);
        
        int sat = (int) (hsb[1] * pickerWidth);
        int bri = (int) ((1 - hsb[2]) * pickerWidth);
        sat = Math.max(0, Math.min(pickerWidth - 1, sat));
        bri = Math.max(0, Math.min(pickerWidth - 1, bri));
        
        RenderUtil.rect(context, pickerX + sat - 2, pickerY + bri - 2, pickerX + sat + 2, pickerY + bri + 2, Color.BLACK.getRGB());
        
        int hueY = pickerY + pickerWidth + 3;
        for (int i = 0; i < pickerWidth; i++) {
            Color hueColor = Color.getHSBColor((float) i / pickerWidth, 1f, 1f);
            RenderUtil.rect(context, pickerX + i, hueY, pickerX + i + 1, hueY + 6, hueColor.getRGB());
        }
        
        int hueX = (int) (hsb[0] * pickerWidth);
        RenderUtil.rect(context, pickerX + hueX - 2, hueY - 1, pickerX + hueX + 2, hueY + 7, Color.BLACK.getRGB());
        
        int alphaY = hueY + 8;
        RenderUtil.horizontalGradient(context, pickerX, alphaY, pickerX + pickerWidth, alphaY + 6,
            new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), 0),
            new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), 255));
        
        int alphaX = (int) ((currentColor.getAlpha() / 255.0f) * pickerWidth);
        RenderUtil.rect(context, pickerX + alphaX - 2, alphaY - 1, pickerX + alphaX + 2, alphaY + 7, Color.BLACK.getRGB());
    }
    
    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY) && button == 0) {
            pickerOpen = !pickerOpen;
            return;
        }
        
        if (!pickerOpen) return;
        
        int pickerWidth = width - 10;
        int pickerX = x + 5;
        int pickerY = y + height + 2;
        
        if (mouseX >= pickerX && mouseX <= pickerX + pickerWidth &&
            mouseY >= pickerY && mouseY <= pickerY + pickerWidth) {
            dragging = true;
            updateColorFromPicker(mouseX, mouseY);
        }
        
        int hueY = pickerY + pickerWidth + 3;
        if (mouseX >= pickerX && mouseX <= pickerX + pickerWidth &&
            mouseY >= hueY && mouseY <= hueY + 6) {
            dragging = true;
            hsb[0] = (float) (mouseX - pickerX) / pickerWidth;
            updateColor();
        }
        
        int alphaY = hueY + 8;
        if (mouseX >= pickerX && mouseX <= pickerX + pickerWidth &&
            mouseY >= alphaY && mouseY <= alphaY + 6) {
            draggingAlpha = true;
            int alpha = (int) ((mouseX - pickerX) / pickerWidth * 255);
            alpha = Math.max(0, Math.min(255, alpha));
            Color c = (Color) setting.getValue();
            setting.setValue(new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha));
        }
    }
    
    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        draggingAlpha = false;
    }
    
    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!pickerOpen) return;
        
        int pickerWidth = width - 10;
        int pickerX = x + 5;
        int pickerY = y + height + 2;
        
        if (dragging) {
            if (mouseX >= pickerX && mouseX <= pickerX + pickerWidth &&
                mouseY >= pickerY && mouseY <= pickerY + pickerWidth) {
                updateColorFromPicker(mouseX, mouseY);
            }
            
            int hueY = pickerY + pickerWidth + 3;
            if (mouseX >= pickerX && mouseX <= pickerX + pickerWidth &&
                mouseY >= hueY && mouseY <= hueY + 6) {
                hsb[0] = Math.max(0, Math.min(1, (float) (mouseX - pickerX) / pickerWidth));
                updateColor();
            }
        }
        
        if (draggingAlpha) {
            int alphaY = pickerOffsetY + pickerWidth + 11;
            if (mouseX >= pickerX && mouseX <= pickerX + pickerWidth) {
                int alpha = (int) ((mouseX - pickerX) / pickerWidth * 255);
                alpha = Math.max(0, Math.min(255, alpha));
                Color c = (Color) setting.getValue();
                setting.setValue(new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha));
            }
        }
    }
    
    private void updateColorFromPicker(double mouseX, double mouseY) {
        int pickerWidth = width - 10;
        int pickerX = x + 5;
        int pickerY = y + height + 2;
        
        hsb[1] = (float) (mouseX - pickerX) / pickerWidth;
        hsb[2] = 1 - (float) (mouseY - pickerY) / pickerWidth;
        hsb[1] = Math.max(0, Math.min(1, hsb[1]));
        hsb[2] = Math.max(0, Math.min(1, hsb[2]));
        updateColor();
    }
    
    private void updateColor() {
        Color c = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
        Color oldColor = (Color) setting.getValue();
        setting.setValue(new Color(c.getRed(), c.getGreen(), c.getBlue(), oldColor.getAlpha()));
    }
    
    public int getHeight() {
        return pickerOpen ? height + PICKER_HEIGHT : height;
    }
}