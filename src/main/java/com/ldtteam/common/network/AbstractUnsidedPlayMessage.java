package com.ldtteam.common.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;

/**
 * Bidirectional message
 */
public abstract class AbstractUnsidedPlayMessage extends AbstractPlayMessage<Player> implements
    IClientboundDistributor,
    IServerboundDistributor
{
    @Override
    protected LogicalSide getExecutionSide()
    {
        return null;
    }

    @Override
    protected void onExecute(final Player player, final boolean isLogicalServer)
    {
        if (isLogicalServer)
        {
            onServerExecute((ServerPlayer) player);
        }
        else 
        {
            onClientExecute(player);
        }
    }

    protected abstract void onClientExecute(Player player);

    protected abstract void onServerExecute(ServerPlayer player);
}
