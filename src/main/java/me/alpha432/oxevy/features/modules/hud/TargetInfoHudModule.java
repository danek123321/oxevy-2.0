package me.alpha432.oxevy.features.modules.hud;

import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.event.impl.render.Render2DEvent;
import me.alpha432.oxevy.event.impl.render.Render3DEvent;
import me.alpha432.oxevy.event.system.Subscribe;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.manager.TargetManager;
import me.alpha432.oxevy.util.render.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.List;
import java.util.Locale;

public class TargetInfoHudModule extends HudModule {
    public final Setting<Boolean> showArmorDurability = bool("ArmorDurability", true);
    public final Setting<Boolean> showEffects = bool("PotionEffects", true);
    public final Setting<Boolean> showHeldItem = bool("HeldItem", true);
    public final Setting<Boolean> showPing = bool("Ping", true);
    public final Setting<Boolean> showCombatPrediction = bool("CombatPrediction", true);
    public final Setting<Boolean> showCombo = bool("ComboCounter", true);
    public final Setting<Boolean> glowEffect = bool("GlowEffect", true);
    public final Setting<Boolean> rangeCircle = bool("RangeCircle", true);
    public final Setting<Boolean> lineOfSight = bool("LineOfSight", true);

    public TargetInfoHudModule() {
        super("TargetInfo", "Current target tracking and combat prediction", 160, 120);
    }

    @Override
    public void drawContent(Render2DEvent e) {
        if (nullCheck()) return;

        TargetManager tm = Oxevy.targetManager;
        LivingEntity target = tm.getTarget();
        if (target == null) {
            setWidth(80);
            setHeight(mc.font.lineHeight + 4);
            return;
        }

        float x = getX();
        float y = getY();
        GuiGraphics ctx = e.getContext();
        int lineHeight = mc.font.lineHeight;
        float drawY = y;
        int maxWidth = 0;

        String name = target.getName().getString();
        double dist = mc.player.distanceTo(target);
        String header = "§f§l" + name + " §7(" + String.format(Locale.US, "%.1f", dist) + "m)";
        ctx.drawString(mc.font, header, (int) x, (int) drawY, 0xFFFFFFFF);
        maxWidth = Math.max(maxWidth, mc.font.width(header));
        drawY += lineHeight + 2;

        float health = target.getHealth();
        float maxHealth = target.getMaxHealth();
        float absorption = target.getAbsorptionAmount();
        float barW = 120;
        float barH = 4;
        RenderUtil.rect(ctx, x, drawY, x + barW, drawY + barH, new Color(40, 40, 40, 200).getRGB());
        float healthPct = Math.min(1f, health / maxHealth);
        RenderUtil.rect(ctx, x, drawY, x + barW * healthPct, drawY + barH, healthColor(healthPct));
        if (absorption > 0) {
            float absPct = Math.min(1f - healthPct, absorption / (maxHealth * 2f));
            RenderUtil.rect(ctx, x + barW * healthPct, drawY, x + barW * (healthPct + absPct), drawY + barH, 0xFFFFFF00);
        }
        String hpStr = String.format(Locale.US, "%.1f", health) + (absorption > 0 ? " §e+" + String.format(Locale.US, "%.1f", absorption) : "") + " §7/ §f" + String.format(Locale.US, "%.1f", maxHealth);
        ctx.drawString(mc.font, hpStr, (int) x, (int) (drawY + barH + 1), 0xFFCCCCCC);
        maxWidth = Math.max(maxWidth, (int) barW);
        drawY += barH + lineHeight + 2;

        if (showArmorDurability.getValue()) {
            StringBuilder armorStr = new StringBuilder("§7Armor: ");
            for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
                ItemStack stack = target.getItemBySlot(slot);
                if (stack.isEmpty()) continue;
                int max = stack.getMaxDamage();
                int cur = max - stack.getDamageValue();
                int pct = max > 0 ? (cur * 100 / max) : 100;
                armorStr.append(pct > 50 ? "§a" : pct > 25 ? "§e" : "§c").append(pct).append("% ");
            }
            if (armorStr.length() > 7) {
                ctx.drawString(mc.font, armorStr.toString(), (int) x, (int) drawY, 0xFFAAAAAA);
                maxWidth = Math.max(maxWidth, mc.font.width(armorStr.toString()));
                drawY += lineHeight;
            }
        }

        if (showEffects.getValue() && !target.getActiveEffects().isEmpty()) {
            for (MobEffectInstance eff : target.getActiveEffects()) {
                String line = "§7" + eff.getEffect().value().getDescriptionId().replace("effect.minecraft.", "") + " §f" + (eff.getAmplifier() + 1 > 1 ? (eff.getAmplifier() + 1) + " " : "") + "§8" + (eff.getDuration() / 20) + "s";
                ctx.drawString(mc.font, line, (int) x, (int) drawY, 0xFFCCCCCC);
                maxWidth = Math.max(maxWidth, mc.font.width(line));
                drawY += lineHeight;
            }
        }

        if (showHeldItem.getValue()) {
            ItemStack main = target.getMainHandItem();
            if (!main.isEmpty()) {
                String itemStr = "§7Held: §f" + main.getHoverName().getString() + (main.getCount() > 1 ? " x" + main.getCount() : "");
                ctx.drawString(mc.font, itemStr, (int) x, (int) drawY, 0xFFCCCCCC);
                maxWidth = Math.max(maxWidth, mc.font.width(itemStr));
                drawY += lineHeight;
            }
        }

        if (showPing.getValue() && target instanceof Player player) {
            int ping = getPing(player);
            if (ping >= 0) {
                String pingStr = "§7Ping: §f" + ping + " ms";
                ctx.drawString(mc.font, pingStr, (int) x, (int) drawY, pingColor(ping));
                maxWidth = Math.max(maxWidth, mc.font.width(pingStr));
                drawY += lineHeight;
            }
        }

        if (showCombatPrediction.getValue()) {
            float damagePerHit = getEstimatedDamagePerHit(target);
            if (damagePerHit > 0) {
                int hits = (int) Math.ceil((health + absorption) / damagePerHit);
                String predStr = "§7~§f " + hits + " hit" + (hits != 1 ? "s" : "") + " to kill §7(§f" + String.format(Locale.US, "%.1f", damagePerHit) + "§7/hit)";
                ctx.drawString(mc.font, predStr, (int) x, (int) drawY, 0xFFAAAAAA);
                maxWidth = Math.max(maxWidth, mc.font.width(predStr));
                drawY += lineHeight;
            }
        }

        if (showCombo.getValue() && tm.getComboCount() > 0) {
            String comboStr = "§6Combo: §f" + tm.getComboCount();
            ctx.drawString(mc.font, comboStr, (int) x, (int) drawY, 0xFFFFFF00);
            maxWidth = Math.max(maxWidth, mc.font.width(comboStr));
            drawY += lineHeight;
        }

        if (lineOfSight.getValue()) {
            boolean los = hasLineOfSight(target);
            ctx.drawString(mc.font, "§7LOS: " + (los ? "§aYes" : "§cNo"), (int) x, (int) drawY, 0xFFAAAAAA);
            maxWidth = Math.max(maxWidth, mc.font.width("LOS: Yes"));
            drawY += lineHeight;
        }

        setWidth(Math.max(100, maxWidth + 6));
        setHeight(drawY - y);
    }

    @Subscribe
    public void onRender3D(Render3DEvent event) {
        if (nullCheck() || !glowEffect.getValue() && !rangeCircle.getValue() && !lineOfSight.getValue()) return;
        LivingEntity target = Oxevy.targetManager.getTarget();
        if (target == null) return;
        Vec3 pos = target.position();
        AABB box = target.getBoundingBox();
        PoseStack stack = event.getMatrix();
        if (glowEffect.getValue()) RenderUtil.drawBox(stack, box, Oxevy.colorManager.getColor(), 2f, true);
        if (rangeCircle.getValue()) RenderUtil.drawCircle(stack, new Vec3(pos.x, box.minY + 0.02, pos.z), 0.5f, 32, new Color(255, 200, 0, 180), 1.5f, true);
        if (lineOfSight.getValue()) RenderUtil.drawLine(stack, mc.player.getEyePosition(1f), target.getEyePosition(1f), hasLineOfSight(target) ? new Color(0, 255, 0, 200) : new Color(255, 0, 0, 200), 1.2f, true);
    }

    private int healthColor(float pct) { return new Color((int) ((pct > 0.5f ? (1 - pct) * 2 : 1f) * 255), (int) ((pct <= 0.5f ? pct * 2 : 1f) * 255), 0).getRGB(); }
    private int getPing(Player player) { try { var info = mc.getConnection().getPlayerInfo(player.getName().getString()); return info != null ? info.getLatency() : -1; } catch (Throwable t) { return -1; } }
    private int pingColor(int ms) { return ms < 50 ? 0xFF44FF44 : ms < 100 ? 0xFFAAAA44 : 0xFFFF4444; }
    private float getEstimatedDamagePerHit(LivingEntity target) { try { double base = mc.player.getAttributeValue(Attributes.ATTACK_DAMAGE); if (base <= 0) return 4f; double armor = target.getAttributeValue(Attributes.ARMOR); double toughness = target.getAttributeValue(Attributes.ARMOR_TOUGHNESS); double armorReduction = Math.min(20, Math.max(armor / 5, armor - base / (2 + toughness / 4))); double reduction = 1 - armorReduction / 25; double damage = base * Math.max(0.2, reduction); if (mc.player.fallDistance > 0 && !mc.player.onGround()) damage *= 1.5; return (float) Math.max(0.5, damage); } catch (Throwable t) { return 4f; } }
    private boolean hasLineOfSight(LivingEntity target) { Vec3 from = mc.player.getEyePosition(1f); Vec3 to = target.getEyePosition(1f); return mc.level.clip(new net.minecraft.world.level.ClipContext(from, to, net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, mc.player)).getType() == net.minecraft.world.phys.HitResult.Type.MISS; }
}
