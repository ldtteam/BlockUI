package com.ldtteam.common;

import com.ldtteam.common.language.LanguageMapBouncer;
import com.ldtteam.common.sideless.ClientSideless;
import com.ldtteam.common.sideless.ServerSideless;
import com.ldtteam.common.sideless.Sideless;
import net.minecraftforge.fml.DistExecutor;

public class CommonLdtTeamInit
{
    private static boolean wasInitialized = false;

    public static synchronized void setup(final String modId)
    {
        init();
        LanguageMapBouncer.load(modId);
    }

    private static void init()
    {
        if (wasInitialized)
        {
            return;
        }
        wasInitialized = true;

        Sideless.set(DistExecutor.safeRunForDist(() -> ClientSideless::new, () -> ServerSideless::new));
    }
}
