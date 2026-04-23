package me.alpha432.oxevy.features.modules.player;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import net.minecraft.network.chat.Component;

public class AutoRespawnModule extends Module {
    public final Setting<Integer> delay = num("Delay", 0, 0, 20);

    private int deathTick = 0;
    private boolean wasDead = false;

    public AutoRespawnModule() {
        super("AutoRespawn", "Automatically respawns", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (nullCheck()) return;

        boolean isDead = mc.player.getHealth() <= 0;

        if (isDead && !wasDead) {
            deathTick = 0;
        }

        if (isDead) {
            deathTick++;

            int requiredDelay = delay.getValue() * 20;

            if (deathTick >= requiredDelay) {
                mc.gui.getChat().addMessage(Component.literal("/spawn"));
            }
        }

        wasDead = isDead;
    }
}