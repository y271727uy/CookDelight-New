package com.y271727uy.cookdelight.client.render;

import com.y271727uy.cookdelight.client.state.IngredientHighlightState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class IngredientHighlightRenderer {
    public void render(GuiGraphics graphics, AbstractContainerScreen<?> screen, IngredientHighlightState state) {
        if (!state.active() || state.requiredIngredients().isEmpty()) {
            return;
        }

        int guiLeft = screen.getGuiLeft();
        int guiTop = screen.getGuiTop();
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 300);

        for (int slotIndex = 0; slotIndex < screen.getMenu().slots.size(); slotIndex++) {
            if (slotIndex == state.hoveredSlotIndex()) {
                continue;
            }

            Slot slot = screen.getMenu().slots.get(slotIndex);
            int x = guiLeft + slot.x;
            int y = guiTop + slot.y;
            if (!slot.hasItem()) {
                graphics.fill(x, y, x + 16, y + 16, 0x88C6C6C6);
                continue;
            }

            ItemStack slotItem = slot.getItem();
            if (!matchesAnyIngredient(slotItem, state.requiredIngredients())) {
                graphics.fill(x, y, x + 16, y + 16, 0xAA8B8B8B);
            }
        }

        graphics.pose().popPose();
    }

    private boolean matchesAnyIngredient(ItemStack stack, java.util.List<Ingredient> ingredients) {
        for (Ingredient ingredient : ingredients) {
            if (ingredient.test(stack)) {
                return true;
            }
        }
        return false;
    }
}
