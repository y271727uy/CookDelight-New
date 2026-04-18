package com.y271727uy.cookdelight.client.logic;

import com.y271727uy.cookdelight.client.recipe.LookupProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;
import java.util.function.BooleanSupplier;

public record KitchenTargetDefinition(
		List<String> identifiers,
		LookupProfile lookupProfile,
		boolean skillet,
		String titleKey,
		boolean allowReflectionFallback,
		BooleanSupplier enabledSupplier
) {
	public KitchenTargetDefinition {
		identifiers = List.copyOf(identifiers);
		titleKey = titleKey == null ? "" : titleKey;
		enabledSupplier = enabledSupplier == null ? () -> true : enabledSupplier;
	}

	public boolean enabled() {
		return enabledSupplier.getAsBoolean();
	}

	public boolean matches(String actualId, String fallbackId) {
		for (String identifier : identifiers) {
			if (identifier.equals(actualId) || identifier.equals(fallbackId)) {
				return true;
			}
		}
		return false;
	}

	public com.y271727uy.cookdelight.client.logic.KitchenOverlayTarget toTarget(net.minecraft.core.BlockPos blockPos, BlockEntity blockEntity) {
		return new com.y271727uy.cookdelight.client.logic.KitchenOverlayTarget(
				blockPos,
				blockEntity,
				skillet,
				lookupProfile,
				Component.translatable(titleKey),
				allowReflectionFallback
		);
	}
}


