package me.alpha432.oxevy.features.modules.render;

import me.alpha432.oxevy.event.impl.render.Render3DEvent;
import me.alpha432.oxevy.event.system.Subscribe;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.render.RenderUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ItemESPModule extends Module {
    
    public Setting<EspStyle> style = mode("Style", EspStyle.BOTH);
    public Setting<Boolean> showBoxes = bool("Boxes", true);
    public Setting<Boolean> showLines = bool("Lines", true);
    public Setting<BoxSize> boxSize = mode("BoxSize", BoxSize.ACCURATE);
    public Setting<Color> color = color("Color", 255, 255, 0, 180);
    public Setting<Float> lineWidth = num("LineWidth", 1.5f, 0.5f, 3f);
    public Setting<Boolean> throughWalls = bool("ThroughWalls", true);
    
    private final List<ItemEntity> items = new ArrayList<>();
    
    public ItemESPModule() {
        super("ItemESP", "Highlights dropped items with boxes and tracers", Category.RENDER);
    }
    
    @Subscribe
    public void onRender3D(Render3DEvent event) {
        items.clear();
        
        if (mc.level == null || mc.player == null) return;
        
        for (var entity : mc.level.entitiesForRendering()) {
            if (entity instanceof ItemEntity item) {
                items.add(item);
            }
        }
        
        double extraSize = switch (boxSize.getValue()) {
            case ACCURATE -> 0.0;
            case FANCY -> 0.1;
        };
        
        boolean hasBoxes = showBoxes.getValue() && style.getValue() != EspStyle.LINES;
        boolean hasLines = showLines.getValue() && style.getValue() != EspStyle.BOXES;
        
        int lineColor = color.getValue().getRGB() & 0x00FFFFFF | 0x80000000;
        
        if (hasBoxes) {
            List<AABB> boxes = new ArrayList<>(items.size());
            for (ItemEntity item : items) {
                AABB box = getLerpedBox(item, event.getDelta());
                if (extraSize != 0) box = box.move(0, extraSize, 0).inflate(extraSize);
                boxes.add(box);
            }
            RenderUtil.drawOutlinedBoxes(event.getMatrix(), boxes, lineColor, throughWalls.getValue(), lineWidth.getValue());
        }
        
        if (hasLines) {
            Vec3 start = mc.player.getEyePosition(event.getDelta());
            List<Vec3> ends = new ArrayList<>(items.size());
            
            for (ItemEntity item : items) {
                AABB box = getLerpedBox(item, event.getDelta());
                ends.add(box.getCenter());
            }
            
            RenderUtil.drawTracers(event.getMatrix(), event.getDelta(), ends, lineColor, lineWidth.getValue(), throughWalls.getValue());
        }
    }
    
    private AABB getLerpedBox(ItemEntity entity, float delta) {
        double x = Mth.lerp(delta, entity.xo, entity.getX());
        double y = Mth.lerp(delta, entity.yo, entity.getY());
        double z = Mth.lerp(delta, entity.zo, entity.getZ());
        
        double size = 0.3;
        return new AABB(x - size, y, z - size, x + size, y + size * 2, z + size);
    }
    
    public enum EspStyle {
        BOXES,
        LINES,
        BOTH
    }
    
    public enum BoxSize {
        ACCURATE,
        FANCY
    }
}
