package com.y271727uy.cookdelight.client.render;

import net.minecraft.client.gui.GuiGraphics;

public class BoxRenderer {
    public BoxRenderContext begin(GuiGraphics graphics, int x, int y, int width, int height, float fade, int maxAlpha) {
        graphics.pose().pushPose();

        float scale = 0.85f + (0.15f * fade);
        float translateX = x + (width / 2.0f);
        float translateY = y + (height / 2.0f);
        graphics.pose().translate(translateX, translateY, 0);
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.pose().translate(-translateX, -translateY, 0);

        int alpha = Math.max(0, Math.min(maxAlpha, (int) (maxAlpha * fade)));
        int bgColor = (alpha << 24) | 0x111111;
        int borderColor = (alpha << 24) | 0x2A2A2A;
        int textColor = ((int) (255 * fade) << 24) | 0xFFFFFF;

        graphics.fill(x - 2, y - 2, x + width + 2, y + height + 2, borderColor);
        graphics.fill(x, y, x + width, y + height, bgColor);

        return new BoxRenderContext(x, y, width, height, alpha, textColor);
    }

    public void end(GuiGraphics graphics) {
        graphics.pose().popPose();
    }

    public record BoxRenderContext(int x, int y, int width, int height, int alpha, int textColor) {
    }
}
