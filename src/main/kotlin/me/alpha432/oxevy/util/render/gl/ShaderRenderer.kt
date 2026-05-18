package me.alpha432.oxevy.util.render.gl

import me.alpha432.oxevy.util.render.RenderUtil
import net.minecraft.client.gui.GuiGraphics
import java.awt.Color

object ShaderRenderer {

    private var initialized = false
    private var rectProgram = -1

    fun init() {
        if (initialized) return
        rectProgram = ShaderManager.getOrCreateProgram("rect")
        initialized = true
    }

    fun drawRect(guiGraphics: GuiGraphics, x: Float, y: Float, w: Float, h: Float, color: Int) {
        drawShaderRectFallback(guiGraphics, x, y, w, h, color, 0f, color, color, 0, 0f, 0)
    }

    fun drawRoundedRect(
        guiGraphics: GuiGraphics,
        x: Float, y: Float, w: Float, h: Float,
        radius: Float, color: Int
    ) {
        RenderUtil.roundRect(guiGraphics, x, y, w, h, radius, color)
    }

    fun drawGradientRectH(
        guiGraphics: GuiGraphics,
        x: Float, y: Float, w: Float, h: Float,
        colorLeft: Int, colorRight: Int
    ) {
        RenderUtil.horizontalGradient(guiGraphics, x, y, x + w, y + h,
            Color(colorLeft, true), Color(colorRight, true))
    }

    fun drawGradientRectV(
        guiGraphics: GuiGraphics,
        x: Float, y: Float, w: Float, h: Float,
        colorTop: Int, colorBottom: Int
    ) {
        RenderUtil.verticalGradient(guiGraphics, x, y, x + w, y + h,
            Color(colorTop, true), Color(colorBottom, true))
    }

    private fun drawShaderRectFallback(
        guiGraphics: GuiGraphics,
        x: Float, y: Float, w: Float, h: Float,
        color: Int, radius: Float,
        color1: Int, color2: Int,
        gradientDir: Int,
        borderWidth: Float, borderColor: Int
    ) {
        if (radius > 0.5f) {
            RenderUtil.roundRect(guiGraphics, x, y, w, h, radius, color)
        } else {
            RenderUtil.rect(guiGraphics, x, y, x + w, y + h, color)
        }
    }

    fun cleanup() {
        ShaderManager.deleteAll()
        initialized = false
        rectProgram = -1
    }
}
