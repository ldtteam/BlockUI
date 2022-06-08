package com.ldtteam.common;

import com.ldtteam.common.language.LanguageMapBouncer;
import com.ldtteam.common.sideless.ClientSideless;
import com.ldtteam.common.sideless.ServerSideless;
import com.ldtteam.common.sideless.Sideless;
import net.minecraftforge.fml.DistExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommonLdtTeamInit
{
    public static final Logger COMMON_LOG = LogManager.getLogger("ldt-common");

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
