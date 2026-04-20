package me.alpha432.oxevy.features.modules.render;

import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.event.impl.render.Render2DEvent;
import me.alpha432.oxevy.event.impl.render.Render3DEvent;
import me.alpha432.oxevy.event.system.Subscribe;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.MathUtil;
import me.alpha432.oxevy.util.render.RenderUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HealthBarModule extends Module {
    public final Setting<Float> barWidth = num("BarWidth", 40f, 20f, 80f);
    public final Setting<Float> barHeight = num("BarHeight", 4f, 2f, 8f);
    public final Setting<Boolean> showAbsorption = bool("ShowAbsorption", true);
    public final Setting<Boolean> showArmor = bool("ShowArmor", true);
    public final Setting<Float> maxDistance = num("MaxDistance", 64f, 16f, 128f);
    public final Setting<Float> animSpeed = num("AnimSpeed", 8f, 1f, 20f);

    private final List<HealthBarEntry> entriesThisFrame = new ArrayList<>();
    private final Map<Integer, Float> displayedHealth = new HashMap<>();
    private final Map<Integer, Float> displayedAbsorption = new HashMap<>();

    public HealthBarModule() {
        super("HealthBar", "Animated health bars with gradient and smooth interpolation", Category.RENDER);
    }

    @Subscribe
    public void onRender3D(Render3DEvent event) {
        if (nullCheck()) return;
        entriesThisFrame.clear();
        Vec3 camera = mc.gameRenderer.getMainCamera().position();
        float delta = event.getDelta();
        float speed = animSpeed.getValue() * delta;

        for (Player player : mc.level.players()) {
            if (player == mc.player || player.isInvisible() && !mc.player.isSpectator()) continue;
            double dist = camera.distanceTo(player.position());
            if (dist > maxDistance.getValue()) continue;

            float offset = me.alpha432.oxevy.features.modules.client.ClickGuiModule.getInstance().healthBarOffset.getValue();
            Vec3 pos = player.position().add(0, player.getEyeHeight(player.getPose()) + offset, 0);
            float[] screen = event.worldToScreen(pos, camera);
            if (screen == null) continue;

            int id = player.getId();
            float health = player.getHealth();
            float maxHealth = player.getMaxHealth();
            float absorption = player.getAbsorptionAmount();
            int armor = player.getArmorValue();

            float curDispHealth = displayedHealth.getOrDefault(id, health);
            float curDispAbs = displayedAbsorption.getOrDefault(id, absorption);
            curDispHealth = MathUtil.lerp(curDispHealth, health, speed);
            curDispAbs = MathUtil.lerp(curDispAbs, absorption, speed);
            displayedHealth.put(id, curDispHealth);
            displayedAbsorption.put(id, curDispAbs);

            entriesThisFrame.add(new HealthBarEntry(
                id, screen[0], screen[1],
                curDispHealth, health, maxHealth, curDispAbs, absorption, armor
            ));
        }

        // Cleanup old entries
        Set<Integer> seen = new HashSet<>();
        for (HealthBarEntry entry : entriesThisFrame) {
            seen.add(entry.id);
        }
        displayedHealth.keySet().removeIf(id -> !seen.contains(id));
        displayedAbsorption.keySet().removeIf(id -> !seen.contains(id));
    }

    @Subscribe
    public void onRender2D(Render2DEvent event) {
        if (nullCheck()) return;
        GuiGraphics context = event.getContext();
        for (HealthBarEntry e : entriesThisFrame) {
            renderHealthBar(context, e);
        }
    }

    private void renderHealthBar(GuiGraphics context, HealthBarEntry e) {
        float w = barWidth.getValue();
        float h = barHeight.getValue();
        float halfW = w / 2f;
        float x = e.sx - halfW;
        float y = e.sy;

        int bgColor = new Color(30, 30, 30, 200).getRGB();

        // Background
        RenderUtil.rect(context, x, y, x + w, y + h, bgColor);

        float maxTotal = e.maxHealth * 2f;
        float healthPct = Math.min(1f, e.displayHealth / e.maxHealth);
        float absPct = Math.min(1f - healthPct, Math.max(0, e.displayAbsorption / maxTotal));

        // Health segment - gradient green -> yellow -> red
        float healthEnd = w * healthPct;
        if (healthEnd > 0.5f) {
            int c1 = healthColor(1f);
            int c2 = healthColor(healthPct);
            RenderUtil.horizontalGradient(context, x, y, x + healthEnd, y + h, new Color(c1), new Color(c2));
        } else {
            RenderUtil.rect(context, x, y, x + healthEnd, y + h, healthColor(healthPct));
        }

        // Absorption segment (gold/yellow)
        if (showAbsorption.getValue() && absPct > 0) {
            float absStart = healthEnd;
            float absEnd = x + w * (healthPct + absPct);
            RenderUtil.rect(context, x + absStart, y, absEnd, y + h, new Color(255, 220, 0, 255).getRGB());
        }

        // Armor text above bar
        if (showArmor.getValue() && e.armor > 0) {
            String armorStr = "" + e.armor;
            int tw = mc.font.width(armorStr);
            context.drawString(mc.font, armorStr, (int) (e.sx - tw / 2f), (int) (y - mc.font.lineHeight - 1), 0xFFAAAAAA);
        }
    }

    private int healthColor(float pct) {
        float r = pct > 0.5f ? (1 - pct) * 2 : 1f;
        float g = pct <= 0.5f ? pct * 2 : 1f;
        return new Color((int) (r * 255), (int) (g * 255), 0).getRGB();
    }

    private record HealthBarEntry(int id, float sx, float sy,
                                  float displayHealth, float health, float maxHealth,
                                  float displayAbsorption, float absorption, int armor) {}
}