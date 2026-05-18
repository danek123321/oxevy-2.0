package me.alpha432.oxevy.features.modules.movement;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;

public class AutoWalkModule extends Module {
    public final Setting<Boolean> smart = bool("Smart", false);
    public final Setting<Integer> blockDistance = num("BlockDistance", 4, 1, 10);
    public final Setting<Boolean> jump = bool("Jump", false);

    public AutoWalkModule() {
        super("AutoWalk", "Automatically walks", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (nullCheck()) return;
        mc.player.zza = 1;

        if (jump.getValue() && mc.player.horizontalCollision && mc.player.onGround()) {
            mc.player.jumpFromGround();
        }

        if (smart.getValue()) {
            mc.player.setSprinting(true);
        }
    }
}