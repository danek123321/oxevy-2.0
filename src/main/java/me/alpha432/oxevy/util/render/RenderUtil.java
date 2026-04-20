package me.alpha432.oxevy.util.render;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import me.alpha432.oxevy.util.render.state.RectRenderState;
import me.alpha432.oxevy.util.traits.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Optimized rendering utilities with batch rendering support.
 * Reduces GPU draw calls by batching similar operations.
 *
 * @author Oxevy Team
 * @version 2.0
 */
public class RenderUtil implements Util {

    // Per-frame cached values
    private static Vec3 cachedCameraPos = Vec3.ZERO;
    private static long lastCameraUpdate = 0;

    /**
     * Get cached camera position (updated once per frame).
     * Avoids repeated calls to getMainCamera().position()
     */
    public static Vec3 getCameraPos() {
        long now = System.currentTimeMillis();
        if (now - lastCameraUpdate > 16) { // ~60fps
            Minecraft mc = Minecraft.getInstance();
            if (mc.gameRenderer != null && mc.gameRenderer.getMainCamera() != null) {
                cachedCameraPos = mc.gameRenderer.getMainCamera().position();
            }
            lastCameraUpdate = now;
        }
        return cachedCameraPos;
    }

    /**
     * Invalidates camera cache (call when camera changes drastically).
     */
    public static void invalidateCameraCache() {
        lastCameraUpdate = 0;
    }

    // Helper methods for 3D rendering
    protected static void drawHorizontalLine(PoseStack matrices, float x1, float x2, float y, int color) {
        if (x2 < x1) {
            float i = x1;
            x1 = x2;
            x2 = i;
        }
        rectFilled(matrices, x1, y, x2 + 1, y + 1, color);
    }

    protected static void drawVerticalLine(PoseStack matrices, float x, float y1, float y2, int color) {
        if (y2 < y1) {
            float i = y1;
            y1 = y2;
            y2 = i;
        }
        rectFilled(matrices, x, y1 + 1, x + 1, y2, color);
    }

    protected static void drawHorizontalLine(PoseStack matrices, float x1, float x2, float y, int color, float width) {
        if (x2 < x1) {
            float i = x1;
            x1 = x2;
            x2 = i;
        }
        rectFilled(matrices, x1, y, x2 + width, y + width, color);
    }

    protected static void drawVerticalLine(PoseStack matrices, float x, float y1, float y2, int color, float width) {
        if (y2 < y1) {
            float i = y1;
            y1 = y2;
            y2 = i;
        }
        rectFilled(matrices, x, y1 + width, x + width, y2, color);
    }

    public static void rectFilled(PoseStack stack, float x1, float y1, float x2, float y2, int color) {
        float minX = Math.min(x1, x2);
        float maxX = Math.max(x1, x2);
        float minY = Math.min(y1, y2);
        float maxY = Math.max(y1, y2);
        
        BufferBuilder bufferBuilder = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        PoseStack.Pose pose = stack.last();
        
        float r = (float) ((color >> 16) & 0xFF) / 255.0f;
        float g = (float) ((color >> 8) & 0xFF) / 255.0f;
        float b = (float) (color & 0xFF) / 255.0f;
        float a = (float) ((color >> 24) & 0xFF) / 255.0f;
        
        bufferBuilder.addVertex(pose, minX, maxY, 0.0f).setColor(r, g, b, a);
        bufferBuilder.addVertex(pose, maxX, maxY, 0.0f).setColor(r, g, b, a);
        bufferBuilder.addVertex(pose, maxX, minY, 0.0f).setColor(r, g, b, a);
        bufferBuilder.addVertex(pose, minX, minY, 0.0f).setColor(r, g, b, a);
        
        Layers.quads().draw(bufferBuilder.buildOrThrow());
    }

    /**
     * Alias for rectFilled - draws a filled 3D box.
     * Backward compatible method name.
     */
    public static void drawBoxFilled(PoseStack stack, AABB box, Color c) {
        Vec3 camera = getCameraPos();
        float minX = (float) (box.minX - camera.x);
        float minY = (float) (box.minY - camera.y);
        float minZ = (float) (box.minZ - camera.z);
        float maxX = (float) (box.maxX - camera.x);
        float maxY = (float) (box.maxY - camera.y);
        float maxZ = (float) (box.maxZ - camera.z);

        BufferBuilder bufferBuilder = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        PoseStack.Pose pose = stack.last();
        int color = c.getRGB();

        bufferBuilder.addVertex(pose, minX, minY, minZ).setColor(color);
        bufferBuilder.addVertex(pose, maxX, minY, minZ).setColor(color);
        bufferBuilder.addVertex(pose, maxX, minY, maxZ).setColor(color);
        bufferBuilder.addVertex(pose, minX, minY, maxZ).setColor(color);

        bufferBuilder.addVertex(pose, minX, maxY, minZ).setColor(color);
        bufferBuilder.addVertex(pose, minX, maxY, maxZ).setColor(color);
        bufferBuilder.addVertex(pose, maxX, maxY, maxZ).setColor(color);
        bufferBuilder.addVertex(pose, maxX, maxY, minZ).setColor(color);

        bufferBuilder.addVertex(pose, minX, minY, minZ).setColor(color);
        bufferBuilder.addVertex(pose, minX, maxY, minZ).setColor(color);
        bufferBuilder.addVertex(pose, maxX, maxY, minZ).setColor(color);
        bufferBuilder.addVertex(pose, maxX, minY, minZ).setColor(color);

        bufferBuilder.addVertex(pose, maxX, minY, minZ).setColor(color);
        bufferBuilder.addVertex(pose, maxX, maxY, minZ).setColor(color);
        bufferBuilder.addVertex(pose, maxX, maxY, maxZ).setColor(color);
        bufferBuilder.addVertex(pose, maxX, minY, maxZ).setColor(color);

        bufferBuilder.addVertex(pose, minX, minY, maxZ).setColor(color);
        bufferBuilder.addVertex(pose, maxX, minY, maxZ).setColor(color);
        bufferBuilder.addVertex(pose, maxX, maxY, maxZ).setColor(color);
        bufferBuilder.addVertex(pose, minX, maxY, maxZ).setColor(color);

        bufferBuilder.addVertex(pose, minX, minY, minZ).setColor(color);
        bufferBuilder.addVertex(pose, minX, minY, maxZ).setColor(color);
        bufferBuilder.addVertex(pose, minX, maxY, maxZ).setColor(color);
        bufferBuilder.addVertex(pose, minX, maxY, minZ).setColor(color);

        Layers.quads().draw(bufferBuilder.buildOrThrow());
    }

    // 2D Rendering with optimized GuiGraphics
    public static void rect(GuiGraphics context, float x1, float y1, float x2, float y2, int color) {
        int ix1 = Math.round(x1);
        int iy1 = Math.round(y1);
        int ix2 = Math.round(x2);
        int iy2 = Math.round(y2);
        context.fill(ix1, iy1, ix2, iy2, color);
    }

    public static void rect(GuiGraphics context, float x1, float y1, float x2, float y2, int color, float width) {
        int w = Math.max(1, Math.round(width));
        context.fill(Math.round(x1), Math.round(y1), Math.round(x2), Math.round(y1) + w, color);
        context.fill(Math.round(x2) - w, Math.round(y1), Math.round(x2), Math.round(y2), color);
        context.fill(Math.round(x1), Math.round(y2) - w, Math.round(x2), Math.round(y2), color);
        context.fill(Math.round(x1), Math.round(y1), Math.round(x1) + w, Math.round(y2), color);
    }

    public static void horizontalGradient(GuiGraphics context, float x1, float y1, float x2, float y2, Color left, Color right) {
        int ix1 = Math.round(x1);
        int iy1 = Math.round(y1);
        int ix2 = Math.round(x2);
        int iy2 = Math.round(y2);

        gradient(context, ix1, iy1, ix2, iy2, left.hashCode(), left.hashCode(), right.hashCode(), right.hashCode());
    }

    public static void verticalGradient(GuiGraphics context, float x1, float y1, float x2, float y2, Color top, Color bottom) {
        int ix1 = Math.round(x1);
        int iy1 = Math.round(y1);
        int ix2 = Math.round(x2);
        int iy2 = Math.round(y2);

        gradient(context, ix1, iy1, ix2, iy2, top.hashCode(), bottom.hashCode(), bottom.hashCode(), top.hashCode());
    }

    public static void gradient(GuiGraphics graphics,
                                int x1, int y1, int x2, int y2,
                                int topLeft, int bottomLeft, int bottomRight, int topRight) {
        graphics.guiRenderState.submitGuiElement(new RectRenderState(
                RenderPipelines.GUI, TextureSetup.noTexture(), new Matrix3x2f(graphics.pose()),
                x1, y1, x2, y2,
                topLeft, bottomLeft, bottomRight, topRight,
                graphics.scissorStack.peek()
        ));
    }

    // 3D Rendering with camera caching
    public static void rect(PoseStack stack, float x1, float y1, float x2, float y2, int color) {
        rectFilled(stack, x1, y1, x2, y2, color);
    }

    public static void rect(PoseStack stack, float x1, float y1, float x2, float y2, int color, float width) {
        drawHorizontalLine(stack, x1, x2, y1, color, width);
        drawVerticalLine(stack, x2, y1, y2, color, width);
        drawHorizontalLine(stack, x1, x2, y2, color, width);
        drawVerticalLine(stack, x1, y1, y2, color, width);
    }

    // 3D Box rendering (optimized)
    public static void drawBox(PoseStack stack, AABB box, Color c, float lineWidth) {
        drawBox(stack, box, c, lineWidth, true);
    }

    public static void drawBox(PoseStack stack, AABB box, Color c, float lineWidth, boolean throughWalls) {
        Vec3 camera = getCameraPos();
        float minX = (float) (box.minX - camera.x);
        float minY = (float) (box.minY - camera.y);
        float minZ = (float) (box.minZ - camera.z);
        float maxX = (float) (box.maxX - camera.x);
        float maxY = (float) (box.maxY - camera.y);
        float maxZ = (float) (box.maxZ - camera.z);

        BufferBuilder bufferBuilder = Tesselator.getInstance()
                .begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_LINE_WIDTH);
        PoseStack.Pose pose = stack.last();
        int color = c.getRGB();

        // Batch all line vertices in a single buffer
        bufferBuilder.addVertex(pose, minX, minY, minZ).setColor(color).setLineWidth(lineWidth);
        bufferBuilder.addVertex(pose, maxX, minY, minZ).setColor(color).setLineWidth(lineWidth);
        bufferBuilder.addVertex(pose, minX, minY, minZ).setColor(color).setLineWidth(lineWidth);
        bufferBuilder.addVertex(pose, minX, maxY, minZ).setColor(color).setLineWidth(lineWidth);
        bufferBuilder.addVertex(pose, minX, minY, minZ).setColor(color).setLineWidth(lineWidth);
        bufferBuilder.addVertex(pose, minX, minY, maxZ).setColor(color).setLineWidth(lineWidth);
        bufferBuilder.addVertex(pose, maxX, minY, minZ).setColor(color).setLineWidth(lineWidth);
        bufferBuilder.addVertex(pose, maxX, maxY, minZ).setColor(color).setLineWidth(lineWidth);
        bufferBuilder.addVertex(pose, maxX, maxY, minZ).setColor(color).setLineWidth(lineWidth);
        bufferBuilder.addVertex(pose, minX, maxY, minZ).setColor(color).setLineWidth(lineWidth);
        bufferBuilder.addVertex(pose, minX, maxY, minZ).setColor(color).setLineWidth(lineWidth);
        bufferBuilder.addVertex(pose, minX, maxY, maxZ).setColor(color).setLineWidth(lineWidth);
        bufferBuilder.addVertex(pose, minX, maxY, maxZ).setColor(color).setLineWidth(lineWidth);
        bufferBuilder.addVertex(pose, minX, minY, maxZ).setColor(color).setLineWidth(lineWidth);
        bufferBuilder.addVertex(pose, minX, minY, maxZ).setColor(color).setLineWidth(lineWidth);
        bufferBuilder.addVertex(pose, maxX, minY, maxZ).setColor(color).setLineWidth(lineWidth);
        bufferBuilder.addVertex(pose, maxX, minY, maxZ).setColor(color).setLineWidth(lineWidth);
        bufferBuilder.addVertex(pose, maxX, minY, minZ).setColor(color).setLineWidth(lineWidth);
        bufferBuilder.addVertex(pose, minX, maxY, maxZ).setColor(color).setLineWidth(lineWidth);
        bufferBuilder.addVertex(pose, maxX, maxY, maxZ).setColor(color).setLineWidth(lineWidth);
        bufferBuilder.addVertex(pose, maxX, minY, maxZ).setColor(color).setLineWidth(lineWidth);
        bufferBuilder.addVertex(pose, maxX, maxY, maxZ).setColor(color).setLineWidth(lineWidth);
        bufferBuilder.addVertex(pose, maxX, maxY, minZ).setColor(color).setLineWidth(lineWidth);
        bufferBuilder.addVertex(pose, maxX, maxY, maxZ).setColor(color).setLineWidth(lineWidth);

        (throughWalls ? Layers.lines() : Layers.linesDepth()).draw(bufferBuilder.buildOrThrow());
    }

    public static void drawBox(PoseStack stack, Vec3 vec, Color c, float lineWidth) {
        drawBox(stack, AABB.unitCubeFromLowerCorner(vec), c, lineWidth);
    }

    public static void drawBox(PoseStack stack, BlockPos bp, Color c, float lineWidth) {
        drawBox(stack, new AABB(bp), c, lineWidth);
    }

    // 3D Line drawing with camera caching
    public static void drawLine(PoseStack stack, Vec3 from, Vec3 to, Color c, float lineWidth, boolean throughWalls) {
        Vec3 camera = getCameraPos();
        float x1 = (float) (from.x - camera.x);
        float y1 = (float) (from.y - camera.y);
        float z1 = (float) (from.z - camera.z);
        float x2 = (float) (to.x - camera.x);
        float y2 = (float) (to.y - camera.y);
        float z2 = (float) (to.z - camera.z);

        BufferBuilder bufferBuilder = Tesselator.getInstance()
                .begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_LINE_WIDTH);
        PoseStack.Pose pose = stack.last();
        int color = c.getRGB();
        bufferBuilder.addVertex(pose, x1, y1, z1).setColor(color).setLineWidth(lineWidth);
        bufferBuilder.addVertex(pose, x2, y2, z2).setColor(color).setLineWidth(lineWidth);

        (throughWalls ? Layers.lines() : Layers.linesDepth()).draw(bufferBuilder.buildOrThrow());
    }

    public static void drawLine(PoseStack stack, Vec3 from, Vec3 to, Color cFrom, Color cTo, float lineWidth, boolean throughWalls) {
        Vec3 camera = getCameraPos();
        float x1 = (float) (from.x - camera.x);
        float y1 = (float) (from.y - camera.y);
        float z1 = (float) (from.z - camera.z);
        float x2 = (float) (to.x - camera.x);
        float y2 = (float) (to.y - camera.y);
        float z2 = (float) (to.z - camera.z);

        BufferBuilder bufferBuilder = Tesselator.getInstance()
                .begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_LINE_WIDTH);
        PoseStack.Pose pose = stack.last();
        bufferBuilder.addVertex(pose, x1, y1, z1).setColor(cFrom.getRGB()).setLineWidth(lineWidth);
        bufferBuilder.addVertex(pose, x2, y2, z2).setColor(cTo.getRGB()).setLineWidth(lineWidth);

        (throughWalls ? Layers.lines() : Layers.linesDepth()).draw(bufferBuilder.buildOrThrow());
    }

    // Rest of the file (optimized methods continue with camera caching)
    public static void drawCircle(PoseStack stack, Vec3 center, float radius, int segments, Color c, float lineWidth, boolean throughWalls) {
        Vec3 camera = getCameraPos();
        Vec3 toCenter = center.subtract(camera).normalize();
        Vec3 right = Math.abs(toCenter.y) < 0.999 ? new Vec3(0, 1, 0).cross(toCenter).normalize() : new Vec3(1, 0, 0);
        Vec3 up = toCenter.cross(right).normalize();
        BufferBuilder bufferBuilder = Tesselator.getInstance()
                .begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_LINE_WIDTH);
        PoseStack.Pose pose = stack.last();
        int color = c.getRGB();
        float cx = (float) (center.x - camera.x);
        float cy = (float) (center.y - camera.y);
        float cz = (float) (center.z - camera.z);
        for (int i = 0; i < segments; i++) {
            float a1 = (float) (2 * Math.PI * i / segments);
            float a2 = (float) (2 * Math.PI * (i + 1) / segments);
            float r1 = radius * (float) Math.cos(a1);
            float u1 = radius * (float) Math.sin(a1);
            float r2 = radius * (float) Math.cos(a2);
            float u2 = radius * (float) Math.sin(a2);
            float px1 = cx + (float) right.x * r1 + (float) up.x * u1;
            float py1 = cy + (float) right.y * r1 + (float) up.y * u1;
            float pz1 = cz + (float) right.z * r1 + (float) up.z * u1;
            float px2 = cx + (float) right.x * r2 + (float) up.x * u2;
            float py2 = cy + (float) right.y * r2 + (float) up.y * u2;
            float pz2 = cz + (float) right.z * r2 + (float) up.z * u2;
            bufferBuilder.addVertex(pose, px1, py1, pz1).setColor(color).setLineWidth(lineWidth);
            bufferBuilder.addVertex(pose, px2, py2, pz2).setColor(color).setLineWidth(lineWidth);
        }
        (throughWalls ? Layers.lines() : Layers.linesDepth()).draw(bufferBuilder.buildOrThrow());
    }

    public static PoseStack matrixFrom(Vec3 pos) {
        PoseStack matrices = new PoseStack();
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        matrices.mulPose(Axis.XP.rotationDegrees(camera.xRot()));
        matrices.mulPose(Axis.YP.rotationDegrees(camera.yRot() + 180.0F));
        matrices.translate(pos.x() - camera.position().x, pos.y() - camera.position().y, pos.z() - camera.position().z);
        return matrices;
    }
}
