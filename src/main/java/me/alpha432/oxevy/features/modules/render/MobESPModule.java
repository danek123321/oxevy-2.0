package me.alpha432.oxevy.features.modules.render;

import me.alpha432.oxevy.event.impl.render.Render3DEvent;
import me.alpha432.oxevy.event.system.Subscribe;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.render.RenderUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

public class MobESPModule extends Module {
    
    public Setting<EspStyle> style = mode("Style", EspStyle.BOTH);
    public Setting<Boolean> showBoxes = bool("Boxes", true);
    public Setting<Boolean> showLines = bool("Lines", true);
    public Setting<BoxSize> boxSize = mode("BoxSize", BoxSize.ACCURATE);
    public Setting<Boolean> filterHostile = bool("FilterHostile", false);
    public Setting<Boolean> filterNeutral = bool("FilterNeutral", false);
    public Setting<Boolean> filterPassive = bool("FilterPassive", false);
    public Setting<Boolean> filterInvisible = bool("FilterInvisible", false);
    public Setting<Boolean> filterNamed = bool("FilterNamed", false);
    public Setting<Float> lineWidth = num("LineWidth", 1.5f, 0.5f, 3f);
    public Setting<Boolean> throughWalls = bool("ThroughWalls", true);
    
    private final List<LivingEntity> mobs = new ArrayList<>();
    
    public MobESPModule() {
        super("MobESP", "Highlights mobs with boxes and tracers", Category.RENDER);
    }
    
    @Subscribe
    public void onRender3D(Render3DEvent event) {
        mobs.clear();
        
        if (mc.level == null || mc.player == null) return;
        
        StreamSupport.stream(mc.level.entitiesForRendering().spliterator(), false)
            .filter(LivingEntity.class::isInstance)
            .map(e -> (LivingEntity) e)
            .filter(e -> !(e instanceof Player))
            .filter(e -> !e.isRemoved() && e.getHealth() > 0)
            .filter(e -> !filterHostile.getValue() || !isHostile(e))
            .filter(e -> !filterNeutral.getValue() || !isNeutral(e))
            .filter(e -> !filterPassive.getValue() || !isPassive(e))
            .filter(e -> !filterInvisible.getValue() || !e.isInvisible())
            .filter(e -> !filterNamed.getValue() || !e.hasCustomName())
            .forEach(mobs::add);
        
        double extraSize = switch (boxSize.getValue()) {
            case ACCURATE -> 0.0;
            case FANCY -> 0.02;
        };
        
        boolean hasBoxes = showBoxes.getValue() && style.getValue() != EspStyle.LINES;
        boolean hasLines = showLines.getValue() && style.getValue() != EspStyle.BOXES;
        
        if (hasBoxes) {
            List<AABB> boxes = new ArrayList<>(mobs.size());
            for (LivingEntity entity : mobs) {
                AABB box = getLerpedBox(entity, event.getDelta());
                if (extraSize != 0) box = box.move(0, extraSize, 0).inflate(extraSize);
                boxes.add(box);
            }
            RenderUtil.drawOutlinedBoxes(event.getMatrix(), boxes, 0x80FF6600, throughWalls.getValue(), lineWidth.getValue());
        }
        
        if (hasLines) {
            Vec3 start = mc.player.getEyePosition(event.getDelta());
            List<Vec3> ends = new ArrayList<>(mobs.size());
            
            for (LivingEntity entity : mobs) {
                AABB box = getLerpedBox(entity, event.getDelta());
                ends.add(box.getCenter());
            }
            
            int color = getMobColor();
            RenderUtil.drawTracers(event.getMatrix(), event.getDelta(), ends, color, lineWidth.getValue(), throughWalls.getValue());
        }
    }
    
    private AABB getLerpedBox(LivingEntity entity, float delta) {
        double x = Mth.lerp(delta, entity.xo, entity.getX());
        double y = Mth.lerp(delta, entity.yo, entity.getY());
        double z = Mth.lerp(delta, entity.zo, entity.getZ());
        
        double width = entity.getBbWidth();
        double height = entity.getBbHeight();
        
        return new AABB(
            x - width / 2,
            y,
            z - width / 2,
            x + width / 2,
            y + height,
            z + width / 2
        );
    }
    
    private boolean isHostile(LivingEntity entity) {
        String name = entity.getType().toString().toLowerCase();
        return name.contains("zombie") || name.contains("skeleton") || name.contains("spider") 
            || name.contains("creeper") || name.contains("enderman") || name.contains("ghast")
            || name.contains("blaze") || name.contains("wither") || name.contains("phantom")
            || name.contains("shulker") || name.contains("warden");
    }
    
    private boolean isNeutral(LivingEntity entity) {
        String name = entity.getType().toString().toLowerCase();
        return name.contains("wolf") || name.contains("piglin") || name.contains("zombifiedpiglin")
            || name.contains("polar_bear") || name.contains("bee") || name.contains("llama")
            || name.contains("TraderLlama") || name.contains("Dolphin") || name.contains("Goat");
    }
    
    private boolean isPassive(LivingEntity entity) {
        String name = entity.getType().toString().toLowerCase();
        return name.contains("cow") || name.contains("sheep") || name.contains("pig") 
            || name.contains("chicken") || name.contains("rabbit") || name.contains("villager")
            || name.contains("mooshroom") || name.contains("horse") || name.contains("cat")
            || name.contains("parrot") || name.contains("turtle") || name.contains("axolotl")
            || name.contains("frog") || name.contains("allay") || name.contains("bat");
    }
    
    private int getMobColor() {
        if (mobs.isEmpty()) return 0x8000FF00;
        float dist = (float) mc.player.distanceTo(mobs.get(0));
        float f = dist / 20f;
        float r = Mth.clamp(2 - f, 0, 1);
        float g = Mth.clamp(f, 0, 1);
        return (int) (r * 255) << 16 | (int) (g * 255) << 8 | 0x800000FF;
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
