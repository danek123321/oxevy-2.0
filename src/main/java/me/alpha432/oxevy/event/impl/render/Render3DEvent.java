package me.alpha432.oxevy.event.impl.render;

import com.mojang.blaze3d.vertex.PoseStack;
import me.alpha432.oxevy.event.Event;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import net.minecraft.world.phys.Vec3;

public class Render3DEvent extends Event {
    private final PoseStack matrix;
    private final float delta;
    private final Matrix4f projectionMatrix;
    private final Matrix4f positionMatrix;

    public Render3DEvent(PoseStack matrix, float delta, Matrix4f projectionMatrix, Matrix4f positionMatrix) {
        this.matrix = matrix;
        this.delta = delta;
        this.projectionMatrix = projectionMatrix;
        this.positionMatrix = positionMatrix;
    }

    public PoseStack getMatrix() {
        return matrix;
    }

    public float getDelta() {
        return delta;
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public Matrix4f getPositionMatrix() {
        return positionMatrix;
    }

    /** Projects world position to screen coordinates. Returns [x, y] or null if behind camera. */
    public float[] worldToScreen(Vec3 worldPos, Vec3 cameraPos) {
        Vector4f pos = new Vector4f(
            (float) (worldPos.x - cameraPos.x),
            (float) (worldPos.y - cameraPos.y),
            (float) (worldPos.z - cameraPos.z),
            1.0f
        );
        positionMatrix.transform(pos);
        projectionMatrix.transform(pos);
        if (pos.w <= 0) return null;
        float ndcX = pos.x / pos.w;
        float ndcY = pos.y / pos.w;
        if (ndcX < -1 || ndcX > 1 || ndcY < -1 || ndcY > 1) return null;
        int w = net.minecraft.client.Minecraft.getInstance().getWindow().getWidth();
        int h = net.minecraft.client.Minecraft.getInstance().getWindow().getHeight();
        float scale = (float) net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScale();
        float screenX = (ndcX + 1) * (w / 2f) / scale;
        float screenY = (1 - ndcY) * (h / 2f) / scale;
        return new float[] { screenX, screenY };
    }
}
