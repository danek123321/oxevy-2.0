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

    private final List<NametagEntry> entriesThisFrame = new ArrayList<>();

    public NametagsModule() {
        super("Nametags", "Shows player nametags", Category.RENDER);
    }

    @Subscribe
    public void onRender3D(Render3DEvent event) {
        if (nullCheck()) return;
        entriesThisFrame.clear();
        Vec3 camera = mc.gameRenderer.getMainCamera().position();

        for (Player player : mc.level.players()) {
            if (player == mc.player || player.isInvisible() && !mc.player.isSpectator()) continue;
            double dist = camera.distanceTo(player.position());
            if (dist > maxDistance.getValue()) continue;

            float offset = me.alpha432.oxevy.features.modules.client.ClickGuiModule.getInstance().nameTagOffset.getValue();
            float[] screen = event.worldToScreen(player.position().add(0, player.getEyeHeight(player.getPose()) + offset, 0), camera);
            if (screen == null) continue;

            int ping = -1;
            GameType gameMode = null;
            if (mc.getConnection() != null) {
                PlayerInfo info = mc.getConnection().getPlayerInfo(player.getName().getString());
                if (info != null) {
                    ping = info.getLatency();
                    gameMode = info.getGameMode();
                }
            }

            entriesThisFrame.add(new NametagEntry(
                screen[0], screen[1], textSize.getValue(),
                player.getName().getString(),
                player.getHealth(), player.getMaxHealth(),
                player.getAbsorptionAmount(), player.getArmorValue(),
                ping, gameMode,
                player.getMainHandItem(), player.getOffhandItem(),
                player.getItemBySlot(EquipmentSlot.HEAD), player.getItemBySlot(EquipmentSlot.CHEST), player.getItemBySlot(EquipmentSlot.LEGS), player.getItemBySlot(EquipmentSlot.FEET)
            ));
        }
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
        int textColor = new Color(255, 255, 255, alpha).getRGB();

        int nameY = (int) e.y;
        context.drawString(mc.font, e.name, (int) (e.x - mc.font.width(e.name) * e.scale / 2), nameY, textColor);
        nameY += mc.font.lineHeight + 2;

        String healthStr = String.format("%.1f", e.health);
        int hpColor = healthColor(e.health / e.maxHealth, alpha);
        context.drawString(mc.font, healthStr, (int) (e.x - mc.font.width(healthStr) * e.scale / 2), nameY, hpColor);
        
        String maxHealthStr = "/" + String.format("%.1f", e.maxHealth);
        context.drawString(mc.font, maxHealthStr, (int) (e.x + mc.font.width(healthStr) * e.scale / 2 + 2), nameY, new Color(180, 180, 180, alpha).getRGB());
        
        if (e.absorption > 0) {
            String absStr = "+" + String.format("%.0f", e.absorption);
            context.drawString(mc.font, absStr, (int) (e.x + mc.font.width(healthStr + maxHealthStr) * e.scale / 2 + 4), nameY, new Color(255, 255, 0, alpha).getRGB());
        }
        nameY += mc.font.lineHeight + 1;

        if (e.armor > 0) {
            String armorStr = "A:" + e.armor;
            context.drawString(mc.font, armorStr, (int) (e.x - mc.font.width(armorStr) * e.scale / 2), nameY, new Color(100, 150, 255, alpha).getRGB());
            nameY += mc.font.lineHeight;
        }

        if (e.ping >= 0) {
            String pingStr = e.ping + "ms";
            int pingCol = pingColor(e.ping, alpha);
            context.drawString(mc.font, pingStr, (int) (e.x - mc.font.width(pingStr) * e.scale / 2), nameY, pingCol);
            nameY += mc.font.lineHeight;
        }

        if (e.gameMode != null) {
            String gmStr = gameModeStr(e.gameMode);
            context.drawString(mc.font, gmStr, (int) (e.x - mc.font.width(gmStr) * e.scale / 2), nameY, new Color(160, 200, 255, alpha).getRGB());
            nameY += mc.font.lineHeight;
        }

        int armorStartX = (int) (e.x - 34);
        drawItem(context, e.head, armorStartX, nameY);
        drawItem(context, e.chest, armorStartX + 18, nameY);
        drawItem(context, e.legs, armorStartX + 36, nameY);
        drawItem(context, e.feet, armorStartX + 54, nameY);
        nameY += 18;
        drawItem(context, e.mainHand, (int) (e.x - 16), nameY);
        drawItem(context, e.offHand, (int) (e.x + 2), nameY);
    }

    private void drawItem(GuiGraphics context, ItemStack stack, int x, int y) {
        if (!stack.isEmpty()) {
            context.renderItem(stack, x, y);
            context.renderItemDecorations(mc.font, stack, x, y);
        }
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
        return new Color((int) (r * 255), (int) (g * 255), 0, alpha).getRGB();
    }

    private int pingColor(int pingMs, int alpha) {
        if (pingMs < 50) return new Color(100, 255, 100, alpha).getRGB();
        if (pingMs < 100) return new Color(200, 255, 100, alpha).getRGB();
        if (pingMs < 150) return new Color(255, 255, 100, alpha).getRGB();
        return new Color(255, 100, 100, alpha).getRGB();
    }

    private record NametagEntry(float x, float y, float scale, String name,
                                float health, float maxHealth, float absorption, int armor,
                                int ping, GameType gameMode,
                                ItemStack mainHand, ItemStack offHand,
                                ItemStack head, ItemStack chest, ItemStack legs, ItemStack feet) {}
}