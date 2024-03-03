package com.ldtteam.common.network;

import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import java.util.function.BiFunction;

/**
 * Class to connect message type with proper sided registration.
 */
public record PlayMessageType<T extends AbstractUnsidedPlayMessage>(ResourceLocation id,
    BiFunction<FriendlyByteBuf, PlayMessageType<T>, T> messageFactory,
    boolean allowNullPlayer,
    @Nullable PayloadAction<T, Player> client,
    @Nullable PayloadAction<T, ServerPlayer> server)
{
    /**
     * Creates type for Server (sender) -> Client (receiver) message
     */
    public static <T extends AbstractClientPlayMessage> PlayMessageType<T> forClient(final String modId,
        final String messageName,
        final BiFunction<FriendlyByteBuf, PlayMessageType<T>, T> messageFactory)
    {
        return forClient(modId, messageName, messageFactory, false, false);
    }

    /**
     * Creates type for Client (sender) -> Server (receiver) message
     */
    public static <T extends AbstractServerPlayMessage> PlayMessageType<T> forServer(final String modId,
        final String messageName,
        final BiFunction<FriendlyByteBuf, PlayMessageType<T>, T> messageFactory)
    {
        return forServer(modId, messageName, messageFactory, false, false);
    }

    /**
     * Creates type for bidirectional message
     */
    public static <T extends AbstractPlayMessage> PlayMessageType<T> forBothSides(final String modId,
        final String messageName,
        final BiFunction<FriendlyByteBuf, PlayMessageType<T>, T> messageFactory)
    {
        return forBothSides(modId, messageName, messageFactory, false, false);
    }

    /**
     * Creates type for Server (sender) -> Client (receiver) message
     *
     * @param playerNullable         if false then message wont execute without player
     * @param executeOnNetworkThread if true will execute on logical side main thread
     */
    public static <T extends AbstractClientPlayMessage> PlayMessageType<T> forClient(final String modId,
        final String messageName,
        final BiFunction<FriendlyByteBuf, PlayMessageType<T>, T> messageFactory,
        final boolean playerNullable,
        final boolean executeOnNetworkThread)
    {
        return new PlayMessageType<T>(new ResourceLocation(modId, messageName),
            messageFactory,
            playerNullable,
            threadRedirect(AbstractClientPlayMessage::onExecute, executeOnNetworkThread),
            null);
    }

    /**
     * Creates type for Client (sender) -> Server (receiver) message
     *
     * @param playerNullable         if false then message wont execute without player
     * @param executeOnNetworkThread if true will execute on logical side main thread
     */
    public static <T extends AbstractServerPlayMessage> PlayMessageType<T> forServer(final String modId,
        final String messageName,
        final BiFunction<FriendlyByteBuf, PlayMessageType<T>, T> messageFactory,
        final boolean playerNullable,
        final boolean executeOnNetworkThread)
    {
        return new PlayMessageType<T>(new ResourceLocation(modId, messageName),
            messageFactory,
            playerNullable,
            null,
            threadRedirect(AbstractServerPlayMessage::onExecute, executeOnNetworkThread));
    }

    /**
     * Creates type for bidirectional message
     *
     * @param playerNullable         if false then message wont execute without player
     * @param executeOnNetworkThread if true will execute on logical side main thread
     */
    public static <T extends AbstractPlayMessage> PlayMessageType<T> forBothSides(final String modId,
        final String messageName,
        final BiFunction<FriendlyByteBuf, PlayMessageType<T>, T> messageFactory,
        final boolean playerNullable,
        final boolean executeOnNetworkThread)
    {
        return new PlayMessageType<T>(new ResourceLocation(modId, messageName),
            messageFactory,
            playerNullable,
            threadRedirect(AbstractPlayMessage::onClientExecute, executeOnNetworkThread),
            threadRedirect(AbstractPlayMessage::onServerExecute, executeOnNetworkThread));
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
        registry.play(id, buf -> messageFactory.apply(buf, this), handlers -> {
            // intentionally don't call the registry method if we do not have the handler
            if (client != null)
            {
                handlers.client(this::onClient);
            }
            if (server != null)
            {
                handlers.server(this::onServer);
            }
        });
    }

    private void onClient(final T payload, final PlayPayloadContext context)
    {
        final Player player = context.player().orElse(null);
        if (!allowNullPlayer && player == null)
        {
            wrongPlayerException(context, payload);
            return;
        }
        client.handle(payload, context, player);
    }

    private void onServer(final T payload, final PlayPayloadContext context)
    {
        final ServerPlayer serverPlayer = context.player().orElse(null) instanceof final ServerPlayer sp ? sp : null;
        if ((!allowNullPlayer && serverPlayer == null))
        {
            wrongPlayerException(context, payload);
            return;
        }
        server.handle(payload, context, serverPlayer);
    }

    private static <T extends AbstractUnsidedPlayMessage, U extends Player> PayloadAction<T, U> threadRedirect(final PayloadAction<T, U> payloadAction, final boolean executeOnNetworkThread)
    {
        return executeOnNetworkThread ? payloadAction : (payload, context, player) -> context.workHandler().execute(() -> payloadAction.handle(payload, context, player));
    }

    private static final Logger LOGGER = LogUtils.getLogger();

    private static void wrongPlayerException(final PlayPayloadContext context, final AbstractUnsidedPlayMessage payload)
    {
        final Player player = context.player().orElse(null);
        LOGGER.warn("Invalid packet received for - " + payload.getClass().getName() +
            " player: " +
            (player == null ? "MISSING" : player.getClass().getName()) +
            " logical-side: " +
            context.flow().getReceptionSide());
    }

    @FunctionalInterface
    private interface PayloadAction<T, U>
    {
        void handle(T payload, PlayPayloadContext context, U player);
    }
}
