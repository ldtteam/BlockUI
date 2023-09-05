package com.ldtteam.blockui.util.texture;

import com.google.gson.JsonObject;
import com.ldtteam.blockui.mod.BlockUI;
import com.ldtteam.blockui.util.cursor.CursorUtils;
import com.ldtteam.blockui.util.resloc.OutOfJarResourceLocation;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.NativeImage.Format;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

/**
 * Used for textured cursors.
 *
 * @see MouseCursorImage#setCursorImage(ResourceLocation, int, int)
 */
public class CursorTexture extends AbstractTexture
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CursorTexture.class);
    private final ResourceLocation resourceLocation;

    private int hotspotX = 0;
    private int hotspotY = 0;
    private long glfwCursorAddress = 0;
    @Nullable
    private NativeImage nativeImage = null;

    public CursorTexture(final ResourceLocation resLoc)
    {
        this.resourceLocation = resLoc;
    }

    /**
     * Sets cursor hotspot. Hotspot is position in the image which should be used as 0,0 when rendering the cursor (eg. image with
     * 24x24 resolution will be centered on mouse point with hotspot 12x12).
     * 
     * @param x hotspot left offset
     * @param y hotspot top offset
     */
    public void setHotspot(final int x, final int y)
    {
        if (hotspotX != x || hotspotY != y)
        {
            hotspotX = x;
            hotspotY = y;
            onDataChange();
        }
    }

    /**
     * @return true if this is current cursor image, false otherwise
     */
    public boolean isCursorNow()
    {
        return CursorUtils.isCurrentCursor(glfwCursorAddress) && glfwCursorAddress != 0;
    }

    private void onDataChange()
    {
        if (!RenderSystem.isOnRenderThread())
        {
            RenderSystem.recordRenderCall(this::onDataChange);
            return;
        }

        if (isCursorNow())
        {
            destroyCursorHandle();
            setCursor();
        }
        else
        {
            destroyCursorHandle();
        }
    }

    private void destroyCursorHandle()
    {
        if (glfwCursorAddress != 0)
        {
            RenderSystem.assertOnRenderThread();
            if (isCursorNow())
            {
                CursorUtils.resetCursor();
            }

            GLFW.glfwDestroyCursor(glfwCursorAddress);
            glfwCursorAddress = 0;
        }
    }

    /**
     * Sets this texture as cursor image. Resets to default if anything went wrong during setup of this texture.
     */
    public void setCursor()
    {
        if (glfwCursorAddress == 0 && nativeImage != null)
        {
            RenderSystem.assertOnRenderThread();
            try (var stack = MemoryStack.stackPush())
            {
                final GLFWImage image = GLFWImage.malloc(stack);
                image.width(nativeImage.getWidth());
                image.height(nativeImage.getHeight());
                MemoryUtil.memPutAddress(image.address() + GLFWImage.PIXELS, nativeImage.pixels);
                glfwCursorAddress = GLFW.glfwCreateCursor(image, hotspotX, hotspotY);
            }

            if (glfwCursorAddress == 0)
            {
                LOGGER.error("Cannot create textured cursor for resource location: " + resourceLocation);
            }
        }

        if (glfwCursorAddress != 0)
        {
            CursorUtils.setCursorAddress(glfwCursorAddress);
        }
        else
        {
            CursorUtils.resetCursor();
        }
    }

    @Override
    public void load(final ResourceManager resourceManager) throws IOException
    {
        if (nativeImage != null)
        {
            close();
        }

        final Resource resource = OutOfJarResourceLocation.getResourceHandle(resourceLocation, resourceManager);
        try (var is = resource.open())
        {
            nativeImage = NativeImage.read(is);
        }
        if (nativeImage.format() != Format.RGBA)
        {
            LOGGER.error("Cannot load texture for cursor as it is not in RGBA format, resource location: " + resourceLocation);
            close();
        }

        resource.metadata().getSection(CursorMetadataSection.SERIALIZER).ifPresent(metadata -> {
            // manual set to avoid double onDataChange call
            this.hotspotX = metadata.hotspotX;
            this.hotspotY = metadata.hotspotY;
        });

        onDataChange();
    }

    @Override
    public void close()
    {
        destroyCursorHandle();
        if (nativeImage != null)
        {
            nativeImage.close();
            nativeImage = null;
        }
    }

    public static record CursorMetadataSection(int hotspotX, int hotspotY)
    {
        public static final CursorMetadataSectionSerializer SERIALIZER = new CursorMetadataSectionSerializer();
    }

    private static class CursorMetadataSectionSerializer implements MetadataSectionSerializer<CursorMetadataSection>
    {
        @Override
        public String getMetadataSectionName()
        {
            return "ldtteam." + BlockUI.MOD_ID + ".cursor";
        }

        @Override
        public CursorMetadataSection fromJson(final JsonObject jsonObject)
        {
            return new CursorMetadataSection(GsonHelper.getAsInt(jsonObject, "hotspot.x", 0), GsonHelper.getAsInt(jsonObject, "hotspot.y", 0));
        }
    }
}
