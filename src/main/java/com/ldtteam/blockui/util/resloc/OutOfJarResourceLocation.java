package com.ldtteam.blockui.util.resloc;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.Resource.IoSupplier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import org.jetbrains.annotations.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class OutOfJarResourceLocation extends ResourceLocation
{
    private final Path nioPath;

    protected OutOfJarResourceLocation(final String namespace, final Path path, @Nullable final String pathString)
    {
        super(namespace,
            pathString != null ? pathString : path.toString().toLowerCase().replace('\\', '/').replaceAll("[^a-z0-9/._-]", "_"));
        this.nioPath = path;
    }

    public static OutOfJarResourceLocation of(final String namespace, final Path path)
    {
        return new OutOfJarResourceLocation(namespace, path, null);
    }

    @SuppressWarnings("resource")
    public static OutOfJarResourceLocation ofMinecraftFolder(final String namespace, final String... parts)
    {
        Path path = Minecraft.getInstance().gameDirectory.toPath().resolve(namespace);
        for (final String part : parts)
        {
            path = path.resolve(part);
        }
        return of(namespace, path);
    }

    public Path getNioPath()
    {
        return nioPath;
    }

    public static boolean fileExists(final ResourceLocation resLoc, final ResourceManager fallbackManager)
    {
        if (resLoc instanceof final OutOfJarResourceLocation nioResLoc)
        {
            return Files.exists(nioResLoc.nioPath);
        }
        return fallbackManager.getResource(resLoc).isPresent();
    }

    public static Resource getResourceHandle(final ResourceLocation resLoc, final ResourceManager fallbackManager)
    {
        if (resLoc instanceof final OutOfJarResourceLocation nioResLoc)
        {
            return fileExists(nioResLoc.withFileSuffix(".mcmeta"), fallbackManager) ?
                new OutOfJarResource(nioResLoc, parseMetadata(nioResLoc)) :
                new OutOfJarResource(nioResLoc);
        }
        return fallbackManager.getResource(resLoc).orElseThrow(() -> new RuntimeException("File not found: " + resLoc));
    }

    private static IoSupplier<ResourceMetadata> parseMetadata(final OutOfJarResourceLocation path)
    {
        return () -> {
            return ResourceMetadata.EMPTY;
        };
    }

    public static InputStream openStream(final ResourceLocation resLoc, final ResourceManager fallbackManager) throws IOException
    {
        if (resLoc instanceof final OutOfJarResourceLocation nioResLoc)
        {
            return Files.newInputStream(nioResLoc.nioPath);
        }
        return fallbackManager.open(resLoc);
    }

    public static BufferedReader openReader(final ResourceLocation resLoc, final ResourceManager fallbackManager) throws IOException
    {
        if (resLoc instanceof final OutOfJarResourceLocation nioResLoc)
        {
            return Files.newBufferedReader(nioResLoc.nioPath);
        }
        return fallbackManager.openAsReader(resLoc);
    }

    @Override
    public int compareNamespaced(final ResourceLocation o)
    {
        if (o instanceof final OutOfJarResourceLocation nioResLoc)
        {
            final int ret = this.getNamespace().compareTo(nioResLoc.getNamespace());
            return ret != 0 ? ret : this.nioPath.compareTo(nioResLoc.nioPath);
        }
        return super.compareNamespaced(o);
    }

    @Override
    public int compareTo(final ResourceLocation o)
    {
        if (o instanceof final OutOfJarResourceLocation nioResLoc)
        {
            final int ret = this.nioPath.compareTo(nioResLoc.nioPath);
            return ret != 0 ? ret : this.getNamespace().compareTo(nioResLoc.getNamespace());
        }
        return super.compareTo(o);
    }

    @Override
    public int hashCode()
    {
        return 31 * this.getNamespace().hashCode() + this.nioPath.hashCode();
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof final OutOfJarResourceLocation nioResLoc)
        {
            return this.getNamespace().equals(nioResLoc.getNamespace()) && this.nioPath.equals(nioResLoc.nioPath);
        }
        return false;
    }

    /**
     * @param prefix this will get prepended to filename
     */
    public OutOfJarResourceLocation withFilePrefix(final String prefix)
    {
        return of(getNamespace(), nioPath.resolveSibling(prefix + nioPath.getFileName().toString()));
    }

    /**
     * @param prefix this will get prepended to path using {@link Path#resolve(String)}
     */
    public OutOfJarResourceLocation withPathPrefix(final String prefix)
    {
        return of(getNamespace(), Path.of(prefix).resolve(nioPath));
    }

    /**
     * @param suffix this will get appended to filename
     */
    public OutOfJarResourceLocation withFileSuffix(final String suffix)
    {
        return of(getNamespace(), nioPath.resolveSibling(nioPath.getFileName().toString() + suffix));
    }

    /**
     * @param suffix this will get appended to path using {@link Path#resolve(String)}
     */
    public OutOfJarResourceLocation withPathSuffix(final String suffix)
    {
        return of(getNamespace(), nioPath.resolve(suffix));
    }

    public static ResourceLocation withSuffix(final ResourceLocation resLoc, final String suffix)
    {
        if (resLoc instanceof final OutOfJarResourceLocation nioResLoc)
        {
            return nioResLoc.withFileSuffix(suffix);
        }
        return new ResourceLocation(resLoc.getNamespace(), resLoc.getPath() + suffix);
    }

    public static class OutOfJarResource extends Resource
    {
        private final OutOfJarResourceLocation resLoc;

        public OutOfJarResource(final OutOfJarResourceLocation resLoc, final IoSupplier<ResourceMetadata> metadataSupplier)
        {
            super(null, () -> Files.newInputStream(resLoc.getNioPath()), metadataSupplier);
            this.resLoc = resLoc;
        }

        public OutOfJarResource(final OutOfJarResourceLocation resLoc)
        {
            super(null, () -> Files.newInputStream(resLoc.getNioPath()));
            this.resLoc = resLoc;
        }

        @Override
        public String sourcePackId()
        {
            return "blockui out-of-jar resource: " + resLoc;
        }
    }
}
