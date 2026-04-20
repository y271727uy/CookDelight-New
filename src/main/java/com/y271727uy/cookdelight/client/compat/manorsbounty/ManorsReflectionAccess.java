package com.y271727uy.cookdelight.client.compat.manorsbounty;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ManorsReflectionAccess {
    private static final String[] INPUT_METHODS = new String[]{
            "getInputs", "getInputStacks", "getItems", "getInventory", "getContents", "getIngredients", "getInput", "getIngredient"
    };
    private static final String[] INPUT_FIELDS = new String[]{
            "inputs", "inputStacks", "items", "inventory", "contents", "ingredients", "input", "ingredient"
    };
    private static final String[] OUTPUT_METHODS = new String[]{
            "getOutput", "getResult", "getResultItem", "getProduct", "getOutputItem", "getProducedItem", "getCurrentOutput", "getOutputStack", "getResultStack", "getProductStack", "getOutputInventory"
    };
    private static final String[] OUTPUT_FIELDS = new String[]{
            "output", "result", "resultItem", "product", "outputItem", "producedItem", "currentOutput", "outputStack", "resultStack", "productStack", "outputInventory"
    };
    private static final String[] CURRENT_PROGRESS_METHODS = new String[]{
            "getCookTime", "getProgress", "getProcessTime", "getFermentTime", "getBrewTime", "getBlendTime", "getFryTime", "getTeaTime"
    };
    private static final String[] CURRENT_PROGRESS_FIELDS = new String[]{
            "cookTime", "progress", "processTime", "fermentTime", "brewTime", "blendTime", "fryTime", "teaTime"
    };
    private static final String[] TOTAL_PROGRESS_METHODS = new String[]{
            "getCookTimeTotal", "getMaxProgress", "getTotalProgress", "getProcessTimeTotal", "getTotalTime", "getRecipeTime", "getFermentTimeTotal", "getBrewTimeTotal", "getBlendTimeTotal", "getFryTimeTotal", "getTeaTimeTotal"
    };
    private static final String[] TOTAL_PROGRESS_FIELDS = new String[]{
            "cookTimeTotal", "maxProgress", "totalProgress", "processTimeTotal", "totalTime", "recipeTime", "fermentTimeTotal", "brewTimeTotal", "blendTimeTotal", "fryTimeTotal", "teaTimeTotal"
    };
    private static final String[] INT_METHODS = new String[]{
            "getCookingTime", "getCookTime", "getProgress", "getTime", "getAmount", "getSecond"
    };
    private static final String[] INT_FIELDS = new String[]{
            "cookingTime", "cookTime", "progress", "time", "amount", "second"
    };

    private final Map<Class<?>, Optional<MemberHandle>> inputHandleCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, Optional<MemberHandle>> outputHandleCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, Optional<MemberHandle>> currentProgressHandleCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, Optional<MemberHandle>> totalProgressHandleCache = new ConcurrentHashMap<>();

    public List<ItemStack> readInputs(BlockEntity blockEntity, String[] preferredMethods, String[] preferredFields) {
        List<ItemStack> preferred = extractStacks(readPreferredMember(blockEntity, preferredMethods, preferredFields));
        return preferred.isEmpty() ? readInputs(blockEntity) : preferred;
    }

    public List<ItemStack> readInputs(BlockEntity blockEntity, String[] preferredMethods, String[] preferredFields, int... preferredSlots) {
        List<ItemStack> slotPreferred = readStacksFromSlots(blockEntity, preferredSlots);
        if (!slotPreferred.isEmpty()) {
            return slotPreferred;
        }

        List<ItemStack> preferred = extractStacks(readPreferredMember(blockEntity, preferredMethods, preferredFields));
        return preferred.isEmpty() ? readInputs(blockEntity) : preferred;
    }

    public List<ItemStack> readInputs(BlockEntity blockEntity) {
        return inputHandleCache
                .computeIfAbsent(blockEntity.getClass(), type -> locateHandle(type, INPUT_METHODS, INPUT_FIELDS))
                .map(handle -> extractStacks(handle.read(blockEntity)))
                .orElseGet(List::of);
    }

    public ItemStack readOutput(BlockEntity blockEntity, String[] preferredMethods, String[] preferredFields) {
        ItemStack preferred = extractFirstStack(readPreferredMember(blockEntity, preferredMethods, preferredFields));
        return preferred.isEmpty() ? readOutput(blockEntity) : preferred;
    }

    public ItemStack readOutput(BlockEntity blockEntity, String[] preferredMethods, String[] preferredFields, int... preferredSlots) {
        ItemStack slotPreferred = extractFirstStackFromSlots(blockEntity, preferredSlots);
        if (!slotPreferred.isEmpty()) {
            return slotPreferred;
        }

        ItemStack preferred = extractFirstStack(readPreferredMember(blockEntity, preferredMethods, preferredFields));
        return preferred.isEmpty() ? ItemStack.EMPTY : preferred;
    }

    public List<ItemStack> readStacksFromSlots(BlockEntity blockEntity, int... preferredSlots) {
        if (blockEntity == null || preferredSlots == null || preferredSlots.length == 0) {
            return List.of();
        }

        List<ItemStack> stacks = new ArrayList<>();
        if (blockEntity instanceof Container container) {
            for (int slot : preferredSlots) {
                if (slot < 0 || slot >= container.getContainerSize()) {
                    continue;
                }
                ItemStack stack = container.getItem(slot);
                if (!stack.isEmpty()) {
                    stacks.add(stack.copy());
                }
            }
        }
        return stacks;
    }

    public ItemStack readOutput(BlockEntity blockEntity) {
        return outputHandleCache
                .computeIfAbsent(blockEntity.getClass(), type -> locateHandle(type, OUTPUT_METHODS, OUTPUT_FIELDS))
                .map(handle -> extractFirstStack(handle.read(blockEntity)))
                .orElse(ItemStack.EMPTY);
    }

    public int readCurrentProgress(BlockEntity blockEntity, String[] preferredMethods, String[] preferredFields) {
        int preferred = extractInt(readPreferredMember(blockEntity, preferredMethods, preferredFields));
        return preferred > 0 ? preferred : readCurrentProgress(blockEntity);
    }

    public int readCurrentProgress(BlockEntity blockEntity) {
        return currentProgressHandleCache
                .computeIfAbsent(blockEntity.getClass(), type -> locateHandle(type, CURRENT_PROGRESS_METHODS, CURRENT_PROGRESS_FIELDS))
                .map(handle -> extractInt(handle.read(blockEntity)))
                .orElse(0);
    }

    public int readTotalProgress(BlockEntity blockEntity, String[] preferredMethods, String[] preferredFields) {
        int preferred = extractInt(readPreferredMember(blockEntity, preferredMethods, preferredFields));
        return preferred > 0 ? preferred : readTotalProgress(blockEntity);
    }

    public int readTotalProgress(BlockEntity blockEntity) {
        return totalProgressHandleCache
                .computeIfAbsent(blockEntity.getClass(), type -> locateHandle(type, TOTAL_PROGRESS_METHODS, TOTAL_PROGRESS_FIELDS))
                .map(handle -> extractInt(handle.read(blockEntity)))
                .orElse(0);
    }

    private Object readPreferredMember(BlockEntity blockEntity, String[] methodNames, String[] fieldNames) {
        if (methodNames != null) {
            for (String methodName : methodNames) {
                Method method = findZeroArgMethod(blockEntity.getClass(), methodName);
                if (method == null) {
                    continue;
                }

                try {
                    method.setAccessible(true);
                    Object value = method.invoke(blockEntity);
                    if (value != null) {
                        return value;
                    }
                } catch (ReflectiveOperationException ignored) {
                }
            }
        }

        if (fieldNames != null) {
            for (String fieldName : fieldNames) {
                Field field = findField(blockEntity.getClass(), fieldName);
                if (field == null) {
                    continue;
                }

                try {
                    field.setAccessible(true);
                    Object value = field.get(blockEntity);
                    if (value != null) {
                        return value;
                    }
                } catch (ReflectiveOperationException ignored) {
                }
            }
        }

        return null;
    }

    private Optional<MemberHandle> locateHandle(Class<?> type, String[] methodNames, String[] fieldNames) {
        for (String methodName : methodNames) {
            Method method = findZeroArgMethod(type, methodName);
            if (method != null) {
                method.setAccessible(true);
                return Optional.of(new MethodHandle(method));
            }
        }

        for (String fieldName : fieldNames) {
            Field field = findField(type, fieldName);
            if (field != null) {
                field.setAccessible(true);
                return Optional.of(new FieldHandle(field));
            }
        }

        return Optional.empty();
    }

    private Method findZeroArgMethod(Class<?> type, String name) {
        if (type == null || name == null || name.isEmpty()) {
            return null;
        }

        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getParameterCount() == 0 && method.getName().equals(name)) {
                    return method;
                }
            }
        }

        try {
            Method method = type.getMethod(name);
            if (method.getParameterCount() == 0) {
                return method;
            }
        } catch (NoSuchMethodException ignored) {
        }
        return null;
    }

    private Field findField(Class<?> type, String name) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }

    private List<ItemStack> extractStacks(Object value) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof Optional<?> optional) {
            return optional.map(this::extractStacks).orElseGet(List::of);
        }
        if (value instanceof ItemStack stack) {
            return stack.isEmpty() ? List.of() : List.of(stack.copy());
        }
        if (value instanceof ItemStack[] stacks) {
            List<ItemStack> results = new ArrayList<>();
            for (ItemStack stack : stacks) {
                if (stack != null && !stack.isEmpty()) {
                    results.add(stack.copy());
                }
            }
            return results;
        }
        if (value instanceof Container container) {
            List<ItemStack> results = new ArrayList<>();
            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                ItemStack stack = container.getItem(slot);
                if (!stack.isEmpty()) {
                    results.add(stack.copy());
                }
            }
            return results;
        }
        if (value instanceof IItemHandler handler) {
            List<ItemStack> results = new ArrayList<>();
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack stack = handler.getStackInSlot(slot);
                if (!stack.isEmpty()) {
                    results.add(stack.copy());
                }
            }
            return results;
        }
        if (value instanceof Collection<?> collection) {
            List<ItemStack> results = new ArrayList<>();
            for (Object element : collection) {
                results.addAll(extractStacks(element));
            }
            return results;
        }
        if (value instanceof Iterable<?> iterable) {
            List<ItemStack> results = new ArrayList<>();
            for (Object element : iterable) {
                results.addAll(extractStacks(element));
            }
            return results;
        }
        if (value.getClass().isArray()) {
            List<ItemStack> results = new ArrayList<>();
            int length = java.lang.reflect.Array.getLength(value);
            for (int index = 0; index < length; index++) {
                results.addAll(extractStacks(java.lang.reflect.Array.get(value, index)));
            }
            return results;
        }
        if (value instanceof Map<?, ?> map) {
            List<ItemStack> results = new ArrayList<>();
            for (Object element : map.values()) {
                results.addAll(extractStacks(element));
            }
            return results;
        }
        Object unwrapped = unwrapValue(value);
        if (unwrapped != value) {
            return extractStacks(unwrapped);
        }
        return List.of();
    }

    private ItemStack extractFirstStack(Object value) {
        if (value instanceof Container container) {
            return extractOutputFromContainer(container);
        }
        if (value instanceof IItemHandler handler) {
            return extractOutputFromHandler(handler);
        }

        Object unwrapped = unwrapValue(value);
        if (unwrapped != value) {
            return extractFirstStack(unwrapped);
        }

        List<ItemStack> stacks = extractStacks(value);
        return stacks.isEmpty() ? ItemStack.EMPTY : stacks.get(0).copy();
    }

    private ItemStack extractFirstStackFromSlots(BlockEntity blockEntity, int... preferredSlots) {
        if (blockEntity == null || preferredSlots == null || preferredSlots.length == 0) {
            return ItemStack.EMPTY;
        }

        if (blockEntity instanceof Container container) {
            for (int slot : preferredSlots) {
                if (slot < 0 || slot >= container.getContainerSize()) {
                    continue;
                }
                ItemStack stack = container.getItem(slot);
                if (!stack.isEmpty()) {
                    return stack.copy();
                }
            }
        }

        if (blockEntity.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER).isPresent()) {
            IItemHandler handler = blockEntity.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER).orElse(null);
            if (handler != null) {
                for (int slot : preferredSlots) {
                    if (slot < 0 || slot >= handler.getSlots()) {
                        continue;
                    }
                    ItemStack stack = handler.getStackInSlot(slot);
                    if (!stack.isEmpty()) {
                        return stack.copy();
                    }
                }
            }
        }

        return ItemStack.EMPTY;
    }

    private int extractInt(Object value) {
        if (value instanceof Optional<?> optional) {
            return optional.map(this::extractInt).orElse(0);
        }
        if (value instanceof Number number) {
            return Math.max(0, number.intValue());
        }

        Object unwrapped = unwrapValue(value);
        if (unwrapped != value) {
            return extractInt(unwrapped);
        }

        Integer reflectedInt = extractIntByReflection(value);
        if (reflectedInt != null) {
            return reflectedInt;
        }

        return 0;
    }

    private Integer extractIntByReflection(Object value) {
        if (value == null) {
            return null;
        }

        for (String methodName : INT_METHODS) {
            Method method = findZeroArgMethod(value.getClass(), methodName);
            if (method == null) {
                continue;
            }

            try {
                method.setAccessible(true);
                Object nestedValue = method.invoke(value);
                if (nestedValue != null && nestedValue != value) {
                    return extractInt(nestedValue);
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }

        for (String fieldName : INT_FIELDS) {
            Field field = findField(value.getClass(), fieldName);
            if (field == null) {
                continue;
            }

            try {
                field.setAccessible(true);
                Object nestedValue = field.get(value);
                if (nestedValue != null && nestedValue != value) {
                    return extractInt(nestedValue);
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }

        return null;
    }

    private ItemStack extractOutputFromContainer(Container container) {
        for (int slot = container.getContainerSize() - 1; slot >= 0; slot--) {
            ItemStack stack = container.getItem(slot);
            if (!stack.isEmpty()) {
                return stack.copy();
            }
        }
        return ItemStack.EMPTY;
    }

    private ItemStack extractOutputFromHandler(IItemHandler handler) {
        for (int slot = handler.getSlots() - 1; slot >= 0; slot--) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                return stack.copy();
            }
        }
        return ItemStack.EMPTY;
    }

    private Object unwrapValue(Object value) {
        if (value == null) {
            return null;
        }

        String[] methodNames = new String[]{"resolve", "getValue", "value", "getStack", "getResult", "getOutput"};
        for (String methodName : methodNames) {
            try {
                Method method = value.getClass().getMethod(methodName);
                if (method.getParameterCount() == 0) {
                    method.setAccessible(true);
                    Object unwrapped = method.invoke(value);
                    if (unwrapped != null) {
                        return unwrapped;
                    }
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return value;
    }

    private interface MemberHandle {
        Object read(Object instance);
    }

    private record MethodHandle(Method method) implements MemberHandle {
        @Override
        public Object read(Object instance) {
            try {
                return method.invoke(instance);
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
        }
    }

    private record FieldHandle(Field field) implements MemberHandle {
        @Override
        public Object read(Object instance) {
            try {
                return field.get(instance);
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
        }
    }
}



