package com.y271727uy.cookdelight.client.logic;

public final class KitchenOverlayProviders {
	private KitchenOverlayProviders() {
	}

	public static KitchenOverlayTargetProvider configured() {
		return new ConfiguredKitchenOverlayTargetProvider(KitchenTargetRegistry.defaults());
	}
}

