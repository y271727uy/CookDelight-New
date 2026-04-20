package com.y271727uy.cookdelight.client.recipe;

import com.y271727uy.cookdelight.config.CookDelightConfig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ItemFrameRecipeLookup {
    private final Map<OutputLookupKey, Optional<ResolvedRecipe>> outputCache = new HashMap<>();
    private RecipeManager cachedRecipeManager;

    public Optional<ResolvedRecipe> findRecipe(Level level, ItemStack output) {
        if (level == null || output.isEmpty()) {
            return Optional.empty();
        }

        syncRecipeManager(level);
        List<String> patterns = RecipeLookupSupport.normalizePatterns(CookDelightConfig.itemFrameRecipeTypes());
        OutputLookupKey cacheKey = new OutputLookupKey(RecipeLookupSupport.buildItemIdentityKey(output), patterns);
        return outputCache.computeIfAbsent(cacheKey, ignored -> resolveRecipe(level, output, patterns));
    }

    public boolean isLikelyFood(ItemStack stack) {
        return RecipeLookupSupport.isLikelyFood(stack);
    }

    private Optional<ResolvedRecipe> resolveRecipe(Level level, ItemStack output, List<String> patterns) {
        return level.getRecipeManager().getRecipes().stream()
                .map(recipe -> RecipeLookupSupport.toCandidate(level, recipe))
                .filter(candidate -> !candidate.output().isEmpty())
                .filter(candidate -> ItemStack.isSameItemSameTags(candidate.output(), output))
                .filter(candidate -> !candidate.ingredients().isEmpty())
                .sorted(RecipeLookupSupport.outputComparator(patterns))
                .map(RecipeLookupSupport::toResolvedRecipe)
                .findFirst();
    }

    private void syncRecipeManager(Level level) {
        RecipeManager currentRecipeManager = level.getRecipeManager();
        if (cachedRecipeManager != currentRecipeManager) {
            cachedRecipeManager = currentRecipeManager;
            outputCache.clear();
        }
    }

    private record OutputLookupKey(String outputKey, List<String> patterns) {
        private OutputLookupKey {
            patterns = List.copyOf(patterns);
        }
    }
}



