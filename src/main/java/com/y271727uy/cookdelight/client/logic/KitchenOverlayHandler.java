package com.y271727uy.cookdelight.client.logic;

import com.y271727uy.cookdelight.client.recipe.LookupProfile;
import com.y271727uy.cookdelight.client.recipe.RecipeLookupService;
import com.y271727uy.cookdelight.client.recipe.ResolvedRecipe;
import com.y271727uy.cookdelight.client.state.KitchenOverlayState;
import com.y271727uy.cookdelight.config.CookDelightConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class KitchenOverlayHandler {
    private static final float FADE_STEP = 0.15f;

    private final RecipeLookupService recipeLookupService;
    private KitchenOverlayState state = KitchenOverlayState.hidden();

    public KitchenOverlayHandler(RecipeLookupService recipeLookupService) {
        this.recipeLookupService = recipeLookupService;
    }

    public void tick(Minecraft minecraft) {
        if (!CookDelightConfig.CLIENT.enableCookingPotOverlay.get() && !CookDelightConfig.CLIENT.enableSkilletOverlay.get()) {
            state = KitchenOverlayState.hidden();
            return;
        }

        if (minecraft.level == null || minecraft.player == null) {
            state = fadeOut(state);
            return;
        }

        Optional<KitchenTarget> kitchenTarget = findKitchenTarget(minecraft);
        if (kitchenTarget.isEmpty()) {
            state = fadeOut(state);
            return;
        }

        KitchenSnapshot snapshot = readSnapshot(kitchenTarget.get().blockPos(), kitchenTarget.get().blockEntity(), kitchenTarget.get().skillet());
        if (snapshot.ingredients().isEmpty()) {
            state = fadeOut(state);
            return;
        }

        LookupProfile profile = kitchenTarget.get().skillet() ? LookupProfile.SKILLET : LookupProfile.COOKING_POT;
        Optional<ResolvedRecipe> recipe = recipeLookupService.findPredictedRecipe(minecraft.level, snapshot.ingredients(), profile);
        state = visible(snapshot, recipe.orElse(null), kitchenTarget.get().skillet());
    }

    public KitchenOverlayState getState() {
        return state;
    }

    private KitchenOverlayState visible(KitchenSnapshot snapshot, ResolvedRecipe recipe, boolean skillet) {
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
                previousFade,
                fade
        );
    }

    private Optional<KitchenTarget> findKitchenTarget(Minecraft minecraft) {
        if (minecraft.hitResult == null || minecraft.hitResult.getType() != HitResult.Type.BLOCK) {
            return Optional.empty();
        }

        BlockHitResult blockHitResult = (BlockHitResult) minecraft.hitResult;
        BlockEntity blockEntity = minecraft.level.getBlockEntity(blockHitResult.getBlockPos());
        if (blockEntity == null) {
            return Optional.empty();
        }

        String id = String.valueOf(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType()));
        if ("farmersdelight:cooking_pot".equals(id)) {
            if (!CookDelightConfig.CLIENT.enableCookingPotOverlay.get()) {
                return Optional.empty();
            }
            return Optional.of(new KitchenTarget(blockHitResult.getBlockPos(), blockEntity, false));
        }

        if ("farmersdelight:skillet".equals(id)) {
            if (!CookDelightConfig.CLIENT.enableSkilletOverlay.get()) {
                return Optional.empty();
            }
            return Optional.of(new KitchenTarget(blockHitResult.getBlockPos(), blockEntity, true));
        }

        return Optional.empty();
    }

    private KitchenSnapshot readSnapshot(BlockPos pos, BlockEntity blockEntity, boolean skillet) {
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

            if (ingredients.isEmpty() && skillet && nbt.contains("Ingredient")) {
                ItemStack parsed = ItemStack.of(nbt.getCompound("Ingredient"));
                if (!parsed.isEmpty()) {
                    ingredients.add(parsed.copy());
                }
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

    private record KitchenTarget(BlockPos blockPos, BlockEntity blockEntity, boolean skillet) {
    }

    private record KitchenSnapshot(BlockPos blockPos, List<ItemStack> ingredients, int currentCookTime, int totalCookTime) {
    }
}


