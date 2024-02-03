package com.ldtteam.common.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client (sender) -> Server (receiver) message
 */
public abstract class AbstractServerPlayMessage extends AbstractUnsidedPlayMessage implements IServerboundDistributor
{
    @Override
    protected LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    protected void onExecute(final IPayloadContext context, final Player player)
    {
        if (!(player instanceof final ServerPlayer serverPlayer))
        {
            throw new RuntimeException("Server side message but player is not ServerPlayer? " + baseExceptionString(context, player));
        }
        onExecute(context, serverPlayer);
    }

    /**
     * Executes message action on main thread.
     *
     * @param context network context
     * @param player  server player which is receiving this packet
     */
    protected abstract void onExecute(IPayloadContext context, ServerPlayer player);
}
