package me.alpha432.oxevy.features.modules.render;

import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.event.impl.render.Render2DEvent;
import me.alpha432.oxevy.event.impl.render.Render3DEvent;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.render.NametagUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class NameTagsModule extends Module {
    public final Setting<Float> scale = num("Scale", 1.5f, 0.1f, 5.0f);
    public final Setting<Double> range = num("Range", 100.0, 16.0, 256.0);
    public final Setting<Boolean> unlimitedRange = bool("UnlimitedRange", true);
    public final Setting<Boolean> ignoreSelf = bool("IgnoreSelf", true);
    public final Setting<Boolean> ignoreFriends = bool("IgnoreFriends", false);
    public final Setting<Boolean> showHealth = bool("Health", true);
    public final Setting<Boolean> ping = bool("Ping", true);
    public final Setting<Boolean> distance = bool("Distance", false);
    public final Setting<Boolean> gamemode = bool("Gamemode", false);
    public final Setting<Boolean> healthBar = bool("HealthBar", true);
    public final Setting<Boolean> armor = bool("Armor", true);
    public final Setting<Boolean> watermark = bool("Watermark", true);
    public final Setting<Boolean> shadow = bool("Shadow", true);
    public final Setting<Boolean> players = bool("Players", true);
    public final Setting<Boolean> mobs = bool("Mobs", false);

    private final List<Entity> renderList = new ArrayList<>();
    private final Vector3d pos = new Vector3d();

    public NameTagsModule() {
        super("NameTags", "Custom name tags above entities", Category.RENDER);
    }

    public float getScale() { return scale.getValue(); }
    public boolean isUnlimitedRange() { return isEnabled() && unlimitedRange.getValue(); }
    public boolean isSeeThrough() { return false; }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (!isEnabled()) return;
        if (mc.level == null || mc.player == null) return;

        NametagUtils.onRender(event.getMatrix(), event.getProjectionMatrix());

        renderList.clear();
        double maxDist = unlimitedRange.getValue() ? Double.MAX_VALUE : range.getValue() * range.getValue();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == mc.player && ignoreSelf.getValue()) continue;
            if (!players.getValue() && entity.getType() == EntityType.PLAYER) continue;
            if (!mobs.getValue() && entity.getType() != EntityType.PLAYER) continue;
            if (entity.distanceToSqr(mc.player) > maxDist) continue;

            if (entity instanceof Player player && ignoreFriends.getValue() && Oxevy.friendManager.isFriend(player)) continue;

            renderList.add(entity);
        }

        renderList.sort(Comparator.comparingDouble(e -> e.distanceToSqr(mc.player)));
    }

    @Override
    public void onRender2D(Render2DEvent event) {
        if (!isEnabled()) return;
        if (mc.level == null || mc.player == null) return;

        GuiGraphics ctx = event.getContext();

        for (Entity entity : renderList) {
            pos.set(entity.getX(), entity.getY() + entity.getBbHeight() + 0.5, entity.getZ());

            if (!NametagUtils.to2D(pos, scale.getValue())) continue;

            if (entity instanceof Player player) {
                renderNametagPlayer(ctx, player);
            }
        }
    }

    private void renderNametagPlayer(GuiGraphics ctx, Player player) {
        NametagUtils.begin(pos, ctx);

        StringBuilder sb = new StringBuilder();

        if (gamemode.getValue()) {
            var conn = mc.getConnection();
            if (conn != null) {
                PlayerInfo info = conn.getPlayerInfo(player.getUUID());
                if (info != null && info.getGameMode() != null) {
                    sb.append("[").append(info.getGameMode().getName().charAt(0)).append("] ");
                }
            }
        }

        if (watermark.getValue() && !player.getUUID().equals(mc.player.getUUID())) {
            sb.append("O ");
        }

        sb.append(player.getGameProfile().name());

        if (showHealth.getValue()) {
            int health = Math.round(player.getHealth() + player.getAbsorptionAmount());
            sb.append(" ").append(health);
        }

        if (ping.getValue()) {
            var conn = mc.getConnection();
            if (conn != null) {
                PlayerInfo info = conn.getPlayerInfo(player.getUUID());
                if (info != null) {
                    sb.append(" [").append(info.getLatency()).append("ms]");
                }
            }
        }

        if (distance.getValue()) {
            double dist = Math.round(mc.player.distanceTo(player) * 10) / 10.0;
            sb.append(" ").append(dist).append("m");
        }

        String text = sb.toString();
        int textWidth = mc.font.width(text);
        int textHeight = mc.font.lineHeight;

        int bgX = -textWidth / 2 - 4;
        int bgY = -textHeight - 2;
        int bgW = textWidth + 8;
        int bgH = textHeight + 4;

        ctx.fill(bgX, bgY, bgX + bgW, bgY + bgH, 0x90000000);
        ctx.fill(bgX, bgY, bgX + bgW, bgY + 1, 0xFF00FF66);
        ctx.drawString(mc.font, text, -textWidth / 2, -textHeight, 0xFFFFFFFF, shadow.getValue());

        if (armor.getValue()) {
            int armorVal = player.getArmorValue();
            if (armorVal > 0) {
                String armorText = " " + armorVal + " \u2694";
                int armorWidth = mc.font.width(armorText);
                ctx.fill(-armorWidth / 2 - 2, bgY - textHeight - 4, armorWidth / 2 + 2, bgY - textHeight, 0x80000000);
                ctx.drawString(mc.font, armorText, -armorWidth / 2, bgY - textHeight - 2, 0xFF888888, shadow.getValue());
            }
        }

        NametagUtils.end(ctx);
    }
}
