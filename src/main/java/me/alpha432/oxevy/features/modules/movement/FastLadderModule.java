package me.alpha432.oxevy.features.modules.movement;

import me.alpha432.oxevy.features.modules.Module;
import net.minecraft.world.phys.Vec3;

public class FastLadderModule extends Module {

    public FastLadderModule() {
        super("FastLadder", "Climbs ladders faster", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        if (!mc.player.onClimbable() || !mc.player.horizontalCollision)
            return;

        if (mc.player.input.getMoveVector().length() <= 1e-5F)
            return;

        Vec3 velocity = mc.player.getDeltaMovement();
        mc.player.setDeltaMovement(velocity.x, 0.2872, velocity.z);
    }
}
