package com.ldtteam.blockui;

import com.ldtteam.blockui.util.cursor.Cursor;
import com.ldtteam.common.fakelevel.SingleBlockFakeLevel;
import com.mojang.blaze3d.platform.cursor.CursorType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;

public class BOGuiGraphics extends GuiGraphicsExtractor
{
    private SingleBlockFakeLevel fakeLevel = null;

    private int cursorMaxDepth = -1;
    private CursorType selectedCursor = Cursor.DEFAULT;

    public BOGuiGraphics(final Minecraft mc, final CountingMatrix3x2fStack ps, final GuiRenderState renderState, final int mx, final int my)
    {
        super(mc, ps, renderState, mx, my);
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
        super.itemDecorations(getFont(itemStack), itemStack, x, y);
    }

    public void renderItemDecorations(final ItemStack itemStack, final int x, final int y, @Nullable final String altStackSize)
    {
        super.itemDecorations(getFont(itemStack), itemStack, x, y, altStackSize);
    }

    public int drawString(final String text, final int x, final int y, final int color)
    {
        return drawString(text, x, y, color, false);
    }

    public int drawString(final String text, final int x, final int y, final int color, final boolean shadow)
    {
        super.text(minecraft.font, text, x, y, color, shadow);
        return x + minecraft.font.width(text); // should return end pos
    }

    public void setCursor(final CursorType cursor)
    {
        final int size = ((CountingMatrix3x2fStack) pose()).size;
        if (size >= cursorMaxDepth)
        {
            cursorMaxDepth = size;
            selectedCursor = cursor;
        }
    }

    /**
     * @param debugXoffset debug string x offset
     */
    public CursorType applyCursor(final int debugXoffset)
    {
        if (Pane.debugging)
        {
            drawString(selectedCursor.toString(), debugXoffset, -minecraft.font.lineHeight, Color.getByName("white"));
        }

        // requestCursor(selectedCursor);
        // need to direct this to vanilla gui
        return selectedCursor;
    }

    public static double getAltSpeedFactor(final Minecraft mc)
    {
        return mc.hasAltDown() ? 5 : 1;
    }

    public ScreenRectangle calcTransformedPaneBounds(final Pane pane)
    {
        return new ScreenRectangle(0, 0, pane.getWidth(), pane.getHeight()).transformAxisAligned(pose());
    }

    public SingleBlockFakeLevel getFakeLevel()
    {
        if (fakeLevel == null)
        {
            fakeLevel = new SingleBlockFakeLevel(Minecraft.getInstance().level);
        }
        return fakeLevel;
    }

    public static class CountingMatrix3x2fStack extends Matrix3x2fStack
    {
        private int size = 0;

        public CountingMatrix3x2fStack(final int stackSize)
        {
            super(stackSize);
        }

        @Override
        public Matrix3x2fStack clear()
        {
            size = 0;
            return super.clear();
        }

        @Override
        public Matrix3x2fStack popMatrix()
        {
            size--;
            return super.popMatrix();
        }

        @Override
        public Matrix3x2fStack pushMatrix()
        {
            size++;
            return super.pushMatrix();
        }
    }
}
