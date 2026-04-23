package me.alpha432.oxevy.features.modules.hud;

import me.alpha432.oxevy.features.gui.HudEditorScreen;
import me.alpha432.oxevy.features.modules.Module;

public class HudEditorModule extends Module {
    public HudEditorModule() {
        super("HudEditor", "Edit HUD element positions", Category.HUD);
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

