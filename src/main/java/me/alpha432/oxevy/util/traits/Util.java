package me.alpha432.oxevy.util.traits;

import me.alpha432.oxevy.event.system.EventBus;
import net.minecraft.client.Minecraft;

public interface Util {
    Minecraft mc = Minecraft.getInstance();
    EventBus EVENT_BUS = new EventBus();
}
