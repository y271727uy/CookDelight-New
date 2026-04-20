package com.y271727uy.cookdelight.client.logic;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public record KitchenTargetDefinition(
		List<String> identifiers,
		Supplier<List<? extends String>> recipeTypePatternsSupplier,
		boolean skillet,
		String titleKey,
		boolean allowReflectionFallback,
		int maxUnusedInputs,
		boolean requireUniqueBestPrediction,
		KitchenMachineSnapshotReader snapshotReader,
		BooleanSupplier enabledSupplier
) {
	public KitchenTargetDefinition {
		identifiers = List.copyOf(identifiers);
		titleKey = titleKey == null ? "" : titleKey;
		recipeTypePatternsSupplier = recipeTypePatternsSupplier == null ? List::of : recipeTypePatternsSupplier;
		maxUnusedInputs = Math.max(0, maxUnusedInputs);
		snapshotReader = snapshotReader == null ? GenericKitchenMachineSnapshotReader.standard(allowReflectionFallback) : snapshotReader;
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

	public List<String> recipeTypePatterns() {
		List<? extends String> patterns = recipeTypePatternsSupplier.get();
		if (patterns == null || patterns.isEmpty()) {
			return List.of();
		}

		return patterns.stream()
				.map(pattern -> pattern == null ? "" : pattern)
				.toList();
	}

	public KitchenOverlayTarget toTarget(BlockPos blockPos, BlockEntity blockEntity) {
		return KitchenOverlayTarget.createWithPredictionPolicy(
				blockPos,
				blockEntity,
				skillet,
				recipeTypePatterns(),
				Component.translatable(titleKey),
				maxUnusedInputs,
				requireUniqueBestPrediction,
				snapshotReader
		);
	}
}

