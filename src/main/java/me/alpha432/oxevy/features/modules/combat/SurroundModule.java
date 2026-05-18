package me.alpha432.oxevy.features.modules.combat;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.InteractionUtil;
import net.minecraft.core.BlockPos;
import java.util.ArrayList;
import java.util.List;

public class SurroundModule extends Module {
    public final Setting<Integer> blocks = num("Blocks", 1, 1, 8);
    public final Setting<Boolean> center = bool("Center", true);
    public final Setting<Boolean> autoCenter = bool("AutoCenter", false);
    public final Setting<Boolean> onJump = bool("OnJump", false);

    public SurroundModule() {
        super("Surround", "Places obsidian around you", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (nullCheck()) return;
        if (center.getValue() && mc.player.onGround()) {
            mc.player.setPos(Math.round(mc.player.getX()) + 0.5, mc.player.getY(), Math.round(mc.player.getZ()) + 0.5);
        }
        List<BlockPos> positions = getPositions();
        for (BlockPos pos : positions) {
            if (mc.level.getBlockState(pos).isAir()) {
                InteractionUtil.place(pos, false);
            }
        }
    }

    private List<BlockPos> getPositions() {
        List<BlockPos> positions = new ArrayList<>();
        BlockPos playerPos = BlockPos.containing(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        positions.add(playerPos.north());
        positions.add(playerPos.south());
        positions.add(playerPos.east());
        positions.add(playerPos.west());
        if (blocks.getValue() >= 4) {
            positions.add(playerPos.north().east());
            positions.add(playerPos.north().west());
            positions.add(playerPos.south().east());
            positions.add(playerPos.south().west());
        }
        return positions;
    }
}