package me.alpha432.oxevy.features.modules.hud;

import me.alpha432.oxevy.event.impl.render.Render2DEvent;
import me.alpha432.oxevy.features.modules.client.ClickGuiModule;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.BuildConfig;

import java.awt.Color;
import java.io.File;

public class WatermarkHudModule extends HudModule {
    public Setting<String> text = str("Text", BuildConfig.NAME);
    public Setting<Boolean> version = bool("Version", true);
    public Setting<Boolean> customColor = bool("CustomColor", false);
    public Setting<Color> color = color("Color", 255, 255, 255, 255);

    private Object watermarkId = null;

    public WatermarkHudModule() {
        super("Watermark", "Display watermark", 100, 10);
        color.setVisibility(v -> customColor.getValue());
    }

    private void ensureWatermarkLoaded() {
        if (watermarkId != null) return;
        watermarkId = me.alpha432.oxevy.util.render.RenderUtil.loadTexture(new File("images/watermark.png"), "oxevy/watermark");
    }

    @Override
    public void drawContent(Render2DEvent e) {
        String watermarkString = text.getValue().toLowerCase() + (version.getValue() ? " v" + BuildConfig.VERSION : "");

        ensureWatermarkLoaded();

        int imgW = 0, imgH = 0;
        int iconSize = mc.font.lineHeight + 4;
        if (watermarkId != null) {
            imgW = iconSize;
            imgH = iconSize;
        }

        setWidth(mc.font.width(watermarkString) + imgW + 10);
        setHeight(iconSize + 2);

        float x = getX();
        float y = getY();
        
        int drawColor = customColor.getValue() ? color.getValue().getRGB() : ClickGuiModule.getInstance().color.getValue().getRGB();

        float currentX = x + 4;
        if (watermarkId != null) {
            e.getContext().blit(net.minecraft.client.renderer.RenderPipelines.GUI, (net.minecraft.resources.Identifier) watermarkId, (int) currentX, (int) (y + 1), imgW, imgH, 0, 0, imgW, imgH, imgW, imgH);
            currentX += imgW + 4;
        }

        e.getContext().drawString(mc.font, watermarkString, (int) currentX, (int) (y + (getHeight() - mc.font.lineHeight) / 2f), drawColor);
    }
}
