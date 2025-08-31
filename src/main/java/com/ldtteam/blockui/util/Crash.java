package com.ldtteam.blockui.util;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraftforge.fml.loading.FMLEnvironment;
import java.util.concurrent.TimeUnit;

public class Crash
{
    private Crash()
    {
        // util class
    }

    public static IllegalArgumentException argument(final String message)
    {
        return crash(new IllegalArgumentException(message));
    }

    public static IllegalStateException state(final String message)
    {
        return crash(new IllegalStateException(message));
    }

    public static void argumentIfDev(final String message)
    {
        crashIfDev(new IllegalArgumentException(message));
    }

    public static void crashIfDev(final RuntimeException t)
    {
        if (!FMLEnvironment.production)
        {
            crash(t);
        }
    }

    public static <T extends Throwable> T crash(final T t)
    {
        if (!SharedConstants.IS_RUNNING_IN_IDE)
        {
            if (!FMLEnvironment.production)
            {
                SharedConstants.IS_RUNNING_IN_IDE = true;
                Util.setPause(msg -> {
                    System.setProperty("java.awt.headless", "false");
                    final var thread = new Thread(() -> SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, """
                    <html>BlockUI was about to crash, you have 60 seconds before the actual crash.
                    You should put breakpoint into vanillas Util#doPause(String).
                    (note: this can only happen in non-production environment)
                    Reason:
                    """ + msg)));
                    thread.setContextClassLoader(ClassLoader.getSystemClassLoader());
                    thread.start();
                    sleep(TimeUnit.MINUTES.toMillis(1));
                });
            }
        }
        return (T) Util.pauseInIde(t);
    }

    private static void sleep(long millis)
    {
        long remainingTime = millis;
        long endTime = System.currentTimeMillis() + remainingTime;
        while (remainingTime > 0)
        {
            try
            {
                Thread.sleep(remainingTime);
                return;
            }
            catch (InterruptedException e)
            {
                remainingTime = endTime - System.currentTimeMillis();
            }
        }
    }

    public static class CheckArgument
    {
        private CheckArgument()
        {
            // util class
        }

        public static void equals(final int what, final int value, final String message)
        {
            if (what != value)
            {
                throw crash(new IllegalArgumentException(message));
            }
        }

        public static void notEquals(final int what, final int value, final String message)
        {
            if (what == value)
            {
                throw crash(new IllegalArgumentException(message));
            }
        }

        public static <T> T notNull(final T what, final String message)
        {
            if (what == null)
            {
                throw crash(new IllegalArgumentException(message));
            }
            return what;
        }

        public static int inRange(final int what, final int minIncl, final int maxIncl, final String message)
        {
            if (what < minIncl || maxIncl < what)
            {
                throw crash(new IllegalArgumentException((message + " = %d not in [%d, %d]").formatted(what, minIncl, maxIncl)));
            }
            return what;
        }

        public static float inRange(final float what, final float minIncl, final float maxIncl, final String message)
        {
            if (what < minIncl || maxIncl < what)
            {
                throw crash(new IllegalArgumentException((message + " = %f not in [%f, %f]").formatted(what, minIncl, maxIncl)));
            }
            return what;
        }
    }
}
