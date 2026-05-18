package me.alpha432.oxevy.util;

import java.awt.Color;

public class Palette {
    private static final Color[] colors = {
        new Color(255, 0, 63),
        new Color(255, 0, 255),
        new Color(0, 153, 0),
        new Color(0, 255, 0),
        new Color(0, 255, 80),
        new Color(255, 255, 0),
        new Color(255, 153, 0)
    };
    
    public static Color getColor(float progress) {
        progress = Math.max(0, Math.min(1, progress));
        int index = (int) (progress * (colors.length - 1));
        return colors[index];
    }
    
    public static Color getBackColor() {
        return new Color(20, 20, 20);
    }
    
    public static Color getInterpolatedPaletteColor(float progress) {
        return getColor(progress);
    }
}