package com.y271727uy.cookdelight.registry;

import com.y271727uy.cookdelight.CookDelight;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CookDelight.MOD_ID);

    public static final RegistryObject<Item> EQUALS = ITEMS.register("equals",
            () -> new Item(new Item.Properties()));
}
