package me.alpha432.oxevy.features.modules.hud;

import me.alpha432.oxevy.event.impl.render.Render2DEvent;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.render.RenderUtil;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.resources.Identifier;

import java.io.File;

public class MenuWatermark extends Module {
    public Setting<Integer> red = num("Red", 255, 0, 255);
    public Setting<Integer> green = num("Green", 255, 0, 255);
    public Setting<Integer> blue = num("Blue", 255, 0, 255);
    public Setting<Integer> alpha = num("Alpha", 255, 0, 255);

    private Identifier watermarkId = null;

    public MenuWatermark() {
        super("MenuWatermark", "Shows watermark on main menu", Category.HUD);
    }

    private void ensureWatermarkLoaded() {
        if (watermarkId != null) return;
        watermarkId = RenderUtil.loadTexture(new File("images/watermark.png"), "oxevy/menu_watermark");
    }

    @Override
    public void onRender2D(Render2DEvent event) {
        if (!(mc.screen instanceof TitleScreen)) return;

        ensureWatermarkLoaded();

        int color = (alpha.getValue() << 24) | (red.getValue() << 16) | (green.getValue() << 8) | blue.getValue();
        
        float screenWidth = mc.getWindow().getGuiScaledWidth();
        
        String text = "oxevy";
        int textWidth = mc.font.width(text);
        int imgSize = mc.font.lineHeight * 4;

        float x = screenWidth - textWidth - imgSize - 20;
        float y = 10;

        if (watermarkId != null) {
            event.getContext().blit(net.minecraft.client.renderer.RenderPipelines.GUI, watermarkId, (int) x, (int) y, imgSize, imgSize, 0, 0, imgSize, imgSize, imgSize, imgSize);
            x += imgSize + 5;
        }
        
        event.getContext().drawString(mc.font, text, (int) x, (int) y + (imgSize - mc.font.lineHeight) / 2, color);
    }
}
