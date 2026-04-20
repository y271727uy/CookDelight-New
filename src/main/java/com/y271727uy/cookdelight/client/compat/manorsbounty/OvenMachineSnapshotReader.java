package com.y271727uy.cookdelight.client.compat.manorsbounty;

public final class OvenMachineSnapshotReader extends AbstractManorsMachineSnapshotReader {
    @Override
    protected int[] inputSlotHints() {
        return new int[]{0, 1, 2, 3, 4, 5};
    }

    @Override
    protected int[] outputSlotHints() {
        return new int[]{6};
    }

    @Override
    protected String[] inputMethodHints() {
        return new String[]{"getInput", "getOvenInputs", "getBakeInputs", "getBakingInputs", "getCookInputs", "getInputInventory", "getInputSlots", "getIngredients", "getInputs"};
    }

    @Override
    protected String[] inputFieldHints() {
        return new String[]{"ovenInputs", "bakeInputs", "bakingInputs", "cookInputs", "inputInventory", "inputSlots", "ingredients", "inputs"};
    }

    @Override
    protected String[] outputMethodHints() {
        return new String[]{"getOvenOutput", "getBakeOutput", "getBakedOutput", "getCookOutput", "getProduct", "getResultItem", "getOutputStack", "getOutput", "getResult"};
    }

    @Override
    protected String[] outputFieldHints() {
        return new String[]{"ovenOutput", "bakeOutput", "bakedOutput", "cookOutput", "product", "resultItem", "outputStack", "output", "result"};
    }

    @Override
    protected String[] currentProgressMethodHints() {
        return new String[]{"getBakeTime", "getBakeProgress", "getCookTime", "getCookingTime", "getCookingProgress", "getProgress", "getTime"};
    }

    @Override
    protected String[] currentProgressFieldHints() {
        return new String[]{"bakeTime", "bakeProgress", "cookTime", "cookingTime", "cookingProgress", "progress", "time"};
    }

    @Override
    protected String[] totalProgressMethodHints() {
        return new String[]{"getBakeTimeTotal", "getCookingTimeTotal", "getCookTimeTotal", "getProgressTotal", "getMaxProgress", "getTimeTotal"};
    }

    @Override
    protected String[] totalProgressFieldHints() {
        return new String[]{"bakeTimeTotal", "cookingTimeTotal", "cookTimeTotal", "progressTotal", "maxProgress", "timeTotal", "maxCookingTime"};
    }
}

