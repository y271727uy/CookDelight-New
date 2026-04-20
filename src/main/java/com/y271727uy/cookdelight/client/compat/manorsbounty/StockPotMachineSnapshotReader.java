package com.y271727uy.cookdelight.client.compat.manorsbounty;

public final class StockPotMachineSnapshotReader extends AbstractManorsMachineSnapshotReader {
    @Override
    protected int[] inputSlotHints() {
        return new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
    }

    @Override
    protected int[] outputSlotHints() {
        return new int[]{0};
    }

    @Override
    protected String[] inputMethodHints() {
        return new String[]{"getStockPotInputs", "getStockpotInputs", "getSoupInputs", "getInputInventory", "getInputs"};
    }

    @Override
    protected String[] inputFieldHints() {
        return new String[]{"stockPotInputs", "stockpotInputs", "soupInputs", "inputInventory", "inputs"};
    }

    @Override
    protected String[] outputMethodHints() {
        return new String[]{"getStockPotOutput", "getStockpotOutput", "getSoupOutput", "getOutputStack", "getOutput", "getResult"};
    }

    @Override
    protected String[] outputFieldHints() {
        return new String[]{"stockPotOutput", "stockpotOutput", "soupOutput", "outputStack", "output", "result"};
    }

    @Override
    protected String[] currentProgressMethodHints() {
        return new String[]{"getStockTime", "getCookTime", "getProgress"};
    }

    @Override
    protected String[] currentProgressFieldHints() {
        return new String[]{"stockTime", "cookTime", "progress"};
    }

    @Override
    protected String[] totalProgressMethodHints() {
        return new String[]{"getStockTimeTotal", "getCookTimeTotal", "getMaxProgress"};
    }

    @Override
    protected String[] totalProgressFieldHints() {
        return new String[]{"stockTimeTotal", "cookTimeTotal", "maxProgress"};
    }
}

