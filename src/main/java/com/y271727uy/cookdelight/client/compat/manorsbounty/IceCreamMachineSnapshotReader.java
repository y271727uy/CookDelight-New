package com.y271727uy.cookdelight.client.compat.manorsbounty;

public final class IceCreamMachineSnapshotReader extends AbstractManorsMachineSnapshotReader {
    @Override
    protected int[] inputSlotHints() {
        return new int[]{2, 3};
    }

    @Override
    protected int[] outputSlotHints() {
        return new int[]{1};
    }

    @Override
    protected String[] inputMethodHints() {
        return new String[]{"getIceCreamInputs", "getIceCreamMachineInputs", "getFreezerInputs", "getFrozenInputs", "getInputInventory", "getInputSlots", "getIngredients", "getInputs"};
    }

    @Override
    protected String[] inputFieldHints() {
        return new String[]{"iceCreamInputs", "iceCreamMachineInputs", "freezerInputs", "frozenInputs", "inputInventory", "inputSlots", "ingredients", "inputs"};
    }

    @Override
    protected String[] outputMethodHints() {
        return new String[]{"getIceCreamOutput", "getIceCreamMachineOutput", "getIceCreamResult", "getFrozenOutput", "getFrozenResult", "getProduct", "getResultItem", "getOutputStack", "getOutput", "getResult"};
    }

    @Override
    protected String[] outputFieldHints() {
        return new String[]{"iceCreamOutput", "iceCreamMachineOutput", "iceCreamResult", "frozenOutput", "frozenResult", "product", "resultItem", "outputStack", "output", "result"};
    }

    @Override
    protected String[] currentProgressMethodHints() {
        return new String[]{"getFreezeTime", "getFreezeProgress", "getIceCreamTime", "getChillTime", "getProgress", "getTime"};
    }

    @Override
    protected String[] currentProgressFieldHints() {
        return new String[]{"freezeTime", "freezeProgress", "iceCreamTime", "chillTime", "progress", "time"};
    }

    @Override
    protected String[] totalProgressMethodHints() {
        return new String[]{"getFreezeTimeTotal", "getFreezeProgressTotal", "getIceCreamTimeTotal", "getChillTimeTotal", "getProgressTotal", "getMaxProgress", "getTimeTotal"};
    }

    @Override
    protected String[] totalProgressFieldHints() {
        return new String[]{"freezeTimeTotal", "freezeProgressTotal", "iceCreamTimeTotal", "chillTimeTotal", "progressTotal", "maxProgress", "timeTotal"};
    }
}

