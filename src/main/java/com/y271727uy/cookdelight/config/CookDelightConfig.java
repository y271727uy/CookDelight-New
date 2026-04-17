package com.y271727uy.cookdelight.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class CookDelightConfig {
    public static final Client CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;

    static {
        final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT_SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    public static class Client {
        public final ForgeConfigSpec.BooleanValue enableItemFrameRecipeOverlay;
        public final ForgeConfigSpec.BooleanValue enableSmartIngredientHighlighting;
        public final ForgeConfigSpec.IntValue smartIngredientHighlightingDelay;
        public final ForgeConfigSpec.BooleanValue enableCookingPotOverlay;
        public final ForgeConfigSpec.BooleanValue enableSkilletOverlay;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> preferredOutputRecipeTypes;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> cookingPotRecipeTypes;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> skilletRecipeTypes;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> foodKeywordHints;

        public Client(ForgeConfigSpec.Builder builder) {
            builder.push("Tweaks Delight Configuration");

            enableItemFrameRecipeOverlay = builder
                    .comment("Enable the Item Frame Recipe Overlay")
                    .define("enableItemFrameRecipeOverlay", true);

            enableSmartIngredientHighlighting = builder
                    .comment("Enable the Smart Ingredient Highlighting overlay in containers")
                    .define("enableSmartIngredientHighlighting", true);

            smartIngredientHighlightingDelay = builder
                    .comment("Delay (in milliseconds) before items are highlighted when holding the Shift key")
                    .defineInRange("smartIngredientHighlightingDelay", 500, 0, 5000);

            enableCookingPotOverlay = builder
                    .comment("Enable the quick HUD when looking at a Cooking Pot")
                    .define("enableCookingPotOverlay", true);

            enableSkilletOverlay = builder
                    .comment("Enable the quick HUD when looking at a Skillet")
                    .define("enableSkilletOverlay", true);

            preferredOutputRecipeTypes = builder
                    .comment("Recipe type priority used when resolving an item into its preferred ingredient list")
                    .defineList("preferredOutputRecipeTypes", List.of(
                            "farmersdelight:cooking",
                            "cooking",
                            "campfire_cooking",
                            "smelting",
                            "smoking",
                            "blasting",
                            "farmersdelight:cutting",
                            "cutting",
                            "crafting"
                    ), value -> value instanceof String string && !string.trim().isEmpty());

            cookingPotRecipeTypes = builder
                    .comment("Recipe type fragments checked when predicting the output of a Cooking Pot")
                    .defineList("cookingPotRecipeTypes", List.of(
                            "farmersdelight:cooking",
                            "cooking"
                    ), value -> value instanceof String string && !string.trim().isEmpty());

            skilletRecipeTypes = builder
                    .comment("Recipe type fragments checked when predicting the output of a Skillet")
                    .defineList("skilletRecipeTypes", List.of(
                            "campfire_cooking",
                            "smoking",
                            "skillet"
                    ), value -> value instanceof String string && !string.trim().isEmpty());

            foodKeywordHints = builder
                    .comment("Fallback keywords for food-like placeable meals that are not marked as edible items")
                    .defineList("foodKeywordHints", List.of(
                            "pie",
                            "stew",
                            "soup",
                            "feast",
                            "cake",
                            "meal",
                            "salad",
                            "potage",
                            "roast"
                    ), value -> value instanceof String string && !string.trim().isEmpty());

            builder.pop();
        }
    }
}
