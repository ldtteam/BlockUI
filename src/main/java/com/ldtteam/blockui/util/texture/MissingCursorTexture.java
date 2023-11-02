package com.ldtteam.blockui.util.texture;

import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

/**
 * Backed by missing vanilla texture.
 */
public final class MissingCursorTexture extends CursorTexture
{
   public  static final MissingCursorTexture INSTANCE = new MissingCursorTexture();

    private MissingCursorTexture()
    {
        super(null);
        super.nativeImage = MissingTextureAtlasSprite.getTexture().getPixels();
    }

    @Override
    public void close()
    {
        // Noop
    }

    @Override
    protected void destroyCursorHandle()
    {
        // Noop
    }

    @Override
    public void load(final ResourceManager resourceManager) throws IOException
    {
        // Noop
    }
}
