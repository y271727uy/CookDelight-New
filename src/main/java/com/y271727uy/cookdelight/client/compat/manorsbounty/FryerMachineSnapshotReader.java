package com.y271727uy.cookdelight.client.compat.manorsbounty;

public final class FryerMachineSnapshotReader extends AbstractManorsMachineSnapshotReader {
    @Override
    protected int[] inputSlotHints() {
        return new int[]{1, 2, 3, 4};
    }

    @Override
    protected String[] inputMethodHints() {
        return new String[]{"getFryerInputs", "getFryerInventory", "getInputInventory", "getInputs"};
    }

    @Override
    protected String[] inputFieldHints() {
        return new String[]{"fryerInputs", "inputInventory", "inputHandler", "inputs"};
    }

    @Override
    protected String[] outputMethodHints() {
        return new String[]{"getFryerOutput", "getFryerResult", "getOutputStack", "getOutput", "getResult"};
    }

    @Override
    protected String[] outputFieldHints() {
        return new String[]{"fryerOutput", "outputStack", "resultStack", "output", "result"};
    }

    @Override
    protected String[] currentProgressMethodHints() {
        return new String[]{"getFryTime", "getProgress", "getCookTime"};
    }

    @Override
    protected String[] currentProgressFieldHints() {
        return new String[]{"fryTime", "progress", "cookTime"};
    }

    @Override
    protected String[] totalProgressMethodHints() {
        return new String[]{"getFryTimeTotal", "getMaxProgress", "getCookTimeTotal"};
    }

    @Override
    protected String[] totalProgressFieldHints() {
        return new String[]{"fryTimeTotal", "maxProgress", "cookTimeTotal"};
    }
}

