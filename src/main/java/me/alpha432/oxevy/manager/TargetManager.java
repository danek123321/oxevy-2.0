package me.alpha432.oxevy.manager;

import me.alpha432.oxevy.features.Feature;
import net.minecraft.world.entity.LivingEntity;

public class TargetManager extends Feature {
    private LivingEntity target;
    private int comboCount;
    private long lastHitTime;
    private static final long COMBO_TIMEOUT_MS = 2000;

    public void setTarget(LivingEntity entity) {
        if (target != null && entity != null && target.getId() != entity.getId()) {
            comboCount = 0;
        }
        this.target = entity;
    }

    public LivingEntity getTarget() {
        if (target != null && (target.isRemoved() || !target.isAlive())) {
            target = null;
            comboCount = 0;
        }
        return target;
    }

    public void recordHit(LivingEntity entity) {
        if (entity == null) return;
        if (target != null && target.getId() == entity.getId()) {
            long now = System.currentTimeMillis();
            if (now - lastHitTime > COMBO_TIMEOUT_MS) {
                comboCount = 0;
            }
            comboCount++;
            lastHitTime = now;
        } else {
            comboCount = 1;
            lastHitTime = System.currentTimeMillis();
        }
    }

    public void clearTarget() {
        this.target = null;
        this.comboCount = 0;
    }

    public int getComboCount() {
        if (target == null) return 0;
        if (System.currentTimeMillis() - lastHitTime > COMBO_TIMEOUT_MS) {
            comboCount = 0;
        }
        return comboCount;
    }

    public long getLastHitTime() {
        return lastHitTime;
    }
}
