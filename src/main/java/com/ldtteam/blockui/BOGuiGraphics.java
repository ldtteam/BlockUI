package com.ldtteam.blockui;

import com.ldtteam.blockui.util.cursor.Cursor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

public class BOGuiGraphics extends GuiGraphics
{
    private int cursorMaxDepth = -1;
    private Cursor selectedCursor = Cursor.DEFAULT;

    public BOGuiGraphics(final Minecraft mc, final PoseStack ps, final BufferSource buffers)
    {
        super(mc, ps, buffers);
    }

    private Font getFont(@Nullable final ItemStack itemStack)
    {
        if (itemStack != null)
        {
            final Font font = IClientItemExtensions.of(itemStack).getFont(itemStack, IClientItemExtensions.FontContext.ITEM_COUNT);
            if (font != null)
            {
                return font;
            }
        }
        return minecraft.font;
    }

    public void renderItemDecorations(final ItemStack itemStack, final int x, final int y)
    {
        super.renderItemDecorations(getFont(itemStack), itemStack, x, y);
    }

    public void renderItemDecorations(final ItemStack itemStack, final int x, final int y, @Nullable final String altStackSize)
    {
        super.renderItemDecorations(getFont(itemStack), itemStack, x, y, altStackSize);
    }

    public int drawString(final String text, final float x, final float y, final int color)
    {
        return drawString(text, x, y, color, false);
    }

    public int drawString(final String text, final float x, final float y, final int color, final boolean shadow)
    {
        return super.drawString(minecraft.font, text, x, y, color, shadow);
    }

    public void setCursor(final Cursor cursor)
    {
        if (pose().poseStack.size() >= cursorMaxDepth)
        {
            cursorMaxDepth = pose().poseStack.size();
            selectedCursor = cursor;
        }
    }

    public void applyCursor()
    {
        selectedCursor.apply();
    }
}
