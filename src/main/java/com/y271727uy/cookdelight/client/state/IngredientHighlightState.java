package com.y271727uy.cookdelight.client.state;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public record IngredientHighlightState(
        int hoveredSlotIndex,
        ItemStack trackingItem,
        List<Ingredient> requiredIngredients,
        boolean active
) {
    public IngredientHighlightState {
        trackingItem = trackingItem.copy();
        requiredIngredients = List.copyOf(requiredIngredients);
    }

    public static IngredientHighlightState inactive() {
        return new IngredientHighlightState(-1, ItemStack.EMPTY, List.of(), false);
    }
}
