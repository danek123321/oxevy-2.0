package me.alpha432.oxevy.features.modules.movement;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class SafeWalkModule extends Module {
    public final Setting<Boolean> edgeDetect = bool("EdgeDetect", true);
    public final Setting<Boolean> liquid = bool("Liquid", true);

    public SafeWalkModule() {
        super("SafeWalk", "Prevents walking off edges", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (nullCheck()) return;
        if (!mc.player.onGround()) return;

        Direction direction = mc.player.getDirection();

        BlockPos front = BlockPos.containing(mc.player.getX(), mc.player.getY(), mc.player.getZ()).relative(direction).below();

        if (!mc.level.getBlockState(front).isAir()) return;

        if (edgeDetect.getValue()) {
            mc.player.setDeltaMovement(0, mc.player.getDeltaMovement().y, 0);
        }
    }

    public boolean shouldStop() {
        if (!isEnabled() || nullCheck()) return false;

        if (!mc.player.onGround()) return false;

        Direction direction = mc.player.getDirection();
        BlockPos front = BlockPos.containing(mc.player.getX(), mc.player.getY(), mc.player.getZ()).relative(direction).below();

        return mc.level.getBlockState(front).isAir();
    }
}