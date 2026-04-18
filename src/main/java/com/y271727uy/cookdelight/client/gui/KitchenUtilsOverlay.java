package com.y271727uy.cookdelight.client.gui;

import com.y271727uy.cookdelight.client.logic.KitchenOverlayHandler;
import com.y271727uy.cookdelight.client.recipe.RecipeLookupService;
import com.y271727uy.cookdelight.client.render.KitchenOverlayRenderer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;

public class KitchenUtilsOverlay {
    private final KitchenOverlayHandler handler;
    private final KitchenOverlayRenderer renderer;

    public KitchenUtilsOverlay() {
        this(new RecipeLookupService());
    }

    public KitchenUtilsOverlay(RecipeLookupService recipeLookupService) {
        this.handler = new KitchenOverlayHandler(recipeLookupService);
        this.renderer = new KitchenOverlayRenderer();
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
