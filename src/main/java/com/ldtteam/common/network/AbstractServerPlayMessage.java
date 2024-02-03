package com.ldtteam.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

/**
 * Client (sender) -> Server (receiver) message
 */
public abstract class AbstractServerPlayMessage extends AbstractUnsidedPlayMessage implements IServerboundDistributor
{
    /**
     * @param type message type
     */
    public AbstractServerPlayMessage(final PlayMessageType<?> type)
    {
        super(type);
    }

    /**
     * In this constructor you deserialize received network payload. Formerly known as <code>#fromBytes(FriendlyByteBuf)</code>
     *
     * @param buf received network payload
     * @param type message type
     */
    public AbstractServerPlayMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(type);
    }

    /**
     * Executes message action on main thread.
     *
     * @param context network context
     * @param player  server player which is receiving this packet
     */
    protected abstract void onExecute(final PlayPayloadContext context, final ServerPlayer player);
}
