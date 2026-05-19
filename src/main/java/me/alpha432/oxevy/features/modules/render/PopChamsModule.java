package me.alpha432.oxevy.features.modules.render;

import me.alpha432.oxevy.event.impl.network.PacketEvent;
import me.alpha432.oxevy.event.impl.render.Render3DEvent;
import me.alpha432.oxevy.event.system.Subscribe;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.render.RenderUtil;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PopChamsModule extends Module {

    public Setting<Boolean> onlyOne = bool("OnlyOne", false);
    public Setting<Double> renderTime = num("RenderTime", 1.0, 0.1, 6.0);
    public Setting<Double> yModifier = num("YModifier", 0.75, -4.0, 4.0);
    public Setting<Double> scaleModifier = num("ScaleModifier", -0.25, -4.0, 4.0);
    public Setting<Boolean> fadeOut = bool("FadeOut", true);
    public Setting<Color> sideColor = color("SideColor", 255, 255, 255, 25);
    public Setting<Color> lineColor = color("LineColor", 255, 255, 255, 127);

    private final List<GhostPlayer> ghosts = new ArrayList<>();

    public PopChamsModule() {
        super("PopChams", "Renders a ghost where players pop totem", Category.RENDER);
    }

    @Override
    public void onDisable() {
        ghosts.clear();
    }

    @Subscribe
    public void onPacketReceive(PacketEvent.Receive event) {
        if (!(event.getPacket() instanceof ClientboundEntityEventPacket packet)) return;
        if (packet.getEventId() != 36) return;

        var entity = packet.getEntity(mc.level);
        if (!(entity instanceof Player player) || entity == mc.player) return;

        if (onlyOne.getValue()) {
            ghosts.removeIf(g -> g.uuid.equals(entity.getUUID()));
        }

        ghosts.add(new GhostPlayer(player));
    }

    @Subscribe
    public void onRender3D(Render3DEvent event) {
        ghosts.removeIf(ghost -> ghost.render(event));
    }

    private class GhostPlayer {
        private final UUID uuid;
        private final double x, y, z;
        private final double bbWidth, bbHeight;
        private double timer, scale = 1;
        private double currentY;

        public GhostPlayer(Player player) {
            uuid = player.getUUID();
            x = player.getX();
            y = player.getY();
            z = player.getZ();
            currentY = y;
            bbWidth = player.getBbWidth();
            bbHeight = player.getBbHeight();
        }

        public boolean render(Render3DEvent event) {
            timer += event.getDelta();
            if (timer > renderTime.getValue()) return true;

            currentY += yModifier.getValue() * event.getDelta();
            scale += scaleModifier.getValue() * event.getDelta();

            int preSideA = sideColor.getValue().getAlpha();
            int preLineA = lineColor.getValue().getAlpha();

            Color side = sideColor.getValue();
            Color line = lineColor.getValue();

            if (fadeOut.getValue()) {
                float alphaFactor = (float) (1 - timer / renderTime.getValue());
                side = new Color(side.getRed(), side.getGreen(), side.getBlue(), (int) (side.getAlpha() * alphaFactor));
                line = new Color(line.getRed(), line.getGreen(), line.getBlue(), (int) (line.getAlpha() * alphaFactor));
            }

            double w = bbWidth * scale / 2;
            double h = bbHeight * scale;
            AABB box = new AABB(x - w, currentY, z - w, x + w, currentY + h, z + w);

            RenderUtil.drawSolidBoxes(event.getMatrix(), java.util.List.of(box), side.getRGB(), true);
            RenderUtil.drawOutlinedBoxes(event.getMatrix(), java.util.List.of(box), line.getRGB(), true);

            return false;
        }
    }
}
