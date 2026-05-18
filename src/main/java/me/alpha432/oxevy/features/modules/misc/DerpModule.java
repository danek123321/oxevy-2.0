package me.alpha432.oxevy.features.modules.misc;

import me.alpha432.oxevy.features.modules.Module;

import java.util.Random;

public class DerpModule extends Module {

    private final Random random = new Random();

    public DerpModule() {
        super("Derp", "Randomly moves your head", Category.MISC);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        float yaw = mc.player.getYRot() + random.nextFloat() * 360F - 180F;
        float pitch = random.nextFloat() * 180F - 90F;

        mc.player.setYRot(yaw);
        mc.player.setXRot(pitch);
    }
}
