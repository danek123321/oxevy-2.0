package me.alpha432.oxevy.features.modules.player;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;

public class AirPlaceModule extends Module {
    public final Setting<Boolean> noDelay = bool("NoDelay", true);

    public AirPlaceModule() {
        super("AirPlace", "Place blocks faster", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;
        
        if (noDelay.getValue()) {
            mc.rightClickDelay = 0;
        }
    }
}
