package me.alpha432.oxevy.features.modules.hud;

import me.alpha432.oxevy.event.impl.render.Render2DEvent;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import net.minecraft.client.gui.screens.TitleScreen;

public class MenuWatermark extends Module {
    public Setting<Integer> red = num("Red", 255, 0, 255);
    public Setting<Integer> green = num("Green", 255, 0, 255);
    public Setting<Integer> blue = num("Blue", 255, 0, 255);
    public Setting<Integer> alpha = num("Alpha", 255, 0, 255);

    public MenuWatermark() {
        super("MenuWatermark", "Shows watermark on main menu", Category.HUD);
    }

    @Override
    public void onRender2D(Render2DEvent event) {
        if (!(mc.screen instanceof TitleScreen)) return;

        int color = (alpha.getValue() << 24) | (red.getValue() << 16) | (green.getValue() << 8) | blue.getValue();
        
        float screenWidth = mc.getWindow().getGuiScaledWidth();
        float screenHeight = mc.getWindow().getGuiScaledHeight();
        
        float x = screenWidth - mc.font.width("oxevy") - 10;
        float y = 10;
        
        event.getContext().drawString(mc.font, "oxevy", (int) x, (int) y, color);
    }
}
