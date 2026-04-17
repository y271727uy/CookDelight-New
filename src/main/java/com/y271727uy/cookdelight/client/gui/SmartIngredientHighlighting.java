package com.y271727uy.cookdelight.client.gui;

import com.y271727uy.cookdelight.client.logic.IngredientHighlightHandler;
import com.y271727uy.cookdelight.client.recipe.RecipeLookupService;
import com.y271727uy.cookdelight.client.render.IngredientHighlightRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.ScreenEvent;

public class SmartIngredientHighlighting {
    private final IngredientHighlightHandler handler;
    private final IngredientHighlightRenderer renderer;

    public SmartIngredientHighlighting() {
        this(RecipeLookupService.getInstance());
    }

    public SmartIngredientHighlighting(RecipeLookupService recipeLookupService) {
        this.handler = new IngredientHighlightHandler(recipeLookupService);
        this.renderer = new IngredientHighlightRenderer();
    }

    @SubscribeEvent
    public void onScreenRender(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            handler.reset();
            return;
        }

        handler.update(Minecraft.getInstance(), screen, event.getMouseX(), event.getMouseY());
        renderer.render(event.getGuiGraphics(), screen, handler.getState());
    }
}
