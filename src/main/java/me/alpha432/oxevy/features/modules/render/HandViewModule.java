package me.alpha432.oxevy.features.modules.render;

import me.alpha432.oxevy.event.impl.render.Render3DEvent;
import me.alpha432.oxevy.event.system.Subscribe;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;

public class HandViewModule extends Module {

    // General
    public Setting<Boolean> oldAnimations = bool("OldAnimations", false);
    public Setting<Boolean> skipSwapping = bool("SkipSwapping", false);
    public Setting<Boolean> disableEating = bool("DisableEating", false);
    public Setting<Boolean> swordSlash = bool("SwordSlash", false);
    public Setting<SwingMode> swingMode = mode("SwingMode", SwingMode.None);
    public Setting<Integer> swingSpeed = num("SwingSpeed", 6, 0, 20);
    public Setting<Double> mainSwing = num("MainSwing", 0.0, 0.0, 1.0);
    public Setting<Double> offSwing = num("OffSwing", 0.0, 0.0, 1.0);

    // Main Hand
    public Setting<Double> mainScaleX = num("MainScaleX", 1.0, 0.0, 5.0);
    public Setting<Double> mainScaleY = num("MainScaleY", 1.0, 0.0, 5.0);
    public Setting<Double> mainScaleZ = num("MainScaleZ", 1.0, 0.0, 5.0);
    public Setting<Double> mainPosX = num("MainPosX", 0.0, -3.0, 3.0);
    public Setting<Double> mainPosY = num("MainPosY", 0.0, -3.0, 3.0);
    public Setting<Double> mainPosZ = num("MainPosZ", 0.0, -3.0, 3.0);
    public Setting<Double> mainRotX = num("MainRotX", 0.0, -180.0, 180.0);
    public Setting<Double> mainRotY = num("MainRotY", 0.0, -180.0, 180.0);
    public Setting<Double> mainRotZ = num("MainRotZ", 0.0, -180.0, 180.0);

    // Off Hand
    public Setting<Double> offScaleX = num("OffScaleX", 1.0, 0.0, 5.0);
    public Setting<Double> offScaleY = num("OffScaleY", 1.0, 0.0, 5.0);
    public Setting<Double> offScaleZ = num("OffScaleZ", 1.0, 0.0, 5.0);
    public Setting<Double> offPosX = num("OffPosX", 0.0, -3.0, 3.0);
    public Setting<Double> offPosY = num("OffPosY", 0.0, -3.0, 3.0);
    public Setting<Double> offPosZ = num("OffPosZ", 0.0, -3.0, 3.0);
    public Setting<Double> offRotX = num("OffRotX", 0.0, -180.0, 180.0);
    public Setting<Double> offRotY = num("OffRotY", 0.0, -180.0, 180.0);
    public Setting<Double> offRotZ = num("OffRotZ", 0.0, -180.0, 180.0);

    // Arm
    public Setting<Double> armScaleX = num("ArmScaleX", 1.0, 0.0, 5.0);
    public Setting<Double> armScaleY = num("ArmScaleY", 1.0, 0.0, 5.0);
    public Setting<Double> armScaleZ = num("ArmScaleZ", 1.0, 0.0, 5.0);
    public Setting<Double> armPosX = num("ArmPosX", 0.0, -3.0, 3.0);
    public Setting<Double> armPosY = num("ArmPosY", 0.0, -3.0, 3.0);
    public Setting<Double> armPosZ = num("ArmPosZ", 0.0, -3.0, 3.0);
    public Setting<Double> armRotX = num("ArmRotX", 0.0, -180.0, 180.0);
    public Setting<Double> armRotY = num("ArmRotY", 0.0, -180.0, 180.0);
    public Setting<Double> armRotZ = num("ArmRotZ", 0.0, -180.0, 180.0);

    public HandViewModule() {
        super("HandView", "Alters the way items are rendered in your hands", Category.RENDER);
    }

    public boolean oldAnimations() {
        return isEnabled() && oldAnimations.getValue();
    }

    public boolean skipSwapping() {
        return isEnabled() && skipSwapping.getValue();
    }

    public boolean disableEating() {
        return isEnabled() && disableEating.getValue();
    }

    public boolean swordSlash() {
        return isEnabled() && swordSlash.getValue();
    }

    public int getSwingSpeed() {
        return isEnabled() ? swingSpeed.getValue() : 6;
    }

    public double getMainSwing() { return mainSwing.getValue(); }
    public double getOffSwing() { return offSwing.getValue(); }

    public enum SwingMode {
        Offhand,
        Mainhand,
        None
    }
}
