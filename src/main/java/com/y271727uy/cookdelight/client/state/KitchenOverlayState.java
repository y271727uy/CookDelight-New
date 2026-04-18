package com.y271727uy.cookdelight.client.state;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

// Original kitchen overlay state restored.
public record KitchenOverlayState(
        BlockPos targetPos,
        List<ItemStack> visibleIngredients,
        List<Ingredient> recipeIngredients,
        ItemStack predictedOutput,
        int currentCookTime,
        int totalCookTime,
        boolean skillet,
        Component title,
        float previousFadeProgress,
        float fadeProgress
) {
    public KitchenOverlayState {
        visibleIngredients = visibleIngredients.stream().map(ItemStack::copy).toList();
        recipeIngredients = List.copyOf(recipeIngredients);
        predictedOutput = predictedOutput.copy();
        title = title == null ? Component.empty() : title;
        currentCookTime = Math.max(0, currentCookTime);
        totalCookTime = Math.max(0, totalCookTime);
    }

    public static KitchenOverlayState hidden() {
        return new KitchenOverlayState(null, List.of(), List.of(), ItemStack.EMPTY, 0, 0, false, Component.empty(), 0.0f, 0.0f);
    }
}
