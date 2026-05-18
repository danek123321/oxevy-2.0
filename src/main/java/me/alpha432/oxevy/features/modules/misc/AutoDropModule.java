package me.alpha432.oxevy.features.modules.misc;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.features.settings.Setting;
import me.alpha432.oxevy.util.inventory.InventoryUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

public class AutoDropModule extends Module {

    public Setting<String> itemList = str("Items", "minecraft:rotten_flesh,minecraft:poisonous_potato,minecraft:wheat_seeds,minecraft:bone,minecraft:arrow");
    public Setting<Boolean> dropEntireStack = bool("Drop Entire Stack", true);
    public Setting<Boolean> inventoryOnly = bool("Inventory Only", false);

    private List<String> cachedItems;
    private long lastCacheTime;

    public AutoDropModule() {
        super("AutoDrop", "Automatically drops specified items from inventory", Category.MISC);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        if (mc.screen instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
                && !(mc.screen instanceof net.minecraft.client.gui.screens.inventory.InventoryScreen))
            return;

        long now = System.currentTimeMillis();
        if (cachedItems == null || now - lastCacheTime > 5000) {
            String raw = itemList.getValue();
            cachedItems = raw == null || raw.isEmpty()
                    ? List.of()
                    : Arrays.asList(raw.split(","));
            lastCacheTime = now;
        }

        if (cachedItems.isEmpty()) return;

        for (int slot = 9; slot < 45; slot++) {
            int invSlot = slot >= 36 ? slot - 36 : slot;
            ItemStack stack = mc.player.getInventory().getItem(invSlot);
            if (stack.isEmpty()) continue;

            String itemName = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            if (!cachedItems.contains(itemName)) continue;

            InventoryUtil.click(slot, dropEntireStack.getValue() ? 1 : 0, ClickType.THROW);
        }
    }
}
