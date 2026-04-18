package com.y271727uy.cookdelight.client.logic;

import java.util.List;

public final class KitchenTargetRegistry {
	private static final List<KitchenTargetDefinition> DEFAULT_TARGETS = List.of(
			KitchenTargetDsl.cookingPot(),
			KitchenTargetDsl.skillet(),
			KitchenTargetDsl.kaleidoscopePot(),
			KitchenTargetDsl.kaleidoscopeStockpot(),
			KitchenTargetDsl.shawarmaSpit(),
			KitchenTargetDsl.keg()
	);

	private KitchenTargetRegistry() {
	}

	public static List<KitchenTargetDefinition> defaults() {
		return DEFAULT_TARGETS;
	}
}

