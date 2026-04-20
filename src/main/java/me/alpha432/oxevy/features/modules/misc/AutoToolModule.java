package me.alpha432.oxevy.features.modules.misc;

import me.alpha432.oxevy.features.modules.Module;
import me.alpha432.oxevy.util.inventory.InventoryUtil;
import me.alpha432.oxevy.util.inventory.Result;
import me.alpha432.oxevy.util.inventory.ResultType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import java.util.function.BiPredicate;

/**
 * AutoTool: Automatically switches to the best mining tool for the targeted block.
 */
public class AutoToolModule extends Module {
    private int lastSelectedSlot = -1;
    private boolean swappedThisSession = false;

    public AutoToolModule() {
        super("AutoTool", "Automatically switches to the best mining tool for the target block", Category.MISC);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        lastSelectedSlot = -1;
        swappedThisSession = false;
    }

    @Override
    public void onDisable() {
        // Revert to the previous slot if we swapped earlier
        if (swappedThisSession && lastSelectedSlot >= 0) {
            InventoryUtil.swap(lastSelectedSlot);
        }
        swappedThisSession = false;
        lastSelectedSlot = -1;
    }

    @Override
    public void onTick() {
        if (mc.level == null || mc.player == null) return;

        // Determine the block currently being targeted for breaking
        BlockPos targetPos = null;
        if (mc.hitResult instanceof BlockHitResult hit) {
            targetPos = hit.getBlockPos();
        }

        if (targetPos == null) return;

        BlockState state = mc.level.getBlockState(targetPos);
        Block block = state.getBlock();

        // Decide which tool we should use
        boolean wantAxe = isWoodBlock(block);
        boolean wantPick = isStoneLikeBlock(block);
        boolean wantShovel = isDirtLikeBlock(block);

        if (!wantAxe && !wantPick && !wantShovel) return; // unknown block; do nothing

        Result tool = null;
        if (wantAxe) {
            tool = findBestTool(Items.NETHERITE_AXE, Items.DIAMOND_AXE, Items.IRON_AXE, Items.STONE_AXE, Items.WOODEN_AXE);
        } else if (wantPick) {
            tool = findBestTool(Items.NETHERITE_PICKAXE, Items.DIAMOND_PICKAXE, Items.IRON_PICKAXE, Items.STONE_PICKAXE, Items.WOODEN_PICKAXE);
        } else if (wantShovel) {
            tool = findBestTool(Items.NETHERITE_SHOVEL, Items.DIAMOND_SHOVEL, Items.IRON_SHOVEL, Items.STONE_SHOVEL, Items.WOODEN_SHOVEL);
        }

        if (tool != null && tool.found()) {
            // remember current slot before swapping
            if (!swappedThisSession) {
                lastSelectedSlot = InventoryUtil.selected();
                swappedThisSession = true;
            }
            InventoryUtil.swap(tool);
        }
    }

    private Result findBestTool(Item first, Item... rest) {
        Result res = InventoryUtil.find(first, InventoryUtil.FULL_SCOPE);
        if (res.found()) return res;
        for (Item it : rest) {
            res = InventoryUtil.find(it, InventoryUtil.FULL_SCOPE);
            if (res.found()) return res;
        }
        return null;
    }

    // Simple heuristics for block types using Blocks constants
    private boolean isWoodBlock(Block b) {
        return b == Blocks.OAK_LOG || b == Blocks.BIRCH_LOG || b == Blocks.SPRUCE_LOG || b == Blocks.JUNGLE_LOG || b == Blocks.ACACIA_LOG || b == Blocks.DARK_OAK_LOG
                || b == Blocks.OAK_WOOD || b == Blocks.BIRCH_WOOD || b == Blocks.SPRUCE_WOOD || b == Blocks.JUNGLE_WOOD || b == Blocks.ACACIA_WOOD || b == Blocks.DARK_OAK_WOOD;
    }

    private boolean isStoneLikeBlock(Block b) {
        return b == Blocks.STONE || b == Blocks.COBBLESTONE || b == Blocks.STONE_BRICKS
                || b == Blocks.GRANITE || b == Blocks.DIORITE || b == Blocks.ANDESITE
                || b == Blocks.NETHERRACK || b == Blocks.BLACKSTONE
                || b == Blocks.IRON_ORE || b == Blocks.GOLD_ORE || b == Blocks.COAL_ORE;
    }

    private boolean isDirtLikeBlock(Block b) {
        return b == Blocks.DIRT || b == Blocks.GRASS_BLOCK || b == Blocks.SAND || b == Blocks.SANDSTONE;
    }
}
