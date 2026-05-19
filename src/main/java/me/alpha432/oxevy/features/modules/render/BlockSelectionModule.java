package me.alpha432.oxevy.features.modules.render;

import me.alpha432.oxevy.event.impl.render.Render3DEvent;
import me.alpha432.oxevy.event.system.Subscribe;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.render.RenderUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.awt.Color;

public class BlockSelectionModule extends Module {

    public Setting<Boolean> advanced = bool("Advanced", true);
    public Setting<Boolean> oneSide = bool("SingleSide", false);
    public Setting<Color> sideColor = color("SideColor", 255, 255, 255, 50);
    public Setting<Color> lineColor = color("LineColor", 255, 255, 255, 255);
    public Setting<Boolean> hideInside = bool("HideWhenInside", true);

    public BlockSelectionModule() {
        super("BlockSelection", "Modifies block selection rendering", Category.RENDER);
    }

    @Subscribe
    public void onRender3D(Render3DEvent event) {
        if (mc.hitResult == null || !(mc.hitResult instanceof BlockHitResult result)) return;
        if (result.getType() == HitResult.Type.MISS) return;

        if (hideInside.getValue() && result.isInside()) return;

        BlockPos bp = result.getBlockPos();
        net.minecraft.core.Direction side = result.getDirection();

        VoxelShape shape = mc.level.getBlockState(bp).getShape(mc.level, bp);
        if (shape.isEmpty()) return;

        AABB box = shape.bounds();

        if (oneSide.getValue()) {
            AABB sideBox = getSideBox(bp, box, side);
            RenderUtil.drawSolidBoxes(event.getMatrix(), java.util.List.of(sideBox), sideColor.getValue().getRGB(), true);
            RenderUtil.drawOutlinedBoxes(event.getMatrix(), java.util.List.of(sideBox), lineColor.getValue().getRGB(), true);
        } else {
            if (advanced.getValue()) {
                for (AABB b : shape.toAabbs()) {
                    AABB movedBox = b.move(bp);
                    RenderUtil.drawSolidBoxes(event.getMatrix(), java.util.List.of(movedBox), sideColor.getValue().getRGB(), true);
                    RenderUtil.drawOutlinedBoxes(event.getMatrix(), java.util.List.of(movedBox), lineColor.getValue().getRGB(), true);
                }
            } else {
                AABB movedBox = box.move(bp);
                RenderUtil.drawSolidBoxes(event.getMatrix(), java.util.List.of(movedBox), sideColor.getValue().getRGB(), true);
                RenderUtil.drawOutlinedBoxes(event.getMatrix(), java.util.List.of(movedBox), lineColor.getValue().getRGB(), true);
            }
        }
    }

    private AABB getSideBox(BlockPos bp, AABB box, net.minecraft.core.Direction side) {
        return switch (side) {
            case UP -> new AABB(bp.getX() + box.minX, bp.getY() + box.maxY, bp.getZ() + box.minZ,
                    bp.getX() + box.maxX, bp.getY() + box.maxY + 0.001, bp.getZ() + box.maxZ);
            case DOWN -> new AABB(bp.getX() + box.minX, bp.getY() + box.minY, bp.getZ() + box.minZ,
                    bp.getX() + box.maxX, bp.getY() + box.minY + 0.001, bp.getZ() + box.maxZ);
            case NORTH -> new AABB(bp.getX() + box.minX, bp.getY() + box.minY, bp.getZ() + box.minZ,
                    bp.getX() + box.maxX, bp.getY() + box.maxY, bp.getZ() + box.minZ + 0.001);
            case SOUTH -> new AABB(bp.getX() + box.minX, bp.getY() + box.minY, bp.getZ() + box.maxZ - 0.001,
                    bp.getX() + box.maxX, bp.getY() + box.maxY, bp.getZ() + box.maxZ);
            case WEST -> new AABB(bp.getX() + box.minX, bp.getY() + box.minY, bp.getZ() + box.minZ,
                    bp.getX() + box.minX + 0.001, bp.getY() + box.maxY, bp.getZ() + box.maxZ);
            case EAST -> new AABB(bp.getX() + box.maxX - 0.001, bp.getY() + box.minY, bp.getZ() + box.minZ,
                    bp.getX() + box.maxX, bp.getY() + box.maxY, bp.getZ() + box.maxZ);
        };
    }
}
