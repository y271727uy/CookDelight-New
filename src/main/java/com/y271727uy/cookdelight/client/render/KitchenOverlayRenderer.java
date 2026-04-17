package com.y271727uy.cookdelight.client.render;

import com.y271727uy.cookdelight.client.state.KitchenOverlayState;
import com.y271727uy.cookdelight.registry.ItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;

public class KitchenOverlayRenderer {
    private final BoxRenderer boxRenderer = new BoxRenderer();

    public void render(RenderGuiOverlayEvent.Post event, KitchenOverlayState state) {
        float fade = Mth.lerp(event.getPartialTick(), state.previousFadeProgress(), state.fadeProgress());
        if (fade <= 0.01f || state.visibleIngredients().isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        GuiGraphics graphics = event.getGuiGraphics();
        boolean hasOutput = !state.predictedOutput().isEmpty();
        int baseItemsWidth = state.visibleIngredients().size() * 18;
        int outputWidth = hasOutput ? 40 : 8;
        int boxWidth = Math.max(70, baseItemsWidth + outputWidth);
        int boxHeight = state.skillet() ? 38 : 44;
        int x = minecraft.getWindow().getGuiScaledWidth() / 2 - boxWidth / 2;
        int y = minecraft.getWindow().getGuiScaledHeight() / 2 + 15;

        BoxRenderer.BoxRenderContext box = boxRenderer.begin(graphics, x, y, boxWidth, boxHeight, fade, 200);
        Component header = state.skillet()
                ? Component.translatable("gui.cookdelight.skillet")
                : Component.translatable("gui.cookdelight.cooking_pot");
        graphics.drawString(minecraft.font, header, box.x() + 4, box.y() + 4, box.textColor(), false);

        int currentX = box.x() + 4;
        int itemY = box.y() + 17;
        for (ItemStack ingredient : state.visibleIngredients()) {
            graphics.renderItem(ingredient, currentX, itemY);
            currentX += 18;
        }

        if (hasOutput) {
            graphics.renderItem(new ItemStack(ItemRegistry.EQUALS.get()), currentX + 1, itemY);
            graphics.renderItem(state.predictedOutput(), currentX + 21, itemY);
        }

        if (!state.skillet() && state.totalCookTime() > 0) {
            float progress = Mth.clamp((float) state.currentCookTime() / (float) state.totalCookTime(), 0.0f, 1.0f);
            int barWidth = box.width() - 8;
            int filledWidth = (int) (barWidth * progress);
            graphics.fill(box.x() + 4, box.y() + 37, box.x() + 4 + barWidth, box.y() + 39, (box.alpha() << 24) | 0x333333);
            graphics.fill(box.x() + 4, box.y() + 37, box.x() + 4 + filledWidth, box.y() + 39, (box.alpha() << 24) | 0xFF8800);
        }

        boxRenderer.end(graphics);
    }
}
