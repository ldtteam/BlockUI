package com.ldtteam.common.sideless;

import com.ldtteam.common.sideless.Sideless.SidelessImpl;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

public class ServerSideless implements SidelessImpl
{
    @Override
    public GameType getPlayerGameMode(final Player player)
    {
        if (player instanceof ServerPlayer serverPlayer)
        {
            return serverPlayer.gameMode.getGameModeForPlayer();
        }
        throw new IllegalArgumentException("player is not ServerPlayer? " + player.getClass());
    }

    @Override
    public String getEarlyLoadingLanguageCode()
    {
        return "en_us";
    }
}
