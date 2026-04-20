package me.alpha432.oxevy.features.modules.hud;

import me.alpha432.oxevy.event.impl.render.Render2DEvent;
import me.alpha432.oxevy.features.modules.client.ClickGuiModule;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.BuildConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class WatermarkHudModule extends HudModule {
    public Setting<String> text = str("Text", BuildConfig.NAME);
    public Setting<Boolean> version = bool("Version", true);

    private Object watermarkId = null;
    private Object watermarkTex = null;

    public WatermarkHudModule() {
        super("Watermark", "Display watermark", 100, 10);
    }

    private void ensureWatermarkLoaded() {
        if (watermarkId != null) return;
        File f = new File("images/watermark.png");
        if (!f.exists()) return;
        try (InputStream fis = new FileInputStream(f)) {
            // Use reflection to avoid compile-time dependency on mappings
            Class<?> nativeImageClass = Class.forName("net.minecraft.client.texture.NativeImage");
            Method read = nativeImageClass.getMethod("read", InputStream.class);
            Object img = read.invoke(null, fis);

            Class<?> nativeTexClass = Class.forName("net.minecraft.client.texture.NativeImageBackedTexture");
            Constructor<?> texCtor = nativeTexClass.getConstructor(nativeImageClass);
            Object tex = texCtor.newInstance(img);

            Object textureManager = mc.getTextureManager();
            Method registerMethod = textureManager.getClass().getMethod("registerDynamicTexture", String.class, nativeTexClass);
            Object id = registerMethod.invoke(textureManager, "oxevy/watermark", tex);

            watermarkId = id;
            watermarkTex = tex;
        } catch (Throwable ignored) {}
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
                // find suitable drawTexture method reflectively (different mappings/version have different signatures)
                Method drawMethod = null;
                for (Method m : e.getContext().getClass().getMethods()) {
                    if (!m.getName().equals("drawTexture")) continue;
                    if (m.getParameterCount() >= 5) { // pick a likely overload
                        drawMethod = m;
                        break;
                    }
                }
                if (drawMethod != null) {
                    // Try invoking with many args if possible
                    if (drawMethod.getParameterCount() == 11) {
                        drawMethod.invoke(e.getContext(), watermarkId, imgX, (int) y, imgW, imgH, 0, 0, imgW, imgH, imgW, imgH);
                    } else if (drawMethod.getParameterCount() == 5) {
                        drawMethod.invoke(e.getContext(), watermarkId, imgX, (int) y, imgW, imgH);
                    } else {
                        // attempt generic invocation with first 5 params
                        Object[] args = new Object[drawMethod.getParameterCount()];
                        args[0] = watermarkId; args[1] = imgX; args[2] = (int) y; args[3] = imgW; args[4] = imgH;
                        for (int i = 5; i < args.length; i++) args[i] = 0;
                        drawMethod.invoke(e.getContext(), args);
                    }
                }
            } catch (Throwable t) {
                // fallback: ignore
            }
        }

        e.getContext().drawString(mc.font, watermarkString, textX, (int) y + (mc.font.lineHeight - mc.font.lineHeight) / 2, color);
    }
}
