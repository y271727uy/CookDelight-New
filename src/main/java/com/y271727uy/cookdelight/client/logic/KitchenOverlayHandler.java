package com.y271727uy.cookdelight.client.logic;

import com.y271727uy.cookdelight.client.recipe.RecipeLookupService;
import com.y271727uy.cookdelight.client.recipe.ResolvedRecipe;
import com.y271727uy.cookdelight.client.state.KitchenOverlayState;
import com.y271727uy.cookdelight.config.CookDelightConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class KitchenOverlayHandler {
    private static final float FADE_STEP = 0.15f;

    private final RecipeLookupService recipeLookupService;
    private final KitchenOverlayTargetProvider targetProvider;
    private KitchenOverlayState state = KitchenOverlayState.hidden();

    public KitchenOverlayHandler(RecipeLookupService recipeLookupService) {
        this(recipeLookupService, KitchenOverlayProviders.configured());
    }

    public KitchenOverlayHandler(RecipeLookupService recipeLookupService, KitchenOverlayTargetProvider targetProvider) {
        this.recipeLookupService = recipeLookupService;
        this.targetProvider = targetProvider;
    }

    public void tick(Minecraft minecraft) {
        if (!CookDelightConfig.CLIENT.enableCookingPotOverlay.get()
                && !CookDelightConfig.CLIENT.enableSkilletOverlay.get()
                && !CookDelightConfig.CLIENT.enableKaleidoscopeOverlay.get()
                && !CookDelightConfig.CLIENT.enableKegOverlay.get()) {
            state = KitchenOverlayState.hidden();
            return;
        }

        if (minecraft.level == null || minecraft.player == null) {
            state = fadeOut(state);
            return;
        }

        Optional<KitchenOverlayTarget> kitchenTarget = targetProvider.findTarget(minecraft);
        if (kitchenTarget.isEmpty()) {
            state = fadeOut(state);
            return;
        }

        KitchenOverlayTarget target = kitchenTarget.get();
        KitchenSnapshot snapshot = readSnapshot(target.blockPos(), target.blockEntity(), target.allowReflectionFallback());
        if (snapshot.ingredients().isEmpty()) {
            state = fadeOut(state);
            return;
        }

        Optional<ResolvedRecipe> recipe = recipeLookupService.findPredictedRecipe(minecraft.level, snapshot.ingredients(), target.lookupProfile());
        state = visible(snapshot, recipe.orElse(null), target.skillet(), target.title());
    }

    public KitchenOverlayState getState() {
        return state;
    }

    private KitchenOverlayState visible(KitchenSnapshot snapshot, ResolvedRecipe recipe, boolean skillet, Component title) {
        float previousFade = state.fadeProgress();
        float fade = Math.min(1.0f, previousFade + FADE_STEP);
        return new KitchenOverlayState(
                snapshot.blockPos(),
                snapshot.ingredients(),
                recipe != null ? recipe.ingredients() : List.of(),
                recipe != null ? recipe.output() : ItemStack.EMPTY,
                snapshot.currentCookTime(),
                snapshot.totalCookTime(),
                skillet,
                title,
                previousFade,
                fade
        );
    }

    private KitchenOverlayState fadeOut(KitchenOverlayState currentState) {
        float previousFade = currentState.fadeProgress();
        float fade = Math.max(0.0f, previousFade - FADE_STEP);
        if (fade <= 0.0f) {
            return KitchenOverlayState.hidden();
        }

        return new KitchenOverlayState(
                currentState.targetPos(),
                currentState.visibleIngredients(),
                currentState.recipeIngredients(),
                currentState.predictedOutput(),
                currentState.currentCookTime(),
                currentState.totalCookTime(),
                currentState.skillet(),
                currentState.title(),
                previousFade,
                fade
        );
    }

    private KitchenSnapshot readSnapshot(BlockPos pos, BlockEntity blockEntity, boolean allowReflectionFallback) {
        List<ItemStack> ingredients = new ArrayList<>();
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
        }

        if (ingredients.isEmpty()) {
            Direction[] directions = new Direction[]{null, Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
            for (Direction direction : directions) {
                blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, direction).ifPresent(handler -> {
                    int limit = Math.min(6, handler.getSlots());
                    for (int slot = 0; slot < limit; slot++) {
                        ItemStack stack = handler.getStackInSlot(slot);
                        if (!stack.isEmpty()) {
                            ingredients.add(stack.copy());
                        }
                    }
                });
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

        return new KitchenSnapshot(pos, ingredients, currentCookTime, totalCookTime);
    }

    private List<ItemStack> readItemsByReflection(BlockEntity blockEntity) {
        List<ItemStack> results = new ArrayList<>();
        String[] methodNames = new String[]{"getItem", "getIngredient", "getIngredients", "getContents", "getHeldItem", "getOutput", "getResult", "getResultItem"};

        for (String methodName : methodNames) {
            try {
                Object value = blockEntity.getClass().getMethod(methodName).invoke(blockEntity);
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
            } catch (ReflectiveOperationException ignored) {
            }

            if (!results.isEmpty()) {
                break;
            }
        }

        return results;
    }


    private record KitchenSnapshot(BlockPos blockPos, List<ItemStack> ingredients, int currentCookTime, int totalCookTime) {
    }
}

