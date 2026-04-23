package me.alpha432.oxevy.features.modules.hud;

import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.event.impl.render.Render2DEvent;
import me.alpha432.oxevy.features.modules.client.ClickGuiModule;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.render.AnimationUtil;
import me.alpha432.oxevy.util.ColorUtil;
import me.alpha432.oxevy.util.render.RenderUtil;
import net.minecraft.client.gui.GuiGraphics;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

public class ArrayListHudModule extends HudModule {
    public final Setting<Boolean> sortAlphabetical = bool("SortAlphabetical", false);
    public final Setting<Boolean> rainbow = bool("Rainbow", true);
    public final Setting<Boolean> smoothAnimations = bool("SmoothAnimations", true);
    public final Setting<Float> animationSpeed = num("AnimationSpeed", 0.15f, 0.05f, 0.5f);
    public final Setting<Boolean> bar = bool("Bar", true);
    public final Setting<Integer> spacing = num("Spacing", 1, 0, 5);

    private final Map<Module, Float> moduleAnimations = new HashMap<>();
    private final List<Module> enabledModules = new ArrayList<>(128);
    private final Map<Module, Integer> cachedNameWidths = new HashMap<>(128);
    private final Set<Module> enabledSet = new HashSet<>(128);

    public ArrayListHudModule() {
        super("ArrayList", "Shows enabled modules", 100, 50);
    }

    @Override
    public void drawContent(Render2DEvent e) {
        GuiGraphics ctx = e.getContext();
        float x = getX();
        float y = getY();
        int lineHeight = mc.font.lineHeight;
        float drawY = y;
        float maxWidth = 0;

        enabledModules.clear();
        enabledSet.clear();
        cachedNameWidths.clear();
        for (Module module : Oxevy.moduleManager.getModules()) {
            if ((module.isEnabled() || (smoothAnimations.getValue() && moduleAnimations.getOrDefault(module, 0f) > 0.01f)) 
                && !module.hidden && module.getCategory() != Module.Category.HUD) {
                enabledModules.add(module);
                if (module.isEnabled()) enabledSet.add(module);
            }
        }

        // Cache name widths once per frame; used for sorting and alignment.
        for (Module module : enabledModules) {
            cachedNameWidths.put(module, mc.font.width(module.getName()));
        }

        if (sortAlphabetical.getValue()) {
            enabledModules.sort(Comparator.comparing(Module::getName));
        } else {
            enabledModules.sort((m1, m2) -> Integer.compare(
                cachedNameWidths.getOrDefault(m2, 0),
                cachedNameWidths.getOrDefault(m1, 0)
            ));
        }

        for (Module module : enabledModules) {
            maxWidth = Math.max(maxWidth, cachedNameWidths.getOrDefault(module, 0));
        }
        
        setWidth(maxWidth + (bar.getValue() ? 6 : 4));
        setHeight(enabledModules.isEmpty() ? 10 : enabledModules.size() * (lineHeight + spacing.getValue()));
        
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        boolean isLeft = x < screenWidth / 2.0f;

        final boolean doRainbow = rainbow.getValue();
        final boolean doSmooth = smoothAnimations.getValue();
        final float speed = doSmooth ? animationSpeed.getValue() : 1.0f;
        final int baseColor = ClickGuiModule.getInstance().color.getValue().getRGB();
        
        for (int i = 0; i < enabledModules.size(); i++) {
            Module module = enabledModules.get(i);
            String text = module.getName();
            int textWidth = cachedNameWidths.getOrDefault(module, mc.font.width(text));

            float targetAnim = enabledSet.contains(module) ? 1.0f : 0.0f;
            moduleAnimations.putIfAbsent(module, 0.0f);
            float currentAnim = moduleAnimations.get(module);
            currentAnim = AnimationUtil.animate(currentAnim, targetAnim, speed, AnimationUtil.Easing.EASE_OUT);
            moduleAnimations.put(module, currentAnim);

            if (currentAnim < 0.01f && !enabledSet.contains(module)) continue;

            int color = doRainbow 
                ? Color.getHSBColor(((System.currentTimeMillis() + (i * 200)) / 10 % 360) / 360f, 0.7f, 1.0f).getRGB()
                : baseColor;
            
            int finalColor = AnimationUtil.interpolateColor(0, color, currentAnim);
            
            int textX;
            if (isLeft) {
                textX = (int) (x + 2 + (bar.getValue() ? 2 : 0));
                if (bar.getValue()) {
                    RenderUtil.rect(ctx, x, drawY, x + 2, drawY + lineHeight + spacing.getValue(), color);
                }
            } else {
                textX = (int) (x + getWidth() - 2 - textWidth - (bar.getValue() ? 2 : 0));
                if (bar.getValue()) {
                    RenderUtil.rect(ctx, x + getWidth() - 2, drawY, x + getWidth(), drawY + lineHeight + spacing.getValue(), color);
                }
            }

            ctx.pose().pushMatrix();
            if (doSmooth) {
                float slide = (1.0f - currentAnim) * (isLeft ? -20 : 20);
                ctx.pose().translate(slide, 0);
            }
            ctx.drawString(mc.font, text, textX, (int) (drawY + spacing.getValue() / 2f), finalColor);
            ctx.pose().popMatrix();

            drawY += lineHeight + spacing.getValue();
        }

        moduleAnimations.entrySet().removeIf(entry -> !enabledSet.contains(entry.getKey()) && entry.getValue() < 0.01f);
    }
}
