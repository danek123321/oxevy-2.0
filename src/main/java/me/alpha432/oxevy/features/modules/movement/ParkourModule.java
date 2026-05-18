package me.alpha432.oxevy.features.modules.movement;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;

public class ParkourModule extends Module {

    public Setting<Float> minDepth = num("Min Depth", 0.5f, 0.05f, 10f);
    public Setting<Float> edgeDistance = num("Edge Distance", 0.001f, 0.001f, 0.25f);
    public Setting<Boolean> jumpWhileSneaking = bool("Jump While Sneaking", false);

    public ParkourModule() {
        super("Parkour", "Automatically jumps at the edge of blocks", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;

        if (!mc.player.onGround() || mc.options.keyJump.isDown()) return;

        if (!jumpWhileSneaking.getValue()
                && (mc.player.isShiftKeyDown() || mc.options.keyShift.isDown()))
            return;

        var box = mc.player.getBoundingBox();
        var adjustedBox = box.expandTowards(0, -minDepth.getValue(), 0)
                .inflate(-edgeDistance.getValue(), 0, -edgeDistance.getValue());

        if (!mc.level.noCollision(mc.player, adjustedBox)) return;

        mc.player.jumpFromGround();
    }
}
