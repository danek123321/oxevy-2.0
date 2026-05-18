package me.alpha432.oxevy.features.modules.movement;

import me.alpha432.oxevy.features.modules.Module;
import net.minecraft.world.phys.Vec3;

public class NoWebModule extends Module {

    public NoWebModule() {
        super("NoWeb", "Prevents webs from slowing you down", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        mc.player.stuckSpeedMultiplier = Vec3.ZERO;
    }
}
