package com.y271727uy.cookdelight.client.logic;

import com.y271727uy.cookdelight.client.compat.manorsbounty.ManorsBountyCompat;

import java.util.ArrayList;
import java.util.List;

public final class KitchenTargetRegistry {
	private static final List<KitchenTargetDefinition> DEFAULT_TARGETS;

	static {
		List<KitchenTargetDefinition> definitions = new ArrayList<>(List.of(
				KitchenTargetDsl.cookingPot(),
				KitchenTargetDsl.skillet(),
				KitchenTargetDsl.kaleidoscopePot(),
				KitchenTargetDsl.kaleidoscopeStockpot(),
				KitchenTargetDsl.shawarmaSpit(),
				KitchenTargetDsl.keg()
		));
		definitions.addAll(ManorsBountyCompat.definitions());
		DEFAULT_TARGETS = List.copyOf(definitions);
	}

	private KitchenTargetRegistry() {
	}

	public static List<KitchenTargetDefinition> defaults() {
		return DEFAULT_TARGETS;
	}
}

