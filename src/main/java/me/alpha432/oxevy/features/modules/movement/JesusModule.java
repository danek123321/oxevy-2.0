package me.alpha432.oxevy.features.modules.movement;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

public class JesusModule extends Module {
    public final Setting<Mode> mode = mode("Mode", Mode.Vanilla);
    public final Setting<Float> motion = num("Motion", 0.1f, 0.01f, 0.5f);

    private boolean wasInWater = false;

    public enum Mode {
        Vanilla
    }

    public JesusModule() {
        super("Jesus", "Walk on water", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (nullCheck()) return;

        switch (mode.getValue()) {
            case Vanilla -> {
                if (mc.player.isInWater()) {
                    mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, motion.getValue(), mc.player.getDeltaMovement().z);
                    wasInWater = true;
                } else if (wasInWater && mc.player.onGround()) {
                    wasInWater = false;
                }

                FluidState fluid = mc.level.getFluidState(mc.player.blockPosition());
                if (!fluid.isEmpty()) {
                    mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, motion.getValue(), mc.player.getDeltaMovement().z);
                }
            }
        }
    }
}