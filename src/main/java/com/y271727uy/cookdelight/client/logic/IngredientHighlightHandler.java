package com.y271727uy.cookdelight.client.logic;

import com.y271727uy.cookdelight.client.recipe.RecipeLookupService;
import com.y271727uy.cookdelight.client.recipe.ResolvedRecipe;
import com.y271727uy.cookdelight.client.state.IngredientHighlightState;
import com.y271727uy.cookdelight.config.CookDelightConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class IngredientHighlightHandler {
    private final RecipeLookupService recipeLookupService;

    private int lastHoveredSlotIndex = -1;
    private long hoverStartTime = 0L;
    private ItemStack trackingItem = ItemStack.EMPTY;
    private IngredientHighlightState state = IngredientHighlightState.inactive();

    public IngredientHighlightHandler(RecipeLookupService recipeLookupService) {
        this.recipeLookupService = recipeLookupService;
    }

    public void update(Minecraft minecraft, AbstractContainerScreen<?> screen, double mouseX, double mouseY) {
        if (!CookDelightConfig.CLIENT.enableSmartIngredientHighlighting.get()) {
            reset();
            return;
        }

        if (minecraft.level == null || minecraft.player == null) {
            reset();
            return;
        }

        int hoveredSlotIndex = findHoveredSlotIndex(screen, mouseX, mouseY);
        if (hoveredSlotIndex < 0 || !Screen.hasShiftDown()) {
            reset();
            return;
        }

        Slot hoveredSlot = screen.getMenu().slots.get(hoveredSlotIndex);
        if (!hoveredSlot.hasItem()) {
            reset();
            return;
        }

        ItemStack stack = hoveredSlot.getItem();
        if (!recipeLookupService.isLikelyFood(stack)) {
            reset();
            return;
        }

        if (lastHoveredSlotIndex != hoveredSlotIndex || !ItemStack.isSameItemSameTags(stack, trackingItem)) {
            lastHoveredSlotIndex = hoveredSlotIndex;
            trackingItem = stack.copy();
            hoverStartTime = System.currentTimeMillis();
            state = new IngredientHighlightState(hoveredSlotIndex, trackingItem, java.util.List.of(), false);
            return;
        }

        long elapsed = System.currentTimeMillis() - hoverStartTime;
        if (elapsed <= CookDelightConfig.CLIENT.smartIngredientHighlightingDelay.get()) {
            state = new IngredientHighlightState(hoveredSlotIndex, trackingItem, state.requiredIngredients(), false);
            return;
        }

        Optional<ResolvedRecipe> recipe = recipeLookupService.findPreferredRecipeByOutput(minecraft.level, trackingItem);
        if (recipe.isPresent() && recipe.get().hasIngredients()) {
            state = new IngredientHighlightState(hoveredSlotIndex, trackingItem, recipe.get().ingredients(), true);
            return;
        }

        reset();
    }

    public IngredientHighlightState getState() {
        return state;
    }

    public void reset() {
        lastHoveredSlotIndex = -1;
        hoverStartTime = 0L;
        trackingItem = ItemStack.EMPTY;
        state = IngredientHighlightState.inactive();
    }

    private int findHoveredSlotIndex(AbstractContainerScreen<?> screen, double mouseX, double mouseY) {
        int guiLeft = screen.getGuiLeft();
        int guiTop = screen.getGuiTop();
        for (int slotIndex = 0; slotIndex < screen.getMenu().slots.size(); slotIndex++) {
            Slot slot = screen.getMenu().slots.get(slotIndex);
            if (mouseX >= guiLeft + slot.x && mouseX < guiLeft + slot.x + 16 &&
                    mouseY >= guiTop + slot.y && mouseY < guiTop + slot.y + 16) {
                return slotIndex;
            }
        }
        return -1;
    }
}


