package com.y271727uy.cookdelight.client.logic;

import com.y271727uy.cookdelight.client.recipe.KitchenRecipeLookup;
import com.y271727uy.cookdelight.client.recipe.ResolvedRecipe;
import com.y271727uy.cookdelight.client.state.KitchenOverlayState;
import com.y271727uy.cookdelight.config.CookDelightConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class KitchenOverlayHandler {
    private static final float FADE_STEP = 0.15f;

    private final KitchenRecipeLookup recipeLookup;
    private final KitchenOverlayTargetProvider targetProvider;
    private KitchenOverlayState state = KitchenOverlayState.hidden();

    public KitchenOverlayHandler(KitchenRecipeLookup recipeLookup) {
        this(recipeLookup, KitchenOverlayProviders.configured());
    }

    public KitchenOverlayHandler(KitchenRecipeLookup recipeLookup, KitchenOverlayTargetProvider targetProvider) {
        this.recipeLookup = recipeLookup;
        this.targetProvider = targetProvider;
    }

    public void tick(Minecraft minecraft) {
        if (!CookDelightConfig.CLIENT.enableCookingPotOverlay.get()
                && !CookDelightConfig.CLIENT.enableSkilletOverlay.get()
                && !CookDelightConfig.CLIENT.enableKaleidoscopeOverlay.get()
                && !CookDelightConfig.CLIENT.enableKegOverlay.get()
                && !CookDelightConfig.CLIENT.enableManorsBountyMachineOverlay.get()) {
            state = KitchenOverlayState.hidden();
            return;
        }

        if (minecraft.level == null || minecraft.player == null) {
            state = fadeOut(state);
            return;
        }

        Optional<KitchenOverlayTarget> kitchenTarget = targetProvider.findTarget(minecraft);
        if (kitchenTarget.isEmpty()) {
            state = fadeOut(state);
            return;
        }

        KitchenOverlayTarget target = kitchenTarget.get();
        KitchenMachineSnapshot snapshot = target.snapshotReader().read(target.blockPos(), target.blockEntity());
        if (snapshot.ingredients().isEmpty()) {
            state = fadeOut(state);
            return;
        }

        Optional<ResolvedRecipe> recipe = recipeLookup.findRecipe(
                minecraft.level,
                snapshot.ingredients(),
                snapshot.explicitOutput(),
                target.recipeTypePatterns(),
                target.maxUnusedInputs(),
                target.requireUniqueBestPrediction()
        );
        state = visible(snapshot, recipe.orElse(null), target.skillet(), target.title());
    }

    public KitchenOverlayState getState() {
        return state;
    }

    private KitchenOverlayState visible(KitchenMachineSnapshot snapshot, ResolvedRecipe recipe, boolean skillet, Component title) {
        float previousFade = state.fadeProgress();
        float fade = Math.min(1.0f, previousFade + FADE_STEP);
        ItemStack displayOutput = !snapshot.explicitOutput().isEmpty()
                ? snapshot.explicitOutput()
                : recipe != null ? recipe.output() : ItemStack.EMPTY;
        return new KitchenOverlayState(
                snapshot.blockPos(),
                snapshot.ingredients(),
                recipe != null ? recipe.ingredients() : List.of(),
                displayOutput,
                snapshot.currentCookTime(),
                snapshot.totalCookTime(),
                skillet,
                title,
                previousFade,
                fade
        );
    }

    private KitchenOverlayState fadeOut(KitchenOverlayState currentState) {
        float previousFade = currentState.fadeProgress();
        float fade = Math.max(0.0f, previousFade - FADE_STEP);
        if (fade <= 0.0f) {
            return KitchenOverlayState.hidden();
        }

        return new KitchenOverlayState(
                currentState.targetPos(),
                currentState.visibleIngredients(),
                currentState.recipeIngredients(),
                currentState.predictedOutput(),
                currentState.currentCookTime(),
                currentState.totalCookTime(),
                currentState.skillet(),
                currentState.title(),
                previousFade,
                fade
        );
    }
}

