package me.alpha432.oxevy.features.modules.movement;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class FreeCam extends Module {

    private final Setting<Float> speed = num("Speed", 0.5f, 0.1f, 5.0f);
    private final Setting<Boolean> noClip = bool("NoClip", true);
    private final Setting<Boolean> freeze = bool("FreezePlayer", true);

    private Vec3 savedPosition;
    private float savedYaw;
    private float savedPitch;
    private boolean wasFlying;

    public FreeCam() {
        super("FreeCam", "Free camera movement while player stays in place", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (nullCheck()) return;

        savedPosition = new Vec3(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        savedYaw = mc.player.getYRot();
        savedPitch = mc.player.getXRot();
        wasFlying = mc.player.getAbilities().flying;

        if (freeze.getValue()) {
            mc.player.getAbilities().flying = true;
        }
    }

    @Override
    public void onDisable() {
        if (nullCheck()) return;

        if (savedPosition != null) {
            mc.player.setPos(savedPosition.x, savedPosition.y, savedPosition.z);
        }

        mc.player.setYRot(savedYaw);
        mc.player.setXRot(savedPitch);
        mc.player.getAbilities().flying = wasFlying;
        mc.player.fallDistance = 0;
    }

    @Override
    public void onTick() {
        if (nullCheck()) return;

        double forward = 0;
        double strafe = 0;
        float yaw = mc.player.getYRot();

        if (GLFW.glfwGetKey(mc.getWindow().handle(), GLFW.GLFW_KEY_W) == 1) forward = 1;
        if (GLFW.glfwGetKey(mc.getWindow().handle(), GLFW.GLFW_KEY_S) == 1) forward = -1;
        if (GLFW.glfwGetKey(mc.getWindow().handle(), GLFW.GLFW_KEY_A) == 1) strafe = -1;
        if (GLFW.glfwGetKey(mc.getWindow().handle(), GLFW.GLFW_KEY_D) == 1) strafe = 1;

        double vertical = 0;
        if (GLFW.glfwGetKey(mc.getWindow().handle(), GLFW.GLFW_KEY_SPACE) == 1) vertical = 1;
        if (GLFW.glfwGetKey(mc.getWindow().handle(), GLFW.GLFW_KEY_LEFT_SHIFT) == 1) vertical = -1;

        double speedValue = speed.getValue().doubleValue();

        double motionX = 0, motionY = 0, motionZ = 0;

        if (forward != 0 || strafe != 0) {
            double radians = Math.toRadians(yaw);
            double sin = Math.sin(radians);
            double cos = Math.cos(radians);

            motionX = forward * speedValue * -sin + strafe * speedValue * cos;
            motionZ = forward * speedValue * cos + strafe * speedValue * sin;
        }

        motionY = vertical * speedValue;

        Vec3 pos = new Vec3(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        double newX = pos.x + motionX;
        double newY = pos.y + motionY;
        double newZ = pos.z + motionZ;

        if (noClip.getValue()) {
            mc.player.setPos(newX, newY, newZ);
        } else {
            double clampedX = Math.max(-30000000, Math.min(30000000, newX));
            double clampedZ = Math.max(-30000000, Math.min(30000000, newZ));
            mc.player.setPos(clampedX, newY, clampedZ);
        }

        mc.player.setYHeadRot(yaw);
    }

}
