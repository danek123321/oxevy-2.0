package me.alpha432.oxevy.features.gui;

import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class LiquidMainMenu extends Screen {

    public LiquidMainMenu() {
        super(Component.literal("Main Menu"));
    }

    @Override
    protected void init() {
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xFF080810);

        int accent = 0xFF00FF66;
        int textShadow = 0xFF1A1A2E;

        context.pose().pushMatrix();
        float logoScale = 3.5f;
        context.pose().translate(width / 2f, height / 2f - 90);
        context.pose().scale(logoScale, logoScale);

        context.drawString(font, "OXEVY", -font.width("OXEVY") / 2, 0, 0xFF00FF66, true);
        context.pose().popMatrix();

        String subtitle = "Premium Minecraft Client";
        context.drawString(font, subtitle, width / 2 - font.width(subtitle) / 2, height / 2 - 55, 0xFF888888, false);

        int btnY = height / 2;

        drawButton(context, "\u25B6  PLAY", width / 2 - 80, btnY, 160, 35, mouseX, mouseY, true);
        drawButton(context, "Singleplayer", width / 2 - 80, btnY + 40, 77, 20, mouseX, mouseY, false);
        drawButton(context, "Multiplayer", width / 2 + 3, btnY + 40, 77, 20, mouseX, mouseY, false);
        drawButton(context, "\u2699  Settings", width / 2 - 80, btnY + 65, 77, 20, mouseX, mouseY, false);
        drawButton(context, "\u25C8 ClickGUI", width / 2 + 3, btnY + 65, 77, 20, mouseX, mouseY, false);
        drawButton(context, "Exit", width / 2 - 80, btnY + 90, 160, 20, mouseX, mouseY, false);

        String info = "Welcome, " + minecraft.getUser().getName();
        context.drawString(font, info, 10, height - 25, 0xFFAAAAAA, false);

        String version = "Oxevy v1.0.0 | MC 1.21.11";
        context.drawString(font, version, width - font.width(version) - 10, height - 25, 0xFF555555, false);

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawButton(GuiGraphics context, String text, int x, int y, int w, int h, int mouseX, int mouseY, boolean primary) {
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;

        int bgColor = hovered ? (primary ? 0xFF00FF66 : 0xFF1A1A2E) : (primary ? 0x8800FF66 : 0x55000000);
        int borderColor = hovered ? 0xFFFFFFFF : 0x4400FF66;
        int textColor = hovered ? 0xFFFFFFFF : (primary ? 0xFF00FF66 : 0xFFCCCCCC);

        if (primary) {
            RenderUtil.rect(context, x, y, x + w, y + h, bgColor);
            RenderUtil.rect(context, x, y, x + w, y + h, borderColor, 1.0f);
        } else {
            RenderUtil.rect(context, x, y, x + w, y + h, bgColor);
            if (hovered) {
                RenderUtil.rect(context, x, y, x + 2, y + h, 0xFF00FF66);
            }
        }

        context.drawString(font, text, x + w / 2 - font.width(text) / 2, y + h / 2 - 4, textColor, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        if (button == 0) {
            int centerX = width / 2 - 80;
            int btnY = height / 2;

            if (isHovered(mouseX, mouseY, centerX, btnY, 160, 35)) {
                minecraft.setScreen(new SelectWorldScreen(this));
                return true;
            }
            if (isHovered(mouseX, mouseY, centerX, btnY + 40, 77, 20)) {
                minecraft.setScreen(new SelectWorldScreen(this));
                return true;
            }
            if (isHovered(mouseX, mouseY, centerX + 83, btnY + 40, 77, 20)) {
                minecraft.setScreen(new JoinMultiplayerScreen(this));
                return true;
            }
            if (isHovered(mouseX, mouseY, centerX, btnY + 65, 77, 20)) {
                minecraft.setScreen(new OptionsScreen(this, minecraft.options));
                return true;
            }
            if (isHovered(mouseX, mouseY, centerX + 83, btnY + 65, 77, 20)) {
                minecraft.setScreen(new OxevyGui());
                return true;
            }
            if (isHovered(mouseX, mouseY, centerX, btnY + 90, 160, 20)) {
                minecraft.stop();
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    private boolean isHovered(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }
}
