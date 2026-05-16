package me.alpha432.oxevy.features.modules.player;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;

public class FastPlaceModule extends Module {
    private final Setting<Mode> mode = mode("Mode", Mode.ALL);

    public FastPlaceModule() {
        super("FastPlace", "Removes right-click delay", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (nullCheck()) return;

        switch (mode.getValue()) {
            case ALL -> mc.rightClickDelay = 0;
            case XP -> {
                if (mc.player.isHolding(net.minecraft.world.item.Items.EXPERIENCE_BOTTLE))
                    mc.rightClickDelay = 0;
            }
        }
    }

    private enum Mode {
        ALL, XP
    }
}
