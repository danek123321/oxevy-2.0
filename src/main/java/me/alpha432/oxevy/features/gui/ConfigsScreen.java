package me.alpha432.oxevy.features.gui;

import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.features.modules.client.ClickGuiModule;
import me.alpha432.oxevy.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ConfigsScreen extends Screen {
    private static final Minecraft mc = Minecraft.getInstance();

    private static final int WIDTH = 360;
    private static final int HEIGHT = 260;

    private int scrollOffset = 0;
    private int maxScroll = 0;
    private String message = "";
    private int messageTimer = 0;

    private final List<ConfigEntry> entries = new ArrayList<>();

    public ConfigsScreen() {
        super(Component.literal("Config Manager"));
        refreshEntries();
    }

    private void refreshEntries() {
        entries.clear();
        Path cfg = Oxevy.configManager.getConfigFolder();
        entries.add(new ConfigEntry("modules.json", cfg.resolve("modules.json"), true));
        entries.add(new ConfigEntry("commands.json", cfg.resolve("commands.json"), true));
        entries.add(new ConfigEntry("friends.json", cfg.resolve("friends.json"), true));
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        int x = (context.guiWidth() - WIDTH) / 2;
        int y = (context.guiHeight() - HEIGHT) / 2;

        context.fill(0, 0, context.guiWidth(), context.guiHeight(), new Color(0, 0, 0, 150).getRGB());

        RenderUtil.rect(context, x, y, x + WIDTH, y + HEIGHT, 0xFF121212);
        RenderUtil.rect(context, x, y, x + WIDTH, y + 2, ClickGuiModule.getInstance().color.getValue().getRGB());

        context.drawString(mc.font, "Config Manager", x + 15, y + 12, 0xFFAAAAAA);
        context.drawString(mc.font, "Style: " + ClickGuiModule.getInstance().style.getValue().name(), x + WIDTH - 15 - mc.font.width("Style: THUNDERHACK"), y + 12, new Color(ClickGuiModule.getInstance().color.getValue().getRGB()).getRGB());

        int contentY = y + 35 - scrollOffset;
        int idx = 0;
        for (ConfigEntry entry : entries) {
            boolean hovered = mouseX >= x + 15 && mouseX <= x + WIDTH - 15 && mouseY >= contentY && mouseY <= contentY + 22;
            RenderUtil.roundRect(context, x + 15, contentY, WIDTH - 30, 22, 6f, hovered ? 0x1AFFFFFF : 0x12FFFFFF);

            context.drawString(mc.font, entry.name, x + 22, contentY + 7, 0xFFE6E6E6);
            String status = entry.exists ? "OK" : "MISSING";
            int statusColor = entry.exists ? 0xFF55FF55 : 0xFFFF5555;
            context.drawString(mc.font, status, x + WIDTH - 80, contentY + 7, statusColor);

            // Save button
            boolean saveHover = mouseX >= x + WIDTH - 65 && mouseX <= x + WIDTH - 40 && mouseY >= contentY + 4 && mouseY <= contentY + 18;
            RenderUtil.roundRect(context, x + WIDTH - 65, contentY + 4, 24, 14, 4f, saveHover ? 0x33FFFFFF : 0x22FFFFFF);
            context.drawString(mc.font, "S", x + WIDTH - 56, contentY + 7, 0xFFAAAAAA);

            // Load button
            boolean loadHover = mouseX >= x + WIDTH - 38 && mouseX <= x + WIDTH - 16 && mouseY >= contentY + 4 && mouseY <= contentY + 18;
            RenderUtil.roundRect(context, x + WIDTH - 38, contentY + 4, 22, 14, 4f, loadHover ? 0x33FFFFFF : 0x22FFFFFF);
            context.drawString(mc.font, "L", x + WIDTH - 30, contentY + 7, 0xFFAAAAAA);

            idx++;
            contentY += 28;
        }

        maxScroll = Math.max(0, contentY - (y + HEIGHT) + 10);

        if (messageTimer > 0) messageTimer--;
        if (!message.isEmpty() && messageTimer > 0) {
            context.drawString(mc.font, message, x + 15, y + HEIGHT - 20, 0xFF55FF55);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        int x = (mc.getWindow().getGuiScaledWidth() - WIDTH) / 2;
        int y = (mc.getWindow().getGuiScaledHeight() - HEIGHT) / 2;

        if (button == 0) {
            int contentY = y + 35 - scrollOffset;
            for (int idx = 0; idx < entries.size(); idx++) {
                ConfigEntry entry = entries.get(idx);
                boolean saveHover = mouseX >= x + WIDTH - 65 && mouseX <= x + WIDTH - 40 && mouseY >= contentY + 4 && mouseY <= contentY + 18;
                boolean loadHover = mouseX >= x + WIDTH - 38 && mouseX <= x + WIDTH - 16 && mouseY >= contentY + 4 && mouseY <= contentY + 18;

                if (saveHover) {
                    saveConfig(entry);
                    return true;
                }
                if (loadHover) {
                    loadConfig(entry);
                    return true;
                }
                contentY += 28;
            }
        }

        if (button == 1) {
            OxevySettingsScreen.open();
            return true;
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) (verticalAmount * 10)));
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            OxevySettingsScreen.open();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void saveConfig(ConfigEntry entry) {
        try {
            Oxevy.configManager.save();
            refreshEntries();
            setMessage("Config saved");
        } catch (Exception e) {
            setMessage("Failed to save: " + e.getMessage());
        }
    }

    private void loadConfig(ConfigEntry entry) {
        try {
            Oxevy.configManager.load();
            refreshEntries();
            setMessage("Config loaded from disk");
        } catch (Exception e) {
            setMessage("Failed to load: " + e.getMessage());
        }
    }

    private void setMessage(String msg) {
        this.message = msg;
        this.messageTimer = 100;
    }

    private static class ConfigEntry {
        final String name;
        final Path path;
        boolean exists;

        ConfigEntry(String name, Path path, boolean checkExists) {
            this.name = name;
            this.path = path;
            if (checkExists) {
                this.exists = Files.exists(path);
            }
        }
    }
}
