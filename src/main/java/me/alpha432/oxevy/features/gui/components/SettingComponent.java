package me.alpha432.oxevy.features.gui.components;

import me.alpha432.oxevy.features.settings.Setting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;

public class SettingComponent {
    public int x, y;
    public int width = 100;
    public int height = 20;
    public Setting<?> setting;
    
    public SettingComponent(Setting<?> setting) {
        this.setting = setting;
    }
    
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
    }
    
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public int getHeight() { return height; }
    public int getWidth() { return width; }
}