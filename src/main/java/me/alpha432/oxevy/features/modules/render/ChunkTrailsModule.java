package me.alpha432.oxevy.features.modules.render;

import me.alpha432.oxevy.event.impl.render.Render3DEvent;
import me.alpha432.oxevy.event.system.Subscribe;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.render.RenderUtil;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;

import java.awt.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkTrailsModule extends Module {
    private final Map<String, Set<ChunkPos>> visitedChunks = new ConcurrentHashMap<>();

    public final Setting<Color> color = color("Color", 255, 0, 0, 100);
    public final Setting<Boolean> outline = bool("Outline", true);
    public final Setting<Color> outlineColor = color("OutlineColor", 255, 0, 0, 255);
    public final Setting<Float> lineWidth = num("LineWidth", 1.5f, 0.1f, 5.0f);
    public final Setting<Integer> height = num("Height", 0, -64, 320);
    public final Setting<Boolean> relativeHeight = bool("RelativeHeight", false);
    public final Setting<Runnable> clear = button("Clear", () -> visitedChunks.clear());

    public ChunkTrailsModule() {
        super("ChunkTrails", "Shows all chunks that you have visited", Category.RENDER);
    }

    @Override
    public void onTick() {
        if (mc.player != null && mc.level != null) {
            String dimension = mc.level.dimension().toString();
            visitedChunks.computeIfAbsent(dimension, k -> Collections.synchronizedSet(new HashSet<>()))
                    .add(new ChunkPos(mc.player.blockPosition()));
        }
    }

    @Subscribe
    public void onRender3D(Render3DEvent event) {
        if (mc.level == null) return;
        String dimension = mc.level.dimension().toString();
        Set<ChunkPos> chunks = visitedChunks.get(dimension);
        if (chunks == null) return;

        synchronized (chunks) {
            for (ChunkPos pos : chunks) {
                double minX = pos.getMinBlockX();
                double minZ = pos.getMinBlockZ();
                double maxX = pos.getMaxBlockX() + 1;
                double maxZ = pos.getMaxBlockZ() + 1;
                
                double y = height.getValue();
                if (relativeHeight.getValue() && mc.player != null) {
                    y += Math.floor(mc.player.getY());
                }

                AABB box = new AABB(minX, y, minZ, maxX, y + 0.05, maxZ);
                
                RenderUtil.drawBoxFilled(event.getMatrix(), box, color.getValue());
                if (outline.getValue()) {
                    RenderUtil.drawBox(event.getMatrix(), box, outlineColor.getValue(), lineWidth.getValue());
                }
            }
        }
    }
}

