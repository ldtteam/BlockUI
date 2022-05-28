package com.ldtteam.common.language;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ldtteam.blockui.mod.BlockUI;
import com.ldtteam.common.sideless.Sideless;
import net.minecraft.locale.Language;
import org.apache.commons.io.IOUtils;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LanguageMapBouncer
{
    private static Map<String, String> languageMap = new HashMap<>();

    public static void load(final String modId)
    {
        if (languageMap == null)
        {
            BlockUI.MOD_LOG.warn("Trying to load language after mod loading phase for modid: " + modId);
            return;
        }

        InputStream is = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream(String.format("assets/%s/lang/%s.json", modId, Sideless.getEarlyLoadingLanguageCode()));
        if (is == null) // impossible on server, super unlikely on client
        {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(String.format("assets/%s/lang/en_us.json", modId));
        }

        languageMap.putAll(new Gson().fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), new TypeToken<Map<String, String>>()
        {}.getType()));

        IOUtils.closeQuietly(is);
    }

    public static void unload()
    {
        languageMap = null;
    }

    public static String translateKey(final String key)
    {
        if (languageMap != null)
        {
            final String res = languageMap.get(key);
            if (res != null)
            {
                return res;
            }
        }
        return Language.getInstance().getOrDefault(key);
    }
}
