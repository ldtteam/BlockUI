package com.ldtteam.blockui.util.resloc;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class OutOfJarResourceLocation extends ResourceLocation
{
    private final Path nioPath;

    protected OutOfJarResourceLocation(final String namespace, final Path path, final String pathString)
    {
        super(new String[] {namespace, pathString});
        this.nioPath = path;
    }

    public static OutOfJarResourceLocation of(final String namespace, final Path path)
    {
        final Path fullPath = path.toAbsolutePath().normalize();
        return new OutOfJarResourceLocation(namespace,
            fullPath,
            fullPath.toString().toLowerCase().replace('\\', '/').replace(':', '_'));
    }

    @SuppressWarnings("resource")
    public OutOfJarResourceLocation ofMinecraftFolder(final String namespace, final String... parts)
    {
        Path path = Minecraft.getInstance().gameDirectory.toPath().resolve(namespace);
        for (int i = 0; i < parts.length; i++)
        {
            path.resolve(parts[i]);
        }
        return of(namespace, path);
    }

    public Path getNioPath()
    {
        return nioPath;
    }

    public static InputStream openStream(final ResourceLocation resLoc, final ResourceManager fallbackManager) throws IOException
    {
        if (resLoc instanceof OutOfJarResourceLocation nioResLoc)
        {
            return Files.newInputStream(nioResLoc.nioPath);
        }
        return fallbackManager.getResource(resLoc).getInputStream();
    }

    @Override
    public int compareNamespaced(ResourceLocation o)
    {
        if (o instanceof OutOfJarResourceLocation nioResLoc)
        {
            int ret = this.namespace.compareTo(nioResLoc.namespace);
            return ret != 0 ? ret : this.nioPath.compareTo(nioResLoc.nioPath);
        }
        return super.compareNamespaced(o);
    }

    @Override
    public int compareTo(ResourceLocation o)
    {
        if (o instanceof OutOfJarResourceLocation nioResLoc)
        {
            int ret = this.nioPath.compareTo(nioResLoc.nioPath);
            return ret != 0 ? ret : this.namespace.compareTo(nioResLoc.namespace);
        }
        return super.compareTo(o);
    }

    @Override
    public int hashCode()
    {
        return 31 * this.namespace.hashCode() + this.nioPath.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof OutOfJarResourceLocation nioResLoc)
        {
            return this.namespace.equals(nioResLoc.namespace) && this.nioPath.equals(nioResLoc.nioPath);
        }
        return false;
    }
}
