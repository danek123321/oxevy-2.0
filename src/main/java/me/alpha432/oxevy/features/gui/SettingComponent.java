package me.alpha432.oxevy.features.gui;

import me.alpha432.oxevy.features.settings.Setting;
import net.minecraft.client.gui.GuiGraphics;

public abstract class SettingComponent {
    protected int x, y;
    protected final int width, height;
    protected final Setting setting;
    
    public SettingComponent(Setting setting, int x, int y, int width, int height) {
        this.setting = setting;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    public abstract void render(GuiGraphics context, int mouseX, int mouseY, float delta);
    
    public void mouseClicked(double mouseX, double mouseY, int button) {}
    
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {}
    
    public void mouseReleased(double mouseX, double mouseY, int button) {}
    
    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
    
    public Setting getSetting() {
        return setting;
    }
}
