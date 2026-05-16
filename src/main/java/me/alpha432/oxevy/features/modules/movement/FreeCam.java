package me.alpha432.oxevy.features.modules.movement;

import me.alpha432.oxevy.event.impl.render.Render3DEvent;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.render.RenderUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;

public class FreeCam extends Module {

    private final Setting<Double> horizontalSpeed = num("HSpeed", 1.0, 0.05, 10.0);
    private final Setting<Double> verticalSpeed = num("VSpeed", 1.0, 0.05, 5.0);
    private final Setting<Boolean> tracer = bool("Tracer", false);
    private final Setting<Boolean> hideHand = bool("HideHand", true);
    private final Setting<Boolean> disableOnDamage = bool("DisableOnDamage", true);
    private final Setting<Boolean> reloadChunks = bool("ReloadChunks", true);
    private final Setting<Boolean> lookAtPlayer = bool("LookAtPlayer", false);

    private Vec3 camPos;
    private Vec3 prevCamPos;
    private float camYaw;
    private float camPitch;
    private float lastHealth;

    public FreeCam() {
        super("FreeCam", "Client-side free camera that detaches from your body", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (nullCheck()) return;

        lastHealth = Float.MIN_VALUE;
        camPos = mc.player.getEyePosition();
        prevCamPos = camPos;
        camYaw = mc.player.getYRot();
        camPitch = mc.player.getXRot();
    }

    @Override
    public void onDisable() {
        if (nullCheck()) return;

        if (reloadChunks.getValue()) {
            mc.levelRenderer.allChanged();
        }
    }

    @Override
    public void onTick() {
        if (nullCheck()) return;

        if (camPos == null) {
            camPos = mc.player.getEyePosition();
            prevCamPos = camPos;
            camYaw = mc.player.getYRot();
            camPitch = mc.player.getXRot();
        }

        LocalPlayer player = mc.player;

        float currentHealth = player.getHealth();
        if (disableOnDamage.getValue() && currentHealth < lastHealth) {
            toggle();
            return;
        }
        lastHealth = currentHealth;

        if (mc.screen != null) {
            prevCamPos = camPos;
            return;
        }

        double forward = 0, strafe = 0;
        if (GLFW.glfwGetKey(mc.getWindow().handle(), GLFW.GLFW_KEY_W) == 1) forward = 1;
        if (GLFW.glfwGetKey(mc.getWindow().handle(), GLFW.GLFW_KEY_S) == 1) forward = -1;
        if (GLFW.glfwGetKey(mc.getWindow().handle(), GLFW.GLFW_KEY_A) == 1) strafe = -1;
        if (GLFW.glfwGetKey(mc.getWindow().handle(), GLFW.GLFW_KEY_D) == 1) strafe = 1;

        double vertical = 0;
        if (GLFW.glfwGetKey(mc.getWindow().handle(), GLFW.GLFW_KEY_SPACE) == 1) vertical = 1;
        if (GLFW.glfwGetKey(mc.getWindow().handle(), GLFW.GLFW_KEY_LEFT_SHIFT) == 1) vertical = -1;

        double yawRad = camYaw * Mth.DEG_TO_RAD;
        double sinYaw = Mth.sin(yawRad);
        double cosYaw = Mth.cos(yawRad);
        double offsetX = strafe * cosYaw - forward * sinYaw;
        double offsetZ = strafe * sinYaw + forward * cosYaw;

        double vSpeed = Mth.clamp(horizontalSpeed.getValue() * verticalSpeed.getValue(), 0.05, 10);
        double offsetY = vertical * vSpeed;

        Vec3 offsetVec = new Vec3(offsetX, 0, offsetZ).scale(horizontalSpeed.getValue()).add(0, offsetY, 0);
        prevCamPos = camPos;
        camPos = camPos.add(offsetVec);

        if (lookAtPlayer.getValue()) {
            Vec3 playerCenter = player.position().add(0, player.getBbHeight() * 0.5, 0);
            Vec3 dir = playerCenter.subtract(camPos);
            camYaw = (float) Math.toDegrees(Math.atan2(-dir.x, dir.z));
            camPitch = -(float) Math.toDegrees(Math.atan2(dir.y, Math.sqrt(dir.x * dir.x + dir.z * dir.z)));
            camPitch = Mth.clamp(camPitch, -90, 90);
        }
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (!tracer.getValue() || mc.player == null) return;

        Vec3 playerPos = mc.player.position().add(0, mc.player.getBbHeight() * 0.5, 0);
        Color color = new Color(255, 255, 255, 160);
        RenderUtil.drawLine(event.getMatrix(), camPos, playerPos, color, 1.5f, true);
    }

    @Override
    public String getDisplayInfo() {
        return horizontalSpeed.getValue() + ", " + verticalSpeed.getValue();
    }

    public Vec3 getCamPos(float partialTicks) {
        return Mth.lerp(partialTicks, prevCamPos, camPos);
    }

    public float getCamYaw() {
        return camYaw;
    }

    public float getCamPitch() {
        return camPitch;
    }

    public Vec3 getScaledCamDir(double scale) {
        return Vec3.directionFromRotation(camPitch, camYaw).scale(scale);
    }

    public boolean shouldHideHand() {
        return isEnabled() && hideHand.getValue();
    }

    public void turn(double deltaYaw, double deltaPitch) {
        if (lookAtPlayer.getValue()) return;
        camYaw += (float) (deltaYaw * 0.15);
        camPitch += (float) (deltaPitch * 0.15);
        camPitch = Mth.clamp(camPitch, -90, 90);
    }
}
