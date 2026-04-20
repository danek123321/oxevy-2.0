package me.alpha432.oxevy.manager;

import me.alpha432.oxevy.features.modules.client.ClickGuiModule;
import me.alpha432.oxevy.util.ColorUtil;
import me.alpha432.oxevy.util.MathUtil;

import java.awt.*;

public class ColorManager {
    private Color color = new Color(0, 0, 255, 180);
    private Color targetColor = new Color(0, 0, 255, 180);
    private static final float LERP_SPEED = 4f;

    public void init() {
        ClickGuiModule ui = ClickGuiModule.getInstance();
        setColor(ui.color.getValue());
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color color) {
        this.color = color;
        this.targetColor = color;
    }

    /** Set target color for smooth transition (e.g. when switching presets). */
    public void setTargetColor(Color color) {
        this.targetColor = color;
    }

    /** Call each frame to lerp current color toward target. */
    public void update(float delta) {
        int r = (int) MathUtil.lerp(color.getRed(), targetColor.getRed(), delta * LERP_SPEED);
        int g = (int) MathUtil.lerp(color.getGreen(), targetColor.getGreen(), delta * LERP_SPEED);
        int b = (int) MathUtil.lerp(color.getBlue(), targetColor.getBlue(), delta * LERP_SPEED);
        int a = (int) MathUtil.lerp(color.getAlpha(), targetColor.getAlpha(), delta * LERP_SPEED);
        this.color = new Color(r, g, b, a);
    }

    public int getColorAsInt() {
        return ColorUtil.toRGBA(this.color);
    }

    public int getColorAsIntFullAlpha() {
        return ColorUtil.toRGBA(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), 255));
    }

    public int getColorWithAlpha(float offset, int alpha) {
        if (ClickGuiModule.getInstance().rainbow.getValue()) {
            return ColorUtil.rainbow((int) (offset / 10f * ClickGuiModule.getInstance().rainbowHue.getValue())).getRGB();
        }
        return new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), alpha).getRGB();
    }
}
