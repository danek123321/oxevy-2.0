package me.alpha432.oxevy.util.render;

import com.mojang.blaze3d.vertex.PoseStack;
import me.alpha432.oxevy.util.traits.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

public class OverlayRenderer implements Util {
    private float progress;
    private float prevProgress;
    private BlockPos prevPos;

    public void resetProgress() {
        progress = 0;
        prevProgress = 0;
        prevPos = null;
    }

    public void updateProgress() {
        prevProgress = progress;
        progress = mc.gameMode.getDestroyStage() / 9.0f;
        if (progress < prevProgress)
            prevProgress = progress;
    }

    public void render(PoseStack matrices, float partialTicks, BlockPos pos) {
        if (pos == null) return;

        if (prevPos != null && !pos.equals(prevPos))
            resetProgress();

        prevPos = pos;

        boolean breaksInstantly = mc.player.getAbilities().instabuild
            || mc.level.getBlockState(pos).getDestroySpeed(mc.level, pos) >= 1;
        float p = breaksInstantly ? 1 : Mth.lerp(partialTicks, prevProgress, progress);

        float red = p * 2F;
        float green = 2 - red;
        int quadColor = toArgb(red, green, 0, 0.25F);
        int lineColor = toArgb(red, green, 0, 0.5F);

        AABB box = new AABB(pos);
        if (p < 1)
            box = box.deflate((1 - p) * 0.5);

        RenderUtil.drawBoxFilled(matrices, box, quadColor);
        RenderUtil.drawBox(matrices, box, lineColor, 1.5f, false);
    }

    private static int toArgb(float r, float g, float b, float a) {
        return ((int) (a * 255) << 24) | ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
    }
}
