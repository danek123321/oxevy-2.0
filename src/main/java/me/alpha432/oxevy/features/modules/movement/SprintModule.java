package me.alpha432.oxevy.features.modules.movement;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class SprintModule extends Module {
    public final Setting<Boolean> checkServer = bool("CheckServer", true);
    public final Setting<Boolean> hive = bool("Hive", false);
    public final Setting<Boolean> onlyGround = bool("OnlyGround", false);
    public final Setting<Boolean> hunger = bool("Hunger", true);

    public SprintModule() {
        super("Sprint", "Makes you always sprint", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (nullCheck()) return;

        if (onlyGround.getValue() && !mc.player.onGround()) return;

        if (hunger.getValue() && mc.player.getFoodData().getFoodLevel() <= 6) return;

        if (!mc.player.isSprinting()) {
            mc.player.setSprinting(true);
        }
    }
}