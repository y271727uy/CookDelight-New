package com.y271727uy.cookdelight;

import com.y271727uy.cookdelight.client.gui.ItemFrameRecipeOverlay;
import com.y271727uy.cookdelight.client.gui.KitchenUtilsOverlay;
import com.y271727uy.cookdelight.client.gui.SmartIngredientHighlighting;
import com.y271727uy.cookdelight.client.recipe.IngredientHighlightRecipeLookup;
import com.y271727uy.cookdelight.client.recipe.ItemFrameRecipeLookup;
import com.y271727uy.cookdelight.client.recipe.KitchenRecipeLookup;
import com.y271727uy.cookdelight.config.CookDelightConfig;
import com.y271727uy.cookdelight.registry.ItemRegistry;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;

@Mod(CookDelight.MOD_ID)
public class CookDelight
{
    public static final String MOD_ID = "cookdelight";
    private static final ItemFrameRecipeLookup ITEM_FRAME_RECIPE_LOOKUP = new ItemFrameRecipeLookup();
    private static final IngredientHighlightRecipeLookup INGREDIENT_HIGHLIGHT_RECIPE_LOOKUP = new IngredientHighlightRecipeLookup();
    private static final KitchenRecipeLookup KITCHEN_RECIPE_LOOKUP = new KitchenRecipeLookup();

    @SuppressWarnings("removal")
    public CookDelight()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CookDelightConfig.CLIENT_SPEC);
        modEventBus.addListener(this::commonSetup);
        ItemRegistry.ITEMS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {}

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {}

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents { 
        @SubscribeEvent 
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                MinecraftForge.EVENT_BUS.register(new ItemFrameRecipeOverlay(ITEM_FRAME_RECIPE_LOOKUP));
                MinecraftForge.EVENT_BUS.register(new KitchenUtilsOverlay(KITCHEN_RECIPE_LOOKUP));
                MinecraftForge.EVENT_BUS.register(new SmartIngredientHighlighting(INGREDIENT_HIGHLIGHT_RECIPE_LOOKUP));
            });
        }
    }
}
