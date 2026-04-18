package com.y271727uy.cookdelight.client.logic;

import com.y271727uy.cookdelight.client.recipe.RecipeLookupService;
import com.y271727uy.cookdelight.client.recipe.ResolvedRecipe;
import com.y271727uy.cookdelight.client.state.ItemFrameOverlayState;
import com.y271727uy.cookdelight.config.CookDelightConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class ItemFrameOverlayHandler {
    private static final float FADE_STEP = 0.15f;

    private final RecipeLookupService recipeLookupService;
    private ItemFrameOverlayState state = ItemFrameOverlayState.hidden();

    public ItemFrameOverlayHandler(RecipeLookupService recipeLookupService) {
        this.recipeLookupService = recipeLookupService;
    }

    public void tick(Minecraft minecraft) {
        if (!CookDelightConfig.CLIENT.enableItemFrameRecipeOverlay.get()) {
            state = ItemFrameOverlayState.hidden();
            return;
        }

        if (minecraft.level == null || minecraft.player == null) {
            state = fadeOut(state);
            return;
        }

        Optional<FrameTarget> frameTarget = findTarget(minecraft);
        if (frameTarget.isPresent() && recipeLookupService.isLikelyFood(frameTarget.get().stack())) {
            Optional<ResolvedRecipe> resolvedRecipe = recipeLookupService.findPreferredRecipeByOutput(minecraft.level, frameTarget.get().stack());
            if (resolvedRecipe.isPresent() && resolvedRecipe.get().hasIngredients()) {
                state = visible(frameTarget.get().pos(), frameTarget.get().stack(), resolvedRecipe.get().ingredients());
                return;
            }
        }

        state = fadeOut(state);
    }

    public ItemFrameOverlayState getState() {
        return state;
    }

    private ItemFrameOverlayState visible(BlockPos pos, ItemStack stack, List<net.minecraft.world.item.crafting.Ingredient> ingredients) {
        int itemsPerRow = Math.min(ingredients.size(), 3);
        int rows = itemsPerRow == 0 ? 0 : (int) Math.ceil((double) ingredients.size() / itemsPerRow);
        float previousFade = state.fadeProgress();
        float fade = Math.min(1.0f, previousFade + FADE_STEP);
        return new ItemFrameOverlayState(pos, stack, ingredients, previousFade, fade, itemsPerRow, rows);
    }

    private ItemFrameOverlayState fadeOut(ItemFrameOverlayState currentState) {
        float previousFade = currentState.fadeProgress();
        float fade = Math.max(0.0f, previousFade - FADE_STEP);
        if (fade <= 0.0f) {
            return ItemFrameOverlayState.hidden();
        }
        return new ItemFrameOverlayState(
                currentState.targetPos(),
                currentState.targetItem(),
                currentState.ingredients(),
                previousFade,
                fade,
                currentState.itemsPerRow(),
                currentState.rows()
        );
    }

    private Optional<FrameTarget> findTarget(Minecraft minecraft) {
        Entity crosshairEntity = minecraft.crosshairPickEntity;
        if (crosshairEntity instanceof ItemFrame itemFrame) {
            ItemStack stack = itemFrame.getItem();
            if (!stack.isEmpty()) {
                return Optional.of(new FrameTarget(itemFrame.blockPosition(), stack.copy()));
            }
        }


        return Optional.empty();
    }

    private record FrameTarget(BlockPos pos, ItemStack stack) {
    }
}


