package me.alpha432.oxevy.features.modules.render;

import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.event.impl.render.Render2DEvent;
import me.alpha432.oxevy.event.impl.render.Render3DEvent;
import me.alpha432.oxevy.event.system.Subscribe;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.render.NametagUtils;
import me.alpha432.oxevy.util.render.RenderUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ESPModule extends Module {

    public Setting<Mode> mode = mode("Mode", Mode.Box);
    public Setting<Boolean> highlightTarget = bool("HighlightTarget", false);
    public Setting<Boolean> ignoreSelf = bool("IgnoreSelf", true);
    public Setting<Color> playersColor = color("PlayersColor", 255, 255, 255, 255);
    public Setting<Color> animalsColor = color("AnimalsColor", 25, 255, 25, 255);
    public Setting<Color> monstersColor = color("MonstersColor", 255, 25, 25, 255);
    public Setting<Color> miscColor = color("MiscColor", 175, 175, 175, 255);
    public Setting<Color> targetColor = color("TargetColor", 200, 200, 200, 255);
    public Setting<Double> fadeDistance = num("FadeDistance", 3.0, 0.0, 12.0);
    public Setting<Double> fillOpacity = num("FillOpacity", 0.3, 0.0, 1.0);

    private final Vector3d pos1 = new Vector3d();
    private final Vector3d pos2 = new Vector3d();
    private final Vector3d pos = new Vector3d();
    private int count;

    public ESPModule() {
        super("ESP", "Renders entities through walls", Category.RENDER);
    }

    @Subscribe
    public void onRender3D(Render3DEvent event) {
        if (mc.level == null || mc.player == null) return;
        if (mode.getValue() == Mode._2D) return;

        count = 0;

        Entity target = null;
        if (highlightTarget.getValue() && mc.hitResult != null) {
            var hit = mc.hitResult;
            if (hit.getType() == net.minecraft.world.phys.HitResult.Type.ENTITY) {
                target = ((net.minecraft.world.phys.EntityHitResult) hit).getEntity();
            }
        }

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (target != entity && shouldSkip(entity)) continue;

            Color color = getColor(entity);
            if (color == null) continue;

            if (mode.getValue() == Mode.Box || mode.getValue() == Mode.Wireframe) {
                drawBox(event, entity, color, target == entity);
            }

            count++;
        }
    }

    @Override
    public void onRender2D(Render2DEvent event) {
        if (mc.level == null || mc.player == null) return;
        if (mode.getValue() != Mode._2D) return;

        count = 0;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (shouldSkip(entity)) continue;

            Color color = getColor(entity);
            if (color == null) continue;

            AABB box = getLerpedBox(entity, event.getDelta());

            pos1.set(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
            pos2.set(0, 0, 0);

            if (checkCorner(box.minX, box.minY, box.minZ)) continue;
            if (checkCorner(box.maxX, box.minY, box.minZ)) continue;
            if (checkCorner(box.minX, box.minY, box.maxZ)) continue;
            if (checkCorner(box.maxX, box.minY, box.maxZ)) continue;
            if (checkCorner(box.minX, box.maxY, box.minZ)) continue;
            if (checkCorner(box.maxX, box.maxY, box.minZ)) continue;
            if (checkCorner(box.minX, box.maxY, box.maxZ)) continue;
            if (checkCorner(box.maxX, box.maxY, box.maxZ)) continue;

            int sideColor = new Color(color.getRed(), color.getGreen(), color.getBlue(),
                    (int) (color.getAlpha() * fillOpacity.getValue())).getRGB();
            int lineColor = color.getRGB();

            if (sideColor >>> 24 > 0) {
                event.getContext().fill((int) pos1.x, (int) pos1.y,
                        (int) pos2.x, (int) pos2.y, sideColor);
            }

            event.getContext().fill((int) pos1.x, (int) pos1.y, (int) pos2.x, (int) pos1.y + 1, lineColor);
            event.getContext().fill((int) pos1.x, (int) pos2.y, (int) pos2.x, (int) pos2.y + 1, lineColor);
            event.getContext().fill((int) pos1.x, (int) pos1.y, (int) pos1.x + 1, (int) pos2.y, lineColor);
            event.getContext().fill((int) pos2.x, (int) pos1.y, (int) pos2.x + 1, (int) pos2.y, lineColor);

            count++;
        }
    }

    private void drawBox(Render3DEvent event, Entity entity, Color color, boolean isTarget) {
        AABB box = getLerpedBox(entity, event.getDelta());
        Color line = isTarget ? targetColor.getValue() : color;
        Color fill = new Color(color.getRed(), color.getGreen(), color.getBlue(),
                (int) (color.getAlpha() * fillOpacity.getValue()));

        if (mode.getValue() == Mode.Box) {
            RenderUtil.drawSolidBoxes(event.getMatrix(), java.util.List.of(box), fill.getRGB(), true);
            RenderUtil.drawOutlinedBoxes(event.getMatrix(), java.util.List.of(box), line.getRGB(), true);
        } else {
            RenderUtil.drawOutlinedBoxes(event.getMatrix(), java.util.List.of(box), line.getRGB(), true);
        }
    }

    private AABB getLerpedBox(Entity entity, float delta) {
        double x = Mth.lerp(delta, entity.xo, entity.getX());
        double y = Mth.lerp(delta, entity.yo, entity.getY());
        double z = Mth.lerp(delta, entity.zo, entity.getZ());
        double w = entity.getBbWidth() / 2;
        double h = entity.getBbHeight();
        return new AABB(x - w, y, z - w, x + w, y + h, z + w);
    }

    private boolean checkCorner(double x, double y, double z) {
        pos.set(x, y, z);
        if (!NametagUtils.to2D(pos, 1)) return true;

        if (pos.x < pos1.x) pos1.x = pos.x;
        if (pos.y < pos1.y) pos1.y = pos.y;
        if (pos.z < pos1.z) pos1.z = pos.z;

        if (pos.x > pos2.x) pos2.x = pos.x;
        if (pos.y > pos2.y) pos2.y = pos.y;
        if (pos.z > pos2.z) pos2.z = pos.z;

        return false;
    }

    private boolean shouldSkip(Entity entity) {
        if (entity == mc.player && ignoreSelf.getValue()) return true;
        if (entity == mc.getCameraEntity() && mc.options.getCameraType().isFirstPerson()) return true;
        double dist = mc.player.distanceToSqr(entity.position());
        double renderDist = Math.pow(mc.options.renderDistance().get() * 16 + fadeDistance.getValue(), 2);
        return dist > renderDist;
    }

    private Color getColor(Entity entity) {
        double dist = mc.player.distanceTo(entity);
        double fadeDist = fadeDistance.getValue();
        double alpha = 1;
        if (dist <= fadeDist) alpha = dist / fadeDist;
        if (alpha <= 0.075) return null;

        Color baseColor;
        if (entity instanceof Player player) {
            if (Oxevy.friendManager.isFriend(player)) {
                baseColor = new Color(0, 255, 255);
            } else {
                baseColor = playersColor.getValue();
            }
        } else {
            EntityType<?> type = entity.getType();
            if (type == EntityType.COW || type == EntityType.PIG || type == EntityType.SHEEP ||
                    type == EntityType.CHICKEN || type == EntityType.HORSE) {
                baseColor = animalsColor.getValue();
            } else if (type == EntityType.ZOMBIE || type == EntityType.SKELETON ||
                    type == EntityType.CREEPER || type == EntityType.SPIDER) {
                baseColor = monstersColor.getValue();
            } else {
                baseColor = miscColor.getValue();
            }
        }

        return new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(),
                (int) (baseColor.getAlpha() * alpha));
    }

    @Override
    public String getDisplayInfo() {
        return Integer.toString(count);
    }

    public enum Mode {
        Box,
        Wireframe,
        _2D
    }
}
