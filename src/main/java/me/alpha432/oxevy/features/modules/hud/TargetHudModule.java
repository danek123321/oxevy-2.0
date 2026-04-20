package me.alpha432.oxevy.features.modules.hud;

import me.alpha432.oxevy.event.impl.render.Render2DEvent;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.render.AnimationUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.awt.*;

public class TargetHudModule extends HudModule {

    private final Setting<Boolean> showHealth = bool("Health", true);
    private final Setting<Boolean> showDistance = bool("Distance", true);
    private final Setting<Boolean> showArmor = bool("Armor", true);
    private final Setting<Boolean> showPing = bool("Ping", true);
    private final Setting<Boolean> showItems = bool("Items", true);
    private final Setting<Boolean> smoothAnimations = bool("SmoothAnimations", true);

    private Entity target = null;
    private float targetAlpha = 0.0f;
    private float displayHealth;
    private float displayArmor;

    public TargetHudModule() {
        super("TargetHUD", "Shows target info", 180, 60);
    }

    @Override
    public void drawContent(Render2DEvent e) {
        if (nullCheck()) return;
        
        GuiGraphics ctx = e.getContext();
        float x = getX();
        float y = getY();

        Entity newTarget = findTarget();
        
        if (newTarget != target) {
            target = newTarget;
            if (smoothAnimations.getValue()) {
                targetAlpha = 0.0f;
            }
        }

        float targetAlphaTarget = target != null ? 1.0f : 0.0f;
        targetAlpha = AnimationUtil.animate(targetAlpha, targetAlphaTarget, 
            smoothAnimations.getValue() ? 0.15f : 1.0f, AnimationUtil.Easing.EASE_OUT);

        if (target == null || targetAlpha < 0.01f) {
            setWidth(150);
            setHeight(40);
            return;
        }

        int alpha = (int) (targetAlpha * 255);
        float speed = smoothAnimations.getValue() ? 0.2f : 1.0f;

        if (target instanceof LivingEntity living) {
            displayHealth = AnimationUtil.animate(displayHealth, living.getHealth(), speed, AnimationUtil.Easing.EASE_OUT);
        }
        if (target instanceof Player player) {
            displayArmor = AnimationUtil.animate(displayArmor, player.getArmorValue(), speed, AnimationUtil.Easing.EASE_OUT);
        }

        int textColor = (alpha << 24) | 0xFFFFFF;
        int grayColor = (alpha << 24) | 0xAAAAAA;

        String name = target.getName().getString();
        ctx.drawString(mc.font, name, (int) x + 4, (int) y + 2, textColor);

        int lineY = (int) y + 14;
        if (showHealth.getValue() && target instanceof LivingEntity living) {
            float hp = displayHealth;
            float maxHp = living.getMaxHealth();
            ctx.drawString(mc.font, String.format("%.1f", hp), (int) x + 4, lineY, 0xFF44FF44);
            ctx.drawString(mc.font, "/" + String.format("%.1f", maxHp), (int) x + 4 + mc.font.width(String.format("%.1f", hp)), lineY, grayColor);
        }

        lineY += 10;
        if (showArmor.getValue() && target instanceof Player player) {
            int armorPts = (int) displayArmor;
            ctx.drawString(mc.font, "Armor: " + armorPts, (int) x + 4, lineY, 0xFF6699FF);
            
            float toughness = (float) player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS);
            if (toughness > 0) {
                ctx.drawString(mc.font, " (" + (int)toughness + ")", (int) x + 4 + mc.font.width("Armor: " + armorPts), lineY, 0xFFAAAAAA);
            }
        }

        lineY += 10;
        if (showDistance.getValue()) {
            double dist = mc.player.distanceTo(target);
            ctx.drawString(mc.font, String.format("%.1fm", dist), (int) x + 4, lineY, grayColor);
        }

        if (showPing.getValue() && target instanceof Player player) {
            int ping = 0;
            try {
                if (mc.getConnection() != null && mc.getConnection().getPlayerInfo(player.getUUID()) != null) {
                    ping = mc.getConnection().getPlayerInfo(player.getUUID()).getLatency();
                }
            } catch (Exception ignored) {}
            ctx.drawString(mc.font, ping + "ms", (int) x + 60, lineY, pingColor(ping, alpha));
        }

        if (showItems.getValue() && target instanceof Player player) {
            int itemsY = (int) y + 4;
            int armorStartX = (int) x + 4;
            renderItem(ctx, player.getItemBySlot(EquipmentSlot.HEAD), armorStartX, itemsY);
            renderItem(ctx, player.getItemBySlot(EquipmentSlot.CHEST), armorStartX + 18, itemsY);
            renderItem(ctx, player.getItemBySlot(EquipmentSlot.LEGS), armorStartX + 36, itemsY);
            renderItem(ctx, player.getItemBySlot(EquipmentSlot.FEET), armorStartX + 54, itemsY);
            int handsY = itemsY + 18;
            renderItem(ctx, player.getMainHandItem(), (int) x + 4, handsY);
            renderItem(ctx, player.getOffhandItem(), (int) x + 22, handsY);
        }

        setWidth(150);
        setHeight(76);
    }

    private void renderItem(GuiGraphics ctx, ItemStack stack, int x, int y) {
        if (!stack.isEmpty()) {
            ctx.renderItem(stack, x, y);
            ctx.renderItemDecorations(mc.font, stack, x, y);
        }
    }

    private int pingColor(int ping, int alpha) {
        if (ping < 50) return (alpha << 24) | 0x64FF64;
        if (ping < 100) return (alpha << 24) | 0xC8FF64;
        if (ping < 150) return (alpha << 24) | 0xFFFF64;
        return (alpha << 24) | 0xFF6464;
    }

    private Entity findTarget() {
        Entity entity = mc.crosshairPickEntity;
        if (entity != null && entity.isAlive() && entity != mc.player) return entity;
        if (target != null && target.isAlive()) return target;
        
        double closestDist = 8.0;
        Entity closest = null;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (e == mc.player || !e.isAlive()) continue;
            double dist = mc.player.distanceTo(e);
            if (dist < closestDist) {
                closestDist = dist;
                closest = e;
            }
        }
        return closest;
    }
}