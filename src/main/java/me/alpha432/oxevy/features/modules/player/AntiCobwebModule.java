package me.alpha432.oxevy.features.modules.player;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

public class AntiCobwebModule extends Module {
    public final Setting<Boolean> jump = bool("Jump", true);
    public final Setting<Boolean> speed = bool("Speed", true);
    public final Setting<Float> speedStrength = num("SpeedStrength", 1.5f, 1.0f, 3.0f);

    public AntiCobwebModule() {
        super("AntiCobweb", "Escape cobwebs faster", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;

        BlockPos playerPos = BlockPos.containing(mc.player.getX(), mc.player.getY(), mc.player.getZ());

        boolean inCobweb = isInCobweb(playerPos);

        if (inCobweb) {
            if (jump.getValue() && mc.player.onGround()) {
                mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, 0.5, mc.player.getDeltaMovement().z);
            }

            if (speed.getValue()) {
                double forward = mc.player.zza;
                double strafe = mc.player.xxa;

                if (forward != 0 || strafe != 0) {
                    float yaw = mc.player.getYRot();
                    double radians = Math.toRadians(yaw);

                    double motionX = forward * speedStrength.getValue() * -Math.sin(radians) + strafe * speedStrength.getValue() * Math.cos(radians);
                    double motionZ = forward * speedStrength.getValue() * Math.cos(radians) + strafe * speedStrength.getValue() * -Math.sin(radians);

                    mc.player.setDeltaMovement(motionX, mc.player.getDeltaMovement().y, motionZ);
                }
            }
        }
    }

    private boolean isInCobweb(BlockPos pos) {
        return mc.level.getBlockState(pos).is(Blocks.COBWEB);
    }
}
