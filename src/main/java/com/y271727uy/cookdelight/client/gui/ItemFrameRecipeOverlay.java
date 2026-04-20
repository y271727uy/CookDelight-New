package com.y271727uy.cookdelight.client.gui;

import com.y271727uy.cookdelight.client.logic.ItemFrameOverlayHandler;
import com.y271727uy.cookdelight.client.recipe.ItemFrameRecipeLookup;
import com.y271727uy.cookdelight.client.render.ItemFrameOverlayRenderer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;

public class ItemFrameRecipeOverlay {
    private final ItemFrameOverlayHandler handler;
    private final ItemFrameOverlayRenderer renderer;

    public ItemFrameRecipeOverlay() {
        this(new ItemFrameRecipeLookup());
    }

    public ItemFrameRecipeOverlay(ItemFrameRecipeLookup recipeLookup) {
        this.handler = new ItemFrameOverlayHandler(recipeLookup);
        this.renderer = new ItemFrameOverlayRenderer();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        handler.tick(Minecraft.getInstance());
    }

    @SubscribeEvent
    public void onRenderGui(RenderGuiOverlayEvent.Post event) {
        renderer.render(event, handler.getState());
    }
}
