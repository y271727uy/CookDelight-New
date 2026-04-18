package com.y271727uy.cookdelight.client.recipe;

import com.y271727uy.cookdelight.config.CookDelightConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class RecipeLookupService {
    private final Map<String, Optional<ResolvedRecipe>> outputCache = new HashMap<>();
    private final Map<String, Optional<ResolvedRecipe>> ingredientCache = new HashMap<>();
    private RecipeManager cachedRecipeManager;

    public RecipeLookupService() {
    }

    public Optional<ResolvedRecipe> findPreferredRecipeByOutput(Level level, ItemStack output) {
        if (level == null || output.isEmpty()) {
            return Optional.empty();
        }

        syncRecipeManager(level);
        String cacheKey = buildOutputCacheKey(output);
        return outputCache.computeIfAbsent(cacheKey, ignored -> resolvePreferredRecipe(level, output));
    }

    public Optional<ResolvedRecipe> findPredictedRecipe(Level level, List<ItemStack> inputs, LookupProfile profile) {
        if (level == null || inputs == null || inputs.isEmpty()) {
            return Optional.empty();
        }

        List<ItemStack> normalizedInputs = inputs.stream()
                .filter(stack -> stack != null && !stack.isEmpty())
                .map(ItemStack::copy)
                .toList();

        if (normalizedInputs.isEmpty()) {
            return Optional.empty();
        }

        syncRecipeManager(level);
        String cacheKey = buildIngredientCacheKey(normalizedInputs, profile);
        return ingredientCache.computeIfAbsent(cacheKey, ignored -> resolvePredictedRecipe(level, normalizedInputs, profile));
    }

    public boolean isLikelyFood(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        if (stack.getItem().isEdible()) {
            return true;
        }

        String itemId = normalize(getItemId(stack).toString());
        for (String keyword : CookDelightConfig.CLIENT.foodKeywordHints.get()) {
            String normalizedKeyword = normalize(keyword);
            if (!normalizedKeyword.isEmpty() && itemId.contains(normalizedKeyword)) {
                return true;
            }
        }
        return false;
    }

    private Optional<ResolvedRecipe> resolvePreferredRecipe(Level level, ItemStack output) {
        List<Recipe<?>> matches = new ArrayList<>();
        for (Recipe<?> recipe : level.getRecipeManager().getRecipes()) {
            ItemStack result = recipe.getResultItem(level.registryAccess());
            if (!result.isEmpty() && ItemStack.isSameItemSameTags(result, output)) {
                matches.add(recipe);
            }
        }

        if (matches.isEmpty()) {
            return Optional.empty();
        }

        List<String> priorities = normalizedPatterns(LookupProfile.OUTPUT_PREFERENCE);
        matches.sort(Comparator.comparingInt(recipe -> priorityOf(level, recipe, priorities)));
        return Optional.of(toResolvedRecipe(level, matches.get(0)));
    }

    private Optional<ResolvedRecipe> resolvePredictedRecipe(Level level, List<ItemStack> inputs, LookupProfile profile) {
        List<String> patterns = normalizedPatterns(profile);
        List<Recipe<?>> candidates = level.getRecipeManager().getRecipes().stream()
                .filter(recipe -> patterns.isEmpty() || matchesConfiguredType(level, recipe, patterns))
                .sorted(Comparator.comparingInt(recipe -> priorityOf(level, recipe, patterns)))
                .toList();

        for (Recipe<?> recipe : candidates) {
            List<Ingredient> ingredients = compactIngredients(recipe);
            if (!ingredients.isEmpty() && matchesAllIngredients(inputs, ingredients)) {
                return Optional.of(toResolvedRecipe(level, recipe));
            }
        }

        return Optional.empty();
    }

    private ResolvedRecipe toResolvedRecipe(Level level, Recipe<?> recipe) {
        return new ResolvedRecipe(
                recipe.getResultItem(level.registryAccess()),
                compactIngredients(recipe),
                recipeTypeId(level, recipe)
        );
    }

    private List<Ingredient> compactIngredients(Recipe<?> recipe) {
        return recipe.getIngredients().stream()
                .filter(ingredient -> ingredient != null && !ingredient.isEmpty())
                .toList();
    }

    private boolean matchesAllIngredients(List<ItemStack> inputs, List<Ingredient> ingredients) {
        List<ItemStack> expandedInputs = expandInputs(inputs, ingredients.size());
        return matchIngredientAt(ingredients, expandedInputs, 0, new boolean[expandedInputs.size()]);
    }

    private boolean matchIngredientAt(List<Ingredient> ingredients, List<ItemStack> inputs, int ingredientIndex, boolean[] usedInputs) {
        if (ingredientIndex >= ingredients.size()) {
            return true;
        }

        Ingredient ingredient = ingredients.get(ingredientIndex);
        for (int inputIndex = 0; inputIndex < inputs.size(); inputIndex++) {
            if (usedInputs[inputIndex]) {
                continue;
            }

            ItemStack input = inputs.get(inputIndex);
            if (!ingredient.test(input)) {
                continue;
            }

            usedInputs[inputIndex] = true;
            if (matchIngredientAt(ingredients, inputs, ingredientIndex + 1, usedInputs)) {
                return true;
            }
            usedInputs[inputIndex] = false;
        }

        return false;
    }

    private List<ItemStack> expandInputs(List<ItemStack> inputs, int maxIngredients) {
        List<ItemStack> expanded = new ArrayList<>();
        for (ItemStack stack : inputs) {
            int copies = Math.max(1, Math.min(stack.getCount(), maxIngredients));
            for (int i = 0; i < copies; i++) {
                expanded.add(stack.copy());
            }
        }
        return expanded;
    }

    private int priorityOf(Level level, Recipe<?> recipe, List<String> patterns) {
        String typeId = normalize(recipeTypeId(level, recipe));
        for (int index = 0; index < patterns.size(); index++) {
            if (typeId.contains(patterns.get(index))) {
                return index;
            }
        }
        return Integer.MAX_VALUE;
    }

    private boolean matchesConfiguredType(Level level, Recipe<?> recipe, List<String> patterns) {
        String typeId = normalize(recipeTypeId(level, recipe));
        for (String pattern : patterns) {
            if (!pattern.isEmpty() && typeId.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    private List<String> normalizedPatterns(LookupProfile profile) {
        List<? extends String> rawPatterns = switch (profile) {
            case OUTPUT_PREFERENCE -> CookDelightConfig.CLIENT.preferredOutputRecipeTypes.get();
            case COOKING_POT -> CookDelightConfig.CLIENT.cookingPotRecipeTypes.get();
            case SKILLET -> CookDelightConfig.CLIENT.skilletRecipeTypes.get();
            case KALEIDOSCOPE -> CookDelightConfig.CLIENT.kaleidoscopeRecipeTypes.get();
            case KEG -> CookDelightConfig.CLIENT.kegRecipeTypes.get();
        };

        return rawPatterns.stream()
                .map(this::normalize)
                .filter(pattern -> !pattern.isEmpty())
                .toList();
    }

    private void syncRecipeManager(Level level) {
        RecipeManager currentRecipeManager = level.getRecipeManager();
        if (cachedRecipeManager != currentRecipeManager) {
            cachedRecipeManager = currentRecipeManager;
            outputCache.clear();
            ingredientCache.clear();
        }
    }

    private String buildOutputCacheKey(ItemStack stack) {
        return buildItemIdentityKey(stack);
    }

    private String buildIngredientCacheKey(List<ItemStack> inputs, LookupProfile profile) {
        List<String> parts = inputs.stream()
                .map(stack -> buildItemIdentityKey(stack) + "x" + Math.max(1, stack.getCount()))
                .sorted()
                .toList();
        return profile.name() + "|" + String.join(",", parts);
    }

    private String buildItemIdentityKey(ItemStack stack) {
        String tag = stack.getTag() == null ? "" : stack.getTag().toString();
        return getItemId(stack) + "|" + tag;
    }

    private ResourceLocation getItemId(ItemStack stack) {
        return stack.getItem().builtInRegistryHolder().key().location();
    }

    private String recipeTypeId(Level level, Recipe<?> recipe) {
        ResourceLocation id = level.registryAccess().registryOrThrow(Registries.RECIPE_TYPE).getKey(recipe.getType());
        return id != null ? id.toString() : recipe.getType().toString();
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }
}



