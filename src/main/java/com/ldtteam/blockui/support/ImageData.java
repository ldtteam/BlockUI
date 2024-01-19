package com.ldtteam.blockui.support;

import com.ldtteam.blockui.controls.Image;
import com.ldtteam.blockui.util.records.SizeI;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Record to keep track of image data.
 *
 * @param image   the image resource.
 * @param offsetX the left offset.
 * @param offsetY the right offset.
 * @param width   the width.
 * @param height  the height.
 */
public record ImageData(ResourceLocation image, int offsetX, int offsetY, int width, int height)
{
    /**
     * The default missing image data.
     */
    public static final ImageData MISSING = new ImageData(MissingTextureAtlasSprite.getLocation(), 0, 0, 16, 16);

    /**
     * A map of resource locations and mapped dimensions.
     */
    private static final Map<ResourceLocation, SizeI> MAP_DIMENSIONS = new HashMap<>();

    /**
     * Get the width of the image if available, else get the mapped width.
     */
    public int getWidth()
    {
        return width == 0 ? mapWidth() : width;
    }

    /**
     * Get the height of the image if available, else get the mapped height.
     */
    public int getHeight()
    {
        return height == 0 ? mapHeight() : height;
    }

    /**
     * Get the original dimensions of the loaded image, only works if the image is valid.
     *
     * @return the original image width.
     */
    public int mapWidth()
    {
        if (this.equals(MISSING))
        {
            return 16;
        }
        MAP_DIMENSIONS.computeIfAbsent(image, Image::getImageDimensions);
        return MAP_DIMENSIONS.get(image).width();
    }

    /**
     * Get the original dimensions of the loaded image, only works if the image is valid.
     *
     * @return the original image height.
     */
    public int mapHeight()
    {
        if (this.equals(MISSING))
        {
            return 16;
        }
        MAP_DIMENSIONS.computeIfAbsent(image, Image::getImageDimensions);
        return MAP_DIMENSIONS.get(image).width();
    }
}
