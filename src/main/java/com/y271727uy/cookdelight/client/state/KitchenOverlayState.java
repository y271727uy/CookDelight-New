package com.y271727uy.cookdelight.client.state;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public record KitchenOverlayState(
        BlockPos targetPos,
        List<ItemStack> visibleIngredients,
        List<Ingredient> recipeIngredients,
        ItemStack predictedOutput,
        int currentCookTime,
        int totalCookTime,
        boolean skillet,
        float previousFadeProgress,
        float fadeProgress
) {
    public KitchenOverlayState {
        visibleIngredients = visibleIngredients.stream().map(ItemStack::copy).toList();
        recipeIngredients = List.copyOf(recipeIngredients);
        predictedOutput = predictedOutput.copy();
        currentCookTime = Math.max(0, currentCookTime);
        totalCookTime = Math.max(0, totalCookTime);
    }

    public static KitchenOverlayState hidden() {
        return new KitchenOverlayState(null, List.of(), List.of(), ItemStack.EMPTY, 0, 0, false, 0.0f, 0.0f);
    }
}
