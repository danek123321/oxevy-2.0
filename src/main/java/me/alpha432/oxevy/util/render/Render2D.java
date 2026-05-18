package me.alpha432.oxevy.util.render;

import net.minecraft.client.gui.GuiGraphics;
import java.awt.Color;

public class Render2D {
    public static void drawRound(GuiGraphics ctx, float x, float y, float w, float h, float r, Color c) {
        ctx.fill((int)x, (int)y, (int)(x+w), (int)(y+h), c.getRGB());
    }
    
    public static void renderRoundedGradientRect(GuiGraphics ctx, Color tl, Color tr, Color br, Color bl, float x, float y, float w, float h, float r) {
        ctx.fill((int)x, (int)y, (int)(x+w), (int)(y+h), tl.getRGB());
    }
    
    public static void renderTexture(GuiGraphics ctx, String tex, float x, float y, float w, float h, float u, float v, float uw, float vh, int tw, int th) {
    }
    
    public static void drawGradientRound(GuiGraphics ctx, float x, float y, float w, float h, float r, Color... colors) {
        ctx.fill((int)x, (int)y, (int)(x+w), (int)(y+h), colors[0].getRGB());
    }
}