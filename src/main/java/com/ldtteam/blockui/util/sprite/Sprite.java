package com.ldtteam.blockui.util.sprite;

import com.google.common.collect.ImmutableList;
import com.ldtteam.blockui.util.records.SizeI;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import javax.annotation.Nullable;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.slf4j.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

/**
 * DO NOT PORT
 */
public class Sprite implements AutoCloseable
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ResourceLocation name;
    final int width;
    final int height;
    public NativeImage[] byMipLevel;
    @Nullable
    final Sprite.SpriteMapTexture animatedTexture;

    public Sprite(final ResourceLocation resLoc,
        final SizeI frameSize,
        final NativeImage originalImage,
        final AnimationMetadataSection animMetadata)
    {
        this.name = resLoc;
        this.width = frameSize.width();
        this.height = frameSize.height();
        this.animatedTexture = loadFrames(frameSize, originalImage.getWidth(), originalImage.getHeight(), animMetadata);
        this.byMipLevel = new NativeImage[] {originalImage};
    }

    @Nullable
    private Sprite.SpriteMapTexture loadFrames(final SizeI frameSize,
        final int texWidth,
        final int texHeight,
        final AnimationMetadataSection animMetadata)
    {
        final int columns = texWidth / frameSize.width();
        final int rows = texHeight / frameSize.height();
        final int frameCount = columns * rows;
        final List<Sprite.Frame> list = new ArrayList<>();
        animMetadata.forEachFrame((index, time) -> list.add(new Sprite.Frame(index, time)));
        if (list.isEmpty())
        {
            for (int l = 0; l < frameCount; ++l)
            {
                list.add(new Sprite.Frame(l, animMetadata.getDefaultFrameTime()));
            }
        }
        else
        {
            int i1 = 0;
            final IntSet indexSet = new IntOpenHashSet();

            for (final Iterator<Sprite.Frame> iterator = list.iterator(); iterator.hasNext(); ++i1)
            {
                final Sprite.Frame frame = iterator.next();
                boolean flag = true;
                if (frame.time <= 0)
                {
                    LOGGER.warn("Invalid frame duration on sprite {} frame {}: {}", name, i1, frame.time);
                    flag = false;
                }

                if (frame.index < 0 || frame.index >= frameCount)
                {
                    LOGGER.warn("Invalid frame index on sprite {} frame {}: {}", name, i1, frame.index);
                    flag = false;
                }

                if (flag)
                {
                    indexSet.add(frame.index);
                }
                else
                {
                    iterator.remove();
                }
            }

            final int[] unusedFrames = IntStream.range(0, frameCount).filter((index) -> !indexSet.contains(index)).toArray();
            if (unusedFrames.length > 0)
            {
                LOGGER.warn("Unused frames in sprite {}: {}", name, Arrays.toString(unusedFrames));
            }
        }

        return list.size() <= 1 ? null :
            new Sprite.SpriteMapTexture(ImmutableList.copyOf(list), columns, animMetadata.isInterpolatedFrames());
    }

    void uploadImages(final int parentX, final int parentY, final int frameX, final int frameY, final NativeImage[] mipMappedImages)
    {
        for (int i = 0; i < byMipLevel.length; ++i)
        {
            // Forge: Skip uploading if the texture would be made invalid by mip level
            if ((width >> i) <= 0 || (height >> i) <= 0) break;
            mipMappedImages[i].upload(i,
                parentX >> i,
                parentY >> i,
                frameX >> i,
                frameY >> i,
                width >> i,
                height >> i,
                byMipLevel.length > 1,
                false);
        }
    }

    public int width()
    {
        return width;
    }

    public int height()
    {
        return height;
    }

    @Nullable
    public SpriteTicker createTicker()
    {
        return animatedTexture != null ? animatedTexture.createTicker() : null;
    }

    public void close()
    {
        for (final NativeImage nativeimage : byMipLevel)
        {
            nativeimage.close();
        }
    }

    class SpriteMapTexture
    {
        final List<Sprite.Frame> frames;
        private final int frameRowSize;
        private final boolean interpolateFrames;

        SpriteMapTexture(final List<Sprite.Frame> frames, final int frameRowSize, final boolean interpolateFrames)
        {
            this.frames = frames;
            this.frameRowSize = frameRowSize;
            this.interpolateFrames = interpolateFrames;
        }

        int getFrameX(final int frameIndex)
        {
            return frameIndex % frameRowSize;
        }

        int getFrameY(final int frameIndex)
        {
            return frameIndex / frameRowSize;
        }

        void uploadFrame(final int parentX, final int parentY, final int frameIndex)
        {
            final int frameX = getFrameX(frameIndex) * width;
            final int frameY = getFrameY(frameIndex) * height;
            uploadImages(parentX, parentY, frameX, frameY, byMipLevel);
        }

        public SpriteTicker createTicker()
        {
            return new Ticker(this, interpolateFrames ? new InterpolationEngine() : null);
        }
    }

    static class Frame
    {
        final int index;
        final int time;

        Frame(final int index, final int time)
        {
            this.index = index;
            this.time = time;
        }
    }

    final class InterpolationEngine implements AutoCloseable
    {
        private final NativeImage[] activeFrame = new NativeImage[byMipLevel.length];

        InterpolationEngine()
        {
            for (int i = 0; i < this.activeFrame.length; ++i)
            {
                final int j = width >> i;
                final int k = height >> i;
                this.activeFrame[i] = new NativeImage(Math.max(1, j), Math.max(1, k), false);
            }
        }

        void calcAndUploadInterpolatedFrame(final int parentX, final int parentY, final Sprite.Ticker ticker)
        {
            final Sprite.SpriteMapTexture animTex = ticker.animationInfo;
            final List<Sprite.Frame> frames = animTex.frames;
            final Sprite.Frame curFrame = frames.get(ticker.frame);
            final double frameProgress = 1.0D - (double) ticker.subFrame / (double) curFrame.time;
            final int curFrameIndex = curFrame.index;
            final int nextFrameIndex = (frames.get((ticker.frame + 1) % frames.size())).index;
            if (curFrameIndex != nextFrameIndex)
            {
                for (int mipMapLevel = 0; mipMapLevel < this.activeFrame.length; ++mipMapLevel)
                {
                    final int endWidth = width >> mipMapLevel;
                    final int endHeight = height >> mipMapLevel;
                    if (endWidth < 1 || endHeight < 1) continue;

                    for (int y = 0; y < endHeight; ++y)
                    {
                        for (int x = 0; x < endWidth; ++x)
                        {
                            final int startColor = getPixel(animTex, curFrameIndex, mipMapLevel, x, y);
                            final int endColor = getPixel(animTex, nextFrameIndex, mipMapLevel, x, y);
                            final int r = mix(frameProgress, startColor >> 16 & 255, endColor >> 16 & 255);
                            final int g = mix(frameProgress, startColor >> 8 & 255, endColor >> 8 & 255);
                            final int b = mix(frameProgress, startColor & 255, endColor & 255);
                            activeFrame[mipMapLevel].setPixelRGBA(x, y, startColor & -16777216 | r << 16 | g << 8 | b);
                        }
                    }
                }

                uploadImages(parentX, parentY, 0, 0, this.activeFrame);
            }
        }

        private int getPixel(final Sprite.SpriteMapTexture animTex,
            final int frameIndex,
            final int mipMapLevel,
            final int x,
            final int y)
        {
            return byMipLevel[mipMapLevel].getPixelRGBA(x + (animTex.getFrameX(frameIndex) * width >> mipMapLevel),
                y + (animTex.getFrameY(frameIndex) * height >> mipMapLevel));
        }

        private int mix(final double progress, final int min, final int max)
        {
            return (int) (progress * min + (1.0D - progress) * max);
        }

        public void close()
        {
            for (final NativeImage nativeimage : activeFrame)
            {
                nativeimage.close();
            }
        }
    }

    class Ticker implements SpriteTicker
    {
        int frame;
        int subFrame;
        final Sprite.SpriteMapTexture animationInfo;
        @Nullable
        private final Sprite.InterpolationEngine interpolationData;

        Ticker(final Sprite.SpriteMapTexture animationInfo, @Nullable final Sprite.InterpolationEngine interpolationData)
        {
            this.animationInfo = animationInfo;
            this.interpolationData = interpolationData;
        }

        public void tickAndUpload(final int parentX, final int parentY)
        {
            ++subFrame;
            final Sprite.Frame curFrame = animationInfo.frames.get(frame);
            if (subFrame >= curFrame.time)
            {
                final int i = curFrame.index;
                frame = (frame + 1) % animationInfo.frames.size();
                subFrame = 0;
                final int j = (animationInfo.frames.get(frame)).index;
                if (i != j)
                {
                    animationInfo.uploadFrame(parentX, parentY, j);
                }
            }
            else if (interpolationData != null)
            {
                if (!RenderSystem.isOnRenderThread())
                {
                    RenderSystem.recordRenderCall(() -> interpolationData.calcAndUploadInterpolatedFrame(parentX, parentY, this));
                }
                else
                {
                    interpolationData.calcAndUploadInterpolatedFrame(parentX, parentY, this);
                }
            }
        }

        public void close()
        {
            if (interpolationData != null)
            {
                interpolationData.close();
            }
        }
    }

    public interface SpriteTicker extends AutoCloseable
    {
        void tickAndUpload(int x, int y);

        void close();
    }

    @Nullable
    public static Sprite loadSprite(final ResourceLocation resLoc, final Resource resource)
    {
        AnimationMetadataSection animMetadata;
        try
        {
            animMetadata = resource.metadata().getSection(AnimationMetadataSection.SERIALIZER).orElse(AnimationMetadataSection.EMPTY);
        }
        catch (final Exception exception)
        {
            LOGGER.error("Unable to parse metadata from {}", resLoc, exception);
            return null;
        }

        NativeImage nativeimage;
        try (InputStream inputstream = resource.open())
        {
            nativeimage = NativeImage.read(inputstream);
        }
        catch (final IOException ioexception)
        {
            LOGGER.error("Using missing texture, unable to load {}", resLoc, ioexception);
            return null;
        }

        final SizeI framesize = SizeI.of(animMetadata.calculateFrameSize(nativeimage.getWidth(), nativeimage.getHeight()));
        if (isMultipleOf(nativeimage.getWidth(), framesize.width()) && isMultipleOf(nativeimage.getHeight(), framesize.height()))
        {
            return new Sprite(resLoc, framesize, nativeimage, animMetadata);
        }
        else
        {
            LOGGER.error("Image {} size {},{} is not multiple of frame size {},{}",
                resLoc,
                nativeimage.getWidth(),
                nativeimage.getHeight(),
                framesize.width(),
                framesize.height());
            nativeimage.close();
            return null;
        }
    }

    public static boolean isMultipleOf(final int value, final int multOf)
    {
        return value % multOf == 0;
    }
}
