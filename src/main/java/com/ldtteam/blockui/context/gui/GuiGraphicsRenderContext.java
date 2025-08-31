package com.ldtteam.blockui.context.gui;

import com.ldtteam.blockui.context.BatchStringSupplier;
import com.ldtteam.blockui.context.RenderContext;
import com.ldtteam.blockui.util.Crash;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class GuiGraphicsRenderContext extends RenderContext
{
    private static final int packedTextLight = LightTexture.pack(15, 15); // 15728880;
    private GuiGraphics guiGraphics;

    public void grab(final GuiGraphics vanillGuiGraphics)
    {
        super.grab();
        // wrap with our posestack
        this.guiGraphics = new GuiGraphics(mc, poseStack, vanillGuiGraphics.bufferSource());
    }

    @Override
    public void renderItem(final ItemStack itemStack, final int x, final int y, @Nullable final String altStackSize)
    {
        guiGraphics.renderItem(itemStack, x, y);
        guiGraphics.renderItemDecorations(getFont(itemStack), itemStack, x, y, altStackSize);
        clearRenderState();
    }

    @Override
    public int drawString(final String text, final float x, final float y, final int color, final boolean shadow)
    {
        final int result = guiGraphics.drawString(mc.font, text, x, y, color, shadow);
        clearRenderState();
        return result;
    }

    @Override
    public void drawStringsInBatch(final boolean shadow,
        final int defaultColor,
        final int backgroundColor,
        final Font.DisplayMode mode,
        final BatchStringSupplier textSupplier)
    {
        final MultiBufferSource.BufferSource drawBuffer = guiGraphics.bufferSource();
        final Matrix4f m = poseStack.last().pose();

        textSupplier.accept((text, x, y) -> mc.font
            .drawInBatch(text, x, y, defaultColor, shadow, m, drawBuffer, mode, backgroundColor, packedTextLight));

        guiGraphics.flush();
        clearRenderState();
    }

    @Override
    public void pushTooltipTranslate(final Object renderingAdapter, final int tooltipWidth, final int tooltipHeight)
    {
        if (!(renderingAdapter instanceof final ScreenAdapter scr))
        {
            // TODO: harden this with types somehow?
            throw Crash.state("Gui context with non-screen adapter: " + renderingAdapter.getClass());
        }

        // TODO: validate math done in same scales, especially y sus
        final double renderScale = scr.getRenderScale(), vanillaScale = scr.getVanillaGuiScale();
        final int fbWidth = mc.getWindow().getWidth(), fbHeight = mc.getWindow().getHeight();

        final int marginOffset = 4;
        final int cursorBoxSize = 12;

        int x = scr.getAbsoluteMouseX() + cursorBoxSize;
        int y = Math.max(marginOffset, scr.getAbsoluteMouseY() - cursorBoxSize);

        // scaled absolute cursor > width - scaled tooltip size
        if (x * vanillaScale > fbWidth - renderScale * (tooltipWidth + marginOffset))
        {
            // if overflow then flip tooltip to left
            x -= 2 * cursorBoxSize - tooltipWidth;
        }

        // same condition just need values later too
        final int absoluteY = (int) (y * vanillaScale);
        final int maxAbsoluteMy = fbHeight - (int) (renderScale * (tooltipHeight + marginOffset));
        if (absoluteY > maxAbsoluteMy)
        {
            // but we don't flip here but just move upwards
            y = Math.max(marginOffset, y - (absoluteY - maxAbsoluteMy) / 2);
        }

        poseStack.pushPose();
        poseStack.setIdentity();
        poseStack.translate(x, y, 0);
        poseStack.scale((float) renderScale, (float) renderScale, 1);
    }
}
