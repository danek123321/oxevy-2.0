package me.alpha432.oxevy.features.modules.combat;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.ClickType;

public class AutoTotemModule extends Module {
    public final Setting<Boolean> checkHealth = bool("CheckHealth", true);
    public final Setting<Double> healthThreshold = num("HealthThreshold", 10.0, 1.0, 20.0);

    private int clickDelay = 0;
    private int foundSlot = -1;

    public AutoTotemModule() {
        super("AutoTotem", "Auto holds totems", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        clickDelay = 0;
        foundSlot = -1;
    }

    @Override
    public void onTick() {
        if (mc.player == null || !mc.player.isAlive()) return;

        ItemStack offhand = mc.player.getOffhandItem();

        if (offhand.getItem() == Items.TOTEM_OF_UNDYING) {
            clickDelay = 0;
            foundSlot = -1;
            return;
        }

        boolean needTotem = !checkHealth.getValue() || mc.player.getHealth() <= healthThreshold.getValue();
        if (!needTotem) return;

        if (foundSlot < 0) {
            for (int i = 0; i < 36; i++) {
                if (mc.player.getInventory().getItem(i).getItem() == Items.TOTEM_OF_UNDYING) {
                    foundSlot = i;
                    break;
                }
            }
            if (foundSlot < 0) return;
        }

        int containerId = mc.player.containerMenu.containerId;

        if (clickDelay == 0) {
            // i 0-8 = hotbar -> container 36-44
            // i 9-35 = main -> container 9-35
            int containerSlot = foundSlot < 9 ? foundSlot + 36 : foundSlot;
            mc.gameMode.handleInventoryMouseClick(containerId, containerSlot, 0, ClickType.PICKUP, mc.player);
            clickDelay = 1;
        } else if (clickDelay == 1) {
            mc.gameMode.handleInventoryMouseClick(containerId, 45, 0, ClickType.PICKUP, mc.player);
            clickDelay = 0;
            foundSlot = -1;
        }
    }
}