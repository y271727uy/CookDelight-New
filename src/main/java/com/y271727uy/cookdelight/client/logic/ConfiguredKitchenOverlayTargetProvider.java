package com.y271727uy.cookdelight.client.logic;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;
import java.util.Optional;

public class ConfiguredKitchenOverlayTargetProvider implements KitchenOverlayTargetProvider {
	private final List<KitchenTargetDefinition> definitions;

	public ConfiguredKitchenOverlayTargetProvider(List<KitchenTargetDefinition> definitions) {
		this.definitions = List.copyOf(definitions);
	}

	@Override
	public Optional<KitchenOverlayTarget> findTarget(Minecraft minecraft) {
		if (minecraft == null || minecraft.level == null || minecraft.hitResult == null || minecraft.hitResult.getType() != HitResult.Type.BLOCK) {
			return Optional.empty();
		}

		if (minecraft.crosshairPickEntity instanceof ItemFrame) {
			return Optional.empty();
		}

		BlockHitResult blockHitResult = (BlockHitResult) minecraft.hitResult;
		BlockEntity blockEntity = minecraft.level.getBlockEntity(blockHitResult.getBlockPos());
		if (blockEntity == null) {
			return Optional.empty();
		}

		BlockState blockState = blockEntity.getBlockState();
		String blockId = String.valueOf(minecraft.level.registryAccess().registryOrThrow(Registries.BLOCK).getKey(blockState.getBlock()));
		String blockEntityTypeId = String.valueOf(minecraft.level.registryAccess().registryOrThrow(Registries.BLOCK_ENTITY_TYPE).getKey(blockEntity.getType()));

		for (KitchenTargetDefinition definition : definitions) {
			if (definition.enabled() && definition.matches(blockId, blockEntityTypeId)) {
				return Optional.of(definition.toTarget(blockHitResult.getBlockPos(), blockEntity));
			}
		}

		return Optional.empty();
	}
}

