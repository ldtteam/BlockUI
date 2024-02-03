package com.ldtteam.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

/**
 * Root class of message hierarchy serving as bouncer to vanilla networking.
 * 
 * <pre>
 * public static void onNetworkRegistry(final RegisterPayloadHandlerEvent event)
 * {
 *     final String modVersion = ModList.get().getModContainerById(Constants.MOD_ID).get().getModInfo().getVersion().toString();
 *     final IPayloadRegistrar registry = event.registrar(Constants.MOD_ID).versioned(modVersion);
 * 
 *     // MyMessage extends one of AbstractPlayMessage, AbstractClientPlayMessage, AbstractServerPlayMessage
 *     registry.play(MyMessage.ID, MyMessage::new, MyMessage::handle);
 * }
 * </pre>
 */
abstract class AbstractUnsidedPlayMessage implements CustomPacketPayload
{
    public AbstractUnsidedPlayMessage()
    {}

    /**
     * In this method you deserialize received network payload. Formerly known as <code>#fromBytes(FriendlyByteBuf)</code>
     *
     * @param buf received network payload
     */
    public AbstractUnsidedPlayMessage(final FriendlyByteBuf buf)
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

    /**
     * Used by registry method.
     */
    public void handle(final IPayloadContext context)
    {
        final Player player = context.player().orElse(null);

        if (player == null || !context.protocol().isPlay() ||
            (getExecutionSide() != null && context.flow().getReceptionSide() != getExecutionSide()))
        {
            throw new RuntimeException(baseExceptionString(context, player));
        }

        context.workHandler().execute(() -> onExecute(context, player));
    }

    protected String baseExceptionString(final IPayloadContext context, final Player player)
    {
        return "Invalid packet received for - " + this.getClass().getName() +
            " player: " +
            (player == null ? "MISSING" : player.getGameProfile().getName()) +
            " protocol: " +
            context.protocol() +
            " logical-side: " +
            context.flow().getReceptionSide();
    }

    /**
     * Executes message action on main thread.
     *
     * @param context network context
     * @param player  client/server player which is receiving this packet
     */
    protected abstract void onExecute(final IPayloadContext context, final Player player);
}
