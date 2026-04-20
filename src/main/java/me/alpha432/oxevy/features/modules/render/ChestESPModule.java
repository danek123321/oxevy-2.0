package me.alpha432.oxevy.features.modules.render;

import me.alpha432.oxevy.event.impl.render.Render3DEvent;
import me.alpha432.oxevy.event.system.Subscribe;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.MathUtil;
import me.alpha432.oxevy.util.render.RenderUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ChestESPModule extends Module {
    public final Setting<Float> maxDistance = num("MaxDistance", 64f, 16f, 128f);
    public final Setting<Float> lineWidth = num("LineWidth", 1.5f, 0.5f, 4f);
    public final Setting<Boolean> pulseNew = bool("PulseNew", true);
    public final Setting<Boolean> distanceFade = bool("DistanceFade", true);

    private static final Color CHEST_COLOR = Color.WHITE;
    private static final Color ENDER_COLOR = new Color(180, 0, 255);
    private static final Color TRAPPED_COLOR = new Color(255, 60, 60);
    private static final Color[] SHULKER_PASTELS = {
        new Color(255, 230, 230), new Color(255, 220, 180), new Color(255, 255, 200),
        new Color(200, 255, 200), new Color(200, 255, 255), new Color(200, 200, 255),
        new Color(255, 200, 255), new Color(220, 220, 220), new Color(140, 140, 140),
        new Color(255, 180, 180), new Color(255, 200, 120), new Color(255, 255, 120),
        new Color(120, 255, 120), new Color(120, 255, 255), new Color(180, 180, 255),
        new Color(255, 180, 255)
    };

    private final Map<BlockPos, Long> spawnTime = new HashMap<>();
    private static final long PULSE_DURATION_MS = 800;

    public ChestESPModule() {
        super("ChestESP", "Highlight chests through walls with colored outlines", Category.RENDER);
    }

    @Subscribe
    public void onRender3D(Render3DEvent event) {
        if (nullCheck()) return;
        Vec3 camera = mc.gameRenderer.getMainCamera().position();
        double maxDist = maxDistance.getValue();
        double maxDistSq = maxDist * maxDist;

        int chunkRange = (int) Math.ceil(maxDist / 16);
        int cx0 = camera.x() >= 0 ? (int) camera.x() >> 4 : (int) camera.x() - 15 >> 4;
        int cz0 = camera.z() >= 0 ? (int) camera.z() >> 4 : (int) camera.z() - 15 >> 4;

        for (int dx = -chunkRange; dx <= chunkRange; dx++) {
            for (int dz = -chunkRange; dz <= chunkRange; dz++) {
                var chunk = mc.level.getChunkSource().getChunk(cx0 + dx, cz0 + dz, false);
                if (!(chunk instanceof LevelChunk lc)) continue;
                for (Map.Entry<BlockPos, BlockEntity> entry : lc.getBlockEntities().entrySet()) {
                    BlockPos pos = entry.getKey();
                    if (camera.distanceToSqr(Vec3.atCenterOf(pos)) > maxDistSq) continue;

                    var state = mc.level.getBlockState(pos);
                    var block = state.getBlock();
                    Color baseColor;
                    if (block == Blocks.CHEST) {
                        baseColor = CHEST_COLOR;
                    } else if (block == Blocks.ENDER_CHEST) {
                        baseColor = ENDER_COLOR;
                    } else if (block == Blocks.TRAPPED_CHEST) {
                        baseColor = TRAPPED_COLOR;
                    } else if (block instanceof ShulkerBoxBlock shulker) {
                        int idx = shulker.getColor().getId();
                        baseColor = SHULKER_PASTELS[Math.max(0, Math.min(idx, SHULKER_PASTELS.length - 1))];
                    } else {
                        continue;
                    }

                    long now = System.currentTimeMillis();
                    spawnTime.putIfAbsent(pos, now);

                    float alpha = 1f;
                    if (distanceFade.getValue()) {
                        double dist = Math.sqrt(camera.distanceToSqr(Vec3.atCenterOf(pos)));
                        alpha = (float) Math.max(0.3, 1.0 - (dist / maxDist) * 0.6);
                    }
                    if (pulseNew.getValue()) {
                        long age = now - spawnTime.get(pos);
                        if (age < PULSE_DURATION_MS) {
                            float pulse = 0.6f + 0.4f * (float) Math.sin(age * Math.PI * 2 / 200);
                            alpha *= pulse;
                        }
                    }

                    int a = (int) (255 * alpha);
                    Color c = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), a);
                    RenderUtil.drawBox(event.getMatrix(), pos, c, lineWidth.getValue());
                }
            }
        }

        spawnTime.keySet().removeIf(p -> camera.distanceToSqr(Vec3.atCenterOf(p)) > maxDistSq * 1.5);
    }
}
