package me.alpha432.oxevy.features.modules.movement;

import me.alpha432.oxevy.features.modules.Module;

public class AutoSwimModule extends Module {

    public AutoSwimModule() {
        super("AutoSwim", "Automatically swims in water", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        if (mc.player.horizontalCollision || mc.player.isShiftKeyDown())
            return;

        if (!mc.player.isInWater())
            return;

        if (mc.player.zza > 0)
            mc.player.setSprinting(true);
    }
}
