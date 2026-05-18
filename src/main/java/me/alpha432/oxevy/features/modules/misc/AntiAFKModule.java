package me.alpha432.oxevy.features.modules.misc;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class AntiAFKModule extends Module {

    public Setting<Float> range = num("Range", 3f, 1f, 16f);
    public Setting<Integer> interval = num("Interval", 30, 5, 300);
    public Setting<Boolean> rotate = bool("Rotate", true);

    private final Random random = new Random();
    private int timer;
    private BlockPos startPos;
    private BlockPos targetPos;
    private float targetYaw;
    private float targetPitch;
    private boolean walkingForward;

    public AntiAFKModule() {
        super("AntiAFK", "Prevents getting kicked for being AFK", Category.MISC);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            startPos = mc.player.blockPosition();
        }
        timer = 0;
        walkingForward = false;
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.options.keyUp.setDown(false);
            mc.options.keyJump.setDown(false);
        }
        walkingForward = false;
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        if (mc.player.getHealth() <= 0) {
            disable();
            return;
        }

        if (timer > 0) {
            timer--;
            if (walkingForward && mc.player.distanceToSqr(Vec3.atCenterOf(targetPos)) < 1.0) {
                mc.options.keyUp.setDown(false);
                walkingForward = false;
            }
            return;
        }

        mc.options.keyUp.setDown(false);
        mc.options.keyJump.setDown(false);

        int r = range.getValue().intValue();
        int rx = random.nextInt(2 * r + 1) - r;
        int rz = random.nextInt(2 * r + 1) - r;
        targetPos = startPos.offset(rx, 0, rz);

        if (rotate.getValue()) {
            targetYaw = mc.player.getYRot() + (random.nextFloat() - 0.5f) * 120f;
            targetPitch = Mth.clamp(mc.player.getXRot() + (random.nextFloat() - 0.5f) * 60f, -90f, 90f);
        }

        mc.options.keyUp.setDown(true);
        walkingForward = true;
        timer = interval.getValue() * 20;
    }

    @Override
    public String getDisplayInfo() {
        if (timer > 0)
            return timer / 20 + "s";
        return null;
    }
}
