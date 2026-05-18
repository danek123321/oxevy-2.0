package me.alpha432.oxevy.features.modules.render;

import me.alpha432.oxevy.event.impl.render.Render3DEvent;
import me.alpha432.oxevy.event.system.Subscribe;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.render.RenderUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

import java.awt.Color;
import java.util.*;

public class StorageESPModule extends Module {
    public final Setting<Integer> range = num("Range", 32, 1, 128);
    public final Setting<Boolean> chests = bool("Chests", true);
    public final Setting<Color> chestColor = color("Chest Color", new Color(0, 255, 0));
    public final Setting<Boolean> enderChests = bool("EnderChests", true);
    public final Setting<Color> enderChestColor = color("EnderChest Color", new Color(0, 255, 0));
    public final Setting<Boolean> hoppers = bool("Hoppers", true);
    public final Setting<Color> hopperColor = color("Hopper Color", new Color(255, 165, 0));
    public final Setting<Boolean> furnaces = bool("Furnaces", true);
    public final Setting<Color> furnaceColor = color("Furnace Color", new Color(255, 0, 0));
    public final Setting<Boolean> barrels = bool("Barrels", true);
    public final Setting<Color> barrelColor = color("Barrel Color", new Color(139, 69, 19));
    public final Setting<Boolean> shulkers = bool("Shulkers", true);
    public final Setting<Color> shulkerColor = color("Shulker Color", new Color(255, 0, 255));

    private static class StorageInfo {
        final BlockPos pos;
        final int color;
        
        StorageInfo(BlockPos pos, int color) {
            this.pos = pos;
            this.color = color;
        }
    }
    
    private final List<StorageInfo> foundStorage = new ArrayList<>();
    private final List<AABB> storageBoxes = new ArrayList<>();

    public StorageESPModule() {
        super("StorageESP", "Shows storage blocks", Category.RENDER);
    }

    private long lastScanTime = 0;
    private static final long SCAN_INTERVAL_MS = 50;

    private void scan() {
        if (nullCheck()) return;
        long now = System.currentTimeMillis();
        if (now - lastScanTime < SCAN_INTERVAL_MS) return;
        lastScanTime = now;
        foundStorage.clear();
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
                if (chests.getValue() && be instanceof ChestBlockEntity) {
                    foundStorage.add(new StorageInfo(pos.immutable(), chestColor.getValue().getRGB()));
                    continue;
                }
                if (enderChests.getValue() && be instanceof EnderChestBlockEntity) {
                    foundStorage.add(new StorageInfo(pos.immutable(), enderChestColor.getValue().getRGB()));
                    continue;
                }
                if (hoppers.getValue() && be instanceof HopperBlockEntity) {
                    foundStorage.add(new StorageInfo(pos.immutable(), hopperColor.getValue().getRGB()));
                    continue;
                }
                if (furnaces.getValue() && be instanceof FurnaceBlockEntity) {
                    foundStorage.add(new StorageInfo(pos.immutable(), furnaceColor.getValue().getRGB()));
                    continue;
                }
                if (barrels.getValue() && be instanceof BarrelBlockEntity) {
                    foundStorage.add(new StorageInfo(pos.immutable(), barrelColor.getValue().getRGB()));
                    continue;
                }
                if (shulkers.getValue() && be instanceof ShulkerBoxBlockEntity) {
                    foundStorage.add(new StorageInfo(pos.immutable(), shulkerColor.getValue().getRGB()));
                    continue;
                }
            }
            }
        }
    }

    @Subscribe
    public void onRender3D(Render3DEvent event) {
        scan();
        if (foundStorage.isEmpty()) return;
        storageBoxes.clear();
        
        Map<Integer, List<AABB>> colorGroups = new HashMap<>();
        for (StorageInfo info : foundStorage) {
            colorGroups.computeIfAbsent(info.color, k -> new ArrayList<>()).add(new AABB(info.pos));
        }
        
        for (var entry : colorGroups.entrySet()) {
            int color = entry.getKey();
            List<AABB> boxes = entry.getValue();
            RenderUtil.drawOutlinedBoxes(event.getMatrix(), boxes, color, true, 2f);
            RenderUtil.drawSolidBoxes(event.getMatrix(), boxes, (color & 0x00FFFFFF) | 0x28000000, true);
        }
    }
}
