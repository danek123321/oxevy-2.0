package me.alpha432.oxevy.features.modules.client;

import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.event.impl.input.MouseInputEvent;
import me.alpha432.oxevy.event.impl.render.Render2DEvent;
import me.alpha432.oxevy.event.system.Subscribe;
import me.alpha432.oxevy.features.gui.HudEditorScreen;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.render.RenderUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import org.joml.Vector2f;

import java.awt.*;

public abstract class HudModule extends Module {
    public final Setting<Vector2f> pos = vec2f("Position", 0.1f, 0.1f);
    public final Setting<Boolean> background = bool("Background", true);
    public final Setting<Color> bgColor = color("BGColor", 0, 0, 0, 100);

    // Pose settings
    public final Setting<Boolean> modifyPose = bool("Modify Pose", false);
    public final Setting<Float> scale = num("Scale", 1.0f, 0.1f, 5.0f);
    public final Setting<Float> rotation = num("Rotation", 0.0f, -360.0f, 360.0f);
    public final Setting<Float> skewX = num("SkewX", 0.0f, -2.0f, 2.0f);
    public final Setting<Float> skewY = num("SkewY", 0.0f, -2.0f, 2.0f);
    public final Setting<Runnable> resetPose = button("Reset Pose", () -> {
        scale.setValue(1.0f);
        rotation.setValue(0.0f);
        skewX.setValue(0.0f);
        skewY.setValue(0.0f);
    });

    private float dragX, dragY, width, height;
    private boolean dragging, button;

    public HudModule(String name, String description, float width, float height) {
        super(name, description, Category.HUD);
        this.width = width;
        this.height = height;

        scale.setVisibility(v -> modifyPose.getValue());
        rotation.setVisibility(v -> modifyPose.getValue());
        skewX.setVisibility(v -> modifyPose.getValue());
        skewY.setVisibility(v -> modifyPose.getValue());
        resetPose.setVisibility(v -> modifyPose.getValue());
    }

    public float getX() {
        return mc.getWindow().getGuiScaledWidth() * pos.getValue().x();
    }

    public float getY() {
        float heightWithChat = mc.getWindow().getGuiScaledHeight() - 14;
        float baseY = mc.getWindow().getGuiScaledHeight() * pos.getValue().y();
        float combined = baseY + getHeight();

        if (mc.screen instanceof ChatScreen) {
            baseY = Math.min(combined, heightWithChat) - getHeight();
        }
        return baseY;
    }

    public abstract void drawContent(Render2DEvent e);

    @Subscribe
    public void onRender2DHud(Render2DEvent e) {
        if (nullCheck()) return;
        if (mc.screen != null && !(mc.screen instanceof HudEditorScreen)) return;

        float x = getX();
        float y = getY();

        if (mc.screen instanceof HudEditorScreen && button) {
            if (!dragging && isHovering() && HudEditorScreen.getInstance().currentDragging == null) {
                dragX = getMouseX() - x;
                dragY = getMouseY() - y;
                dragging = true;
                HudEditorScreen.getInstance().currentDragging = this;
            }

            if (dragging) {
                float finalX = Math.min(Math.max(getMouseX() - dragX, 0),
                        mc.getWindow().getGuiScaledWidth() - width);
                float finalY = Math.min(Math.max(getMouseY() - dragY, 0),
                        mc.getWindow().getGuiScaledHeight() - height);

                pos.getValue().x = finalX / mc.getWindow().getGuiScaledWidth();
                pos.getValue().y = finalY / mc.getWindow().getGuiScaledHeight();
            }
        } else {
            dragging = false;
        }

        GuiGraphics context = e.getContext();

        if (background.getValue()) {
            RenderUtil.rect(context, x - 2, y - 2, x + width + 2, y + height + 2, bgColor.getValue().getRGB());
        }

        if (mc.screen instanceof HudEditorScreen) {
            boolean isThisDragging = HudEditorScreen.getInstance().currentDragging == this;
            int borderColor = isThisDragging ? ClickGuiModule.getInstance().color.getValue().getRGB() : 0x55FFFFFF;
            
            RenderUtil.rect(context, x - 2, y - 2, x + width + 2, y + height + 2, borderColor, 1.0f);
            
            if (isHovering() || isThisDragging) {
                String label = getName();
                context.drawString(mc.font, label, (int) x, (int) (y - 12), 0xFFFFFFFF);
            }
        }

        drawContent(e);
    }

    @Subscribe
    public void onMouse(MouseInputEvent e) {
        if (!(mc.screen instanceof HudEditorScreen) || nullCheck()) return;

        if (e.getAction() == 0) {
            button = false;
            dragging = false;
            HudEditorScreen.getInstance().currentDragging = null;
        }

        if (e.getAction() == 1 && isHovering()) {
            button = true;
        }
    }

    public int getMouseX() {
        return (int) (mc.mouseHandler.xpos() / mc.getWindow().getGuiScale());
    }

    public int getMouseY() {
        return (int) (mc.mouseHandler.ypos() / mc.getWindow().getGuiScale());
    }

    public boolean isHovering() {
        float x = getX();
        float y = getY();
        int mouseX = getMouseX();
        int mouseY = getMouseY();

        return mouseX >= x - 2 && mouseX <= x + width + 2 &&
                mouseY >= y - 2 && mouseY <= y + height + 2;
    }

    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public void setWidth(float width) { this.width = width; }
    public void setHeight(float height) { this.height = height; }
}