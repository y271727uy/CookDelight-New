package com.y271727uy.cookdelight.client.compat.manorsbounty;

import com.y271727uy.cookdelight.client.logic.GenericKitchenMachineSnapshotReader;
import com.y271727uy.cookdelight.client.logic.KitchenMachineSnapshot;
import com.y271727uy.cookdelight.client.logic.KitchenMachineSnapshotReader;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public final class ManorsMachineSnapshotReader implements KitchenMachineSnapshotReader {
    private final ManorsMachineDefinition definition;
    private final ManorsReflectionAccess reflectionAccess = new ManorsReflectionAccess();
    private final GenericKitchenMachineSnapshotReader fallbackReader = GenericKitchenMachineSnapshotReader.standard(true);

    public ManorsMachineSnapshotReader(ManorsMachineDefinition definition) {
        this.definition = definition;
    }

    @Override
    public KitchenMachineSnapshot read(BlockPos blockPos, BlockEntity blockEntity) {
        KitchenMachineSnapshot fallbackSnapshot = fallbackReader.read(blockPos, blockEntity);

        List<ItemStack> reflectedInputs = reflectionAccess.readInputs(blockEntity);
        List<ItemStack> visibleInputs = reflectedInputs.isEmpty() ? fallbackSnapshot.ingredients() : reflectedInputs;

        ItemStack explicitOutput = reflectionAccess.readOutput(blockEntity);
        if (explicitOutput.isEmpty()) {
            explicitOutput = fallbackSnapshot.explicitOutput();
        }

        if (!explicitOutput.isEmpty()) {
            visibleInputs = removeSingleMatchingStack(visibleInputs, explicitOutput);
        }

        int currentProgress = reflectionAccess.readCurrentProgress(blockEntity);
        if (currentProgress <= 0) {
            currentProgress = fallbackSnapshot.currentCookTime();
        }

        int totalProgress = reflectionAccess.readTotalProgress(blockEntity);
        if (totalProgress <= 0) {
            totalProgress = fallbackSnapshot.totalCookTime();
        }

        return new KitchenMachineSnapshot(blockPos, visibleInputs, explicitOutput, currentProgress, totalProgress);
    }

    public ManorsMachineDefinition definition() {
        return definition;
    }

    private List<ItemStack> removeSingleMatchingStack(List<ItemStack> source, ItemStack target) {
        if (source.isEmpty()) {
            return source;
        }

        java.util.ArrayList<ItemStack> sanitized = new java.util.ArrayList<>(source.size());
        boolean removed = false;
        for (ItemStack stack : source) {
            if (!removed && ItemStack.isSameItemSameTags(stack, target)) {
                removed = true;
                continue;
            }
            sanitized.add(stack.copy());
        }
        return sanitized;
    }
}




