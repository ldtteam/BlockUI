package com.ldtteam.common.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Bidirectional message
 */
public abstract class AbstractPlayMessage extends AbstractUnsidedPlayMessage implements
    IClientboundDistributor,
    IServerboundDistributor
{
    @Override
    protected LogicalSide getExecutionSide()
    {
        return null;
    }

    @Override
    protected void onExecute(final IPayloadContext context, final Player player)
    {
        if (context.flow().getReceptionSide() == LogicalSide.SERVER)
        {
            if (!(player instanceof final ServerPlayer serverPlayer))
            {
                throw new RuntimeException("Server side message but player is not ServerPlayer? " + baseExceptionString(context, player));
            }
            onServerExecute(context, serverPlayer);
        }
        else
        {
            onClientExecute(context, player);
        }
    }

    /**
     * Executes message action on main thread.
     *
     * @param context network context
     * @param player  client player which is receiving this packet
     */
    protected abstract void onClientExecute(IPayloadContext context, Player player);

    /**
     * Executes message action on main thread.
     *
     * @param context network context
     * @param player  server player which is receiving this packet
     */
    protected abstract void onServerExecute(IPayloadContext context, ServerPlayer player);
}
