package com.ldtteam.blockui.util.cursor;

import com.ldtteam.blockui.mod.BlockUI;
import com.ldtteam.blockui.util.texture.CursorTexture;
import com.ldtteam.blockui.util.texture.IsOurTexture;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface to wrap various cursors
 */
public class Cursor
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Cursor.class);

    /** Probably arrow, but OS dependend */
    public static final CursorType DEFAULT = CursorType.DEFAULT;
    public static final CursorType ARROW = CursorTypes.ARROW;
    public static final CursorType TEXT_CURSOR = CursorTypes.IBEAM;
    public static final CursorType CROSSHAIR = CursorTypes.CROSSHAIR;
    public static final CursorType HAND = CursorTypes.POINTING_HAND;
    public static final CursorType HORIZONTAL_RESIZE = CursorTypes.RESIZE_EW;
    public static final CursorType VERTICAL_RESIZE = CursorTypes.RESIZE_NS;
    public static final CursorType RESIZE_NWSE = new CursorType("resize_nwse", GLFW.GLFW_RESIZE_NWSE_CURSOR);
    public static final CursorType RESIZE_NESW = new CursorType("resize_nesw", GLFW.GLFW_RESIZE_NESW_CURSOR);
    public static final CursorType RESIZE = CursorTypes.RESIZE_ALL;
    public static final CursorType NOT_ALLOWED = CursorTypes.NOT_ALLOWED;

    public static CursorType of(final Identifier resLoc)
    {
        final TextureManager texManager = Minecraft.getInstance().getTextureManager();
        final AbstractTexture texture = texManager.getTexture(resLoc);
        if (!(texture instanceof CursorTexture))
        {
            if (IsOurTexture.isOur(texture))
            {
                LOGGER.warn("Trying to use special BlockUI texture as cursor? Things may not work well: " + resLoc.toString());
            }

            texManager.registerAndLoad(resLoc, new CursorTexture(resLoc));
        }

        return new TexturedCursorType(resLoc);
    }

    public static class TexturedCursorType extends CursorType
    {
        private final Identifier resLoc;

        protected TexturedCursorType(final Identifier resLoc)
        {
            super(BlockUI.MOD_ID + "-tex-cursor:" + resLoc.toString(), -1L);
            this.resLoc = resLoc;
        }

        @Override
        public void select(final Window window)
        {
            final AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(resLoc);

            if (!(texture instanceof final CursorTexture cursorTexture))
            {
                throw new IllegalArgumentException("Did you forget to load CursorTexture (or create CursorType) for: " + resLoc);
            }

            GLFW.glfwSetCursor(window.handle(), cursorTexture.getGlfwCursorAddress());
        }
    }
}
