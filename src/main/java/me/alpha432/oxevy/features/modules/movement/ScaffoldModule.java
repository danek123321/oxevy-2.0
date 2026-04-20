package me.alpha432.oxevy.features.modules.movement;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class ScaffoldModule extends Module {
    private final Setting<Boolean> autoJump = bool("AutoJump", false);
    private final Setting<Boolean> keepY = bool("KeepY", false);
    private final Setting<Integer> blocksPerTick = num("BPT", 1, 1, 4);
    private final Setting<Boolean> tower = bool("Tower", false);

    private int startY;

    public ScaffoldModule() {
        super("Scaffold", "Places blocks beneath you", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (nullCheck()) return;
        startY = (int) mc.player.getY();
    }

    @Override
    public void onTick() {
        if (nullCheck()) return;

        double px = mc.player.getX();
        double py = mc.player.getY();
        double pz = mc.player.getZ();

        if (keepY.getValue() && py > startY + 1) {
            startY = (int) py;
        }

        int placed = 0;
        for (int i = 0; i < blocksPerTick.getValue(); i++) {
            BlockPos pos = getTargetPos(px, py, pz);
            if (pos != null && placeBlock(pos)) {
                placed++;
            }
            if (placed >= blocksPerTick.getValue()) break;
        }

        if ((tower.getValue() || autoJump.getValue()) && mc.player.onGround()) {
            mc.player.setJumping(true);
        }
    }

    private BlockPos getTargetPos(double px, double py, double pz) {
        int x = (int) Math.floor(px);
        int y = keepY.getValue() ? startY - 1 : (int) Math.floor(py) - 1;
        int z = (int) Math.floor(pz);

        BlockPos pos = new BlockPos(x, y, z);
        
        if (mc.level.getBlockState(pos).isAir()) {
            return pos;
        }

        return null;
    }

    private boolean placeBlock(BlockPos pos) {
        if (mc.level == null || mc.player == null || mc.gameMode == null) return false;
        
        var state = mc.level.getBlockState(pos);
        if (!state.isAir() && !state.canBeReplaced()) return false;

        Direction direction = Direction.UP;
        
        for (Direction d : Direction.values()) {
            BlockPos neighbor = pos.relative(d);
            var neighborState = mc.level.getBlockState(neighbor);
            if (!neighborState.isAir() && !neighborState.canBeReplaced()) {
                direction = d;
                break;
            }
        }

        BlockHitResult hitResult = new BlockHitResult(
            Vec3.atCenterOf(pos),
            direction.getOpposite(),
            pos,
            false
        );

        InteractionResult result = mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hitResult);
        
        if (result == InteractionResult.SUCCESS) {
            mc.player.connection.send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            return true;
        }

        return false;
    }
}
