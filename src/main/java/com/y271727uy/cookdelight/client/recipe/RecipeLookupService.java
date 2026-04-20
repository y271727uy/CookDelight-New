package com.y271727uy.cookdelight.client.recipe;

import com.y271727uy.cookdelight.config.CookDelightConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class RecipeLookupService {
    private final Map<OutputLookupKey, Optional<ResolvedRecipe>> outputCache = new HashMap<>();
    private final Map<InputLookupKey, Optional<ResolvedRecipe>> ingredientCache = new HashMap<>();
    private RecipeManager cachedRecipeManager;

    public RecipeLookupService() {
    }

    public Optional<ResolvedRecipe> findPreferredRecipeByOutput(Level level, ItemStack output) {
        return findDisplayRecipeByOutput(level, output, RecipeLookupContext.ITEM_FRAME);
    }

    public Optional<ResolvedRecipe> findDisplayRecipeByOutput(Level level, ItemStack output, RecipeLookupContext context) {
        if (level == null || output.isEmpty()) {
            return Optional.empty();
        }

        syncRecipeManager(level);
        List<String> preferredTypes = outputPatterns(context);
        OutputLookupKey cacheKey = new OutputLookupKey(buildItemIdentityKey(output), context, preferredTypes);
        return outputCache.computeIfAbsent(cacheKey, ignored -> resolvePreferredRecipe(level, output, preferredTypes));
    }

    public Optional<ResolvedRecipe> findPredictedRecipe(Level level, List<ItemStack> inputs, LookupProfile profile) {
        return findPredictedRecipe(level, inputs, RecipeLookupContext.KITCHEN, normalizedPatterns(profile));
    }

    public Optional<ResolvedRecipe> findPredictedRecipe(Level level, List<ItemStack> inputs, List<String> recipeTypePatterns) {
        return findPredictedRecipe(level, inputs, RecipeLookupContext.KITCHEN, recipeTypePatterns);
    }

    public Optional<ResolvedRecipe> findPredictedRecipe(Level level, List<ItemStack> inputs, RecipeLookupContext context, List<String> recipeTypePatterns) {
        if (level == null || inputs == null || inputs.isEmpty()) {
            return Optional.empty();
        }

        List<ItemStack> normalizedInputs = normalizeInputs(inputs);

        if (normalizedInputs.isEmpty()) {
            return Optional.empty();
        }

        syncRecipeManager(level);
        List<String> normalizedPatterns = normalizePatterns(recipeTypePatterns);
        InputLookupKey cacheKey = new InputLookupKey(context, normalizedPatterns, buildIngredientInputKeys(normalizedInputs));
        return ingredientCache.computeIfAbsent(cacheKey, ignored -> resolvePredictedRecipe(level, normalizedInputs, normalizedPatterns));
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

    private Optional<ResolvedRecipe> resolvePreferredRecipe(Level level, ItemStack output, List<String> preferredTypes) {
        return level.getRecipeManager().getRecipes().stream()
                .map(recipe -> toCandidate(level, recipe))
                .filter(candidate -> !candidate.output().isEmpty())
                .filter(candidate -> ItemStack.isSameItemSameTags(candidate.output(), output))
                .filter(candidate -> !candidate.ingredients().isEmpty())
                .sorted(candidateComparator(preferredTypes))
                .map(this::toResolvedRecipe)
                .findFirst();
    }

    private Optional<ResolvedRecipe> resolvePredictedRecipe(Level level, List<ItemStack> inputs, List<String> patterns) {
        List<RecipeCandidate> candidates = level.getRecipeManager().getRecipes().stream()
                .map(recipe -> toCandidate(level, recipe))
                .filter(candidate -> patterns.isEmpty() || matchesConfiguredType(candidate.normalizedRecipeTypeId(), patterns))
                .sorted(candidateComparator(patterns))
                .toList();

        for (RecipeCandidate candidate : candidates) {
            if (!candidate.ingredients().isEmpty() && matchesAllIngredients(inputs, candidate.ingredients())) {
                return Optional.of(toResolvedRecipe(candidate));
            }
        }

        return Optional.empty();
    }

    private RecipeCandidate toCandidate(Level level, Recipe<?> recipe) {
        String recipeTypeId = recipeTypeId(level, recipe);
        return new RecipeCandidate(
                recipe.getResultItem(level.registryAccess()),
                compactIngredients(recipe),
                recipeTypeId,
                normalize(recipeTypeId),
                recipe.getId().toString()
        );
    }

    private ResolvedRecipe toResolvedRecipe(RecipeCandidate candidate) {
        return new ResolvedRecipe(candidate.output(), candidate.ingredients(), candidate.recipeTypeId());
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

    private Comparator<RecipeCandidate> candidateComparator(List<String> patterns) {
        return Comparator
                .comparingInt((RecipeCandidate candidate) -> priorityOf(candidate.normalizedRecipeTypeId(), patterns))
                .thenComparingInt(candidate -> candidate.ingredients().size())
                .thenComparing(RecipeCandidate::recipeId);
    }

    private int priorityOf(String normalizedTypeId, List<String> patterns) {
        for (int index = 0; index < patterns.size(); index++) {
            if (matchPattern(normalizedTypeId, patterns.get(index))) {
                return index;
            }
        }
        return patterns.isEmpty() ? 0 : Integer.MAX_VALUE;
    }

    private boolean matchesConfiguredType(String normalizedTypeId, List<String> patterns) {
        for (String pattern : patterns) {
            if (matchPattern(normalizedTypeId, pattern)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchPattern(String normalizedTypeId, String pattern) {
        if (normalizedTypeId.isEmpty() || pattern == null || pattern.isEmpty()) {
            return false;
        }

        if (pattern.indexOf(':') >= 0) {
            return normalizedTypeId.equals(pattern);
        }

        int separatorIndex = normalizedTypeId.indexOf(':');
        String path = separatorIndex >= 0 && separatorIndex + 1 < normalizedTypeId.length()
                ? normalizedTypeId.substring(separatorIndex + 1)
                : normalizedTypeId;
        return path.equals(pattern);
    }

    private List<String> normalizedPatterns(LookupProfile profile) {
        List<? extends String> rawPatterns = switch (profile) {
            case OUTPUT_PREFERENCE -> CookDelightConfig.CLIENT.preferredOutputRecipeTypes.get();
            case COOKING_POT -> CookDelightConfig.CLIENT.cookingPotRecipeTypes.get();
            case SKILLET -> CookDelightConfig.CLIENT.skilletRecipeTypes.get();
            case KALEIDOSCOPE -> CookDelightConfig.CLIENT.kaleidoscopeRecipeTypes.get();
            case KEG -> CookDelightConfig.CLIENT.kegRecipeTypes.get();
        };

        return normalizePatterns(rawPatterns);
    }

    private List<String> outputPatterns(RecipeLookupContext context) {
        return switch (context) {
            case ITEM_FRAME, INGREDIENT_HIGHLIGHT -> normalizedPatterns(LookupProfile.OUTPUT_PREFERENCE);
            case KITCHEN -> List.of();
        };
    }

    private List<ItemStack> normalizeInputs(List<ItemStack> inputs) {
        return inputs.stream()
                .filter(stack -> stack != null && !stack.isEmpty())
                .map(ItemStack::copy)
                .toList();
    }

    private List<String> normalizePatterns(List<? extends String> rawPatterns) {
        if (rawPatterns == null || rawPatterns.isEmpty()) {
            return List.of();
        }

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

    private List<String> buildIngredientInputKeys(List<ItemStack> inputs) {
        return inputs.stream()
                .map(stack -> buildItemIdentityKey(stack) + "x" + Math.max(1, stack.getCount()))
                .sorted()
                .toList();
    }

    private String buildItemIdentityKey(ItemStack stack) {
        String tag = stack.getTag() == null ? "" : stack.getTag().toString();
        return getItemId(stack) + "|" + tag;
    }

    private ResourceLocation getItemId(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null ? id : new ResourceLocation("minecraft", "air");
    }

    private String recipeTypeId(Level level, Recipe<?> recipe) {
        ResourceLocation id = level.registryAccess().registryOrThrow(Registries.RECIPE_TYPE).getKey(recipe.getType());
        return id != null ? id.toString() : recipe.getType().toString();
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }

    private record RecipeCandidate(
            ItemStack output,
            List<Ingredient> ingredients,
            String recipeTypeId,
            String normalizedRecipeTypeId,
            String recipeId
    ) {
    }

    private record OutputLookupKey(String outputKey, RecipeLookupContext context, List<String> patterns) {
        private OutputLookupKey {
            patterns = List.copyOf(patterns);
        }
    }

    private record InputLookupKey(RecipeLookupContext context, List<String> patterns, List<String> inputKeys) {
        private InputLookupKey {
            patterns = List.copyOf(patterns);
            inputKeys = List.copyOf(inputKeys);
        }
    }
}



