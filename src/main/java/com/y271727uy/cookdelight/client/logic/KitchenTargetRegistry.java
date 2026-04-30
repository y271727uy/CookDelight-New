package com.y271727uy.cookdelight.client.logic;

import com.y271727uy.cookdelight.client.compat.manorsbounty.ManorsBountyCompat;

import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class KitchenTargetRegistry {
	private static final List<KitchenTargetDefinition> DEFAULT_TARGETS;

	static {
		List<KitchenTargetDefinition> definitions = new ArrayList<>();
		addIfLoaded(definitions, "farmersdelight", () -> List.of(
				KitchenTargetDsl.cookingPot(),
				KitchenTargetDsl.skillet()
		));
		addIfLoaded(definitions, "kaleidoscope_cookery", () -> List.of(
				KitchenTargetDsl.kaleidoscopePot(),
				KitchenTargetDsl.kaleidoscopeStockpot(),
				KitchenTargetDsl.shawarmaSpit()
		));
		addIfLoaded(definitions, "brewinandchewin", () -> List.of(KitchenTargetDsl.keg()));
		addIfLoaded(definitions, "manors_bounty_machine", ManorsBountyCompat::definitions);
		DEFAULT_TARGETS = List.copyOf(definitions);
	}

	private KitchenTargetRegistry() {
	}

	private static void addIfLoaded(List<KitchenTargetDefinition> definitions, String modId, Supplier<List<KitchenTargetDefinition>> targetSupplier) {
		if (isLoaded(modId)) {
			definitions.addAll(targetSupplier.get());
		}
	}

	private static boolean isLoaded(String modId) {
		return modId != null && !modId.isBlank() && ModList.get().isLoaded(modId);
	}

	public static List<KitchenTargetDefinition> defaults() {
		return DEFAULT_TARGETS;
	}
}

