package me.alpha432.oxevy.features.modules.hud;

import me.alpha432.oxevy.event.impl.input.MouseInputEvent;
import me.alpha432.oxevy.event.impl.render.Render2DEvent;
import me.alpha432.oxevy.event.system.Subscribe;
import me.alpha432.oxevy.features.commands.Command;
import me.alpha432.oxevy.features.gui.HudEditorScreen;
import me.alpha432.oxevy.features.settings.Setting;
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
    public final Setting<Boolean> coloredAxes = bool("ColoredAxes", true);
    public final Setting<Boolean> copyButton = bool("CopyButton", true);

    private static final int COLOR_WHITE = 0xFF_FF_FF_FF;

    public CoordinatesHudModule() {
        super("Coordinates", "Display coordinates with direction, biome, light", 180, 80);
    }

    @Override
    public void drawContent(Render2DEvent e) {
        if (nullCheck()) return;

        float x = getX();
        float y = getY();
        GuiGraphics ctx = e.getContext();
        Vec3 pos = mc.player.position();
        BlockPos blockPos = mc.player.blockPosition();
        int chunkX = blockPos.getX() >> 4;
        int chunkZ = blockPos.getZ() >> 4;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        boolean isLeft = x < screenWidth / 2.0f;

        int lineHeight = mc.font.lineHeight;
        float drawY = y;
        int maxWidth = 0;

        if (showDirection.getValue()) {
            String dirLine = "§7▶ §f" + getDirectionString();
            int dirWidth = mc.font.width(dirLine);
            int dirTextX;
            if (isLeft) {
                dirTextX = (int) (x + 2);
            } else {
                dirTextX = (int) (x + getWidth() - 2 - dirWidth);
            }
            ctx.drawString(mc.font, dirLine, dirTextX, (int) drawY, COLOR_WHITE);
            maxWidth = Math.max(maxWidth, dirWidth);
            drawY += lineHeight;
        }

        String xStr = formatCoord('X', pos.x, blockPos.getX(), chunkX, chunkZ);
        String yStr = formatCoord('Y', pos.y, blockPos.getY(), chunkX, chunkZ);
        String zStr = formatCoord('Z', pos.z, blockPos.getZ(), chunkX, chunkZ);
        String coordsLine = xStr + " " + yStr + " " + zStr;
        int coordsWidth = mc.font.width(coordsLine);
        int coordsTextX;
        if (isLeft) {
            coordsTextX = (int) (x + 2);
        } else {
            coordsTextX = (int) (x + getWidth() - 2 - coordsWidth);
        }
        ctx.drawString(mc.font, coordsLine, coordsTextX, (int) drawY, COLOR_WHITE);
        maxWidth = Math.max(maxWidth, coordsWidth);
        drawY += lineHeight;

        String conv = getNetherOverworldLine(blockPos);
        if (conv != null) {
            int convWidth = mc.font.width(conv);
            int convTextX;
            if (isLeft) {
                convTextX = (int) (x + 2);
            } else {
                convTextX = (int) (x + getWidth() - 2 - convWidth);
            }
            ctx.drawString(mc.font, "§7" + conv, convTextX, (int) drawY, 0xFF_AA_AA_AA);
            maxWidth = Math.max(maxWidth, convWidth);
            drawY += lineHeight;
        }

        if (showBiome.getValue()) {
            String biomeLine = "§7⛰ §f" + getBiomeName(blockPos);
            int biomeWidth = mc.font.width(biomeLine);
            int biomeTextX;
            if (isLeft) {
                biomeTextX = (int) (x + 2);
            } else {
                biomeTextX = (int) (x + getWidth() - 2 - biomeWidth);
            }
            ctx.drawString(mc.font, biomeLine, biomeTextX, (int) drawY, COLOR_WHITE);
            maxWidth = Math.max(maxWidth, biomeWidth);
            drawY += lineHeight;
        }

        if (showLight.getValue()) {
            String lightLine = "§7☀ §fLight: §f" + mc.level.getBrightness(LightLayer.BLOCK, blockPos) + " block, §f" + mc.level.getBrightness(LightLayer.SKY, blockPos) + " sky";
            int lightWidth = mc.font.width(lightLine);
            int lightTextX;
            if (isLeft) {
                lightTextX = (int) (x + 2);
            } else {
                lightTextX = (int) (x + getWidth() - 2 - lightWidth);
            }
            ctx.drawString(mc.font, lightLine, lightTextX, (int) drawY, COLOR_WHITE);
            maxWidth = Math.max(maxWidth, lightWidth);
            drawY += lineHeight;
        }

        if (showLocalDifficulty.getValue()) {
            String diffLine = "§7⚔ §fDifficulty: §f" + mc.level.getDifficulty().name();
            int diffWidth = mc.font.width(diffLine);
            int diffTextX;
            if (isLeft) {
                diffTextX = (int) (x + 2);
            } else {
                diffTextX = (int) (x + getWidth() - 2 - diffWidth);
            }
            ctx.drawString(mc.font, diffLine, diffTextX, (int) drawY, COLOR_WHITE);
            maxWidth = Math.max(maxWidth, diffWidth);
            drawY += lineHeight;
        }

        if (copyButton.getValue()) {
            String copyText = "§7[Copy]";
            int copyWidth = mc.font.width(copyText);
            int copyTextX;
            if (isLeft) {
                copyTextX = (int) (x + 2);
            } else {
                copyTextX = (int) (x + getWidth() - 2 - copyWidth);
            }
            boolean hover = isCopyButtonHovering(x, drawY, copyWidth, lineHeight);
            ctx.drawString(mc.font, copyText, copyTextX, (int) drawY, hover ? 0xFF_00_FF_00 : 0xFF_88_88_88);
            maxWidth = Math.max(maxWidth, copyWidth);
            drawY += lineHeight;
        }

        setWidth(Math.max(100, maxWidth + 4));
        setHeight(drawY - y);
    }

    private String formatCoord(char axis, double exact, int block, int chunkX, int chunkZ) {
        String colorCode = coloredAxes.getValue() ? (axis == 'X' ? "§c" : axis == 'Y' ? "§a" : "§b") : "§f";
        return switch (format.getValue()) {
            case Decimal -> colorCode + axis + ": §f" + String.format(Locale.US, "%.2f", exact);
            case Integer -> colorCode + axis + ": §f" + (int) Math.floor(exact);
            case BlockChunk -> axis == 'Y' ? colorCode + "Y: §f" + block : colorCode + axis + ": §f" + block + " §7(ch §f" + (axis == 'X' ? chunkX : chunkZ) + "§7)";
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
        String dim = mc.level.dimension().toString();
        if (dim.contains("the_nether")) return "Overworld: " + (blockPos.getX() * 8) + ", " + (blockPos.getZ() * 8);
        if (dim.contains("overworld")) return "Nether: " + (blockPos.getX() / 8) + ", " + (blockPos.getZ() / 8);
        return null;
    }

    private String getBiomeName(BlockPos pos) { return mc.level.getBiome(pos).unwrapKey().map(ResourceKey::toString).map(s -> s.contains(":") ? s.substring(s.indexOf(':') + 1).replace("]", "") : s).orElse("unknown"); }

    private boolean isCopyButtonHovering(float boxX, float boxY, float w, float h) {
        if (!(mc.screen instanceof HudEditorScreen)) return false;
        return getMouseX() >= boxX && getMouseX() <= boxX + w && getMouseY() >= boxY && getMouseY() <= boxY + h;
    }

    @Subscribe
    public void onMouseClick(MouseInputEvent e) {
        if (e.getAction() != 1 || nullCheck() || !copyButton.getValue()) return;
        int lineHeight = mc.font.lineHeight;
        float copyY = getY() + (showDirection.getValue() ? lineHeight : 0) + lineHeight + (getNetherOverworldLine(mc.player.blockPosition()) != null ? lineHeight : 0) + (showBiome.getValue() ? lineHeight : 0) + (showLight.getValue() ? lineHeight : 0) + (showLocalDifficulty.getValue() ? lineHeight : 0);
        if (!isCopyButtonHovering(getX(), copyY, mc.font.width("[Copy]"), lineHeight)) return;
        mc.keyboardHandler.setClipboard(String.format(Locale.US, "%.2f, %.2f, %.2f", mc.player.getX(), mc.player.getY(), mc.player.getZ()));
        Command.sendMessage("{green}Coordinates copied to clipboard.");
    }
}
