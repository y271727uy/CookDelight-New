package com.y271727uy.cookdelight.client.logic;

import com.y271727uy.cookdelight.client.recipe.LookupProfile;
import com.y271727uy.cookdelight.config.CookDelightConfig;

import java.util.List;
import java.util.function.BooleanSupplier;

public final class KitchenTargetDsl {
	private KitchenTargetDsl() {
	}

	public static KitchenTargetDefinition target(String identifier, LookupProfile lookupProfile, boolean skillet, String titleKey, boolean allowReflectionFallback, BooleanSupplier enabledSupplier) {
		return new KitchenTargetDefinition(List.of(identifier), lookupProfile, skillet, titleKey, allowReflectionFallback, enabledSupplier);
	}

	public static KitchenTargetDefinition cookingPot() {
		return target("farmersdelight:cooking_pot", LookupProfile.COOKING_POT, false, "gui.cookdelight.cooking_pot", false, () -> CookDelightConfig.CLIENT.enableCookingPotOverlay.get());
	}

	public static KitchenTargetDefinition skillet() {
		return target("farmersdelight:skillet", LookupProfile.SKILLET, true, "gui.cookdelight.skillet", false, () -> CookDelightConfig.CLIENT.enableSkilletOverlay.get());
	}

	public static KitchenTargetDefinition kaleidoscopePot() {
		return target("kaleidoscope_cookery:pot", LookupProfile.KALEIDOSCOPE, false, "gui.cookdelight.kaleidoscope_pot", true, () -> CookDelightConfig.CLIENT.enableKaleidoscopeOverlay.get());
	}

	public static KitchenTargetDefinition kaleidoscopeStockpot() {
		return target("kaleidoscope_cookery:stockpot", LookupProfile.KALEIDOSCOPE, false, "gui.cookdelight.kaleidoscope_stockpot", true, () -> CookDelightConfig.CLIENT.enableKaleidoscopeOverlay.get());
	}

	public static KitchenTargetDefinition shawarmaSpit() {
		return target("kaleidoscope_cookery:shawarma_spit", LookupProfile.KALEIDOSCOPE, false, "gui.cookdelight.kaleidoscope_shawarma_spit", true, () -> CookDelightConfig.CLIENT.enableKaleidoscopeOverlay.get());
	}

	public static KitchenTargetDefinition keg() {
		return target("brewinandchewin:keg", LookupProfile.KEG, false, "gui.cookdelight.keg", true, () -> CookDelightConfig.CLIENT.enableKegOverlay.get());
	}
}

