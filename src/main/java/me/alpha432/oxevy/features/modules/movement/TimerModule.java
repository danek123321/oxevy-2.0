package me.alpha432.oxevy.features.modules.movement;

import me.alpha432.oxevy.Oxevy;
import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;

public class TimerModule extends Module {
    private final Setting<Float> speed = num("Speed", 1.5f, 0.1f, 4.0f);

    public TimerModule() {
        super("Timer", "Speeds up the game tick", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (nullCheck()) return;
        Oxevy.TIMER = speed.getValue();
    }

    @Override
    public void onDisable() {
        Oxevy.TIMER = 1.0f;
    }
}
