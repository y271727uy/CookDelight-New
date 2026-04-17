package com.y271727uy.cookdelight.client.render;

import com.y271727uy.cookdelight.client.state.ItemFrameOverlayState;
import com.y271727uy.cookdelight.registry.ItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;

public class ItemFrameOverlayRenderer {
    private final BoxRenderer boxRenderer = new BoxRenderer();

    public void render(RenderGuiOverlayEvent.Post event, ItemFrameOverlayState state) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        float fade = Mth.lerp(event.getPartialTick(), state.previousFadeProgress(), state.fadeProgress());
        if (fade <= 0.01f || state.ingredients().isEmpty() || state.targetItem().isEmpty() || state.itemsPerRow() <= 0) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        int baseItemsWidth = state.itemsPerRow() * 18;
        int outputWidth = 46;
        int boxWidth = Math.max(70, baseItemsWidth + outputWidth);
        int boxHeight = Math.max(40, state.rows() * 18 + 22);
        int x = minecraft.getWindow().getGuiScaledWidth() / 2 + 15;
        int y = minecraft.getWindow().getGuiScaledHeight() / 2 - 20;

        BoxRenderer.BoxRenderContext box = boxRenderer.begin(graphics, x, y, boxWidth, boxHeight, fade, 180);
        graphics.drawString(minecraft.font, Component.translatable("gui.cookdelight.recipe"), box.x() + 4, box.y() + 4, box.textColor(), false);

        int gridStartX = box.x() + 4;
        int gridStartY = box.y() + 17;
        for (int index = 0; index < state.ingredients().size(); index++) {
            ItemStack[] options = state.ingredients().get(index).getItems();
            if (options.length == 0) {
                continue;
            }

            int animatedIndex = (int) ((minecraft.level.getGameTime() / 20) % options.length);
            int col = index % state.itemsPerRow();
            int row = index / state.itemsPerRow();
            graphics.renderItem(options[animatedIndex], gridStartX + col * 18, gridStartY + row * 18);
        }

        int afterGridX = gridStartX + baseItemsWidth + 4;
        int centerY = gridStartY + (state.rows() * 18) / 2 - 9;
        graphics.renderItem(new ItemStack(ItemRegistry.EQUALS.get()), afterGridX, centerY);
        graphics.renderItem(state.targetItem(), afterGridX + 20, centerY);
        boxRenderer.end(graphics);
    }
}

