package com.y271727uy.cookdelight.client.compat.manorsbounty;

public final class FermenterMachineSnapshotReader extends AbstractManorsMachineSnapshotReader {
    @Override
    protected int[] inputSlotHints() {
        return new int[]{2, 3, 4, 5, 6};
    }

    @Override
    protected int[] outputSlotHints() {
        return new int[]{7};
    }

    @Override
    protected String[] inputMethodHints() {
        return new String[]{"getFermenterInputs", "getFermentInputs", "getInputInventory", "getInputs"};
    }

    @Override
    protected String[] inputFieldHints() {
        return new String[]{"fermenterInputs", "fermentInputs", "inputInventory", "inputs"};
    }

    @Override
    protected String[] outputMethodHints() {
        return new String[]{"getFermenterOutput", "getFermentOutput", "getProductStack", "getOutputStack", "getOutput", "getResult"};
    }

    @Override
    protected String[] outputFieldHints() {
        return new String[]{"fermenterOutput", "fermentOutput", "productStack", "outputStack", "output", "result"};
    }

    @Override
    protected String[] currentProgressMethodHints() {
        return new String[]{"getFermentTime", "getProcessTime", "getProgress"};
    }

    @Override
    protected String[] currentProgressFieldHints() {
        return new String[]{"fermentTime", "processTime", "progress"};
    }

    @Override
    protected String[] totalProgressMethodHints() {
        return new String[]{"getFermentTimeTotal", "getProcessTimeTotal", "getMaxProgress"};
    }

    @Override
    protected String[] totalProgressFieldHints() {
        return new String[]{"fermentTimeTotal", "processTimeTotal", "maxProgress"};
    }
}

