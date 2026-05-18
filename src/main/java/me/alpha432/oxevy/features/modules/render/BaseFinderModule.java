package me.alpha432.oxevy.features.modules.render;

import me.alpha432.oxevy.event.impl.render.Render3DEvent;
import me.alpha432.oxevy.event.system.Subscribe;
import me.alpha432.oxevy.features.commands.Command;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.render.RenderUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.Vec3;

public class BaseFinderModule extends Module {
    public final Setting<Integer> range = num("Range", 128, 16, 512);
    public final Setting<Boolean> announce = bool("Announce", true);
    public final Setting<Boolean> tracers = bool("Tracers", true);
    public final Setting<Boolean> boxes = bool("Boxes", true);
    public final Setting<Boolean> fill = bool("Fill", true);
    public final Setting<Integer> red = num("Red", 255, 0, 255);
    public final Setting<Integer> green = num("Green", 0, 0, 255);
    public final Setting<Integer> blue = num("Blue", 0, 0, 255);
    public final Setting<Integer> alpha = num("Alpha", 100, 0, 255);
    public final Setting<Integer> outlineAlpha = num("OutlineAlpha", 255, 0, 255);
    public final Setting<Float> lineWidth = num("LineWidth", 1.5f, 0.1f, 5.0f);

    private final Set<BlockPos> foundBases = new HashSet<>();
    private final Set<BlockPos> announcedBases = new HashSet<>();

    public BaseFinderModule() {
        super("BaseFinder", "Finds player bases by searching for base-related blocks", Category.RENDER);
    }

    @Override
    public void onTick() {
        if (nullCheck()) return;

        foundBases.clear();
        int cx = (int) mc.player.getX() >> 4;
        int cz = (int) mc.player.getZ() >> 4;
        int r = range.getValue() / 16 + 1;

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                var chunk = mc.level.getChunkSource().getChunk(cx + dx, cz + dz, false);
                if (!(chunk instanceof LevelChunk lc)) continue;

                for (var entry : lc.getBlockEntities().entrySet()) {
                    BlockPos pos = entry.getKey();
                    if (mc.player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) > range.getValue() * range.getValue()) continue;
                    
                    BlockEntity be = entry.getValue();
                    if (isBaseBlock(be)) {
                        foundBases.add(pos.immutable());
                        if (announce.getValue() && !announcedBases.contains(pos)) {
                            Command.sendMessage("Base found at: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
                            announcedBases.add(pos.immutable());
                        }
                    }
                }
            }
        }
    }

    private boolean isBaseBlock(BlockEntity be) {
        return be instanceof ChestBlockEntity ||
               be instanceof EnderChestBlockEntity ||
               be instanceof FurnaceBlockEntity ||
               be instanceof HopperBlockEntity ||
               be instanceof ShulkerBoxBlockEntity ||
               be instanceof BarrelBlockEntity ||
               be instanceof BlastFurnaceBlockEntity ||
               be instanceof SmokerBlockEntity ||
               be instanceof BrewingStandBlockEntity ||
               be instanceof SignBlockEntity ||
               be instanceof SpawnerBlockEntity ||
               be instanceof BedBlockEntity;
    }

    @Subscribe
    public void onRender3D(Render3DEvent event) {
        if (foundBases.isEmpty()) return;

        Color color = new Color(red.getValue(), green.getValue(), blue.getValue(), alpha.getValue());
        Color outlineColor = new Color(red.getValue(), green.getValue(), blue.getValue(), outlineAlpha.getValue());

        for (BlockPos pos : foundBases) {
            AABB bb = new AABB(pos);
            if (fill.getValue()) {
                RenderUtil.drawBoxFilled(event.getMatrix(), bb, color);
            }
            if (boxes.getValue()) {
                RenderUtil.drawBox(event.getMatrix(), bb, outlineColor, lineWidth.getValue());
            }
        }

        if (tracers.getValue()) {
            List<Vec3> ends = new ArrayList<>();
            for (BlockPos pos : foundBases) {
                ends.add(Vec3.atCenterOf(pos));
            }
            RenderUtil.drawTracers(event.getMatrix(), event.getDelta(), ends, outlineColor.getRGB(), lineWidth.getValue(), true);
        }
    }

    @Override
    public void onDisable() {
        foundBases.clear();
        announcedBases.clear();
    }
}
