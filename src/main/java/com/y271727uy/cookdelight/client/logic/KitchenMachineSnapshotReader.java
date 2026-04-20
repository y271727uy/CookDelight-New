package com.y271727uy.cookdelight.client.logic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface KitchenMachineSnapshotReader {
    KitchenMachineSnapshot read(BlockPos blockPos, BlockEntity blockEntity);
}

