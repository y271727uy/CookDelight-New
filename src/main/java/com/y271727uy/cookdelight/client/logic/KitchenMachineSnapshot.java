package com.y271727uy.cookdelight.client.logic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record KitchenMachineSnapshot(
        BlockPos blockPos,
        List<ItemStack> ingredients,
        ItemStack explicitOutput,
        int currentCookTime,
        int totalCookTime
) {
    public KitchenMachineSnapshot {
        ingredients = ingredients.stream().map(ItemStack::copy).toList();
        explicitOutput = explicitOutput.copy();
        currentCookTime = Math.max(0, currentCookTime);
        totalCookTime = Math.max(0, totalCookTime);
    }

    public static KitchenMachineSnapshot empty(BlockPos blockPos) {
        return new KitchenMachineSnapshot(blockPos, List.of(), ItemStack.EMPTY, 0, 0);
    }
}

