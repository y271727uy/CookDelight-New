package com.y271727uy.cookdelight.client.gui;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

import java.util.Locale;

final class RecipeTypeCompat {

    private RecipeTypeCompat() {
    }

    static ResourceLocation getRecipeTypeId(RegistryAccess registryAccess, Recipe<?> recipe) {
        return registryAccess.registryOrThrow(Registries.RECIPE_TYPE).getKey(recipe.getType());
    }

    static boolean matchesExactType(RegistryAccess registryAccess, Recipe<?> recipe, String... exactIds) {
        ResourceLocation typeId = getRecipeTypeId(registryAccess, recipe);
        if (typeId == null) return false;

        String typeString = typeId.toString();
        for (String exactId : exactIds) {
            if (typeString.equals(exactId)) return true;
        }
        return false;
    }

    static boolean matchesTypeKeywords(RegistryAccess registryAccess, Recipe<?> recipe, String... keywords) {
        ResourceLocation typeId = getRecipeTypeId(registryAccess, recipe);
        if (typeId == null) return false;

        String namespace = typeId.getNamespace().toLowerCase(Locale.ROOT);
        String path = typeId.getPath().toLowerCase(Locale.ROOT);
        for (String keyword : keywords) {
            String normalized = keyword.toLowerCase(Locale.ROOT);
            if (namespace.contains(normalized) || path.contains(normalized)) {
                return true;
            }
        }
        return false;
    }

    static boolean matchesExactThenKeywords(RegistryAccess registryAccess, Recipe<?> recipe, String exactId, String... keywords) {
        return matchesExactType(registryAccess, recipe, exactId) || matchesTypeKeywords(registryAccess, recipe, keywords);
    }

    static boolean matchesNamespaceAndKeywords(RegistryAccess registryAccess, Recipe<?> recipe, String namespace, String... keywords) {
        ResourceLocation typeId = getRecipeTypeId(registryAccess, recipe);
        if (typeId == null) return false;

        if (namespace.equals(typeId.getNamespace())) return true;
        return matchesTypeKeywords(registryAccess, recipe, keywords);
    }
}

