package me.alpha432.oxevy.features.modules.player;

import me.alpha432.oxevy.event.impl.network.PacketEvent;
import me.alpha432.oxevy.event.system.Subscribe;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Field;

public class VelocityModule extends Module {

    public final Setting<Float> horizontal = num("Horizontal", 0.0f, 0.0f, 1.0f);
    public final Setting<Float> vertical = num("Vertical", 0.0f, 0.0f, 1.0f);
    public final Setting<Boolean> onlyGround = bool("OnlyGround", false);

    private static Field fX, fY, fZ;

    public VelocityModule() {
        super("Velocity", "Reduces knockback from entities", Category.PLAYER);
        initFields();
    }

    private static void initFields() {
        try {
            fX = ClientboundSetEntityMotionPacket.class.getDeclaredField("x");
            fY = ClientboundSetEntityMotionPacket.class.getDeclaredField("y");
            fZ = ClientboundSetEntityMotionPacket.class.getDeclaredField("z");
            fX.setAccessible(true);
            fY.setAccessible(true);
            fZ.setAccessible(true);
        } catch (Exception e) {
            fX = fY = fZ = null;
        }
    }

    @Subscribe
    private void onPacketReceive(PacketEvent.Receive event) {
        if (mc.player == null) return;

        if (event.getPacket() instanceof ClientboundSetEntityMotionPacket packet) {
            if (packet.getId() != mc.player.getId()) return;
            if (onlyGround.getValue() && !mc.player.onGround()) return;

            float hScale = horizontal.getValue();
            float vScale = vertical.getValue();

            if (fX == null) return;

            try {
                double vX = fX.getDouble(packet);
                double vY = fY.getDouble(packet);
                double vZ = fZ.getDouble(packet);

                if (hScale == 0f && vScale == 0f) {
                    event.cancel();
                    return;
                }

                if (hScale == 1f && vScale == 1f) {
                    return;
                }

                event.cancel();
                mc.player.setDeltaMovement(vX * hScale, vY * vScale, vZ * hScale);
            } catch (Exception e) {
            }
        }
    }
}