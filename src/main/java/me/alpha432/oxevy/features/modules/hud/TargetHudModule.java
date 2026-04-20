package me.alpha432.oxevy.features.modules.hud;

import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.event.impl.render.Render2DEvent;
import me.alpha432.oxevy.features.modules.client.HudModule;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.render.AnimationUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.text.DecimalFormat;

public class TargetHudModule extends HudModule {

    private final Setting<Boolean> showHealth = bool("Health", true);
    private final Setting<Boolean> showDistance = bool("Distance", true);
    private final Setting<Boolean> showArmor = bool("Armor", true);
    private final Setting<Boolean> showPing = bool("Ping", true);
    private final Setting<Boolean> showName = bool("Name", true);
    private final Setting<Boolean> showHealthBar = bool("HealthBar", true);
    private final Setting<Boolean> showArmorBar = bool("ArmorBar", true);
    private final Setting<Boolean> smoothAnimations = bool("SmoothAnimations", true);
    private final Setting<Color> healthColor = color("HealthColor", 255, 0, 0, 255);
    private final Setting<Color> armorColor = color("ArmorColor", 0, 120, 255, 255);

    private Entity target = null;
    private float targetAlpha = 0.0f;
    private float lastHealth = 0.0f;

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#");

    public TargetHudModule() {
        super("TargetHUD", "Shows information about your target", 150, 50);
    }

    @Override
    protected void render(Render2DEvent e) {
        super.render(e);

        GuiGraphics ctx = e.getContext();
        float x = getX();
        float y = getY();

        // Find target (looking at entity or KillAura target)
        Entity newTarget = findTarget();
        
        // Update target with animation
        if (newTarget != target) {
            target = newTarget;
            if (smoothAnimations.getValue()) {
                targetAlpha = 0.0f;
            }
        }

        // Update animation
        float targetAlphaTarget = target != null ? 1.0f : 0.0f;
        targetAlpha = AnimationUtil.animate(targetAlpha, targetAlphaTarget, 
            smoothAnimations.getValue() ? 0.15f : 1.0f, AnimationUtil.Easing.EASE_OUT);

        if (target == null || targetAlpha < 0.01f) {
            setWidth(150);
            setHeight(40);
            return;
        }

        // Calculate dimensions
        int maxWidth = 150;
        int height = 45;
        int barWidth = maxWidth - 10;

        // Apply fade animation
        int alpha = (int) (targetAlpha * 255);

        // Draw background
        int bgColor = (alpha << 24) | 0x222222;
        ctx.fill((int) x, (int) y, (int) x + maxWidth, (int) y + height, bgColor);

        // Draw target name
        if (showName.getValue()) {
            String name = target.getName().getString();
            ctx.drawString(mc.font, name, (int) x + 5, (int) y + 3, 
                (alpha << 24) | 0xFFFFFF);
        }

        // Draw health bar
        if (showHealthBar.getValue() && target instanceof LivingEntity living) {
            float health = living.getHealth();
            float maxHealth = living.getMaxHealth();
            float healthPercent = Math.max(0, Math.min(1, health / maxHealth));
            
            // Health bar background
            ctx.fill((int) x + 5, (int) y + 15, (int) x + 5 + barWidth, (int) y + 23, 
                (alpha << 24) | 0x444444);
            
            // Health bar fill
            int hpColor = healthColor.getValue().getRGB();
            int hpAlpha = (alpha << 24) | (hpColor & 0x00FFFFFF);
            int hpBarWidth = (int) (barWidth * healthPercent);
            ctx.fill((int) x + 5, (int) y + 15, 
                (int) x + 5 + hpBarWidth, (int) y + 23, hpAlpha);
        }

        // Draw armor bar
        if (showArmorBar.getValue() && target instanceof Player player) {
            float armor = player.getArmorValue();
            float armorPercent = Math.min(1, armor / 20f);
            
            // Armor bar background
            ctx.fill((int) x + 5, (int) y + 25, (int) x + 5 + barWidth, (int) y + 31, 
                (alpha << 24) | 0x444444);
            
            // Armor bar fill
            int armorColorValue = armorColor.getValue().getRGB();
            int armorAlpha = (alpha << 24) | (armorColorValue & 0x00FFFFFF);
            int armorBarWidth = (int) (barWidth * armorPercent);
            ctx.fill((int) x + 5, (int) y + 25, 
                (int) x + 5 + armorBarWidth, (int) y + 31, armorAlpha);
        }

        // Draw info text
        int infoY = (int) y + 35;
        String infoText = "";

        if (showHealth.getValue() && target instanceof LivingEntity living) {
            infoText += "HP: " + DECIMAL_FORMAT.format(living.getHealth()) + "  ";
        }

        if (showDistance.getValue()) {
            double dist = mc.player.distanceTo(target);
            infoText += "Dist: " + DECIMAL_FORMAT.format(dist) + "m  ";
        }

        if (showArmor.getValue() && target instanceof Player player) {
            infoText += "Armor: " + (int) player.getArmorValue() + "  ";
        }

        if (showPing.getValue() && target instanceof Player player) {
            // Get ping (approximation)
            int ping = 0;
            try {
                ping = mc.getConnection().getPlayerInfo(player.getUUID()) != null ? 
                    mc.getConnection().getPlayerInfo(player.getUUID()).getLatency() : 0;
            } catch (Exception ignored) {}
            infoText += "Ping: " + ping + "ms";
        }

        ctx.drawString(mc.font, infoText, (int) x + 5, infoY, (alpha << 24) | 0xAAAAAA);

        setWidth((int) maxWidth);
        setHeight((int) height);
    }

    private Entity findTarget() {
        // Check KillAura target first
        // You can integrate this with your KillAura module if it has a target field
        
        // Look at entity
        Entity entity = mc.crosshairPickEntity;
        if (entity != null && entity.isAlive()) {
            return entity;
        }

        // Return last valid target
        if (target != null && target.isAlive()) {
            return target;
        }

        return null;
    }
}
