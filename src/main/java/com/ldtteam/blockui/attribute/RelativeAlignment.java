package com.ldtteam.blockui.attribute;

import com.ldtteam.blockui.util.Crash.CheckArgument;

/**
 * Alignment relative to parent element.
 */
public enum RelativeAlignment
{
    TOP_LEFT(VerticalAlignment.TOP, HorizontalAlignment.LEFT),
    TOP_MIDDLE(VerticalAlignment.TOP, HorizontalAlignment.MIDDLE),
    TOP_RIGHT(VerticalAlignment.TOP, HorizontalAlignment.RIGHT),
    MIDDLE_LEFT(VerticalAlignment.MIDDLE, HorizontalAlignment.LEFT),
    MIDDLE_MIDDLE(VerticalAlignment.MIDDLE, HorizontalAlignment.MIDDLE),
    MIDDLE_RIGHT(VerticalAlignment.MIDDLE, HorizontalAlignment.RIGHT),
    BOTTOM_LEFT(VerticalAlignment.BOTTOM, HorizontalAlignment.LEFT),
    BOTTOM_MIDDLE(VerticalAlignment.BOTTOM, HorizontalAlignment.MIDDLE),
    BOTTOM_RIGHT(VerticalAlignment.BOTTOM, HorizontalAlignment.RIGHT);

    public final VerticalAlignment vertical;
    public final HorizontalAlignment horizontal;

    private RelativeAlignment(final VerticalAlignment vertical, final HorizontalAlignment horizontal)
    {
        this.vertical = vertical;
        this.horizontal = horizontal;
    }

    /**
     * @param  outerWidth   width of parent element
     * @param  innerXoffset offset from aligned position (is automatically negated for right-bottom group)
     * @param  innerWidth   width of child element (ie. element having this alignment)
     * @return              zero-aligned offset position (ie. offset from top-left)
     */
    public int calculateX(final int outerWidth, int innerXoffset, final int innerWidth)
    {
        return switch (horizontal)
        {
            case LEFT -> validateFullRange(innerXoffset, outerWidth - innerWidth);
            case MIDDLE -> validateMiddleRange(innerXoffset + (outerWidth - innerWidth) / 2, innerXoffset, outerWidth - innerWidth);
            case RIGHT -> outerWidth - innerWidth - validateFullRange(innerXoffset, outerWidth - innerWidth);
        };
    }

    /**
     * @param  outerHeight  height of parent element
     * @param  innerYoffset offset from aligned position (is automatically negated for right-bottom group)
     * @param  innerHeight  height of child element (ie. element having this alignment)
     * @return              zero-aligned offset position (ie. offset from top-left)
     */
    public int calculateY(final int outerHeight, int innerYoffset, final int innerHeight)
    {
        return switch (vertical)
        {
            case TOP -> validateFullRange(innerYoffset, outerHeight - innerHeight);
            case MIDDLE ->
                validateMiddleRange(innerYoffset + (outerHeight - innerHeight) / 2, innerYoffset, outerHeight - innerHeight);
            case BOTTOM -> outerHeight - innerHeight - validateFullRange(innerYoffset, outerHeight - innerHeight);
        };
    }

    private static int validateFullRange(final int pos, final int range)
    {
        return CheckArgument.inRange(pos, 0, range, "Alignment offset");
    }

    private static int validateMiddleRange(final int alignedPos, final int pos, final int range)
    {
        return CheckArgument
            .inRange(alignedPos, 0, range, "Value \"" + pos + "\" makes the offset be out of parent view, middle alignment offset");
    }

    public enum VerticalAlignment
    {
        TOP,
        MIDDLE,
        BOTTOM;
    }

    public enum HorizontalAlignment
    {
        LEFT,
        MIDDLE,
        RIGHT;
    }
}
