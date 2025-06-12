package com.ldtteam.blockui.util.cursor;

import com.ldtteam.blockui.util.cursor.CursorUtils.StandardCursor;
import net.minecraft.resources.ResourceLocation;

/**
 * Interface to wrap various cursors
 */
@FunctionalInterface
public interface Cursor
{
    /** Probably arrow, but OS dependend */
    public static final Cursor DEFAULT = named(() -> CursorUtils.setStandardCursor(StandardCursor.DEFAULT), StandardCursor.DEFAULT);
    public static final Cursor ARROW = named(() -> CursorUtils.setStandardCursor(StandardCursor.ARROW), StandardCursor.ARROW);
    public static final Cursor TEXT_CURSOR = named(() -> CursorUtils.setStandardCursor(StandardCursor.TEXT_CURSOR), StandardCursor.TEXT_CURSOR);
    public static final Cursor CROSSHAIR = named(() -> CursorUtils.setStandardCursor(StandardCursor.CROSSHAIR), StandardCursor.CROSSHAIR);
    public static final Cursor HAND = named(() -> CursorUtils.setStandardCursor(StandardCursor.HAND), StandardCursor.HAND);
    public static final Cursor HORIZONTAL_RESIZE = named(() -> CursorUtils.setStandardCursor(StandardCursor.HORIZONTAL_RESIZE), StandardCursor.HORIZONTAL_RESIZE);
    public static final Cursor VERTICAL_RESIZE = named(() -> CursorUtils.setStandardCursor(StandardCursor.VERTICAL_RESIZE), StandardCursor.VERTICAL_RESIZE);
    public static final Cursor RESIZE = named(() -> CursorUtils.setStandardCursor(StandardCursor.RESIZE), StandardCursor.RESIZE);

    public static Cursor of(final ResourceLocation resLoc)
    {
        CursorUtils.loadCursorTexture(resLoc);
        return named(() -> CursorUtils.setCursorImage(resLoc), resLoc);
    }

    /**
     * Apply cursor to main window
     */
    void apply();

    static Cursor named(final Runnable applier, final Object name)
    {
        return new Cursor()
        {
            @Override
            public void apply()
            {
                applier.run();
            }

            @Override
            public String toString()
            {
                return "Cursor: " + name;
            }
        };
    }
}
