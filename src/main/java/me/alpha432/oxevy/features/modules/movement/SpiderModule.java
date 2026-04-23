package me.alpha432.oxevy.features.modules.movement;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;

public class SpiderModule extends Module {
    public final Setting<Float> speed = num("Speed", 0.15f, 0.1f, 0.5f);

    public SpiderModule() {
        super("Spider", "Climb walls and webs", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (nullCheck()) return;

        if (mc.player.verticalCollision || mc.player.horizontalCollision) {
            mc.player.setDeltaMovement(
                mc.player.getDeltaMovement().x, 
                speed.getValue(), 
                mc.player.getDeltaMovement().z
            );
        }
    }
}