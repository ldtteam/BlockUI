package com.ldtteam.blockui.util;

import com.ldtteam.blockui.mod.Log;
import net.minecraft.util.Util;
import net.neoforged.fml.loading.FMLEnvironment;
import java.util.Objects;

/**
 * Utility class for throwing errors which is safe during production.
 */
public class SafeError
{
    /**
     * Safe error throw call that only throws an exception during development, but logs an error in production instead so no crashes to desktop may occur.
     *
     * @param exception the exception instance.
     */
    public static void throwInDev(final RuntimeException exception)
    {
        if (FMLEnvironment.isProduction())
        {
            Log.getLogger().error(exception.getMessage(), exception);
        }
        else
        {
            throw Util.pauseInIde(exception);
        }
    }

    /**
     * @param value        the object reference to check for nullity
     * @param errorMessage detail message to be used in the event that a {@code NullPointerException} is thrown
     * @see Objects#requireNonNull(Object, String)
     */
    public static void requireNonNull(final Object value, final String errorMessage)
    {
        if (value == null)
        {
            throwInDev(new NullPointerException(errorMessage));
        }
    }
}
