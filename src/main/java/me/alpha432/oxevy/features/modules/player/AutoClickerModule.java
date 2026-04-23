package me.alpha432.oxevy.features.modules.player;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;

import java.util.Random;

public class AutoClickerModule extends Module {
    public final Setting<Integer> minCps = num("MinCPS", 8, 1, 20);
    public final Setting<Integer> maxCps = num("MaxCPS", 12, 1, 20);
    public final Setting<Boolean> leftClick = bool("LeftClick", true);
    public final Setting<Boolean> onlyOnGround = bool("OnlyOnGround", false);

    private long lastLeftClick = 0;
    private final Random random = new Random();

    public AutoClickerModule() {
        super("AutoClicker", "Automatically clicks", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (nullCheck()) return;

        if (onlyOnGround.getValue() && !mc.player.onGround()) return;

        if (leftClick.getValue()) {
            long delay = getDelay();
            if (System.currentTimeMillis() - lastLeftClick > delay) {
                if (mc.crosshairPickEntity != null) {
                    mc.gameMode.attack(mc.player, mc.crosshairPickEntity);
                    lastLeftClick = System.currentTimeMillis();
                }
            }
        }
    }

    private long getDelay() {
        int cps = minCps.getValue() + random.nextInt(maxCps.getValue() - minCps.getValue() + 1);
        return 1000 / Math.max(1, cps);
    }
}