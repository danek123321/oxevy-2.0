package me.alpha432.oxevy.features.gui;

import me.alpha432.oxevy.features.modules.client.ClickGuiModule;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.awt.*;

public class BooleanComponent extends SettingComponent {
    private static final Minecraft mc = Minecraft.getInstance();
    
    public BooleanComponent(Setting<Boolean> setting, int x, int y, int width, int height) {
        super(setting, x, y, width, height);
    }
    
    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        boolean value = (Boolean) setting.getValue();
        boolean hovered = isHovered(mouseX, mouseY);
        
        // Background
        RenderUtil.rect(context, x, y, x + width, y + height, 0x11FFFFFF);
        
        // Label
        context.drawString(mc.font, setting.getName(), x + 5, y + (height - 8) / 2, 0xFFBBBBBB);
        
        // Checkbox/Toggle look
        int toggleSize = 10;
        int toggleX = x + width - toggleSize - 5;
        int toggleY = y + (height - toggleSize) / 2;
        
        int color = value ? ClickGuiModule.getInstance().color.getValue().getRGB() : 0xFF333333;
        RenderUtil.rect(context, toggleX, toggleY, toggleX + toggleSize, toggleY + toggleSize, color);
        
        if (hovered) {
            RenderUtil.rect(context, x, y, x + width, y + height, 0x11FFFFFF);
        }
    }
    
    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY) && button == 0) {
            setting.setValue(!(Boolean) setting.getValue());
        }
    }
}

