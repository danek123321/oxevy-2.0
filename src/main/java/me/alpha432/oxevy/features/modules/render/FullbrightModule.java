package me.alpha432.oxevy.features.modules.render;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;

import java.lang.reflect.Field;

public class FullbrightModule extends Module {
    public final Setting<Float> brightness = num("Brightness", 1.0f, 0.0f, 1.0f);
    public final Setting<Boolean> gamma = bool("Gamma", true);

    private Object originalGammaOption;
    private double originalGammaValue;

    public FullbrightModule() {
        super("Fullbright", "Makes the game brighter", Category.RENDER);
    }

    @Override
    public void onEnable() {
        setGamma();
    }

    @Override
    public void onDisable() {
        resetGamma();
    }

    @Override
    public void onTick() {
        if (gamma.getValue()) {
            setGamma();
        }
    }

    private void setGamma() {
        if (mc.gameRenderer == null || mc.options == null) return;
        
        try {
            Field gammaField = mc.options.getClass().getDeclaredField("gamma");
            gammaField.setAccessible(true);
            Object gammaOption = gammaField.get(mc.options);
            
            if (originalGammaOption == null) {
                originalGammaOption = gammaOption;
                Field valueField = gammaOption.getClass().getDeclaredField("value");
                valueField.setAccessible(true);
                originalGammaValue = (double) valueField.get(gammaOption);
            }
            
            Field valueField = gammaOption.getClass().getDeclaredField("value");
            valueField.setAccessible(true);
            valueField.set(gammaOption, (double) brightness.getValue() * 10.0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetGamma() {
        if (mc.gameRenderer == null || mc.options == null || originalGammaOption == null) return;
        
        try {
            Field gammaField = mc.options.getClass().getDeclaredField("gamma");
            gammaField.setAccessible(true);
            Object gammaOption = gammaField.get(mc.options);
            
            Field valueField = gammaOption.getClass().getDeclaredField("value");
            valueField.setAccessible(true);
            valueField.set(gammaOption, originalGammaValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
