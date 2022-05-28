package com.ldtteam.common.sideless;

import com.ldtteam.common.sideless.Sideless.SidelessImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

public class ClientSideless implements SidelessImpl
{
    @Override
    public GameType getPlayerGameMode(final Player player)
    {
        return Minecraft.getInstance().getConnection().getPlayerInfo(player.getUUID()).getGameMode();
    }

    @Override
    public String getEarlyLoadingLanguageCode()
    {
        return Minecraft.getInstance() == null ? LanguageManager.DEFAULT_LANGUAGE_CODE : Minecraft.getInstance().options.languageCode;
    }
}
