package me.alpha432.oxevy.features.modules.render;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;

public class NoWeatherModule extends Module {
    public final Setting<Boolean> disableRain = bool("DisableRain", true);
    public final Setting<Boolean> disableThunder = bool("DisableThunder", true);

    public NoWeatherModule() {
        super("NoWeather", "Disables rain and thunder", Category.RENDER);
    }

    @Override
    public void onTick() {
        if (nullCheck()) return;

        if (disableRain.getValue()) {
            mc.level.setRainLevel(0);
        }

        if (disableThunder.getValue()) {
            mc.level.setThunderLevel(0);
        }
    }
}
