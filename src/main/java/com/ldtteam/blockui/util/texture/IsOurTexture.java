package com.ldtteam.blockui.util.texture;

import net.minecraft.client.renderer.texture.AbstractTexture;

public final class IsOurTexture
{
    public static final boolean isOur(final AbstractTexture texture)
    {
        return texture instanceof OutOfJarTexture || texture instanceof SpriteTexture;
    }
}
