package me.alpha432.oxevy.features.modules.player;

import me.alpha432.oxevy.event.impl.render.Render3DEvent;
import me.alpha432.oxevy.event.system.Subscribe;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.InteractionUtil;
import me.alpha432.oxevy.util.render.OverlayRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class NukerModule extends Module {
    private final Setting<Float> range = num("Range", 5f, 1f, 6f);
    private final Setting<Integer> blocksPerTick = num("BPT", 1, 1, 8);
    private final Setting<Boolean> anyBlock = bool("AnyBlock", false);

    private final OverlayRenderer overlay = new OverlayRenderer();
    private BlockPos currentBlock;
    private final List<BlockPos> breakQueue = new ArrayList<>();
    private long lastBreakTime = 0;
    private static final long BREAK_INTERVAL_MS = 100;

    public NukerModule() {
        super("Nuker", "Breaks blocks around you", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        breakQueue.clear();
        currentBlock = null;
        overlay.resetProgress();
    }

    @Override
    public void onDisable() {
        if (currentBlock != null) {
            mc.gameMode.stopDestroyBlock();
            currentBlock = null;
        }
        overlay.resetProgress();
        breakQueue.clear();
    }

    @Override
    public void onTick() {
        if (nullCheck()) return;

        currentBlock = null;

        double rangeSq = range.getValue() * range.getValue();
        int blockRange = (int) Math.ceil(range.getValue());

        BlockPos center = mc.player.blockPosition();

        if (mc.player.getAbilities().instabuild) {
            handleCreative(center, blockRange, rangeSq);
            return;
        }

        handleSurvival(center, blockRange, rangeSq);
    }

    private void handleCreative(BlockPos center, int blockRange, double rangeSq) {
        mc.gameMode.stopDestroyBlock();
        overlay.resetProgress();

        breakQueue.clear();
        forEachBlock(center, blockRange).filter(pos -> {
            if (center.equals(pos)) return false;
            if (mc.player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > rangeSq) return false;
            return canBreak(pos);
        }).forEach(breakQueue::add);

        if (breakQueue.isEmpty()) return;

        int limit = Math.min(blocksPerTick.getValue(), breakQueue.size());
        for (int i = 0; i < limit; i++) {
            BlockPos pos = breakQueue.get(i);
            mc.getConnection().send(new ServerboundPlayerActionPacket(
                ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, net.minecraft.core.Direction.UP));
        }
        mc.getConnection().send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));

        currentBlock = breakQueue.get(0);
    }

    private void handleSurvival(BlockPos center, int blockRange, double rangeSq) {
        breakQueue.clear();
        forEachBlock(center, blockRange).filter(pos -> {
            if (center.equals(pos)) return false;
            if (mc.player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > rangeSq) return false;
            return canBreak(pos);
        }).sorted(Comparator.comparingDouble(pos ->
            mc.player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)))
            .forEach(breakQueue::add);

        if (breakQueue.isEmpty()) {
            mc.gameMode.stopDestroyBlock();
            overlay.resetProgress();
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastBreakTime < BREAK_INTERVAL_MS / blocksPerTick.getValue()) return;
        lastBreakTime = now;

        currentBlock = breakQueue.get(0);

        if (InteractionUtil.breakBlock(currentBlock)) {
            overlay.updateProgress();
        }
    }

    @Subscribe
    public void onRender3D(Render3DEvent event) {
        overlay.render(event.getMatrix(), event.getDelta(), currentBlock);
    }

    private boolean canBreak(BlockPos pos) {
        BlockState state = mc.level.getBlockState(pos);
        if (state.isAir()) return false;

        if (!anyBlock.getValue()) {
            var block = state.getBlock();
            if (block == Blocks.BEDROCK || block == Blocks.OBSIDIAN ||
                block == Blocks.END_PORTAL_FRAME || block == Blocks.END_PORTAL ||
                block == Blocks.NETHER_PORTAL || block == Blocks.BARRIER ||
                block == Blocks.REINFORCED_DEEPSLATE) {
                return false;
            }
        }

        return !state.getShape(mc.level, pos).isEmpty();
    }

    private Stream<BlockPos> forEachBlock(BlockPos center, int r) {
        List<BlockPos> positions = new ArrayList<>();
        for (int x = -r; x <= r; x++)
            for (int y = -r; y <= r; y++)
                for (int z = -r; z <= r; z++)
                    positions.add(center.offset(x, y, z));
        return positions.stream();
    }
}
