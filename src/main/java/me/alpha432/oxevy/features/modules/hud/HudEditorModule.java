package me.alpha432.oxevy.features.modules.client;

import me.alpha432.oxevy.features.gui.HudEditorScreen;
import me.alpha432.oxevy.features.modules.Module;

public class HudEditorModule extends Module {
    public HudEditorModule() {
        super("HudEditor", "Edit HUD element positions", Category.CLIENT);
    }

    @Override
    public void onEnable() {
        if (nullCheck()) {
            disable();
            return;
        }
        mc.setScreen(HudEditorScreen.getInstance());
        disable();
    }
}

