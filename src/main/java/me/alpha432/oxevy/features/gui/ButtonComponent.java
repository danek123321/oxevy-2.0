package me.alpha432.oxevy.features.gui;

import me.alpha432.oxevy.features.modules.client.ClickGuiModule;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class ButtonComponent extends SettingComponent {
    private static final Minecraft mc = Minecraft.getInstance();
    
    public ButtonComponent(Setting<Runnable> setting, int x, int y, int width, int height) {
        super(setting, x, y, width, height);
    }
    
    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        boolean hovered = isHovered(mouseX, mouseY);
        
        // Background
        int bgColor = hovered ? ClickGuiModule.getInstance().color.getValue().getRGB() : 0x22FFFFFF;
        RenderUtil.rect(context, x, y, x + width, y + height, bgColor);
        
        // Label
        String label = setting.getName();
        context.drawString(mc.font, label, x + (width - mc.font.width(label)) / 2, y + (height - 8) / 2, 0xFFFFFFFF);
    }
    
    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY) && button == 0) {
            setting.run();
        }
    }
}
