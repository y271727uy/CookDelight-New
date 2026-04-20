package com.y271727uy.cookdelight.client.logic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class GenericKitchenMachineSnapshotReader implements KitchenMachineSnapshotReader {
    private static final GenericKitchenMachineSnapshotReader STANDARD = new GenericKitchenMachineSnapshotReader(false);
    private static final GenericKitchenMachineSnapshotReader REFLECTIVE = new GenericKitchenMachineSnapshotReader(true);

    private final boolean allowReflectionFallback;

    private GenericKitchenMachineSnapshotReader(boolean allowReflectionFallback) {
        this.allowReflectionFallback = allowReflectionFallback;
    }

    public static GenericKitchenMachineSnapshotReader standard(boolean allowReflectionFallback) {
        return allowReflectionFallback ? REFLECTIVE : STANDARD;
    }

    @Override
    public KitchenMachineSnapshot read(BlockPos blockPos, BlockEntity blockEntity) {
        List<ItemStack> ingredients = new ArrayList<>();
        ItemStack explicitOutput = ItemStack.EMPTY;
        int currentCookTime = 0;
        int totalCookTime = 0;

        if (blockEntity instanceof Container container) {
            int limit = Math.min(6, container.getContainerSize());
            for (int slot = 0; slot < limit; slot++) {
                ItemStack stack = container.getItem(slot);
                if (!stack.isEmpty()) {
                    ingredients.add(stack.copy());
                }
            }

            explicitOutput = readOutputFromContainer(container);
        }

        if (ingredients.isEmpty()) {
            Direction[] directions = new Direction[]{null, Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
            for (Direction direction : directions) {
                ItemStack handlerOutput = readFromItemHandler(blockEntity, direction, ingredients);
                if (explicitOutput.isEmpty() && !handlerOutput.isEmpty()) {
                    explicitOutput = handlerOutput;
                }
                if (!ingredients.isEmpty()) {
                    break;
                }
            }
        }

        try {
            CompoundTag nbt = blockEntity.saveWithoutMetadata();
            if (ingredients.isEmpty() && nbt.contains("Inventory")) {
                CompoundTag inventoryTag = nbt.getCompound("Inventory");
                if (inventoryTag.contains("Items")) {
                    ListTag itemList = inventoryTag.getList("Items", 10);
                    for (int index = 0; index < itemList.size(); index++) {
                        ItemStack parsed = ItemStack.of(itemList.getCompound(index));
                        if (!parsed.isEmpty()) {
                            ingredients.add(parsed.copy());
                        }
                    }
                }
            }

            if (ingredients.isEmpty() && nbt.contains("Ingredient")) {
                ItemStack parsed = ItemStack.of(nbt.getCompound("Ingredient"));
                if (!parsed.isEmpty()) {
                    ingredients.add(parsed.copy());
                }
            }

            if (explicitOutput.isEmpty()) {
                explicitOutput = readOutputFromNbt(nbt);
            }

            if (ingredients.isEmpty() && allowReflectionFallback) {
                ingredients.addAll(readItemsByReflection(blockEntity));
            }

            if (nbt.contains("CookTime")) {
                currentCookTime = nbt.getInt("CookTime");
            }
            if (nbt.contains("CookTimeTotal")) {
                totalCookTime = nbt.getInt("CookTimeTotal");
            }
        } catch (Exception ignored) {
        }

        List<ItemStack> sanitizedIngredients = !explicitOutput.isEmpty()
                ? removeSingleMatchingStack(ingredients, explicitOutput)
                : ingredients;

        return new KitchenMachineSnapshot(blockPos, sanitizedIngredients, explicitOutput, currentCookTime, totalCookTime);
    }

    private ItemStack readOutputFromNbt(CompoundTag nbt) {
        ItemStack direct = readDirectOutputTag(nbt);
        if (!direct.isEmpty()) {
            return direct;
        }

        if (nbt.contains("Inventory")) {
            ItemStack inventoryOutput = readInventoryOutput(nbt.getCompound("Inventory"));
            if (!inventoryOutput.isEmpty()) {
                return inventoryOutput;
            }
        }

        return readNestedOutput(nbt, 0);
    }

    private ItemStack readDirectOutputTag(CompoundTag nbt) {
        String[] keys = new String[]{"Output", "Result", "Product", "OutputItem", "ResultItem"};
        for (String key : keys) {
            if (!nbt.contains(key)) {
                continue;
            }

            try {
                ItemStack parsed = ItemStack.of(nbt.getCompound(key));
                if (!parsed.isEmpty()) {
                    return parsed.copy();
                }
            } catch (Exception ignored) {
            }
        }
        return ItemStack.EMPTY;
    }

    private ItemStack readInventoryOutput(CompoundTag inventoryTag) {
        if (!inventoryTag.contains("Items")) {
            return ItemStack.EMPTY;
        }

        ListTag itemList = inventoryTag.getList("Items", Tag.TAG_COMPOUND);
        ItemStack best = ItemStack.EMPTY;
        int bestSlot = Integer.MIN_VALUE;
        for (int index = 0; index < itemList.size(); index++) {
            CompoundTag stackTag = itemList.getCompound(index);
            ItemStack parsed = ItemStack.of(stackTag);
            if (parsed.isEmpty()) {
                continue;
            }

            int slot = stackTag.contains("Slot", Tag.TAG_ANY_NUMERIC) ? stackTag.getInt("Slot") : index;
            if (slot >= bestSlot) {
                bestSlot = slot;
                best = parsed.copy();
            }
        }
        return best;
    }

    private ItemStack readNestedOutput(CompoundTag nbt, int depth) {
        if (depth > 4) {
            return ItemStack.EMPTY;
        }

        for (String key : nbt.getAllKeys()) {
            Tag tag = nbt.get(key);
            if (tag instanceof CompoundTag compoundTag) {
                ItemStack direct = readDirectOutputTag(compoundTag);
                if (!direct.isEmpty()) {
                    return direct;
                }

                ItemStack nested = readNestedOutput(compoundTag, depth + 1);
                if (!nested.isEmpty()) {
                    return nested;
                }
            } else if (tag instanceof ListTag listTag) {
                ItemStack fromList = readOutputFromList(listTag, depth + 1);
                if (!fromList.isEmpty()) {
                    return fromList;
                }
            }
        }

        return ItemStack.EMPTY;
    }

    private ItemStack readOutputFromList(ListTag listTag, int depth) {
        if (depth > 4 || listTag.isEmpty()) {
            return ItemStack.EMPTY;
        }

        for (int index = 0; index < listTag.size(); index++) {
            Tag element = listTag.get(index);
            if (element instanceof CompoundTag compoundTag) {
                ItemStack parsed = ItemStack.of(compoundTag);
                if (!parsed.isEmpty()) {
                    return parsed.copy();
                }

                ItemStack nested = readNestedOutput(compoundTag, depth + 1);
                if (!nested.isEmpty()) {
                    return nested;
                }
            }
        }

        return ItemStack.EMPTY;
    }

    private ItemStack readOutputFromContainer(Container container) {
        for (int slot = container.getContainerSize() - 1; slot >= 0; slot--) {
            ItemStack stack = container.getItem(slot);
            if (!stack.isEmpty()) {
                return stack.copy();
            }
        }
        return ItemStack.EMPTY;
    }

    private ItemStack readFromItemHandler(BlockEntity blockEntity, Direction direction, List<ItemStack> ingredients) {
        final ItemStack[] outputHolder = new ItemStack[]{ItemStack.EMPTY};
        blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, direction).ifPresent(handler -> {
            int limit = Math.min(6, handler.getSlots());
            for (int slot = 0; slot < limit; slot++) {
                ItemStack stack = handler.getStackInSlot(slot);
                if (!stack.isEmpty()) {
                    ingredients.add(stack.copy());
                }
            }

            outputHolder[0] = readOutputFromHandler(handler);
        });
        return outputHolder[0];
    }

    private ItemStack readOutputFromHandler(IItemHandler handler) {
        for (int slot = handler.getSlots() - 1; slot >= 0; slot--) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                return stack.copy();
            }
        }
        return ItemStack.EMPTY;
    }

    private List<ItemStack> readItemsByReflection(BlockEntity blockEntity) {
        List<ItemStack> results = new ArrayList<>();
        String[] methodNames = new String[]{"getItem", "getIngredient", "getIngredients", "getContents", "getHeldItem", "getInput", "getInputs"};

        for (String methodName : methodNames) {
            try {
                Object value = blockEntity.getClass().getMethod(methodName).invoke(blockEntity);
                collectStacks(value, results);
            } catch (ReflectiveOperationException ignored) {
            }

            if (!results.isEmpty()) {
                break;
            }
        }

        return results;
    }

    private void collectStacks(Object value, List<ItemStack> results) {
        if (value instanceof ItemStack stack && !stack.isEmpty()) {
            results.add(stack.copy());
        } else if (value instanceof ItemStack[] stacks) {
            for (ItemStack stack : stacks) {
                if (stack != null && !stack.isEmpty()) {
                    results.add(stack.copy());
                }
            }
        } else if (value instanceof Collection<?> collection) {
            for (Object element : collection) {
                if (element instanceof ItemStack stack && !stack.isEmpty()) {
                    results.add(stack.copy());
                }
            }
        } else if (value instanceof Iterable<?> iterable) {
            for (Object element : iterable) {
                if (element instanceof ItemStack stack && !stack.isEmpty()) {
                    results.add(stack.copy());
                }
            }
        }
    }

    private List<ItemStack> removeSingleMatchingStack(List<ItemStack> source, ItemStack target) {
        if (source.isEmpty()) {
            return source;
        }

        List<ItemStack> sanitized = new ArrayList<>(source.size());
        boolean removed = false;
        for (ItemStack stack : source) {
            if (!removed && ItemStack.isSameItemSameTags(stack, target)) {
                removed = true;
                continue;
            }
            sanitized.add(stack.copy());
        }
        return sanitized;
    }
}




