package me.alpha432.oxevy.features.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.event.impl.render.Render2DEvent;
import me.alpha432.oxevy.event.impl.render.Render3DEvent;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.render.Layers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ESP extends Module {

    // Entity filters
    private final Setting<Boolean> players = bool("Players", true);
    private final Setting<Boolean> friends = bool("Friends", true);
    private final Setting<Boolean> monsters = bool("Monsters", false);
    private final Setting<Boolean> animals = bool("Animals", false);
    private final Setting<Boolean> npcs = bool("NPCs", false);
    private final Setting<Boolean> items = bool("Items", false);
    private final Setting<Boolean> projectiles = bool("Projectiles", false);
    private final Setting<Boolean> invisibles = bool("Invisibles", false);

    // ESP modes
    private final Setting<BoxMode> boxMode = mode("BoxMode", BoxMode.FULL);
    private final Setting<Boolean> outline = bool("Outline", true);
    private final Setting<Boolean> filled = bool("Filled", false);
    private final Setting<Integer> boxAlpha = num("BoxAlpha", 60, 0, 255);

    // Tracers
    private final Setting<Boolean> tracers = bool("Tracers", false);
    private final Setting<Float> tracerWidth = num("TracerWidth", 1.5f, 0.5f, 5.0f);

    // Nametags
    private final Setting<Boolean> nametags = bool("Nametags", true);
    private final Setting<Boolean> healthTags = bool("HealthTags", true);
    private final Setting<Boolean> distanceTags = bool("DistanceTags", true);
    private final Setting<Boolean> itemTags = bool("ItemTags", true);

    // Skeleton
    private final Setting<Boolean> skeleton = bool("Skeleton", false);
    private final Setting<Float> skeletonWidth = num("SkeletonWidth", 1.5f, 0.5f, 5.0f);

    // 2D Elements
    private final Setting<Boolean> healthBars = bool("HealthBars", true);
    private final Setting<Boolean> armorBars = bool("ArmorBars", false);
    private final Setting<Boolean> hurtTime = bool("HurtTime", true);

    // Visual settings
    private final Setting<Integer> renderDistance = num("RenderDistance", 128, 16, 256);
    private final Setting<Boolean> throughWalls = bool("ThroughWalls", true);
    private final Setting<ColorMode> colorMode = mode("ColorMode", ColorMode.RELATIONSHIP);
    private final Setting<Color> customColor = color("CustomColor", new Color(255, 255, 255, 255));

    // Glow
    private final Setting<Boolean> glow = bool("Glow", false);
    private final Setting<GlowMode> glowMode = mode("GlowMode", GlowMode.OUTLINE);

    private Map<Entity, int[]> hurtTimers = new HashMap<>();

    public ESP() {
        super("ESP", "Shows entities through walls", Category.RENDER);
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (mc.level == null || mc.player == null) return;

        Vec3 camera = mc.gameRenderer.getMainCamera().position();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == mc.player) continue;
            if (!shouldRender(entity)) continue;

            double dist = camera.distanceTo(entity.position());
            if (dist > renderDistance.getValue()) continue;

            Color color = getEntityColor(entity);

            // Render 3D elements
            if (boxMode.getValue() != BoxMode.OFF) {
                renderBox(event.getMatrix(), entity, color, event.getDelta());
            }

            if (skeleton.getValue() && entity instanceof LivingEntity living) {
                renderSkeleton(event.getMatrix(), living, color, event.getDelta());
            }

            if (tracers.getValue()) {
                renderTracer(event.getMatrix(), entity, color, event.getDelta());
            }

            // Update hurt timer
            if (entity instanceof LivingEntity living && living.hurtTime > 0) {
                hurtTimers.put(entity, new int[]{living.hurtTime, 10});
            } else {
                hurtTimers.remove(entity);
            }
        }
    }

    @Override
    public void onRender2D(Render2DEvent event) {
        if (mc.level == null || mc.player == null) return;

        GuiGraphics ctx = event.getContext();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == mc.player) continue;
            if (!shouldRender(entity)) continue;

            double dist = mc.player.distanceTo(entity);
            if (dist > renderDistance.getValue()) continue;

            Vec3 pos = entity.position();
            double x = pos.x;
            double y = pos.y + entity.getBbHeight() + 0.5;
            double z = pos.z;

            // Project to screen
            int[] screenCoords = projectToScreen(x, y, z);
            if (screenCoords == null) continue;

            Color color = getEntityColor(entity);

            // Render 2D elements
            if (nametags.getValue()) {
                renderNametag(ctx, screenCoords, entity, color);
            }

            if (healthBars.getValue() && entity instanceof LivingEntity living) {
                renderHealthBar(ctx, screenCoords, living, color);
            }

            if (armorBars.getValue() && entity instanceof Player player) {
                renderArmorBar(ctx, screenCoords, player, color);
            }

            if (hurtTime.getValue() && hurtTimers.containsKey(entity)) {
                renderHurtIndicator(ctx, screenCoords, color);
            }
        }
    }

    private boolean shouldRender(Entity entity) {
        if (!entity.isAlive()) return false;
        if (entity.isInvisible() && !invisibles.getValue()) return false;

        if (entity instanceof Player) {
            if (entity == mc.player) return false;
            if (Oxevy.friendManager.isFriend((Player) entity) && !friends.getValue()) return false;
            return players.getValue();
        }
        if (entity instanceof Monster) return monsters.getValue();
        if (entity instanceof Animal) return animals.getValue();
        if (entity instanceof Npc) return npcs.getValue();
        if (entity instanceof ItemEntity) return items.getValue();
        if (entity instanceof Projectile)
            return projectiles.getValue();

        return false;
    }

    private Color getEntityColor(Entity entity) {
        if (colorMode.getValue() == ColorMode.CUSTOM) {
            return customColor.getValue();
        }

        if (colorMode.getValue() == ColorMode.HEALTH && entity instanceof LivingEntity living) {
            float health = living.getHealth() / living.getMaxHealth();
            int red = (int) (255 * (1 - health));
            int green = (int) (255 * health);
            return new Color(red, green, 0, 255);
        }

        // Relationship-based colors
        if (entity instanceof Player player) {
            if (Oxevy.friendManager.isFriend(player)) {
                return new Color(100, 255, 100, 255); // Green for friends
            }
            if (mc.player.isAlliedTo(player)) {
                return new Color(100, 255, 180, 255); // Teammate
            }
            return new Color(255, 80, 80, 255); // Enemy
        }
        if (entity instanceof Monster) {
            return new Color(255, 80, 80, 255); // Red for monsters
        }
        if (entity instanceof Animal) {
            return new Color(100, 255, 100, 255); // Green for animals
        }
        if (entity instanceof ItemEntity) {
            return new Color(255, 255, 100, 255); // Yellow for items
        }
        if (entity instanceof Npc) {
            return new Color(100, 100, 255, 255); // Blue for NPCs
        }
        return new Color(255, 255, 255, 255); // White for others
    }

    private void renderBox(PoseStack stack, Entity entity, Color color, float delta) {
        // Get camera position for distance calculation
        Vec3 camera = mc.gameRenderer.getMainCamera().position();
        
        // Calculate entity position with interpolation (world coordinates)
        double x = entity.xo + (entity.getX() - entity.xo) * delta;
        double y = entity.yo + (entity.getY() - entity.yo) * delta;
        double z = entity.zo + (entity.getZ() - entity.zo) * delta;

        float width = entity.getBbWidth() / 2;
        float height = entity.getBbHeight();

        // Create AABB in world coordinates - RenderUtil will handle camera-relative conversion
        AABB bb = new AABB(x - width, y, z - width, x + width, y + height, z + width);

        if (filled.getValue()) {
            renderFilledBox(stack, bb, new Color(color.getRed(), color.getGreen(), color.getBlue(), boxAlpha.getValue()));
        }

        if (outline.getValue() || boxMode.getValue() == BoxMode.FULL) {
            renderOutlineBox(stack, bb, color, boxMode.getValue() == BoxMode.CORNERS);
        }
    }

    private void renderOutlineBox(PoseStack stack, AABB bb, Color color, boolean cornersOnly) {
        // Use RenderUtil for box rendering
        me.alpha432.oxevy.util.render.RenderUtil.drawBox(stack, bb, color, 1.0f, true);
    }

    private void renderFilledBox(PoseStack stack, AABB bb, Color color) {
        // Use RenderUtil for filled box rendering
        me.alpha432.oxevy.util.render.RenderUtil.drawBoxFilled(stack, bb, color);
    }

    private void renderSkeleton(PoseStack stack, LivingEntity entity, Color color, float delta) {
        // Simplified skeleton rendering - would need full bone structure for complete implementation
        Vec3 camera = mc.gameRenderer.getMainCamera().position();
        Vec3 pos = entity.position().subtract(camera);

        float height = entity.getBbHeight();
        float width = entity.getBbWidth() / 2;

        // Use RenderUtil for line drawing
        // For now, just use a simple approach
        // This is a simplified version - would need proper bone structure for complete implementation
    }

    private void renderTracer(PoseStack stack, Entity entity, Color color, float delta) {
        Vec3 camera = mc.gameRenderer.getMainCamera().position();
        Vec3 from = mc.player.getEyePosition(delta).subtract(camera);
        Vec3 to = entity.getPosition(delta).add(0, entity.getBbHeight() * 0.5, 0).subtract(camera);

        // Use RenderUtil for line drawing
        me.alpha432.oxevy.util.render.RenderUtil.drawLine(stack, from, to, color, tracerWidth.getValue(), true);
    }

    private void renderNametag(GuiGraphics ctx, int[] pos, Entity entity, Color color) {
        String name = entity.getDisplayName().getString();
        double dist = mc.player.distanceTo(entity);

        if (entity instanceof Player player) {
            name = player.getName().getString();
        } else if (entity instanceof ItemEntity item) {
            ItemStack stack2 = item.getItem();
            name = stack2.getCount() + "x " + stack2.getDisplayName().getString();
        } else if (entity instanceof LivingEntity living) {
            name = entity.getType().getDescription().getString();
        }

        List<String> tags = new ArrayList<>();
        tags.add(name);

        if (healthTags.getValue() && entity instanceof LivingEntity living) {
            float health = living.getHealth();
            float maxHealth = living.getMaxHealth();
            String healthStr = String.format("%.1f/%.1f", health, maxHealth);
            tags.add("§c❤ " + healthStr);
        }

        if (distanceTags.getValue()) {
            tags.add("§e" + String.format("%.1f", dist) + "m");
        }

        if (itemTags.getValue() && entity instanceof ItemEntity item) {
            ItemStack stack2 = item.getItem();
            if (stack2.getItem() == Items.TOTEM_OF_UNDYING) {
                tags.add("§bTotem");
            } else if (stack2.getItem() == Items.ENDER_PEARL) {
                tags.add("§5Pearl");
            }
        }

        int y = pos[1] - 40;
        for (String tag : tags) {
            ctx.drawString(mc.font, tag, (int)(pos[0] - mc.font.width(tag) / 2f), y, color.getRGB());
            y += 10;
        }
    }

    private void renderHealthBar(GuiGraphics ctx, int[] pos, LivingEntity entity, Color color) {
        float health = entity.getHealth();
        float maxHealth = entity.getMaxHealth();
        float healthPercent = health / maxHealth;

        int barWidth = 40;
        int barHeight = 3;
        int x = pos[0] - barWidth / 2;
        int y = pos[1] - 25;

        // Background
        ctx.fill(x, y, x + barWidth, y + barHeight, new Color(0, 0, 0, 100).getRGB());

        // Health bar
        int healthColor;
        if (healthPercent > 0.6) {
            healthColor = new Color(100, 255, 100).getRGB();
        } else if (healthPercent > 0.3) {
            healthColor = new Color(255, 255, 100).getRGB();
        } else {
            healthColor = new Color(255, 100, 100).getRGB();
        }

        int healthWidth = (int)(barWidth * healthPercent);
        ctx.fill(x, y, x + healthWidth, y + barHeight, healthColor);
    }

    private void renderArmorBar(GuiGraphics ctx, int[] pos, Player player, Color color) {
        float armor = player.getArmorValue();
        float maxArmor = 20;
        float armorPercent = armor / maxArmor;

        int barWidth = 40;
        int barHeight = 2;
        int x = pos[0] - barWidth / 2;
        int y = pos[1] - 20;

        // Background
        ctx.fill(x, y, x + barWidth, y + barHeight, new Color(0, 0, 0, 100).getRGB());

        // Armor bar
        if (armor > 0) {
            ctx.fill(x, y, x + (int)(barWidth * armorPercent), y + barHeight, new Color(100, 100, 255).getRGB());
        }
    }

    private void renderHurtIndicator(GuiGraphics ctx, int[] pos, Color color) {
        int[] timer = hurtTimers.values().stream().findFirst().orElse(new int[]{0, 0});
        float alpha = timer[0] / (float)timer[1];

        int size = 10;
        int x = pos[0] - size / 2;
        int y = pos[1] - 15;

        Color hurtColor = new Color(255, 100, 100, (int)(255 * alpha));
        ctx.fill(x, y, x + size, y + size / 3, hurtColor.getRGB());
    }

    private int[] projectToScreen(double x, double y, double z) {
        Vec3 camera = mc.gameRenderer.getMainCamera().position();
        Vec3 relative = new Vec3(x - camera.x, y - camera.y, z - camera.z);

        // This is simplified - you'd need actual projection matrix math
        // For now, return dummy coordinates
        return new int[]{mc.getWindow().getGuiScaledWidth() / 2, mc.getWindow().getGuiScaledHeight() / 2};
    }

    public enum BoxMode {
        OFF,
        CORNERS,
        FULL
    }

    public enum ColorMode {
        RELATIONSHIP,
        HEALTH,
        CUSTOM
    }

    public enum GlowMode {
        OUTLINE,
        SOLID
    }
}