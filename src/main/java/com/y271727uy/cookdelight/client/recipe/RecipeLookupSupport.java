package com.y271727uy.cookdelight.client.recipe;

import com.y271727uy.cookdelight.config.CookDelightConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

final class RecipeLookupSupport {
    private RecipeLookupSupport() {
    }

    static boolean isLikelyFood(ItemStack stack) {
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

    static List<ItemStack> normalizeInputs(List<ItemStack> inputs) {
        return inputs.stream()
                .filter(stack -> stack != null && !stack.isEmpty())
                .map(ItemStack::copy)
                .toList();
    }

    static List<String> normalizePatterns(List<? extends String> rawPatterns) {
        if (rawPatterns == null || rawPatterns.isEmpty()) {
            return List.of();
        }

        return rawPatterns.stream()
                .map(RecipeLookupSupport::normalize)
                .filter(pattern -> !pattern.isEmpty())
                .toList();
    }

    static RecipeCandidate toCandidate(Level level, Recipe<?> recipe) {
        String recipeTypeId = recipeTypeId(level, recipe);
        String recipeId = recipe.getId().toString();
        return new RecipeCandidate(
                recipe.getResultItem(level.registryAccess()),
                compactIngredients(recipe),
                recipeTypeId,
                normalize(recipeTypeId),
                recipeId,
                normalize(recipeId)
        );
    }

    static ResolvedRecipe toResolvedRecipe(RecipeCandidate candidate) {
        return new ResolvedRecipe(candidate.output(), candidate.ingredients(), candidate.recipeTypeId());
    }

    static Comparator<RecipeCandidate> outputComparator(List<String> patterns) {
        return Comparator
                .comparingInt((RecipeCandidate candidate) -> priorityOf(candidate.normalizedRecipeTypeId(), patterns))
                .thenComparing(Comparator.comparingInt((RecipeCandidate candidate) -> candidate.ingredients().size()).reversed())
                .thenComparing(RecipeCandidate::recipeId);
    }

    static Comparator<RecipeCandidate> kitchenComparator(List<String> patterns) {
        return Comparator
                .comparingInt((RecipeCandidate candidate) -> priorityOf(candidate.normalizedRecipeTypeId(), patterns))
                .thenComparing(Comparator.comparingInt((RecipeCandidate candidate) -> candidate.ingredients().size()).reversed())
                .thenComparing(RecipeCandidate::recipeId);
    }

    static boolean matchesConfiguredType(String normalizedTypeId, List<String> patterns) {
        if (patterns.isEmpty()) {
            return true;
        }

        for (String pattern : patterns) {
            if (matchPattern(normalizedTypeId, pattern)) {
                return true;
            }
        }
        return false;
    }

    static boolean matchesConfiguredPattern(RecipeCandidate candidate, List<String> patterns) {
        if (patterns.isEmpty()) {
            return true;
        }

        for (String pattern : patterns) {
            if (matchCandidatePattern(candidate, pattern)) {
                return true;
            }
        }
        return false;
    }

    static int patternPriority(String normalizedTypeId, List<String> patterns) {
        return priorityOf(normalizedTypeId, patterns);
    }

    static int patternPriority(RecipeCandidate candidate, List<String> patterns) {
        if (patterns.isEmpty()) {
            return 0;
        }

        for (int index = 0; index < patterns.size(); index++) {
            if (matchCandidatePattern(candidate, patterns.get(index))) {
                return index;
            }
        }
        return Integer.MAX_VALUE;
    }

    static String buildItemIdentityKey(ItemStack stack) {
        String tag = stack.getTag() == null ? "" : stack.getTag().toString();
        return getItemId(stack) + "|" + tag;
    }

    static List<String> buildIngredientInputKeys(List<ItemStack> inputs) {
        return inputs.stream()
                .map(stack -> buildItemIdentityKey(stack) + "x" + Math.max(1, stack.getCount()))
                .sorted()
                .toList();
    }

    private static List<Ingredient> compactIngredients(Recipe<?> recipe) {
        return recipe.getIngredients().stream()
                .filter(ingredient -> ingredient != null && !ingredient.isEmpty())
                .toList();
    }

    private static int priorityOf(String normalizedTypeId, List<String> patterns) {
        if (patterns.isEmpty()) {
            return 0;
        }

        for (int index = 0; index < patterns.size(); index++) {
            if (matchPattern(normalizedTypeId, patterns.get(index))) {
                return index;
            }
        }
        return Integer.MAX_VALUE;
    }

    private static boolean matchCandidatePattern(RecipeCandidate candidate, String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return false;
        }

        if (pattern.startsWith("idprefix:")) {
            String expectedPrefix = pattern.substring("idprefix:".length());
            return !expectedPrefix.isEmpty() && candidate.normalizedRecipeId().startsWith(expectedPrefix);
        }
        if (pattern.startsWith("idcontains:")) {
            String expectedFragment = pattern.substring("idcontains:".length());
            return !expectedFragment.isEmpty() && candidate.normalizedRecipeId().contains(expectedFragment);
        }
        if (pattern.startsWith("id:")) {
            String expectedId = pattern.substring("id:".length());
            return !expectedId.isEmpty() && candidate.normalizedRecipeId().equals(expectedId);
        }
        if (pattern.startsWith("type:")) {
            return matchTypePattern(candidate.normalizedRecipeTypeId(), pattern.substring("type:".length()));
        }
        return matchTypePattern(candidate.normalizedRecipeTypeId(), pattern);
    }

    private static boolean matchPattern(String normalizedTypeId, String pattern) {
        return matchTypePattern(normalizedTypeId, pattern);
    }

    private static boolean matchTypePattern(String normalizedTypeId, String pattern) {
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

    private static ResourceLocation getItemId(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null ? id : ResourceLocation.parse("minecraft:air");
    }

    private static String recipeTypeId(Level level, Recipe<?> recipe) {
        ResourceLocation id = level.registryAccess().registryOrThrow(Registries.RECIPE_TYPE).getKey(recipe.getType());
        return id != null ? id.toString() : recipe.getType().toString();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }

    record RecipeCandidate(
            ItemStack output,
            List<Ingredient> ingredients,
            String recipeTypeId,
            String normalizedRecipeTypeId,
            String recipeId,
            String normalizedRecipeId
    ) {
    }
}


