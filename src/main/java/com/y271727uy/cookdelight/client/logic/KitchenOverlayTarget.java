package com.y271727uy.cookdelight.client.logic;

import com.y271727uy.cookdelight.client.recipe.LookupProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;

public record KitchenOverlayTarget(
		BlockPos blockPos,
		BlockEntity blockEntity,
		boolean skillet,
		LookupProfile lookupProfile,
		Component title,
		boolean allowReflectionFallback
) {
}

