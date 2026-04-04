package com.ldtteam.blockui.util.texture;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.mod.BlockUI;
import com.ldtteam.blockui.util.resloc.OutOfJarResourceLocation;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.NativeImage.Format;
import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
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
 * @see Pane#setCursor(CursorType)
 */
public class CursorTexture extends ReloadableTexture
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CursorTexture.class);

    private CursorMetadataSection cursorMetadata = CursorMetadataSection.EMPTY;
    private long glfwCursorAddress = 0;

    public CursorTexture(final Identifier resLoc)
    {
        super(resLoc);
    }

    @Override
    public TextureContents loadContents(final ResourceManager resourceManager) throws IOException
    {
        final Resource resource = OutOfJarResourceLocation.getResourceHandle(resourceId(), resourceManager);
        final NativeImage nativeImage;
        try (var is = resource.open())
        {
            nativeImage = NativeImage.read(is);
        }
        if (nativeImage.format() != Format.RGBA)
        {
            LOGGER.error("Cannot load texture for cursor as it is not in RGBA format, resource location: " + resourceId());

            nativeImage.close();
            return TextureContents.createMissing();
        }

        this.cursorMetadata = resource.metadata().getSection(CursorMetadataSection.TYPE).orElse(CursorMetadataSection.EMPTY);

        return new TextureContents(nativeImage, resource.metadata().getSection(TextureMetadataSection.TYPE).orElse(null));
    }

    @Override
    public void apply(final TextureContents contents)
    {
        try (NativeImage nativeImage = contents.image())
        {
            RenderSystem.assertOnRenderThread();

            this.close();

            try (var stack = MemoryStack.stackPush())
            {
                final GLFWImage image = GLFWImage.malloc(stack);
                image.width(nativeImage.getWidth());
                image.height(nativeImage.getHeight());
                MemoryUtil.memPutAddress(image.address() + GLFWImage.PIXELS, nativeImage.getPointer());
                glfwCursorAddress = GLFW.glfwCreateCursor(image, cursorMetadata.hotspotX, cursorMetadata.hotspotY);
            }

            if (glfwCursorAddress == 0)
            {
                LOGGER.error("Cannot create textured cursor for resource location: " + resourceId());
            }
        }
    }

    @Override
    protected void doLoad(final NativeImage image)
    {
        // Noop
    }

    @Override
    public void close()
    {
        if (glfwCursorAddress != 0)
        {
            RenderSystem.assertOnRenderThread();
            GLFW.glfwDestroyCursor(glfwCursorAddress);
            glfwCursorAddress = 0;
        }
        super.close();
    }

    public long getGlfwCursorAddress()
    {
        return glfwCursorAddress;
    }

    public static record CursorMetadataSection(int hotspotX, int hotspotY)
    {
        public static final CursorMetadataSection EMPTY = new CursorMetadataSection(0, 0);

        public static final Codec<CursorMetadataSection> CODEC = RecordCodecBuilder.create(builder -> builder
            .group(Codec.INT.optionalFieldOf("hotspot.x", 0).forGetter(CursorMetadataSection::hotspotX),
                Codec.INT.optionalFieldOf("hotspot.y", 0).forGetter(CursorMetadataSection::hotspotY))
            .apply(builder, CursorMetadataSection::new));

        public static final MetadataSectionType<CursorMetadataSection> TYPE =
            new MetadataSectionType<>("ldtteam." + BlockUI.MOD_ID + ".cursor", CODEC);
    }
}
