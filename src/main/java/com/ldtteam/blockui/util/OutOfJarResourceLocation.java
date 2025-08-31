package com.ldtteam.blockui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Fake resource location only useable in BlockUI. Used for loading textures outside jar, beware of security issues - the path should
 * be valid and absolute, ie. poiting to a resource file you at least somehow trust.
 */
public class OutOfJarResourceLocation extends ResourceLocation
{
    private final Path nioPath;

    protected OutOfJarResourceLocation(final String namespace, final Path path, final String pathString)
    {
        super(namespace, pathString);
        this.nioPath = path;
    }

    /**
     * @param  namespace dummy namespace (unused in logic), ideally modid
     * @param  path      existing path pointing to valid (potentially trustworthy) resource
     * @return           ResLoc(name, path.toString()) extended with given path
     */
    public static OutOfJarResourceLocation of(final String namespace, final Path path)
    {
        final Path fullPath = path.toAbsolutePath().normalize();
        return new OutOfJarResourceLocation(namespace,
            fullPath,
            fullPath.toString().toLowerCase().replace('\\', '/').replaceAll("[^a-z0-9/._-]", "_"));
    }

    /**
     * Builds path in Minecraft game directory = game dir / namespace / parts
     * 
     * @param  namespace folder name in game dir, ideally mod id
     * @param  parts     parts of path pointing to valid (potentially trustworthy) resource
     * @return           ResLoc(name, path.toString()) extended with path from given parts
     */
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
     * Used for resolving both normal and out-of-jar res loc to input stream
     */
    public static InputStream openStream(final ResourceLocation resLoc, final ResourceManager fallbackManager) throws IOException
    {
        if (resLoc instanceof final OutOfJarResourceLocation nioResLoc)
        {
            return Files.newInputStream(nioResLoc.nioPath);
        }
        return fallbackManager.open(resLoc);
    }

    /**
     * Used for resolving both normal and out-of-jar res loc to buffered reader
     */
    public static BufferedReader openReader(final ResourceLocation resLoc, final ResourceManager fallbackManager) throws IOException
    {
        if (resLoc instanceof final OutOfJarResourceLocation nioResLoc)
        {
            return Files.newBufferedReader(nioResLoc.nioPath);
        }
        return fallbackManager.openAsReader(resLoc);
    }
}
