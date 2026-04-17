package com.y271727uy.cookdelight.client.state;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public record ItemFrameOverlayState(
		BlockPos targetPos,
		ItemStack targetItem,
		List<Ingredient> ingredients,
		float previousFadeProgress,
		float fadeProgress,
		int itemsPerRow,
		int rows
) {
	public ItemFrameOverlayState {
		targetItem = targetItem.copy();
		ingredients = List.copyOf(ingredients);
		itemsPerRow = Math.max(0, itemsPerRow);
		rows = Math.max(0, rows);
	}

	public static ItemFrameOverlayState hidden() {
		return new ItemFrameOverlayState(null, ItemStack.EMPTY, List.of(), 0.0f, 0.0f, 0, 0);
	}
}

