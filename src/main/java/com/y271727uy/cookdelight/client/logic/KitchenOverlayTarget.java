package com.y271727uy.cookdelight.client.logic;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public record KitchenOverlayTarget(
		BlockPos blockPos,
		BlockEntity blockEntity,
		boolean skillet,
		List<String> recipeTypePatterns,
		Component title,
		int maxUnusedInputs,
		boolean requireUniqueBestPrediction,
		KitchenMachineSnapshotReader snapshotReader
) {
	public KitchenOverlayTarget {
		recipeTypePatterns = List.copyOf(recipeTypePatterns);
		maxUnusedInputs = Math.max(0, maxUnusedInputs);
	}

	public static KitchenOverlayTarget createWithPredictionPolicy(
			BlockPos blockPos,
			BlockEntity blockEntity,
			boolean skillet,
			List<String> recipeTypePatterns,
			Component title,
			int maxUnusedInputs,
			boolean requireUniqueBestPrediction,
			KitchenMachineSnapshotReader snapshotReader
	) {
		return new KitchenOverlayTarget(
				blockPos,
				blockEntity,
				skillet,
				recipeTypePatterns,
				title,
				maxUnusedInputs,
				requireUniqueBestPrediction,
				snapshotReader
		);
	}
}
