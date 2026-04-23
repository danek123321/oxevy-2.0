package me.alpha432.oxevy.features.modules.hud;

import me.alpha432.oxevy.event.impl.render.Render2DEvent;
import me.alpha432.oxevy.features.settings.Setting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PotionEffectsHudModule extends HudModule {
    public final Setting<Boolean> showAmplifier = bool("Amplifier", true);
    public final Setting<Boolean> showTime = bool("Time", true);
    public final Setting<Boolean> sortByTime = bool("SortByTime", true);

    private final List<MobEffectInstance> effects = new ArrayList<>(32);

    public PotionEffectsHudModule() {
        super("PotionEffects", "Shows active potion effects", 120, 60);
    }

    @Override
    public void drawContent(Render2DEvent e) {
        if (nullCheck()) return;

        GuiGraphics ctx = e.getContext();
        float x = getX();
        float y = getY();
        int lineHeight = mc.font.lineHeight;
        float drawY = y;
        int maxWidth = 0;

        effects.clear();
        effects.addAll(mc.player.getActiveEffects());

        if (sortByTime.getValue()) {
            effects.sort(Comparator.comparingInt(MobEffectInstance::getDuration));
        } else {
            effects.sort(Comparator.comparing(PotionEffectsHudModule::effectName, String.CASE_INSENSITIVE_ORDER));
        }

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        boolean isLeft = x < screenWidth / 2.0f;

        if (effects.isEmpty()) {
            String none = "§7No effects";
            int w = mc.font.width(none);
            int textX = isLeft ? (int) (x + 2) : (int) (x + getWidth() - 2 - w);
            ctx.drawString(mc.font, none, textX, (int) drawY, 0xFFAAAAAA);
            maxWidth = Math.max(maxWidth, w);
            drawY += lineHeight;
        } else {
            for (MobEffectInstance inst : effects) {
                String name = effectName(inst);
                StringBuilder line = new StringBuilder(name);

                if (showAmplifier.getValue()) {
                    int amp = inst.getAmplifier();
                    if (amp > 0) {
                        line.append(" ").append(amp + 1);
                    }
                }

                if (showTime.getValue()) {
                    line.append(" §7").append(formatDuration(inst.getDuration()));
                }

                String text = line.toString();
                int w = mc.font.width(text);
                int textX = isLeft ? (int) (x + 2) : (int) (x + getWidth() - 2 - w);
                ctx.drawString(mc.font, text, textX, (int) drawY, 0xFFFFFFFF);
                maxWidth = Math.max(maxWidth, w);
                drawY += lineHeight;
            }
        }

        setWidth(Math.max(80, maxWidth + 6));
        setHeight(Math.max(10, drawY - y));
    }

    private static String effectName(MobEffectInstance inst) {
        try {
            return inst.getEffect().value().getDisplayName().getString();
        } catch (Throwable t) {
            // Fallback for mappings/versions
            return inst.getEffect().toString();
        }
    }

    private static String formatDuration(int ticks) {
        if (ticks < 0) return "--:--";
        int totalSeconds = ticks / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}

