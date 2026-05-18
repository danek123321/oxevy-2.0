package me.alpha432.oxevy.features.modules.render;

import me.alpha432.oxevy.event.impl.render.Render3DEvent;
import me.alpha432.oxevy.event.system.Subscribe;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.render.RenderUtil;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class PortalESPModule extends Module {
    
    public Setting<Boolean> showBoxes = bool("Boxes", true);
    public Setting<Boolean> showLines = bool("Lines", true);
    public Setting<Color> color = color("Color", 200, 100, 255, 180);
    public Setting<Float> lineWidth = num("LineWidth", 1.5f, 0.5f, 3f);
    public Setting<Boolean> throughWalls = bool("ThroughWalls", true);
    
    private final List<Vec3> portals = new ArrayList<>();
    
    public PortalESPModule() {
        super("PortalESP", "Highlights nether portals", Category.RENDER);
    }
    
    @Subscribe
    public void onRender3D(Render3DEvent event) {
        portals.clear();
        
        if (mc.level == null || mc.player == null) return;
        
        int cx = (int) mc.player.getX() >> 4;
        int cz = (int) mc.player.getZ() >> 4;
        int range = 32;
        int r = range / 16 + 1;
        
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                var chunk = mc.level.getChunkSource().getChunk(cx + dx, cz + dz, false);
                if (chunk == null) continue;
                
                for (var entry : chunk.getBlockEntities().entrySet()) {
                    var pos = entry.getKey();
                    var be = entry.getValue();
                    
                    if (be.getBlockState().getBlock() == Blocks.NETHER_PORTAL) {
                        portals.add(new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
                    }
                }
            }
        }
        
        if (!portals.isEmpty()) {
            int boxColor = new Color(color.getValue().getRed(), color.getValue().getGreen(), 
                color.getValue().getBlue(), 80).getRGB();
            int lineColor = new Color(color.getValue().getRed(), color.getValue().getGreen(), 
                color.getValue().getBlue(), 200).getRGB();
            
            if (showBoxes.getValue()) {
                List<AABB> boxes = new ArrayList<>();
                for (Vec3 pos : portals) {
                    boxes.add(new AABB(pos.x - 1, pos.y - 1, pos.z - 1, pos.x + 1, pos.y + 2, pos.z + 1));
                }
                RenderUtil.drawOutlinedBoxes(event.getMatrix(), boxes, lineColor, throughWalls.getValue());
            }
            
            if (showLines.getValue()) {
                List<Vec3> ends = new ArrayList<>(portals);
                RenderUtil.drawTracers(event.getMatrix(), event.getDelta(), ends, lineColor, lineWidth.getValue(), throughWalls.getValue());
            }
        }
    }
}