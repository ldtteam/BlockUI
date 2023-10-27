package com.ldtteam.blockui.util.cursor;

import com.ldtteam.blockui.util.texture.CursorTexture;
import com.ldtteam.blockui.util.texture.IsOurTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to manage cursor image.
 */
public class CursorUtils
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CursorUtils.class);
    private static final long[] STANDARD_CURSORS = new long[StandardCursor.values().length];
    private static long lastCursorAddress = 0;

    /**
     * Sets cursor image using given resource location.
     *
     * @param  rl image resource location
     * @return    cursor texture reference
     */
    public static CursorTexture setCursorImage(final ResourceLocation rl)
    {
        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(rl);
        if (!(texture instanceof CursorTexture))
        {
            if (IsOurTexture.isOur(texture))
            {
                LOGGER.warn("Trying to use special BlockUI texture as cursor? Things may not work well: " + rl.toString());
            }

            texture = new CursorTexture(rl);
            Minecraft.getInstance().getTextureManager().register(rl, texture);
        }

        final CursorTexture cursorTexture = (CursorTexture) texture;
        cursorTexture.setCursor();
        return cursorTexture;
    }

    /**
     * Sets cursor image to standard shapes provided by GLFW.
     * 
     * @param shape cursor shape
     */
    public static void setStandardCursor(final StandardCursor shape)
    {
        if (shape == StandardCursor.DEFAULT)
        {
            resetCursor();
            return;
        }

        final int ord = shape.ordinal();

        if (STANDARD_CURSORS[ord] == 0)
        {
            RenderSystem.assertOnRenderThread();
            STANDARD_CURSORS[ord] = GLFW.glfwCreateStandardCursor(shape.glfwValue);
            if (STANDARD_CURSORS[ord] == 0)
            {
                LOGGER.error("Cannot create standard cursor for shape: " + shape);
                return;
            }
        }

        setCursorAddress(STANDARD_CURSORS[ord]);
    }

    /**
     * Sets cursor image to default (usually arrow).
     */
    public static void resetCursor()
    {
        setCursorAddress(0);
    }

    /**
     * Sets cursor image address. If null (zero), cursor is reset to default (usually arrow).
     * 
     * @param cursorAddress cursor handle address or null
     */
    public static void setCursorAddress(final long cursorAddress)
    {
        RenderSystem.assertOnRenderThread();
        if (cursorAddress != lastCursorAddress)
        {
            GLFW.glfwSetCursor(Minecraft.getInstance().getWindow().getWindow(), cursorAddress);
            lastCursorAddress = cursorAddress;
        }
    }

    /**
     * @param  testedAddress param of tested cursor handle
     * @return               true if given address is equal to current cursor handle address
     */
    public static boolean isCurrentCursor(final long testedAddress)
    {
        return testedAddress == lastCursorAddress;
    }

    /**
     * Enum represing all possible cursors defined by GLFW
     */
    public enum StandardCursor
    {
        DEFAULT(Integer.MIN_VALUE),
        ARROW(GLFW.GLFW_ARROW_CURSOR),
        TEXT_CURSOR(GLFW.GLFW_IBEAM_CURSOR),
        CROSSHAIR(GLFW.GLFW_CROSSHAIR_CURSOR),
        HAND(GLFW.GLFW_POINTING_HAND_CURSOR),
        HORIZONTAL_RESIZE(GLFW.GLFW_RESIZE_EW_CURSOR),
        VERTICAL_RESIZE(GLFW.GLFW_RESIZE_NS_CURSOR),
        RESIZE(GLFW.GLFW_RESIZE_ALL_CURSOR),

        /** unsafe */
        RESIZE2(GLFW.GLFW_RESIZE_NWSE_CURSOR),
        /** unsafe */
        RESIZE3(GLFW.GLFW_RESIZE_NESW_CURSOR);

        private final int glfwValue;

        private StandardCursor(final int glfwValue)
        {
            this.glfwValue = glfwValue;
        }
    }
}
