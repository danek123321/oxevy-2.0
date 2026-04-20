package me.alpha432.oxevy.features.gui.items.buttons;

import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.features.gui.OxevyGui;
import me.alpha432.oxevy.features.gui.Widget;
import me.alpha432.oxevy.features.gui.items.Item;
import me.alpha432.oxevy.features.modules.client.ClickGuiModule;
import me.alpha432.oxevy.util.render.AnimationUtil;
import me.alpha432.oxevy.util.ColorUtil;
import me.alpha432.oxevy.util.render.RenderUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

public class Button
        extends Item {
    private boolean state;
    
    // Animation values
    private float hoverAnimation = 0.0f;
    private float toggleAnimation = 0.0f;
    private boolean lastState = false;

    public Button(String name) {
        super(name);
        this.height = 15;
    }

    @Override
    public void drawScreen(GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        // Check if animations are enabled
        boolean animationsEnabled = ClickGuiModule.getInstance().animationsEnabled.getValue();
        float animSpeed = animationsEnabled ? ClickGuiModule.getInstance().animationSpeed.getValue() : 1.0f;
        
        // Update animations
        boolean isHovering = isHovering(mouseX, mouseY);
        float targetHover = isHovering ? 1.0f : 0.0f;
        hoverAnimation = AnimationUtil.animate(hoverAnimation, targetHover, animSpeed, AnimationUtil.Easing.EASE_OUT);
        
        // Update toggle animation when state changes
        if (lastState != getState()) {
            toggleAnimation = 0.0f;
            lastState = getState();
        }
        toggleAnimation = AnimationUtil.animate(toggleAnimation, 1.0f, animSpeed, AnimationUtil.Easing.EASE_OUT_BACK);
        
        // Calculate colors with animations
        int baseColor;
        if (getState()) {
            baseColor = Oxevy.colorManager.getColorWithAlpha(y, ClickGuiModule.getInstance().color.getValue().getAlpha());
        } else {
            baseColor = isHovering ? -2007673515 : 0x11555555;
        }
        
        // Apply hover brightness animation
        int hoverBrightness = (int) (hoverAnimation * 30);
        int animatedColor = AnimationUtil.interpolateColor(baseColor, baseColor + (hoverBrightness << 16) + (hoverBrightness << 8) + hoverBrightness, hoverAnimation);
        
        // Draw background with optional scale animation
        float scale = animationsEnabled && ClickGuiModule.getInstance().scaleAnimation.getValue() ? 
            1.0f + toggleAnimation * 0.02f : 1.0f;
        float centerX = this.x + this.width / 2.0f;
        float centerY = this.y + this.height / 2.0f;
        float drawX = centerX - (this.width * scale / 2.0f);
        float drawY = centerY - (this.height * scale / 2.0f);
        float drawWidth = this.width * scale;
        float drawHeight = this.height * scale - 0.5f;
        
        RenderUtil.rect(context, drawX, drawY, drawX + drawWidth, drawY + drawHeight, animatedColor);
        
        // Draw text synced to ClickGui client color
        int textColor = ColorUtil.toRGBA(ClickGuiModule.getInstance().color.getValue());
        drawString(this.getName(), this.x + 2.3f, this.y - 2.0f - (float) OxevyGui.getClickGui().getTextOffset(), textColor);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && this.isHovering(mouseX, mouseY)) {
            this.onMouseClick();
        }
    }

    public void onMouseClick() {
        this.state = !this.state;
        this.toggle();
        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1f));
    }

    public void toggle() {
    }

    public boolean getState() {
        return this.state;
    }

    @Override
    public int getHeight() {
        return 14;
    }

    public boolean isHovering(int mouseX, int mouseY) {
        for (Widget widget : OxevyGui.getClickGui().getComponents()) {
            if (!widget.drag) continue;
            return false;
        }
        return (float) mouseX >= this.getX() && (float) mouseX <= this.getX() + (float) this.getWidth() && (float) mouseY >= this.getY() && (float) mouseY < this.getY() + (float) this.height;
    }
}
