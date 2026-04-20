package com.y271727uy.cookdelight.client.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class KitchenRecipeLookup {
    private final Map<InputLookupKey, Optional<ResolvedRecipe>> ingredientCache = new HashMap<>();
    private RecipeManager cachedRecipeManager;

    public Optional<ResolvedRecipe> findRecipe(Level level, List<ItemStack> inputs, List<String> recipeTypePatterns) {
        return findRecipe(level, inputs, ItemStack.EMPTY, recipeTypePatterns, Integer.MAX_VALUE, false);
    }

    public Optional<ResolvedRecipe> findRecipe(Level level, List<ItemStack> inputs, ItemStack expectedOutput, List<String> recipeTypePatterns) {
        return findRecipe(level, inputs, expectedOutput, recipeTypePatterns, Integer.MAX_VALUE, false);
    }

    public Optional<ResolvedRecipe> findRecipe(Level level, List<ItemStack> inputs, ItemStack expectedOutput, List<String> recipeTypePatterns, int maxUnusedInputs, boolean requireUniqueBestPrediction) {
        if (level == null || inputs == null || inputs.isEmpty()) {
            return Optional.empty();
        }

        List<ItemStack> normalizedInputs = RecipeLookupSupport.normalizeInputs(inputs);
        if (normalizedInputs.isEmpty()) {
            return Optional.empty();
        }

        syncRecipeManager(level);
        List<String> patterns = RecipeLookupSupport.normalizePatterns(recipeTypePatterns);
        String expectedOutputKey = expectedOutput == null || expectedOutput.isEmpty()
                ? ""
                : RecipeLookupSupport.buildItemIdentityKey(expectedOutput);
        InputLookupKey cacheKey = new InputLookupKey(patterns, RecipeLookupSupport.buildIngredientInputKeys(normalizedInputs), expectedOutputKey, maxUnusedInputs, requireUniqueBestPrediction);
        return ingredientCache.computeIfAbsent(cacheKey, ignored -> resolveRecipe(level, normalizedInputs, expectedOutput, patterns, maxUnusedInputs, requireUniqueBestPrediction));
    }

    private Optional<ResolvedRecipe> resolveRecipe(Level level, List<ItemStack> inputs, ItemStack expectedOutput, List<String> patterns, int maxUnusedInputs, boolean requireUniqueBestPrediction) {
        List<RecipeLookupSupport.RecipeCandidate> candidates = level.getRecipeManager().getRecipes().stream()
                .map(recipe -> RecipeLookupSupport.toCandidate(level, recipe))
                .filter(candidate -> !candidate.ingredients().isEmpty())
                .filter(candidate -> RecipeLookupSupport.matchesConfiguredPattern(candidate, patterns))
                .toList();

        if (!expectedOutput.isEmpty()) {
            List<RecipeLookupSupport.RecipeCandidate> expectedOutputCandidates = candidates.stream()
                    .filter(candidate -> ItemStack.isSameItemSameTags(candidate.output(), expectedOutput))
                    .toList();
            if (!expectedOutputCandidates.isEmpty()) {
                return selectBestMatchingRecipe(inputs, expectedOutputCandidates, patterns, maxUnusedInputs, requireUniqueBestPrediction);
            }
        }

        return selectBestMatchingRecipe(inputs, candidates, patterns, maxUnusedInputs, requireUniqueBestPrediction);
    }

    private Optional<ResolvedRecipe> selectBestMatchingRecipe(List<ItemStack> inputs, List<RecipeLookupSupport.RecipeCandidate> candidates, List<String> patterns, int maxUnusedInputs, boolean requireUniqueBestPrediction) {
        List<ItemStack> expandedInputs = expandInputs(inputs, maxIngredients(candidates));

        List<RecipeMatch> matches = candidates.stream()
                .map(candidate -> buildMatch(candidate, expandedInputs, patterns))
                .flatMap(Optional::stream)
                .filter(match -> match.unusedInputs() <= maxUnusedInputs)
                .sorted(recipeMatchComparator())
                .toList();

        if (matches.isEmpty()) {
            return Optional.empty();
        }

        if (requireUniqueBestPrediction && matches.size() > 1 && recipeMatchComparator().compare(matches.get(0), matches.get(1)) == 0) {
            return Optional.empty();
        }

        return Optional.of(RecipeLookupSupport.toResolvedRecipe(matches.get(0).candidate()));
    }

    private Optional<RecipeMatch> buildMatch(RecipeLookupSupport.RecipeCandidate candidate, List<ItemStack> expandedInputs, List<String> patterns) {
        int ingredientCount = candidate.ingredients().size();
        if (!matchesAllIngredients(expandedInputs, candidate.ingredients())) {
            return Optional.empty();
        }

        int inputUnitCount = expandedInputs.size();
        int unusedInputs = Math.max(0, inputUnitCount - ingredientCount);
        int typePriority = RecipeLookupSupport.patternPriority(candidate, patterns);
        return Optional.of(new RecipeMatch(candidate, typePriority, unusedInputs, ingredientCount));
    }

    private Comparator<RecipeMatch> recipeMatchComparator() {
        return Comparator
                .comparingInt(RecipeMatch::typePriority)
                .thenComparingInt(RecipeMatch::unusedInputs)
                .thenComparing(Comparator.comparingInt(RecipeMatch::ingredientCount).reversed())
                .thenComparing(match -> match.candidate().recipeId());
    }

    private int maxIngredients(List<RecipeLookupSupport.RecipeCandidate> candidates) {
        int max = 1;
        for (RecipeLookupSupport.RecipeCandidate candidate : candidates) {
            max = Math.max(max, candidate.ingredients().size());
        }
        return max;
    }

    private boolean matchesAllIngredients(List<ItemStack> expandedInputs, List<Ingredient> ingredients) {
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

    private void syncRecipeManager(Level level) {
        RecipeManager currentRecipeManager = level.getRecipeManager();
        if (cachedRecipeManager != currentRecipeManager) {
            cachedRecipeManager = currentRecipeManager;
            ingredientCache.clear();
        }
    }

    private record InputLookupKey(List<String> patterns, List<String> inputKeys, String expectedOutputKey, int maxUnusedInputs, boolean requireUniqueBestPrediction) {
        private InputLookupKey {
            patterns = List.copyOf(patterns);
            inputKeys = List.copyOf(inputKeys);
            expectedOutputKey = expectedOutputKey == null ? "" : expectedOutputKey;
            maxUnusedInputs = Math.max(0, maxUnusedInputs);
        }
    }

    private record RecipeMatch(RecipeLookupSupport.RecipeCandidate candidate, int typePriority, int unusedInputs, int ingredientCount) {
    }
}

