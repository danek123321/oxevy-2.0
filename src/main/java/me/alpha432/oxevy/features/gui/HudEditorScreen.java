package me.alpha432.oxevy.features.gui;

import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.features.modules.hud.HudModule;
import me.alpha432.oxevy.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.stream.Collectors;

public class HudEditorScreen extends Screen {
    private static final Minecraft mc = Minecraft.getInstance();
    private static HudEditorScreen INSTANCE;

    public HudModule currentDragging;
    public boolean anyHover;

    private HudEditorScreen() {
        super(Component.literal("HUD Editor"));
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        me.alpha432.oxevy.features.gui.items.Item.context = context;
        anyHover = false;
        
        // Background tint
        context.fill(0, 0, context.guiWidth(), context.guiHeight(), 0x44000000);
        
        // Guide lines
        int centerX = context.guiWidth() / 2;
        int centerY = context.guiHeight() / 2;
        RenderUtil.rect(context, centerX, 0, centerX + 1, context.guiHeight(), 0x22FFFFFF);
        RenderUtil.rect(context, 0, centerY, context.guiWidth(), centerY + 1, 0x22FFFFFF);
        
        // Render HUD modules
        List<HudModule> modules = Oxevy.moduleManager.getModules().stream()
                .filter(m -> m instanceof HudModule && m.isEnabled())
                .map(m -> (HudModule) m)
                .collect(Collectors.toList());

        for (HudModule module : modules) {
            module.onRender2DHud(new me.alpha432.oxevy.event.impl.render.Render2DEvent(context, delta));
        }
        
        // Editor instructions
        String text = "HUD Editor - Drag modules to reposition";
        context.drawString(mc.font, text, (context.guiWidth() - mc.font.width(text)) / 2, 10, 0xFFAAAAAA);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        return super.mouseReleased(click);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        return super.charTyped(input);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
    }

    public static HudEditorScreen getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HudEditorScreen();
        }
        return INSTANCE;
    }
}

