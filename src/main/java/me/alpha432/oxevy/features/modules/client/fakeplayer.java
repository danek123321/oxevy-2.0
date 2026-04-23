package me.alpha432.oxevy.features.modules.client;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;

public class fakeplayer extends Module {

    public final Setting<Float> distance = num("Distance", 2.0f, 1.0f, 10.0f);

    public fakeplayer() {
        super("FakePlayer", "Follows you around", Category.CLIENT);
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onTick() {
    }
}