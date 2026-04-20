package me.alpha432.oxevy.features.modules.hud;

import me.alpha432.oxevy.event.impl.render.Render2DEvent;
import me.alpha432.oxevy.features.modules.client.ClickGuiModule;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.BuildConfig;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileInputStream;

public class WatermarkHudModule extends HudModule {
    public Setting<String> text = str("Text", BuildConfig.NAME);
    public Setting<Boolean> version = bool("Version", true);

    private Identifier watermarkId = null;
    private NativeImageBackedTexture watermarkTex = null;

    public WatermarkHudModule() {
        super("Watermark", "Display watermark", 100, 10);
    }

    private void ensureWatermarkLoaded() {
        if (watermarkId != null) return;
        File f = new File("images/watermark.png");
        if (!f.exists()) return;
        try (FileInputStream fis = new FileInputStream(f)) {
            NativeImage img = NativeImage.read(fis);
            watermarkTex = new NativeImageBackedTexture(img);
            watermarkId = mc.getTextureManager().registerDynamicTexture("oxevy/watermark", watermarkTex);
        } catch (Exception ignored) {}
    }

    @Override
    public void drawContent(Render2DEvent e) {
        String watermarkString = text.getValue() + (version.getValue() ? " " + BuildConfig.VERSION : "");

        ensureWatermarkLoaded();

        int imgW = 0, imgH = 0;
        if (watermarkId != null) {
            imgW = mc.font.lineHeight * 3;
            imgH = mc.font.lineHeight * 3;
        }

        setWidth(mc.font.width(watermarkString) + imgW + 4);
        setHeight(Math.max(mc.font.lineHeight, imgH));

        float x = getX();
        float y = getY();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        boolean isLeft = x < screenWidth / 2.0f;

        int textX;
        int imgX = (int) x + 2;
        if (isLeft) {
            textX = (int) (x + 2 + imgW + 2);
        } else {
            textX = (int) (x + getWidth() - 2 - mc.font.width(watermarkString));
            imgX = (int) (textX - imgW - 2);
        }

        int color = ClickGuiModule.getInstance().color.getValue().getRGB();

        if (watermarkId != null) {
            try {
                e.getContext().drawTexture(watermarkId, imgX, (int) y, imgW, imgH, 0, 0, imgW, imgH, imgW, imgH);
            } catch (Throwable t) {
                // fallback: ignore
            }
        }

        e.getContext().drawString(mc.font, watermarkString, textX, (int) y + (mc.font.lineHeight - mc.font.lineHeight) / 2, color);
    }
}
