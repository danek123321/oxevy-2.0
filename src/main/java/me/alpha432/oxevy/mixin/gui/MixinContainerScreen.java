package me.alpha432.oxevy.mixin.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class MixinContainerScreen {

    @Shadow
    protected AbstractContainerMenu menu;

    @Shadow
    protected int leftPos;

    @Shadow
    protected int topPos;

    @Shadow
    protected int imageWidth;

    @Shadow
    protected abstract void slotClicked(Slot slot, int slotIndex, int button, ClickType actionType);

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        if ((Object)this instanceof CreativeModeInventoryScreen) return;
        int totalSlots = menu.slots.size();
        int containerSlots = totalSlots - 36;
        if (containerSlots < 9 || containerSlots > 54 || containerSlots % 9 != 0) return;

        ((MixinScreenAccessor)(Object)this).invokeAddRenderableWidget(Button.builder(Component.literal("Steal"),
                b -> {
                    for (int i = 0; i < containerSlots; i++) {
                        Slot slot = menu.slots.get(i);
                        if (!slot.hasItem()) continue;
                        slotClicked(slot, slot.index, 0, ClickType.QUICK_MOVE);
                    }
                })
            .bounds(leftPos + imageWidth - 108, topPos + 4, 50, 12)
            .build());

        ((MixinScreenAccessor)(Object)this).invokeAddRenderableWidget(Button.builder(Component.literal("Store"),
                b -> {
                    int playerEnd = containerSlots + 36;
                    for (int i = containerSlots; i < playerEnd; i++) {
                        Slot slot = menu.slots.get(i);
                        if (!slot.hasItem()) continue;
                        slotClicked(slot, slot.index, 0, ClickType.QUICK_MOVE);
                    }
                })
            .bounds(leftPos + imageWidth - 56, topPos + 4, 50, 12)
            .build());

        int dropY = topPos + menu.slots.get(containerSlots).y - 18;
        ((MixinScreenAccessor)(Object)this).invokeAddRenderableWidget(Button.builder(Component.literal("Drop"),
                b -> {
                    for (int i = 0; i < containerSlots; i++) {
                        Slot slot = menu.slots.get(i);
                        if (!slot.hasItem()) continue;
                        slotClicked(slot, slot.index, 1, ClickType.THROW);
                    }
                })
            .bounds(leftPos + imageWidth / 2 - 25, dropY, 50, 12)
            .build());
    }
}
