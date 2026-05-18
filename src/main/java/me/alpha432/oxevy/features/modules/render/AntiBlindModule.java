package me.alpha432.oxevy.features.modules.render;

import me.alpha432.oxevy.features.modules.Module;

public class AntiBlindModule extends Module {
    private double savedDarknessScale = -1.0;

    public AntiBlindModule() {
        super("AntiBlind", "Removes blindness and darkness effects", Category.RENDER);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.options != null) {
            savedDarknessScale = mc.options.darknessEffectScale().get();
            mc.options.darknessEffectScale().set(0.0);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.options != null && savedDarknessScale >= 0.0) {
            mc.options.darknessEffectScale().set(savedDarknessScale);
            savedDarknessScale = -1.0;
        }
    }
}
