package com.y271727uy.cookdelight.client.compat.manorsbounty;

import com.y271727uy.cookdelight.client.logic.GenericKitchenMachineSnapshotReader;
import com.y271727uy.cookdelight.client.logic.KitchenMachineSnapshot;
import com.y271727uy.cookdelight.client.logic.KitchenMachineSnapshotReader;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

abstract class AbstractManorsMachineSnapshotReader implements KitchenMachineSnapshotReader {
    private static final String[] NO_HINTS = new String[0];
    private static final int[] NO_SLOT_HINTS = new int[0];

    private final ManorsReflectionAccess reflectionAccess = new ManorsReflectionAccess();
    private final GenericKitchenMachineSnapshotReader fallbackReader = GenericKitchenMachineSnapshotReader.standard(true);

    @Override
    public final KitchenMachineSnapshot read(BlockPos blockPos, BlockEntity blockEntity) {
        KitchenMachineSnapshot fallbackSnapshot = fallbackReader.read(blockPos, blockEntity);

        List<ItemStack> reflectedInputs = reflectionAccess.readInputs(blockEntity, inputMethodHints(), inputFieldHints(), inputSlotHints());
        List<ItemStack> visibleInputs = reflectedInputs.isEmpty() ? fallbackSnapshot.ingredients() : reflectedInputs;

        ItemStack explicitOutput = reflectionAccess.readOutput(blockEntity, outputMethodHints(), outputFieldHints(), outputSlotHints());
        List<ItemStack> outputStacks = reflectionAccess.readStacksFromSlots(blockEntity, outputSlotHints());
        if (outputStacks.isEmpty() && !explicitOutput.isEmpty()) {
            outputStacks = List.of(explicitOutput);
        }

        if (!outputStacks.isEmpty()) {
            visibleInputs = removeMatchingStacks(visibleInputs, outputStacks);
        }

        int currentProgress = reflectionAccess.readCurrentProgress(blockEntity, currentProgressMethodHints(), currentProgressFieldHints());
        if (currentProgress <= 0) {
            currentProgress = fallbackSnapshot.currentCookTime();
        }

        int totalProgress = reflectionAccess.readTotalProgress(blockEntity, totalProgressMethodHints(), totalProgressFieldHints());
        if (totalProgress <= 0) {
            totalProgress = fallbackSnapshot.totalCookTime();
        }

        return new KitchenMachineSnapshot(blockPos, visibleInputs, explicitOutput, currentProgress, totalProgress);
    }

    protected String[] inputMethodHints() {
        return NO_HINTS;
    }

    protected String[] inputFieldHints() {
        return NO_HINTS;
    }

    protected String[] outputMethodHints() {
        return NO_HINTS;
    }

    protected String[] outputFieldHints() {
        return NO_HINTS;
    }

    protected int[] inputSlotHints() {
        return NO_SLOT_HINTS;
    }

    protected int[] outputSlotHints() {
        return NO_SLOT_HINTS;
    }

    protected String[] currentProgressMethodHints() {
        return NO_HINTS;
    }

    protected String[] currentProgressFieldHints() {
        return NO_HINTS;
    }

    protected String[] totalProgressMethodHints() {
        return NO_HINTS;
    }

    protected String[] totalProgressFieldHints() {
        return NO_HINTS;
    }

    private List<ItemStack> removeMatchingStacks(List<ItemStack> source, List<ItemStack> targets) {
        if (source.isEmpty()) {
            return source;
        }

        List<ItemStack> sanitized = new ArrayList<>(source.size());
        for (ItemStack stack : source) {
            boolean removed = false;
            for (ItemStack target : targets) {
                if (ItemStack.isSameItemSameTags(stack, target)) {
                    removed = true;
                    break;
                }
            }
            if (removed) {
                continue;
            }
            sanitized.add(stack.copy());
        }
        return sanitized;
    }
}

