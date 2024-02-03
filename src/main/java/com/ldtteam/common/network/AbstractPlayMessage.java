package com.ldtteam.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

/**
 * Bidirectional message
 */
public abstract class AbstractPlayMessage extends AbstractUnsidedPlayMessage implements
    IClientboundDistributor,
    IServerboundDistributor
{
    /**
     * @param type message type
     */
    public AbstractPlayMessage(final PlayMessageType<?> type)
    {
        super(type);
    }

    /**
     * In this constructor you deserialize received network payload. Formerly known as <code>#fromBytes(FriendlyByteBuf)</code>
     *
     * @param buf received network payload
     * @param type message type
     */
    public AbstractPlayMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(type);
    }

    /**
     * Executes message action on main thread.
     *
     * @param context network context
     * @param player  client player which is receiving this packet
     */
    protected abstract void onClientExecute(final PlayPayloadContext context, final Player player);

    /**
     * Executes message action on main thread.
     *
     * @param context network context
     * @param player  server player which is receiving this packet
     */
    protected abstract void onServerExecute(final PlayPayloadContext context, final ServerPlayer player);
}
