package me.alpha432.oxevy.features.modules.combat;

import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import net.minecraft.world.phys.Vec3;

public class Strafe extends Module {

    private final Setting<Mode> mode = mode("Mode", Mode.STRAFE);
    private final Setting<Float> speed = num("Speed", 0.28f, 0.1f, 2.0f);
    private final Setting<Boolean> autoJump = bool("AutoJump", true);
    private final Setting<Boolean> strict = bool("Strict", false);
    private final Setting<Boolean> speedCheck = bool("SpeedCheck", true);
    private final Setting<Boolean> lowHop = bool("LowHop", false);
    private final Setting<Float> motionY = num("MotionY", 0.42f, 0.1f, 1.0f);
    private final Setting<Boolean> timerBoost = bool("TimerBoost", false);
    private final Setting<Float> timerSpeed = num("TimerSpeed", 1.2f, 1.0f, 2.0f);
    private final Setting<Boolean> strafeJump = bool("StrafeJump", true);

    private int strafeStage = 0;
    private double moveSpeed = 0;
    private double lastDist = 0;
    private int jumpTicks = 0;

    public Strafe() {
        super("Strafe", "Advanced movement control", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (nullCheck()) return;

        strafeStage = 0;
        moveSpeed = 0;
        lastDist = 0;
        jumpTicks = 0;
    }

    @Override
    public void onDisable() {
        if (nullCheck()) return;

        // Reset timer
        if (timerBoost.getValue()) {
            Oxevy.TIMER = 1.0f;
        }
    }

    @Override
    public void onTick() {
        if (nullCheck()) return;

        // Timer boost
        if (timerBoost.getValue()) {
            Oxevy.TIMER = timerSpeed.getValue();
        }

        // Auto jump
        if (autoJump.getValue() && mc.player.onGround() && isMoving()) {
            mc.player.jumpFromGround();
        }

        // Apply strafe based on mode
        switch (mode.getValue()) {
            case STRAFE:
                normalStrafe();
                break;
            case BHOP:
                bhopStrafe();
                break;
            case LOWHOP:
                lowhopStrafe();
                break;
            case YPORT:
                yportStrafe();
                break;
            case CONSTANT:
                constantStrafe();
                break;
        }
    }

    private void normalStrafe() {
        if (!isMoving()) {
            moveSpeed = 0;
            return;
        }

        if (speedCheck.getValue() && mc.player.zza <= 0) return;

        if (mc.player.onGround()) {
            strafeStage = 2;
            moveSpeed = getBaseSpeed() * 1.35;

            if (strafeJump.getValue()) {
                mc.player.jumpFromGround();
                if (lowHop.getValue()) {
                    mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, 0.4, mc.player.getDeltaMovement().z);
                }
            }
        } else if (strafeStage == 2) {
            strafeStage = 3;
            moveSpeed = lastDist * (strict.getValue() ? 1.395 : 1.52);
        } else if (strafeStage == 3) {
            strafeStage = 4;
            double diff = 0.66 * (lastDist - getBaseSpeed());
            moveSpeed = lastDist - diff;
        } else {
            if (mc.level.getGameTime() % 5 == 0) {
                moveSpeed = lastDist - (lastDist / 159.0);
            }
        }

        moveSpeed = Math.max(moveSpeed, getBaseSpeed());
        setMotion(moveSpeed);

        lastDist = Math.sqrt(
                Math.pow(mc.player.getX() - mc.player.xo, 2) +
                        Math.pow(mc.player.getZ() - mc.player.zo, 2)
        );
    }

    private void bhopStrafe() {
        if (!isMoving()) return;

        if (mc.player.onGround()) {
            mc.player.jumpFromGround();
            moveSpeed = getBaseSpeed() * 2.0;
        } else {
            moveSpeed = getBaseSpeed() * 1.2;
        }

        setMotion(moveSpeed);
    }

    private void lowhopStrafe() {
        if (!isMoving()) return;

        if (mc.player.onGround()) {
            mc.player.jumpFromGround();
            mc.player.setDeltaMovement(
                    mc.player.getDeltaMovement().x,
                    motionY.getValue().doubleValue(),
                    mc.player.getDeltaMovement().z
            );
            moveSpeed = getBaseSpeed() * 1.8;
        } else {
            moveSpeed = getBaseSpeed() * 1.2;
        }

        setMotion(moveSpeed);
    }

    private void yportStrafe() {
        if (!isMoving()) return;

        jumpTicks++;

        if (mc.player.onGround()) {
            mc.player.jumpFromGround();
            moveSpeed = getBaseSpeed() * 2.5;
            jumpTicks = 0;
        } else if (jumpTicks == 1) {
            moveSpeed = getBaseSpeed() * 1.9;
        } else if (jumpTicks == 2) {
            moveSpeed = getBaseSpeed() * 1.7;
        } else if (jumpTicks == 3) {
            moveSpeed = getBaseSpeed() * 1.5;
        } else {
            moveSpeed = getBaseSpeed() * 1.3;
        }

        setMotion(moveSpeed);
    }

    private void constantStrafe() {
        if (!isMoving()) {
            moveSpeed = 0;
            return;
        }

        if (mc.player.onGround() && autoJump.getValue()) {
            mc.player.jumpFromGround();
        }

        moveSpeed = speed.getValue().doubleValue();
        setMotion(moveSpeed);
    }

    private void setMotion(double speed) {
        float forward = mc.player.zza;
        float strafe = mc.player.xxa;
        float yaw = mc.player.getYRot();

        if (forward == 0 && strafe == 0) return;

        // Calculate movement direction
        if (forward != 0) {
            if (strafe > 0) {
                yaw += forward > 0 ? -45 : 45;
            } else if (strafe < 0) {
                yaw += forward > 0 ? 45 : -45;
            }
            strafe = 0;
        }

        double radians = Math.toRadians(yaw);
        double sin = -Math.sin(radians) * forward;
        double cos = Math.cos(radians) * forward;

        double motionX = sin * speed + strafe * speed * Math.cos(radians);
        double motionZ = cos * speed + strafe * speed * Math.sin(radians);

        // Apply motion
        if (isMoving()) {
            mc.player.setDeltaMovement(motionX, mc.player.getDeltaMovement().y, motionZ);
        }
    }

    private boolean isMoving() {
        return mc.player.zza != 0 || mc.player.xxa != 0;
    }

    private double getBaseSpeed() {
        double baseSpeed = 0.272;
        if (mc.player.isSprinting()) {
            baseSpeed *= 1.3;
        }
        return baseSpeed;
    }

    public enum Mode {
        STRAFE,
        BHOP,
        LOWHOP,
        YPORT,
        CONSTANT
    }
}