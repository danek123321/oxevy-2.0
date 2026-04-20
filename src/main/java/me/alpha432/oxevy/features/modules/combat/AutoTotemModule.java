package me.alpha432.oxevy.features.modules.combat;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

public class AutoTotemModule extends Module {
    public final Setting<Boolean> checkHealth = bool("CheckHealth", true);
    public final Setting<Double> healthThreshold = num("HealthThreshold", 10.0, 1.0, 20.0);

    public AutoTotemModule() {
        super("AutoTotem", "Auto holds totems", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || !mc.player.isAlive()) return;

        ItemStack offhand = mc.player.getOffhandItem();
        
        // Already have totem
        if (offhand.getItem() == Items.TOTEM_OF_UNDYING) {
            return;
        }

        boolean needTotem = !checkHealth.getValue() || mc.player.getHealth() <= healthThreshold.getValue();

        if (needTotem) {
            // Find totem in inventory
            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.player.getInventory().getItem(i);
                if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                    // Set selected slot to totem
                    mc.player.getInventory().setSelectedSlot(i);
                    return;
                }
            }
        }
    }
}
