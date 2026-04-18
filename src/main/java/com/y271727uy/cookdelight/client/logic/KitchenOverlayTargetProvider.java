package com.y271727uy.cookdelight.client.logic;

import net.minecraft.client.Minecraft;

import java.util.Optional;

public interface KitchenOverlayTargetProvider {
	Optional<KitchenOverlayTarget> findTarget(Minecraft minecraft);
}

