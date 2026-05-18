package me.alpha432.oxevy.features.modules.movement;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import net.minecraft.world.phys.Vec3;

public class GlideModule extends Module {

    public Setting<Float> fallSpeed = num("Fall Speed", 0.125f, 0.005f, 0.25f);
    public Setting<Float> moveSpeed = num("Move Speed", 1.2f, 1f, 5f);
    public Setting<Float> minHeight = num("Min Height", 0f, 0f, 2f);
    public Setting<Boolean> pauseOnSneak = bool("Pause When Sneaking", true);

    public GlideModule() {
        super("Glide", "Slows your descent while falling", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        if (pauseOnSneak.getValue() && mc.player.isShiftKeyDown())
            return;

        Vec3 v = mc.player.getDeltaMovement();

        if (mc.player.onGround() || mc.player.isInWater() || mc.player.isInLava()
                || mc.player.onClimbable() || v.y >= 0)
            return;

        if (minHeight.getValue() > 0) {
            var box = mc.player.getBoundingBox().expandTowards(0, -minHeight.getValue(), 0);
            if (!mc.level.noCollision(mc.player, box))
                return;
        }

        mc.player.setDeltaMovement(
                v.x * moveSpeed.getValue(),
                Math.max(v.y, -fallSpeed.getValue()),
                v.z * moveSpeed.getValue()
        );
    }
}
