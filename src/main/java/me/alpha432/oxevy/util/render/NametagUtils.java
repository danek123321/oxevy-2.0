package me.alpha432.oxevy.util.render;

import com.mojang.blaze3d.vertex.PoseStack;
import me.alpha432.oxevy.util.traits.Util;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector4f;

public class NametagUtils implements Util {
    private static final Vector4f vec4 = new Vector4f();
    private static final Vector4f mmMat4 = new Vector4f();
    private static final Vector4f pmMat4 = new Vector4f();
    private static final Vector3d camera = new Vector3d();
    private static final Vector3d cameraNegated = new Vector3d();
    private static final Matrix4f rotation = new Matrix4f();
    private static final Matrix4f projection = new Matrix4f();
    private static double windowScale;

    public static double scale;

    private NametagUtils() {
    }

    public static void onRender(PoseStack stack, Matrix4f proj) {
        rotation.set(stack.last().pose());
        projection.set(proj);

        var camPos = mc.gameRenderer.getMainCamera().position();
        camera.set(camPos.x, camPos.y, camPos.z);
        cameraNegated.set(camera);
        cameraNegated.negate();

        windowScale = mc.getWindow().getGuiScale();
    }

    public static boolean to2D(Vector3d pos, double scale) {
        NametagUtils.scale = scale;

        vec4.set((float) (cameraNegated.x + pos.x),
                 (float) (cameraNegated.y + pos.y),
                 (float) (cameraNegated.z + pos.z), 1.0f);

        vec4.mul(rotation, mmMat4);
        mmMat4.mul(projection, pmMat4);

        boolean behind = pmMat4.w <= 0.0f;
        if (behind) return false;

        toScreen(pmMat4);
        double x = pmMat4.x * mc.getWindow().getWidth();
        double y = pmMat4.y * mc.getWindow().getHeight();

        if (Double.isInfinite(x) || Double.isInfinite(y)) return false;

        pos.set(x / windowScale, mc.getWindow().getHeight() - y / windowScale, pmMat4.z);
        return true;
    }

    public static void begin(Vector3d pos, GuiGraphics drawContext) {
        drawContext.pose().pushMatrix();
        drawContext.pose().translate((float) pos.x, (float) pos.y);
        drawContext.pose().scale((float) scale, (float) scale);
    }

    public static void end(GuiGraphics drawContext) {
        drawContext.pose().popMatrix();
    }

    private static void toScreen(Vector4f vec) {
        float newW = 1.0f / vec.w * 0.5f;
        vec.x = vec.x * newW + 0.5f;
        vec.y = vec.y * newW + 0.5f;
        vec.z = vec.z * newW + 0.5f;
        vec.w = newW;
    }
}
