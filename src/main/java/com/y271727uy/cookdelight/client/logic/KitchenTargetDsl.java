package com.y271727uy.cookdelight.client.logic;

import com.y271727uy.cookdelight.config.CookDelightConfig;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public final class KitchenTargetDsl {
	private KitchenTargetDsl() {
	}

	public static KitchenTargetDefinition target(String identifier, Supplier<List<? extends String>> recipeTypePatternsSupplier, boolean skillet, String titleKey, boolean allowReflectionFallback, BooleanSupplier enabledSupplier) {
		return new KitchenTargetDefinition(List.of(identifier), recipeTypePatternsSupplier, skillet, titleKey, allowReflectionFallback, Integer.MAX_VALUE, false, GenericKitchenMachineSnapshotReader.standard(allowReflectionFallback), enabledSupplier);
	}

	public static KitchenTargetDefinition cookingPot() {
		return target("farmersdelight:cooking_pot", () -> CookDelightConfig.CLIENT.cookingPotRecipeTypes.get(), false, "gui.cookdelight.cooking_pot", false, () -> CookDelightConfig.CLIENT.enableCookingPotOverlay.get());
	}

	public static KitchenTargetDefinition skillet() {
		return target("farmersdelight:skillet", () -> CookDelightConfig.CLIENT.skilletRecipeTypes.get(), true, "gui.cookdelight.skillet", false, () -> CookDelightConfig.CLIENT.enableSkilletOverlay.get());
	}

	public static KitchenTargetDefinition kaleidoscopePot() {
		return target("kaleidoscope_cookery:pot", () -> List.of("kaleidoscope_cookery:pot"), false, "gui.cookdelight.kaleidoscope_pot", true, () -> CookDelightConfig.CLIENT.enableKaleidoscopeOverlay.get());
	}

	public static KitchenTargetDefinition kaleidoscopeStockpot() {
		return target("kaleidoscope_cookery:stockpot", () -> List.of("kaleidoscope_cookery:stockpot"), false, "gui.cookdelight.kaleidoscope_stockpot", true, () -> CookDelightConfig.CLIENT.enableKaleidoscopeOverlay.get());
	}

	public static KitchenTargetDefinition shawarmaSpit() {
		return target("kaleidoscope_cookery:shawarma_spit", () -> List.of("kaleidoscope_cookery:shawarma_spit"), false, "gui.cookdelight.kaleidoscope_shawarma_spit", true, () -> CookDelightConfig.CLIENT.enableKaleidoscopeOverlay.get());
	}

	public static KitchenTargetDefinition keg() {
		return target("brewinandchewin:keg", () -> CookDelightConfig.CLIENT.kegRecipeTypes.get(), false, "gui.cookdelight.keg", true, () -> CookDelightConfig.CLIENT.enableKegOverlay.get());
	}
}

