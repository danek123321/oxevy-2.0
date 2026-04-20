package me.alpha432.oxevy.features.modules.hud;

import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.event.impl.render.Render2DEvent;
import me.alpha432.oxevy.features.modules.client.HudModule;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.render.AnimationUtil;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArrayListHudModule extends HudModule {
    public final Setting<Boolean> sortAlphabetical = bool("SortAlphabetical", true);
    public final Setting<Boolean> showEnabledOnly = bool("ShowEnabledOnly", true);
    public final Setting<Boolean> showBackground = bool("Background", true);
    public final Setting<Integer> bgOpacity = num("BackgroundOpacity", 120, 0, 255);
    public final Setting<Boolean> rainbow = bool("Rainbow", false);
    public final Setting<Boolean> outline = bool("Outline", false);
    public final Setting<Boolean> smoothAnimations = bool("SmoothAnimations", true);
    public final Setting<Float> animationSpeed = num("AnimationSpeed", 0.15f, 0.05f, 0.5f);

    // Animation tracking
    private final Map<Module, Float> moduleAnimations = new HashMap<>();
    private final Map<Module, Float> modulePositions = new HashMap<>();

    public ArrayListHudModule() {
        super("ArrayList", "Shows enabled modules", 100, 50);
    }

    @Override
    protected void render(Render2DEvent e) {
        super.render(e);

        GuiGraphics ctx = e.getContext();
        float x = getX();
        float y = getY();
        int lineHeight = mc.font.lineHeight;
        float drawY = y;
        float maxWidth = 0;

        // Get enabled modules
        List<Module> enabledModules = new ArrayList<>();
        for (Module module : Oxevy.moduleManager.getModules()) {
            if (module.isEnabled() && !module.hidden) {
                enabledModules.add(module);
            }
        }

        // Sort modules
        if (sortAlphabetical.getValue()) {
            enabledModules.sort(Comparator.comparing(Module::getName));
        }

        // Render each module with animations
        for (int i = 0; i < enabledModules.size(); i++) {
            Module module = enabledModules.get(i);
            String text = module.getName();
            int textWidth = mc.font.width(text);
            maxWidth = Math.max(maxWidth, textWidth);

            // Update animation for this module
            float targetAnim = 1.0f;
            if (!moduleAnimations.containsKey(module)) {
                moduleAnimations.put(module, 0.0f);
            }
            float currentAnim = moduleAnimations.get(module);
            float speed = smoothAnimations.getValue() ? animationSpeed.getValue() : 1.0f;
            currentAnim = AnimationUtil.animate(currentAnim, targetAnim, speed, AnimationUtil.Easing.EASE_OUT_BACK);
            moduleAnimations.put(module, currentAnim);

            // Calculate position with slide-in animation
            float slideOffset = smoothAnimations.getValue() ? (1.0f - currentAnim) * 20.0f : 0.0f;
            float animatedX = x + slideOffset;
            float alpha = smoothAnimations.getValue() ? currentAnim : 1.0f;

            // Background with animation
            if (showBackground.getValue()) {
                int bgAlpha = (int) (bgOpacity.getValue() * alpha);
                ctx.fill((int) animatedX - 2, (int) drawY - 1, (int) animatedX + textWidth + 2, (int) drawY + lineHeight + 1, 
                    (bgAlpha << 24) | 0x11_11_11);
            }

            // Outline with animation
            if (outline.getValue()) {
                int outlineAlpha = (int) (255 * alpha);
                ctx.fill((int) animatedX - 3, (int) drawY - 2, (int) animatedX + textWidth + 3, (int) drawY - 1, (outlineAlpha << 24));
                ctx.fill((int) animatedX - 3, (int) drawY + lineHeight + 1, (int) animatedX + textWidth + 3, (int) drawY + lineHeight + 2, (outlineAlpha << 24));
                ctx.fill((int) animatedX - 3, (int) drawY - 1, (int) animatedX - 2, (int) drawY + lineHeight + 1, (outlineAlpha << 24));
                ctx.fill((int) animatedX + textWidth + 2, (int) drawY - 1, (int) animatedX + textWidth + 3, (int) drawY + lineHeight + 1, (outlineAlpha << 24));
            }

            // Draw module name with color and animation
            int color = Oxevy.colorManager.getColor().getRGB();
            if (rainbow.getValue()) {
                color = (int) (System.currentTimeMillis() / 10 % 360);
                color = java.awt.Color.HSBtoRGB(color / 360f, 0.8f, 1.0f);
            }
            
            // Apply alpha to color
            int alphaComponent = (int) (alpha * 255);
            int finalColor = (alphaComponent << 24) | (color & 0x00FFFFFF);
            
            ctx.drawString(mc.font, text, (int) animatedX, (int) drawY, finalColor);

            drawY += lineHeight + 1;
        }

        // Clean up animations for disabled modules
        moduleAnimations.entrySet().removeIf(entry -> 
            !enabledModules.contains(entry.getKey()) && entry.getValue() >= 1.0f);

        setWidth(Math.max(getWidth(), maxWidth + 4));
        setHeight(drawY - y);
    }
}
