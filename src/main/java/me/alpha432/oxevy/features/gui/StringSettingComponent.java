package me.alpha432.oxevy.features.gui;

import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class StringSettingComponent extends SettingComponent {
    private static final Minecraft mc = Minecraft.getInstance();
    
    public StringSettingComponent(Setting<?> setting, int x, int y, int width, int height) {
        super(setting, x, y, width, height);
    }
    
    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        // Background
        RenderUtil.rect(context, x, y, x + width, y + height, 0x11FFFFFF);
        
        // Label
        context.drawString(mc.font, setting.getName(), x + 5, y + (height - 8) / 2, 0xFFBBBBBB);
        
        // Value
        String valueText = setting.getValueAsString();
        context.drawString(mc.font, valueText, x + width - 5 - mc.font.width(valueText), y + (height - 8) / 2, 0xFFFFFFFF);
        
        if (isHovered(mouseX, mouseY)) {
            RenderUtil.rect(context, x, y, x + width, y + height, 0x11FFFFFF);
        }
    }
}

