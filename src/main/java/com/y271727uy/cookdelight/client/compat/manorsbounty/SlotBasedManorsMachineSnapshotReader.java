package com.y271727uy.cookdelight.client.compat.manorsbounty;

import java.util.Arrays;

final class SlotBasedManorsMachineSnapshotReader extends AbstractManorsMachineSnapshotReader {
    private final int[] inputSlots;
    private final int[] outputSlots;

    SlotBasedManorsMachineSnapshotReader(int[] inputSlots, int[] outputSlots) {
        this.inputSlots = inputSlots == null ? new int[0] : Arrays.copyOf(inputSlots, inputSlots.length);
        this.outputSlots = outputSlots == null ? new int[0] : Arrays.copyOf(outputSlots, outputSlots.length);
    }

    @Override
    protected int[] inputSlotHints() {
        return Arrays.copyOf(inputSlots, inputSlots.length);
    }

    @Override
    protected int[] outputSlotHints() {
        return Arrays.copyOf(outputSlots, outputSlots.length);
    }
}

