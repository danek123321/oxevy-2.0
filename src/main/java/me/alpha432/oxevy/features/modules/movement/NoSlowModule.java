package me.alpha432.oxevy.features.modules.movement;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;

public class NoSlowModule extends Module {
    public final Setting<Boolean> items = bool("Items", true);
    public final Setting<Boolean> soulsand = bool("Soulsand", true);

    public NoSlowModule() {
        super("NoSlow", "Removes slowdown when using items", Category.MOVEMENT);
    }

    public boolean shouldNoSlow() {
        if (!isEnabled() || nullCheck()) return false;
        return items.getValue();
    }
}