package com.y271727uy.cookdelight.client.compat.manorsbounty;

public final class TeapotMachineSnapshotReader extends AbstractManorsMachineSnapshotReader {
    @Override
    protected int[] inputSlotHints() {
        return new int[]{1, 2, 3, 4, 5, 6};
    }

    @Override
    protected int[] outputSlotHints() {
        return new int[]{0};
    }

    @Override
    protected String[] inputMethodHints() {
        return new String[]{"getTeapotInputs", "getTeaInputs", "getInputInventory", "getInputs"};
    }

    @Override
    protected String[] inputFieldHints() {
        return new String[]{"teapotInputs", "teaInputs", "inputInventory", "inputs"};
    }

    @Override
    protected String[] outputMethodHints() {
        return new String[]{"getTeapotOutput", "getTeaOutput", "getBrewOutput", "getOutputStack", "getOutput", "getResult"};
    }

    @Override
    protected String[] outputFieldHints() {
        return new String[]{"teapotOutput", "teaOutput", "brewOutput", "outputStack", "output", "result"};
    }

    @Override
    protected String[] currentProgressMethodHints() {
        return new String[]{"getTeaTime", "getBrewTime", "getProgress"};
    }

    @Override
    protected String[] currentProgressFieldHints() {
        return new String[]{"teaTime", "brewTime", "progress"};
    }

    @Override
    protected String[] totalProgressMethodHints() {
        return new String[]{"getTeaTimeTotal", "getBrewTimeTotal", "getMaxProgress"};
    }

    @Override
    protected String[] totalProgressFieldHints() {
        return new String[]{"teaTimeTotal", "brewTimeTotal", "maxProgress"};
    }
}

