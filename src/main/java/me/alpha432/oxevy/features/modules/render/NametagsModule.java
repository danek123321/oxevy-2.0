package me.alpha432.oxevy.features.modules.render;

import me.alpha432.oxevy.event.impl.render.Render2DEvent;
import me.alpha432.oxevy.event.impl.render.Render3DEvent;
import me.alpha432.oxevy.event.system.Subscribe;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class NametagsModule extends Module {

    public final Setting<Float> textSize = num("TextSize", 1.0f, 0.5f, 2.5f);
    public final Setting<Float> maxDistance = num("MaxDistance", 64f, 16f, 128f);
    public final Setting<Boolean> armor = bool("Armor", true);
    public final Setting<Boolean> health = bool("Health", true);
    public final Setting<Boolean> ping = bool("Ping", true);
    public final Setting<Boolean> gameMode = bool("GameMode", true);
    public final Setting<Boolean> enchants = bool("Enchants", true);
    public final Setting<Boolean> clientWatermark = bool("ClientWatermark", true);

    private final List<NametagEntry> entriesThisFrame = new ArrayList<>();
    private net.minecraft.resources.Identifier watermarkId = null;

    public NametagsModule() {
        super("Nametags", "Shows player nametags", Category.RENDER);
    }

    private void ensureWatermarkLoaded() {
        if (watermarkId != null) return;
        watermarkId = me.alpha432.oxevy.util.render.RenderUtil.loadTexture(new java.io.File("images/watermark.png"), "oxevy/nametag_watermark");
    }

    @Subscribe
    public void onRender3D(Render3DEvent event) {
        if (nullCheck()) return;
        entriesThisFrame.clear();
        ensureWatermarkLoaded();
        Vec3 camera = mc.gameRenderer.getMainCamera().position();

        for (Player player : mc.level.players()) {
            if (player == mc.player || player.isInvisible() && !mc.player.isSpectator()) continue;
            double dist = camera.distanceTo(player.position());
            if (dist > maxDistance.getValue()) continue;

            float offset = me.alpha432.oxevy.features.modules.client.ClickGuiModule.getInstance().nameTagOffset.getValue();
            float[] screen = event.worldToScreen(player.position().add(0, player.getEyeHeight(player.getPose()) + offset, 0), camera);
            if (screen == null) continue;

            int latency = -1;
            GameType gm = null;
            if (mc.getConnection() != null) {
                PlayerInfo info = mc.getConnection().getPlayerInfo(player.getUUID());
                if (info != null) {
                    latency = info.getLatency();
                    gm = info.getGameMode();
                }
            }

            entriesThisFrame.add(new NametagEntry(
                screen[0], screen[1], textSize.getValue(),
                player.getName().getString(),
                player.getHealth(), player.getMaxHealth(),
                player.getAbsorptionAmount(), player.getArmorValue(),
                latency, gm,
                player.getMainHandItem(), player.getOffhandItem(),
                player.getItemBySlot(EquipmentSlot.HEAD), player.getItemBySlot(EquipmentSlot.CHEST), player.getItemBySlot(EquipmentSlot.LEGS), player.getItemBySlot(EquipmentSlot.FEET),
                isClientUser(player)
            ));
        }
    }

    private boolean isClientUser(Player player) {
        // Placeholder for client user detection
        // In a real scenario, this would check a list of UUIDs or a custom property
        return me.alpha432.oxevy.Oxevy.friendManager.isFriend(player); // For now, friends also get the watermark
    }

    @Override
    public void onRender2D(Render2DEvent event) {
        if (nullCheck()) return;
        GuiGraphics context = event.getContext();
        for (NametagEntry e : entriesThisFrame) {
            renderNametag(context, e);
        }
    }

    private void renderNametag(GuiGraphics context, NametagEntry e) {
        int alpha = 255;
        int nameColor = 0xFFFFFF;
        if (e.isClientUser) {
            nameColor = 0x55FFFF; // Cyan for client users
        }

        String displayTag = e.name;
        if (ping.getValue() && e.ping >= 0) {
            displayTag += " [" + e.ping + "ms]";
        }
        if (gameMode.getValue() && e.gameMode != null) {
            displayTag += " [" + gameModeStr(e.gameMode) + "]";
        }

        int textWidth = mc.font.width(displayTag);
        int iconSize = (int) (mc.font.lineHeight * 1.2f);
        int totalWidth = textWidth + (e.isClientUser && clientWatermark.getValue() && watermarkId != null ? iconSize + 2 : 0);

        float x = e.x - (totalWidth * e.scale) / 2;
        float y = e.y;

        // Background
        context.fill((int) (x - 2), (int) (y - 2), (int) (x + totalWidth * e.scale + 2), (int) (y + mc.font.lineHeight * e.scale + 2), 0x99000000);

        float currentX = x;
        if (e.isClientUser && clientWatermark.getValue() && watermarkId != null) {
            context.blit(net.minecraft.client.renderer.RenderPipelines.GUI, watermarkId, (int) currentX, (int) y, (int) (iconSize * e.scale), (int) (iconSize * e.scale), 0, 0, iconSize, iconSize, iconSize, iconSize);
            currentX += (iconSize + 2) * e.scale;
        }

        context.pose().pushMatrix();
        context.pose().translate(currentX, y);
        context.pose().scale(e.scale, e.scale);
        context.drawString(mc.font, displayTag, 0, 0, nameColor);
        context.pose().popMatrix();

        y += (mc.font.lineHeight + 2) * e.scale;

        // Health Bar
        if (health.getValue()) {
            float barWidth = totalWidth * e.scale;
            float hp = Math.min(e.health + e.absorption, e.maxHealth);
            float healthPct = hp / e.maxHealth;
            int barColor = healthColor(healthPct, alpha);
            int barHeight = 2;

            context.fill((int) x, (int) y, (int) (x + barWidth), (int) (y + barHeight), 0x66000000);
            context.fill((int) x, (int) y, (int) (x + barWidth * healthPct), (int) (y + barHeight), barColor);
            
            String hpText = String.format("%.1f", e.health + e.absorption);
            context.pose().pushMatrix();
            context.pose().translate(x + barWidth / 2 - (mc.font.width(hpText) * 0.5f * e.scale) / 2, (int) (y + barHeight + 1));
            context.pose().scale(0.5f * e.scale, 0.5f * e.scale);
            context.drawString(mc.font, hpText, 0, 0, 0xFFFFFF);
            context.pose().popMatrix();

            y += (barHeight + 6) * e.scale;
        }

        // Armor and Items
        if (armor.getValue()) {
            List<ItemStack> items = new ArrayList<>();
            items.add(e.offHand);
            items.add(e.feet);
            items.add(e.legs);
            items.add(e.chest);
            items.add(e.head);
            items.add(e.mainHand);

            float itemX = e.x - (items.size() * 18 * e.scale) / 2;
            // Draw items ABOVE the name to avoid clutter
            float topY = e.y - 25 * e.scale;

            for (ItemStack stack : items) {
                if (stack != null && !stack.isEmpty()) {
                    drawItem(context, stack, (int) itemX, (int) topY, e.scale);
                }
                itemX += 18 * e.scale;
            }
        }
    }

    private void drawItem(GuiGraphics context, ItemStack stack, int x, int y, float scale) {
        context.pose().pushMatrix();
        context.pose().translate(x, y);
        context.pose().scale(scale, scale);
        context.renderItem(stack, 0, 0);
        context.renderItemDecorations(mc.font, stack, 0, 0);
        context.pose().popMatrix();
    }

    private String gameModeStr(GameType mode) {
        if (mode == GameType.SURVIVAL) return "S";
        if (mode == GameType.CREATIVE) return "C";
        if (mode == GameType.ADVENTURE) return "A";
        if (mode == GameType.SPECTATOR) return "SP";
        return "?";
    }

    private int healthColor(float pct, int alpha) {
        float r = pct > 0.5f ? (1 - pct) * 2 : 1f;
        float g = pct <= 0.5f ? pct * 2 : 1f;
        return new Color((int) (Math.max(0, Math.min(255, r * 255))), (int) (Math.max(0, Math.min(255, g * 255))), 0, alpha).getRGB();
    }

    private record NametagEntry(float x, float y, float scale, String name,
                                float health, float maxHealth, float absorption, int armor,
                                int ping, GameType gameMode,
                                ItemStack mainHand, ItemStack offHand,
                                ItemStack head, ItemStack chest, ItemStack legs, ItemStack feet,
                                boolean isClientUser) {}
}
