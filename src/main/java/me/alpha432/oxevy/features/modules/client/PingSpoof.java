package me.alpha432.oxevy.features.modules.client;

import me.alpha432.oxevy.features.modules.Module;

/**
 * PingSpoof: lightweight stub to satisfy compilation. Real ping spoofing can be implemented if desired.
 */
public class PingSpoof extends Module {
    public PingSpoof() {
        super("PingSpoof", "Spoof your ping to other players", Category.CLIENT);
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {
        // no-op default
    }

    @Override
    public void onTick() {
        // no-op default
    }

    @Override
    public String getDisplayInfo() {
        return "PingSpoof: OFF";
    }
}
