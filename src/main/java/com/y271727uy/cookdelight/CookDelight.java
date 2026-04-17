package com.y271727uy.cookdelight;

import com.y271727uy.cookdelight.client.gui.ItemFrameRecipeOverlay;
import com.y271727uy.cookdelight.client.gui.KitchenUtilsOverlay;
import com.y271727uy.cookdelight.client.gui.SmartIngredientHighlighting;
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
                MinecraftForge.EVENT_BUS.register(new ItemFrameRecipeOverlay());
                MinecraftForge.EVENT_BUS.register(new KitchenUtilsOverlay());
                MinecraftForge.EVENT_BUS.register(new SmartIngredientHighlighting());
            });
        }
    }
}
