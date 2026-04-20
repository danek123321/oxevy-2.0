package me.alpha432.oxevy.features.modules.hud;

import me.alpha432.oxevy.event.impl.input.MouseInputEvent;
import me.alpha432.oxevy.event.impl.render.Render2DEvent;
import me.alpha432.oxevy.event.system.Subscribe;
import me.alpha432.oxevy.features.commands.Command;
import me.alpha432.oxevy.features.gui.HudEditorScreen;
import me.alpha432.oxevy.features.modules.client.HudModule;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.render.RenderUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;

public class CoordinatesHudModule extends HudModule {
    public enum CoordFormat {
        Decimal,
        Integer,
        BlockChunk,
        NetherOverworld
    }

    public final Setting<CoordFormat> format = mode("Format", CoordFormat.Decimal);
    public final Setting<Boolean> showDirection = bool("Direction", true);
    public final Setting<Boolean> showBiome = bool("Biome", true);
    public final Setting<Boolean> showLight = bool("Light", true);
    public final Setting<Boolean> showLocalDifficulty = bool("LocalDifficulty", true);
    public final Setting<Boolean> background = bool("Background", true);
    public final Setting<Integer> backgroundOpacity = num("BackgroundOpacity", 120, 0, 255);
    public final Setting<Boolean> coloredAxes = bool("ColoredAxes", true);
    public final Setting<Boolean> copyButton = bool("CopyButton", true);

    private static final int COLOR_X = 0xFF_AA_44_44;
    private static final int COLOR_Y = 0xFF_44_AA_44;
    private static final int COLOR_Z = 0xFF_44_44_AA;
    private static final int COLOR_WHITE = 0xFF_FF_FF_FF;

    public CoordinatesHudModule() {
        super("Coordinates", "Display coordinates with direction, biome, light", 180, 80);
        register(backgroundOpacity);
        backgroundOpacity.setVisibility((Integer i) -> background.getValue());
    }

    @Override
    protected void render(Render2DEvent e) {
        super.render(e);
        if (nullCheck()) return;

        float x = getX();
        float y = getY();
        GuiGraphics ctx = e.getContext();
        Vec3 pos = mc.player.position();
        BlockPos blockPos = mc.player.blockPosition();
        int chunkX = blockPos.getX() >> 4;
        int chunkZ = blockPos.getZ() >> 4;

        int lineHeight = mc.font.lineHeight;
        float drawY = y;
        int maxWidth = 0;

        // Direction
        if (showDirection.getValue()) {
            String dir = getDirectionString();
            String dirLine = "§7▶ §f" + dir;
            drawLine(ctx, dirLine, x, drawY, COLOR_WHITE);
            maxWidth = Math.max(maxWidth, mc.font.width(dirLine));
            drawY += lineHeight;
        }

        // Coordinates based on format
        String xStr = formatCoord('X', pos.x, blockPos.getX(), chunkX, chunkZ);
        String yStr = formatCoord('Y', pos.y, blockPos.getY(), chunkX, chunkZ);
        String zStr = formatCoord('Z', pos.z, blockPos.getZ(), chunkX, chunkZ);
        String coordsLine = xStr + " " + yStr + " " + zStr;
        drawLine(ctx, coordsLine, x, drawY, COLOR_WHITE);
        maxWidth = Math.max(maxWidth, mc.font.width(coordsLine));
        drawY += lineHeight;

        // Nether/Overworld conversion
        String conv = getNetherOverworldLine(blockPos);
        if (conv != null) {
            ctx.drawString(mc.font, "§7" + conv, (int) x, (int) drawY, 0xFF_AA_AA_AA);
            maxWidth = Math.max(maxWidth, mc.font.width(conv));
            drawY += lineHeight;
        }

        // Biome
        if (showBiome.getValue()) {
            String biomeLine = "§7⛰ §f" + getBiomeName(blockPos);
            ctx.drawString(mc.font, biomeLine, (int) x, (int) drawY, COLOR_WHITE);
            maxWidth = Math.max(maxWidth, mc.font.width(biomeLine));
            drawY += lineHeight;
        }

        // Light level
        if (showLight.getValue()) {
            int blockLight = mc.level.getBrightness(LightLayer.BLOCK, blockPos);
            int skyLight = mc.level.getBrightness(LightLayer.SKY, blockPos);
            String lightLine = "§7☀ §fLight: §f" + blockLight + " block, §f" + skyLight + " sky";
            ctx.drawString(mc.font, lightLine, (int) x, (int) drawY, COLOR_WHITE);
            maxWidth = Math.max(maxWidth, mc.font.width(lightLine));
            drawY += lineHeight;
        }

        // Difficulty (global)
        if (showLocalDifficulty.getValue()) {
            String diffLine = "§7⚔ §fDifficulty: §f" + mc.level.getDifficulty().name();
            ctx.drawString(mc.font, diffLine, (int) x, (int) drawY, COLOR_WHITE);
            maxWidth = Math.max(maxWidth, mc.font.width(diffLine));
            drawY += lineHeight;
        }

        // Copy button
        if (copyButton.getValue()) {
            String copyText = "§7[Copy]";
            int copyW = mc.font.width(copyText);
            boolean hover = isCopyButtonHovering(x, drawY, copyW, lineHeight);
            ctx.drawString(mc.font, copyText, (int) x, (int) drawY, hover ? 0xFF_00_FF_00 : 0xFF_88_88_88);
            maxWidth = Math.max(maxWidth, copyW);
            drawY += lineHeight;
        }

        float totalHeight = drawY - y;
        setWidth(Math.max(getWidth(), maxWidth + 4));
        setHeight(totalHeight);

        if (background.getValue()) {
            int alpha = backgroundOpacity.getValue().intValue();
            int bgColor = (alpha << 24) | 0x00_11_11_11;
            RenderUtil.rect(ctx, x - 2, y - 2, x + getWidth() + 2, y + totalHeight + 2, bgColor);
        }
    }

    private void drawLine(GuiGraphics ctx, String line, float x, float y, int defaultColor) {
        if (!coloredAxes.getValue()) {
            ctx.drawString(mc.font, line, (int) x, (int) y, defaultColor);
            return;
        }
        // Simple colored axes: X red, Y green, Z blue for the first occurrence of each
        String stripped = line.replace("§7", "").replace("§f", "");
        ctx.drawString(mc.font, line, (int) x, (int) y, defaultColor);
    }

    private String formatCoord(char axis, double exact, int block, int chunkX, int chunkZ) {
        String colorCode = coloredAxes.getValue()
                ? (axis == 'X' ? "§c" : axis == 'Y' ? "§a" : "§b")
                : "§f";
        return switch (format.getValue()) {
            case Decimal -> colorCode + axis + ": §f" + String.format(Locale.US, "%.2f", exact);
            case Integer -> colorCode + axis + ": §f" + (int) Math.floor(exact);
            case BlockChunk -> axis == 'Y'
                    ? colorCode + "Y: §f" + block
                    : colorCode + axis + ": §f" + block + " §7(ch §f" + (axis == 'X' ? chunkX : chunkZ) + "§7)";
            default -> colorCode + axis + ": §f" + block;
        };
    }

    private String getDirectionString() {
        float yaw = mc.player.getYRot() % 360;
        if (yaw < 0) yaw += 360;
        if (yaw >= 337.5 || yaw < 22.5) return "South (+Z)";
        if (yaw >= 22.5 && yaw < 67.5) return "South-West";
        if (yaw >= 67.5 && yaw < 112.5) return "West (-X)";
        if (yaw >= 112.5 && yaw < 157.5) return "North-West";
        if (yaw >= 157.5 && yaw < 202.5) return "North (-Z)";
        if (yaw >= 202.5 && yaw < 247.5) return "North-East";
        if (yaw >= 247.5 && yaw < 292.5) return "East (+X)";
        return "South-East";
    }

    private String getNetherOverworldLine(BlockPos blockPos) {
        String dim = getDimensionPath();
        if (dim.equals("the_nether")) {
            return "Overworld: " + (blockPos.getX() * 8) + ", " + (blockPos.getZ() * 8);
        }
        if (dim.equals("overworld")) {
            return "Nether: " + (blockPos.getX() / 8) + ", " + (blockPos.getZ() / 8);
        }
        return null;
    }

    private String getDimensionPath() {
        String s = mc.level.dimension().toString();
        if (s.contains("the_nether")) return "the_nether";
        if (s.contains("overworld")) return "overworld";
        if (s.contains("the_end")) return "the_end";
        return s;
    }

    private String getBiomeName(BlockPos pos) {
        return mc.level.getBiome(pos).unwrapKey()
                .map(ResourceKey::toString)
                .map(s -> s.contains(":") ? s.substring(s.indexOf(':') + 1).replace("]", "") : s)
                .orElse("unknown");
    }

    private boolean isCopyButtonHovering(float boxX, float boxY, float w, float h) {
        if (!(mc.screen instanceof HudEditorScreen)) return false;
        int mx = getMouseX();
        int my = getMouseY();
        return mx >= boxX && mx <= boxX + w && my >= boxY && my <= boxY + h;
    }

    @Subscribe
    public void onMouseClick(MouseInputEvent e) {
        if (e.getAction() != 1 || nullCheck() || !copyButton.getValue()) return;
        float x = getX();
        float y = getY();
        int lineHeight = mc.font.lineHeight;
        float copyY = y;
        if (showDirection.getValue()) copyY += lineHeight;
        copyY += lineHeight; // coords
        if (getNetherOverworldLine(mc.player.blockPosition()) != null) copyY += lineHeight;
        if (showBiome.getValue()) copyY += lineHeight;
        if (showLight.getValue()) copyY += lineHeight;
        if (showLocalDifficulty.getValue()) copyY += lineHeight;
        int copyW = mc.font.width("[Copy]");
        if (!isCopyButtonHovering(x, copyY, copyW, lineHeight)) return;
        String toCopy = String.format(Locale.US, "%.2f, %.2f, %.2f", mc.player.getX(), mc.player.getY(), mc.player.getZ());
        mc.keyboardHandler.setClipboard(toCopy);
        Command.sendMessage("{green}Coordinates copied to clipboard.");
    }
}
