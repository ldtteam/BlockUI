package com.ldtteam.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

/**
 * Root class of message hierarchy serving as bouncer to vanilla networking
 */
public abstract class AbstractPlayMessage<T extends Player> implements CustomPacketPayload
{
    public AbstractPlayMessage()
    {}

    /**
     * In this method you deserialize received network payload. Formerly known as <code>#fromBytes(FriendlyByteBuf)</code>
     *
     * @param buf received network payload
     */
    public AbstractPlayMessage(final FriendlyByteBuf buf)
    {}

    // Bouncer to reduce porting
    @Override
    public void write(final FriendlyByteBuf buf)
    {
        toBytes(buf);
    }

    /**
     * Writes message data to buffer.
     *
     * @param buf fresh network payload
     */
    protected abstract void toBytes(final FriendlyByteBuf buf);

    /**
     * Which sides is message able to be executed at.
     *
     * @return CLIENT or SERVER or null (for both)
     */
    @Nullable
    protected abstract LogicalSide getExecutionSide();

    public void handle(final IPayloadContext context)
    {
        final T player = castPlayer(context.player().orElse(null));

        if (player == null || !context.protocol().isPlay() || (getExecutionSide() != null && context.flow().getReceptionSide() != getExecutionSide()))
        {
            throw new RuntimeException("Invalid packet received for - " + this.getClass().getName() +
                " player: " +
                (player == null ?
                    (context.player().isEmpty() ? "MISSING" : "WRONG CLASS (" + context.player().get().getClass().getName() + ")") :
                    player.getGameProfile().getName()) +
                " protocol: " +
                context.protocol() +
                " logical-side: " +
                context.flow().getReceptionSide());
        }

        final boolean isLogicalServer = context.flow().getReceptionSide() == LogicalSide.SERVER;
        context.workHandler().execute(() -> onExecute(player, isLogicalServer));
    }

    @SuppressWarnings("unchecked")
    private T castPlayer(final Player player)
    {
        try
        {
            return (T) player;
        }
        catch (final ClassCastException e)
        {
            return null;
        }
    }

    /**
     * Executes message action on main thread.
     *
     * @param player          client/server player which is receiving this packet
     * @param isLogicalServer whether message arrived at logical server side
     */
    protected abstract void onExecute(final T player, final boolean isLogicalServer);
}
