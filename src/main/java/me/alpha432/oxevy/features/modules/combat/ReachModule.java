package me.alpha432.oxevy.features.modules.combat;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class ReachModule extends Module {
    public final Setting<Double> combatReach = num("CombatReach", 3.5, 3.0, 6.0);
    public final Setting<Double> blockReach = num("BlockReach", 4.5, 4.5, 6.0);

    private double oldCombatReach;
    private double oldBlockReach;

    public ReachModule() {
        super("Reach", "Increase reach distance", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;
        
        oldCombatReach = mc.player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
        oldBlockReach = mc.player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
        
        mc.player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE).setBaseValue(combatReach.getValue());
        mc.player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE).setBaseValue(blockReach.getValue());
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        
        mc.player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE).setBaseValue(3.0);
        mc.player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE).setBaseValue(4.5);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        
        mc.player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE).setBaseValue(combatReach.getValue());
        mc.player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE).setBaseValue(blockReach.getValue());
    }

    public double getCombatReach() {
        return isEnabled() ? combatReach.getValue() : 3.0;
    }

    public double getBlockReach() {
        return isEnabled() ? blockReach.getValue() : 4.5;
    }
}
