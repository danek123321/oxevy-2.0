package me.alpha432.oxevy.features.modules.render;

import me.alpha432.oxevy.event.impl.render.Render3DEvent;
import me.alpha432.oxevy.event.system.Subscribe;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.render.RenderUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HoleESPModule extends Module {

    public Setting<Integer> range = num("Range", 6, 1, 16);
    public Setting<Color> color = color("Color", new Color(0, 255, 0, 128));
    public Setting<Color> bedrockColor = color("Bedrock Color", new Color(0, 255, 0, 200));
    public Setting<Color> obsidianColor = color("Obsidian Color", new Color(255, 255, 0, 200));
    public Setting<Float> lineWidth = num("Line Width", 1.5f, 0.5f, 4f);
    public Setting<Boolean> throughWalls = bool("Through Walls", false);

    private final Set<BlockPos> scannedAir = new HashSet<>();

    public HoleESPModule() {
        super("HoleESP", "Shows safe holes for crystal pvp", Category.RENDER);
    }

    @Override
    public void onEnable() {
        scannedAir.clear();
    }

    @Subscribe
    public void onRender3D(Render3DEvent event) {
        if (mc.level == null || mc.player == null) return;

        List<AABB> holes = new ArrayList<>();

        BlockPos playerPos = mc.player.blockPosition();
        int r = range.getValue();

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos pos = playerPos.offset(dx, dy, dz);
                    if (isHole(pos)) {
                        holes.add(new AABB(pos));
                    }
                }
            }
        }

        for (AABB box : holes) {
            BlockPos pos = BlockPos.containing(box.minX, box.minY, box.minZ);
            BlockPos below = pos.below();
            var belowState = mc.level.getBlockState(below);
            Color c;
            if (belowState.is(Blocks.BEDROCK)) {
                c = bedrockColor.getValue();
            } else if (belowState.is(Blocks.OBSIDIAN)) {
                c = obsidianColor.getValue();
            } else {
                c = color.getValue();
            }
            RenderUtil.drawBox(event.getMatrix(), box, c, lineWidth.getValue(), throughWalls.getValue());
            RenderUtil.drawBoxFilled(event.getMatrix(), box, new Color(c.getRed(), c.getGreen(), c.getBlue(), 40));
        }
    }

    private boolean isHole(BlockPos pos) {
        if (mc.level == null) return false;

        if (!mc.level.isEmptyBlock(pos)) return false;

        BlockPos below = pos.below();
        var belowState = mc.level.getBlockState(below);
        if (!belowState.is(Blocks.BEDROCK) && !belowState.is(Blocks.OBSIDIAN))
            return false;

        BlockPos above = pos.above();
        if (!mc.level.isEmptyBlock(above)) return false;

        BlockPos above2 = pos.above(2);
        if (!mc.level.isEmptyBlock(above2)) return false;

        BlockPos[] sides = {
                pos.north(), pos.south(), pos.east(), pos.west()
        };
        for (BlockPos side : sides) {
            var state = mc.level.getBlockState(side);
            if (!state.isSolid() || state.isAir())
                return false;
        }

        return true;
    }
}
