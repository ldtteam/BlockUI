package com.ldtteam.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPlayPayloadHandler;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import net.neoforged.neoforge.network.registration.IDirectionAwarePayloadHandlerBuilder;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import org.jetbrains.annotations.Nullable;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Class to connect message type with proper sided registration.
 */
public record PlayMessageType<T extends AbstractUnsidedPlayMessage>(ResourceLocation id,
    BiFunction<FriendlyByteBuf, PlayMessageType<T>, T> messageFactory,
    @Nullable Consumer<IDirectionAwarePayloadHandlerBuilder<T, IPlayPayloadHandler<T>>> payloadHandler)
{
    /**
     * Creates type for Server (sender) -> Client (receiver) message
     */
    public static <T extends AbstractClientPlayMessage> PlayMessageType<T> forClient(final String modId,
        final String messageName,
        final BiFunction<FriendlyByteBuf, PlayMessageType<T>, T> messageFactory)
    {
        return new PlayMessageType<T>(new ResourceLocation(modId, messageName), messageFactory, handlers -> {
            handlers.client((payload, context) -> payload.onExecute(context, ensureClientPlayer(context, payload)));
        });
    }

    /**
     * Creates type for Client (sender) -> Server (receiver) message
     */
    public static <T extends AbstractServerPlayMessage> PlayMessageType<T> forServer(final String modId,
        final String messageName,
        final BiFunction<FriendlyByteBuf, PlayMessageType<T>, T> messageFactory)
    {
        return new PlayMessageType<T>(new ResourceLocation(modId, messageName), messageFactory, handlers -> {
            handlers.server((payload, context) -> payload.onExecute(context, ensureServerPlayer(context, payload)));
        });
    }

    /**
     * Creates type for bidirectional message
     */
    public static <T extends AbstractPlayMessage> PlayMessageType<T> forBothSides(final String modId,
        final String messageName,
        final BiFunction<FriendlyByteBuf, PlayMessageType<T>, T> messageFactory)
    {
        return new PlayMessageType<T>(new ResourceLocation(modId, messageName), messageFactory, handlers -> {
            handlers.client((payload, context) -> {
                payload.onClientExecute(context, ensureClientPlayer(context, payload));
            }).server((payload, context) -> {
                payload.onServerExecute(context, ensureServerPlayer(context, payload));
            });
        });
    }
    /**
     * Creates type for Server (sender) -> Client (receiver) message.
     * Allows null player argument
     */
    public static <T extends AbstractClientPlayMessage> PlayMessageType<T> forClientAllowNullPlayer(final String modId,
        final String messageName,
        final BiFunction<FriendlyByteBuf, PlayMessageType<T>, T> messageFactory)
    {
        return new PlayMessageType<T>(new ResourceLocation(modId, messageName), messageFactory, handlers -> {
            handlers.client((payload, context) -> payload.onExecute(context, context.player().orElse(null)));
        });
    }

    /**
     * Creates type for Client (sender) -> Server (receiver) message.
     * Allows null player argument
     */
    public static <T extends AbstractServerPlayMessage> PlayMessageType<T> forServerAllowNullPlayer(final String modId,
        final String messageName,
        final BiFunction<FriendlyByteBuf, PlayMessageType<T>, T> messageFactory)
    {
        return new PlayMessageType<T>(new ResourceLocation(modId, messageName), messageFactory, handlers -> {
            handlers.server((payload, context) -> payload.onExecute(context, getServerPlayer(context)));
        });
    }

    /**
     * Creates type for bidirectional message.
     * Allows null player argument
     */
    public static <T extends AbstractPlayMessage> PlayMessageType<T> forBothSidesAllowNullPlayer(final String modId,
        final String messageName,
        final BiFunction<FriendlyByteBuf, PlayMessageType<T>, T> messageFactory)
    {
        return new PlayMessageType<T>(new ResourceLocation(modId, messageName), messageFactory, handlers -> {
            handlers.client((payload, context) -> {
                payload.onClientExecute(context, context.player().orElse(null));
            }).server((payload, context) -> {
                payload.onServerExecute(context, getServerPlayer(context));
            });
        });
    }

    /**
     * Call this in following code:
     * 
     * <pre>
     * public static void onNetworkRegistry(final RegisterPayloadHandlerEvent event)
     * {
     *     final String modVersion = ModList.get().getModContainerById(Constants.MOD_ID).get().getModInfo().getVersion().toString();
     *     final IPayloadRegistrar registry = event.registrar(Constants.MOD_ID).versioned(modVersion);
     * 
     *     // MyMessage extends one of AbstractPlayMessage, AbstractClientPlayMessage, AbstractServerPlayMessage
     *     MyMessage.TYPE.register(registry);
     * }
     * </pre>
     * 
     * @param registry event network registry
     */
    public void register(final IPayloadRegistrar registry)
    {
        registry.play(id, buf -> messageFactory.apply(buf, this), payloadHandler);
    }

    private static Player ensureClientPlayer(final PlayPayloadContext context, final AbstractUnsidedPlayMessage payload)
    {
        return context.player().orElseThrow(() -> wrongPlayerException(context, null, payload));
    }

    private static ServerPlayer ensureServerPlayer(final PlayPayloadContext context, final AbstractUnsidedPlayMessage payload)
    {
        return context.player()
            .map(player -> player instanceof final ServerPlayer serverPlayer ? serverPlayer : null)
            .orElseThrow(() -> wrongPlayerException(context, context.player().orElse(null), payload));
    }

    private static ServerPlayer getServerPlayer(final PlayPayloadContext context)
    {
        return context.player().map(player -> player instanceof final ServerPlayer serverPlayer ? serverPlayer : null).orElse(null);
    }

    private static RuntimeException wrongPlayerException(final PlayPayloadContext context,
        final Player player,
        final AbstractUnsidedPlayMessage payload)
    {
        return new RuntimeException("Invalid packet received for - " + payload.getClass().getName() +
            " player: " +
            (player == null ? "MISSING" : player.getClass().getName()) +
            " logical-side: " +
            context.flow().getReceptionSide());
    }
}