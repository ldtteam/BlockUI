package com.ldtteam.common.language;

import net.minecraft.client.Minecraft;

public class ClientLocale
{
    public static String getLocale()
    {
        // Trust me, Minecraft.getInstance() can be null, when you run Data Generators!
        return Minecraft.getInstance() == null ? null : Minecraft.getInstance().options.languageCode;
    }
}
