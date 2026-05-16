package me.alpha432.oxevy.features.modules.render;

import me.alpha432.oxevy.event.impl.render.Render3DEvent;
import me.alpha432.oxevy.event.system.Subscribe;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.render.RenderUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class ChestESPModule extends Module {
    
    public Setting<EspStyle> style = mode("Style", EspStyle.BOTH);
    public Setting<Boolean> showBoxes = bool("Boxes", true);
    public Setting<Boolean> showLines = bool("Lines", true);
    public Setting<Boolean> showChests = bool("Chests", true);
    public Setting<Boolean> showEnderChests = bool("EnderChests", true);
    public Setting<Boolean> showTrappedChests = bool("TrappedChests", true);
    public Setting<Boolean> showShulkers = bool("Shulkers", true);
    public Setting<Boolean> showBarrels = bool("Barrels", true);
    public Setting<Float> lineWidth = num("LineWidth", 1.5f, 0.5f, 3f);
    public Setting<Boolean> throughWalls = bool("ThroughWalls", true);
    public Setting<Boolean> pulseNew = bool("PulseNew", true);
    
    private static final int CHEST_COLOR = 0x80FFC864;
    private static final int ENDER_COLOR = 0x808C00FF;
    private static final int TRAPPED_COLOR = 0x80FF3232;
    private static final int BARREL_COLOR = 0x80B48C64;
    
    private static final int[] SHULKER_COLORS = {
        0x80D7D2D2, 0x80FAAF3C, 0x80EBEB32, 0x806EEB6E,
        0x8019D2EB, 0x803C46F5, 0x80E132F5, 0x80AFAFAF,
        0x80414141, 0x80FA6464, 0x80FAAF3C, 0x80EBEB32,
        0x806EEB6E, 0x803CA5E6, 0x80A05AF5, 0x80F54BD7
    };
    
    private final Map<BlockPos, AABB> chestBoxes = new HashMap<>();
    private final Map<BlockPos, AABB> enderBoxes = new HashMap<>();
    private final Map<BlockPos, AABB> trappedBoxes = new HashMap<>();
    private final Map<BlockPos, AABB> shulkerBoxes = new HashMap<>();
    private final Map<BlockPos, AABB> barrelBoxes = new HashMap<>();
    
    private final Map<BlockPos, Long> spawnTime = new HashMap<>();
    private static final long PULSE_DURATION_MS = 2000;
    
    private long lastScanTime = 0;
    private static final long SCAN_INTERVAL_MS = 50;
    
    public ChestESPModule() {
        super("ChestESP", "Highlights storage containers through walls", Category.RENDER);
    }
    
    private void scan() {
        if (nullCheck()) return;
        
        long now = System.currentTimeMillis();
        if (now - lastScanTime < SCAN_INTERVAL_MS) return;
        lastScanTime = now;
        
        chestBoxes.clear();
        enderBoxes.clear();
        trappedBoxes.clear();
        shulkerBoxes.clear();
        barrelBoxes.clear();
        
        int cx = (int) mc.player.getX() >> 4;
        int cz = (int) mc.player.getZ() >> 4;
        int range = 48;
        int r = range / 16 + 1;
        
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                var chunk = mc.level.getChunkSource().getChunk(cx + dx, cz + dz, false);
                if (!(chunk instanceof LevelChunk lc)) continue;
                
                for (var entry : lc.getBlockEntities().entrySet()) {
                    BlockPos pos = entry.getKey();
                    var be = entry.getValue();
                    var block = be.getBlockState().getBlock();
                    
                    if (block == Blocks.CHEST) {
                        if (!showChests.getValue()) continue;
                        chestBoxes.put(pos.immutable(), getChestBox(pos));
                    } else if (block == Blocks.ENDER_CHEST) {
                        if (!showEnderChests.getValue()) continue;
                        enderBoxes.put(pos.immutable(), getEnderChestBox(pos));
                    } else if (block == Blocks.TRAPPED_CHEST) {
                        if (!showTrappedChests.getValue()) continue;
                        trappedBoxes.put(pos.immutable(), getChestBox(pos));
                    } else if (block instanceof ShulkerBoxBlock) {
                        if (!showShulkers.getValue()) continue;
                        shulkerBoxes.put(pos.immutable(), getShulkerBox(pos));
                    } else if (block == Blocks.BARREL) {
                        if (!showBarrels.getValue()) continue;
                        barrelBoxes.put(pos.immutable(), getBarrelBox(pos));
                    }
                }
            }
        }
    }
    
    @Subscribe
    public void onRender3D(Render3DEvent event) {
        scan();
        
        boolean hasBoxes = showBoxes.getValue() && style.getValue() != EspStyle.LINES;
        boolean hasLines = showLines.getValue() && style.getValue() != EspStyle.BOXES;
        
        if (hasBoxes) {
            renderGroup(event, chestBoxes.values(), CHEST_COLOR);
            renderGroup(event, enderBoxes.values(), ENDER_COLOR);
            renderGroup(event, trappedBoxes.values(), TRAPPED_COLOR);
            renderGroup(event, barrelBoxes.values(), BARREL_COLOR);
            
            if (!shulkerBoxes.isEmpty()) {
                List<AABB> list = new ArrayList<>(shulkerBoxes.values());
                for (int i = 0; i < list.size(); i++) {
                    int color = SHULKER_COLORS[i % SHULKER_COLORS.length];
                    RenderUtil.drawOutlinedBoxes(event.getMatrix(), List.of(list.get(i)), color, throughWalls.getValue(), lineWidth.getValue());
                    RenderUtil.drawSolidBoxes(event.getMatrix(), List.of(list.get(i)), (color & 0x00FFFFFF) | 0x28000000, throughWalls.getValue());
                }
            }
        }
        
        if (hasLines) {
            List<Vec3> ends = new ArrayList<>();
            List<Integer> colors = new ArrayList<>();
            
            addBoxCenters(chestBoxes.values(), (CHEST_COLOR & 0x00FFFFFF) | 0xB4000000, ends, colors);
            addBoxCenters(enderBoxes.values(), (ENDER_COLOR & 0x00FFFFFF) | 0xB4000000, ends, colors);
            addBoxCenters(trappedBoxes.values(), (TRAPPED_COLOR & 0x00FFFFFF) | 0xB4000000, ends, colors);
            addBoxCenters(shulkerBoxes.values(), 0xB4FFC8C8, ends, colors);
            addBoxCenters(barrelBoxes.values(), (BARREL_COLOR & 0x00FFFFFF) | 0xB4000000, ends, colors);
            
            if (!ends.isEmpty()) {
                RenderUtil.drawTracersToPoints(event.getMatrix(), event.getDelta(), 
                    RenderUtil.getTracerOrigin(), ends, colors, lineWidth.getValue(), throughWalls.getValue());
            }
        }
    }
    
    private void renderGroup(Render3DEvent event, Collection<AABB> boxes, int color) {
        if (boxes.isEmpty()) return;
        List<AABB> list = boxes instanceof List ? (List<AABB>) boxes : new ArrayList<>(boxes);
        RenderUtil.drawOutlinedBoxes(event.getMatrix(), list, color, throughWalls.getValue(), lineWidth.getValue());
        RenderUtil.drawSolidBoxes(event.getMatrix(), list, (color & 0x00FFFFFF) | 0x28000000, throughWalls.getValue());
    }
    
    private AABB getChestBox(BlockPos pos) {
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), 
            pos.getX() + 1, pos.getY() + 0.875, pos.getZ() + 1);
    }
    
    private AABB getEnderChestBox(BlockPos pos) {
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), 
            pos.getX() + 1, pos.getY() + 0.875, pos.getZ() + 1);
    }
    
    private AABB getShulkerBox(BlockPos pos) {
        return new AABB(pos.getX() + 0.0625, pos.getY(), pos.getZ() + 0.0625,
            pos.getX() + 0.9375, pos.getY() + 0.9375, pos.getZ() + 0.9375);
    }
    
    private AABB getBarrelBox(BlockPos pos) {
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), 
            pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }
    
    private void addBoxCenters(Collection<AABB> boxes, int color, List<Vec3> ends, List<Integer> colors) {
        for (AABB box : boxes) {
            ends.add(box.getCenter());
            colors.add(color);
        }
    }
    
    public enum EspStyle {
        BOXES,
        LINES,
        BOTH
    }
}
