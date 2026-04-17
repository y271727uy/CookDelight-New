package com.y271727uy.cookdelight.client.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public record ResolvedRecipe(ItemStack output, List<Ingredient> ingredients, String recipeTypeId) {
	public ResolvedRecipe {
		output = output.copy();
		ingredients = List.copyOf(ingredients);
		recipeTypeId = recipeTypeId == null ? "" : recipeTypeId;
	}

	public boolean hasIngredients() {
		return !ingredients.isEmpty();
	}
}

