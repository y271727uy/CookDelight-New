package com.y271727uy.cookdelight.client.compat.manorsbounty;

public final class BlenderMachineSnapshotReader extends AbstractManorsMachineSnapshotReader {
    @Override
    protected int[] inputSlotHints() {
        return new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    }

    @Override
    protected int[] outputSlotHints() {
        return new int[]{12, 11};
    }

    @Override
    protected String[] inputMethodHints() {
        return new String[]{"getBlenderInputs", "getBlendInputs", "getInputInventory", "getInputs"};
    }

    @Override
    protected String[] inputFieldHints() {
        return new String[]{"blenderInputs", "blendInputs", "inputInventory", "inputs"};
    }

    @Override
    protected String[] outputMethodHints() {
        return new String[]{"getBlenderOutput", "getBlendOutput", "getOutputStack", "getOutput", "getResult"};
    }

    @Override
    protected String[] outputFieldHints() {
        return new String[]{"blenderOutput", "blendOutput", "outputStack", "output", "result"};
    }

    @Override
    protected String[] currentProgressMethodHints() {
        return new String[]{"getBlendTime", "getProcessTime", "getProgress"};
    }

    @Override
    protected String[] currentProgressFieldHints() {
        return new String[]{"blendTime", "processTime", "progress"};
    }

    @Override
    protected String[] totalProgressMethodHints() {
        return new String[]{"getBlendTimeTotal", "getProcessTimeTotal", "getMaxProgress"};
    }

    @Override
    protected String[] totalProgressFieldHints() {
        return new String[]{"blendTimeTotal", "processTimeTotal", "maxProgress"};
    }
}

