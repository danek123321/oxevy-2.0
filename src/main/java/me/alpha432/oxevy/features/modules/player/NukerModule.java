package me.alpha432.oxevy.features.modules.player;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.InteractionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

public class NukerModule extends Module {
    private final Setting<Integer> radius = num("Radius", 4, 1, 6);
    private final Setting<Integer> blocksPerTick = num("BPT", 1, 1, 8);
    private final Setting<Boolean> anyBlock = bool("AnyBlock", false);

    public NukerModule() {
        super("Nuker", "Breaks blocks around you", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (nullCheck()) return;

        double px = mc.player.getX();
        double py = mc.player.getY();
        double pz = mc.player.getZ();

        int broken = 0;
        int r = radius.getValue();

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    if (broken >= blocksPerTick.getValue()) break;

                    BlockPos pos = new BlockPos((int) px + x, (int) py + y, (int) pz + z);

                    if (canBreak(pos)) {
                        if (InteractionUtil.breakBlock(pos)) {
                            broken++;
                        }
                    }
                }
            }
        }
    }

    private boolean canBreak(BlockPos pos) {
        if (pos.equals(BlockPos.containing(mc.player.getX(), mc.player.getY(), mc.player.getZ()))) {
            return false;
        }

        var state = mc.level.getBlockState(pos);
        
        if (state.isAir()) {
            return false;
        }

        if (!anyBlock.getValue()) {
            var block = state.getBlock();
            if (block == Blocks.BEDROCK ||
                block == Blocks.OBSIDIAN ||
                block == Blocks.END_PORTAL_FRAME ||
                block == Blocks.END_PORTAL ||
                block == Blocks.NETHER_PORTAL ||
                block == Blocks.BARRIER) {
                return false;
            }
        }

        if (!state.getShape(mc.level, pos).isEmpty()) {
            return true;
        }

        return false;
    }
}
