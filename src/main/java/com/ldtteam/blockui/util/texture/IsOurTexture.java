package com.ldtteam.blockui.util.texture;

import net.minecraft.client.renderer.texture.AbstractTexture;

public final class IsOurTexture
{
    /**
     * Mostly to deny loops in loading (since we load during rendering)
     *
     * @return true if given texture is from BlockUI
     */
    public static final boolean isOur(final AbstractTexture texture)
    {
        return texture instanceof OutOfJarTexture || texture instanceof SpriteTexture || texture instanceof CursorTexture;
    }
}
