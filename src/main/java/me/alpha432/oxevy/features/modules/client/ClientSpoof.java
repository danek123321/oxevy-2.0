package me.alpha432.oxevy.features.modules.client;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;

public class ClientSpoof extends Module {

    public final Setting<Mode> mode = mode("Mode", Mode.VANILLA);
    public final Setting<String> customBrand = str("CustomBrand", "vanilla");

    public ClientSpoof() {
        super("ClientSpoof", "Spoof your client information", Category.CLIENT);
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    public enum Mode {
        VANILLA,
        FORGE,
        FABRIC,
        LUNAR,
        BADLION,
        LABYMOD,
        CUSTOM
    }
}