package com.ldtteam.common.sideless;

import net.minecraft.client.Options;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

public class Sideless
{
    private static SidelessImpl impl = null;

    public static void set(final SidelessImpl sideless)
    {
        impl = sideless;
    }

    /**
     * Vanilla has few useful values but if you want to know exact gamemode u're doomed.
     * 
     * @param  player player to read gamemode from
     * @return        gamemode
     * @see           Player#isCreative() Player.isCreative() == GameType.CREATIVE
     * @see           Player#isSpectator() Player.isSpectator() == GameType.SPECTATOR
     * @see           Player#getAbilities()
     */
    public static GameType getPlayerGameMode(final Player player)
    {
        return impl.getPlayerGameMode(player);
    }

    /**
     * If you want to know client language code before resource pack init.
     * 
     * @return "en_us" on server, otherwise client code
     * @see    Options#languageCode
     */
    public static String getEarlyLoadingLanguageCode()
    {
        return impl.getEarlyLoadingLanguageCode();
    }

    public static interface SidelessImpl
    {
        GameType getPlayerGameMode(Player player);

        String getEarlyLoadingLanguageCode();
    }
}
