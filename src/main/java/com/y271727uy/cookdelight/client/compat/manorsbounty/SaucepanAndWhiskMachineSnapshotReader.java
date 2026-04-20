package com.y271727uy.cookdelight.client.compat.manorsbounty;

public final class SaucepanAndWhiskMachineSnapshotReader extends AbstractManorsMachineSnapshotReader {
    @Override
    protected int[] inputSlotHints() {
        return new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    }

    @Override
    protected int[] outputSlotHints() {
        return new int[]{0};
    }

    @Override
    protected String[] inputMethodHints() {
        return new String[]{"getMainInput", "getSecondaryInput", "getInputInventory", "getInputs"};
    }

    @Override
    protected String[] inputFieldHints() {
        return new String[]{"mainInput", "secondaryInput", "inputInventory", "inputs"};
    }

    @Override
    protected String[] currentProgressMethodHints() {
        return new String[]{"getStirsCount", "getProgress", "getCookTime"};
    }

    @Override
    protected String[] currentProgressFieldHints() {
        return new String[]{"stirsCount", "progress", "cookTime"};
    }

    @Override
    protected String[] totalProgressMethodHints() {
        return new String[]{"getMaxStirsCount", "getProgressTotal", "getMaxProgress"};
    }

    @Override
    protected String[] totalProgressFieldHints() {
        return new String[]{"MAX_STIRS_COUNT", "maxStirsCount", "progressTotal", "maxProgress"};
    }
}

