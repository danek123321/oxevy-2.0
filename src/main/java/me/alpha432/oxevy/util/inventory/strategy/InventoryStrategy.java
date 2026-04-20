package me.alpha432.oxevy.util.inventory.strategy;

import me.alpha432.oxevy.util.inventory.InventoryUtil;
import me.alpha432.oxevy.util.inventory.Result;
import me.alpha432.oxevy.util.inventory.ResultType;
import net.minecraft.world.inventory.ClickType;

public final class InventoryStrategy implements SwapStrategy {
    public static final InventoryStrategy INSTANCE = new InventoryStrategy();

    private InventoryStrategy() {
    }

    @Override
    public boolean swap(Result result) {
        if (result.type() != ResultType.INVENTORY && result.type() != ResultType.HOTBAR)
            return false;
        int slot = inventorySlot(result);
        InventoryUtil.click(slot, InventoryUtil.selected(), ClickType.SWAP);
        return true;
    }

    @Override
    public boolean swapBack(int last, Result result) {
        return swap(result);
    }

    private static int inventorySlot(Result result) {
        return result.type() == ResultType.HOTBAR ? result.slot() + 36 : result.slot();
    }
}
