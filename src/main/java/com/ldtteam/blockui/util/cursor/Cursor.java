package com.ldtteam.blockui.util.cursor;

import com.ldtteam.blockui.util.cursor.CursorUtils.StandardCursor;
import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface Cursor
{
    /** Probably arrow, but OS dependend */
    public static final Cursor DEFAULT = () -> CursorUtils.setStandardCursor(StandardCursor.DEFAULT);
    public static final Cursor ARROW = () -> CursorUtils.setStandardCursor(StandardCursor.ARROW);
    public static final Cursor TEXT_CURSOR = () -> CursorUtils.setStandardCursor(StandardCursor.TEXT_CURSOR);
    public static final Cursor CROSSHAIR = () -> CursorUtils.setStandardCursor(StandardCursor.CROSSHAIR);
    public static final Cursor HAND = () -> CursorUtils.setStandardCursor(StandardCursor.HAND);
    public static final Cursor HORIZONTAL_RESIZE = () -> CursorUtils.setStandardCursor(StandardCursor.HORIZONTAL_RESIZE);
    public static final Cursor VERTICAL_RESIZE = () -> CursorUtils.setStandardCursor(StandardCursor.VERTICAL_RESIZE);
    public static final Cursor RESIZE = () -> CursorUtils.setStandardCursor(StandardCursor.RESIZE);

    public static Cursor of(final ResourceLocation resLoc)
    {
        return () -> CursorUtils.setCursorImage(resLoc);
    }

    void apply();

    default void reset()
    {
        CursorUtils.resetCursor();
    }
}
