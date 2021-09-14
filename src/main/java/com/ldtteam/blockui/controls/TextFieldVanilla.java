package com.ldtteam.blockui.controls;

import com.ldtteam.blockui.PaneParams;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.SharedConstants;

/**
 * Mimics Vanilla text fields.
 */
public class TextFieldVanilla extends TextField
{
    private static final int BACKGROUND_WIDTH_OFFSET = 8;
    private static final float BACKGROUND_X_TRANSLATE = 4F;
    private static final int BACKGROUND_Y_TRANSLATE_OFFSET = 8;
    private static final int BACKGROUND_MOUSE_OFFSET_X = 4;
    private boolean backgroundEnabled = true;
    private int backgroundOuterColor = 0xFFA0A0A0;
    private int backgroundInnerColor = 0xFF000000;

    /**
     * Required default constructor.
     */
    public TextFieldVanilla()
    {
        super();
        filter = new FilterVanilla();
    }

    /**
     * Constructor called when creating an object from xml.
     *
     * @param params xml parameters.
     */
    public TextFieldVanilla(final PaneParams params)
    {
        super(params);
        backgroundEnabled = params.getBoolean("background", backgroundEnabled);
        backgroundOuterColor = params.getColor("backgroundOuter", backgroundOuterColor);
        backgroundInnerColor = params.getColor("backgroundInner", backgroundInnerColor);
        filter = new FilterVanilla();
    }

    public boolean isBackgroundEnabled()
    {
        return backgroundEnabled;
    }

    public void setBackgroundEnabled(final boolean e)
    {
        backgroundEnabled = e;
    }

    public int getBackgroundOuterColor()
    {
        return backgroundOuterColor;
    }

    public void setBackgroundOuterColor(final int c)
    {
        backgroundOuterColor = c;
    }

    public int getBackgroundInnerColor()
    {
        return backgroundInnerColor;
    }

    public void setBackgroundInnerColor(final int c)
    {
        backgroundInnerColor = c;
    }

    @Override
    public int getInternalWidth()
    {
        return backgroundEnabled ? (getWidth() - BACKGROUND_WIDTH_OFFSET) : getWidth();
    }

    @Override
    public void drawSelf(final PoseStack ms, final double mx, final double my)
    {
        if (backgroundEnabled)
        {
            // Draw box
            drawLineRect(ms, x - 1, y - 1, width + 2, height + 2, backgroundOuterColor);
            fill(ms, x, y, width, height, backgroundInnerColor);

            ms.pushPose();
            ms.translate(BACKGROUND_X_TRANSLATE, (height - BACKGROUND_Y_TRANSLATE_OFFSET) / 2, 0);
        }

        super.drawSelf(ms, mx, my);

        if (backgroundEnabled)
        {
            ms.popPose();
        }
    }

    @Override
    public boolean handleClick(final double mx, final double my)
    {
        double mouseX = mx;

        if (backgroundEnabled)
        {
            mouseX -= BACKGROUND_MOUSE_OFFSET_X;
        }

        super.handleClick(mouseX, my);
        return true;
    }

    /*
     * private static class FilterNumeric implements Filter
     * {
     * @Override
     * public String filter(final String s)
     * {
     * final StringBuilder sb = new StringBuilder();
     * for (final char c : s.toCharArray())
     * {
     * if (isAllowedCharacter(c))
     * {
     * sb.append(c);
     * }
     * }
     * return sb.toString();
     * }
     * @Override
     * public boolean isAllowedCharacter(final char c)
     * {
     * return Character.isDigit(c);
     * }
     * }
     */

    private static class FilterVanilla implements Filter
    {
        @Override
        public String filter(final String s)
        {
            return SharedConstants.filterText(s);
        }

        @Override
        public boolean isAllowedCharacter(final char c)
        {
            return SharedConstants.isAllowedChatCharacter(c);
        }
    }
}
