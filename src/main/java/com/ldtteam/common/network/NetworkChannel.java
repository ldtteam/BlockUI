package com.ldtteam.common.network;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalCause;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.NetworkInstance;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor.PacketTarget;
import net.minecraftforge.network.simple.IndexedMessageCodec;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.commons.lang3.tuple.Pair;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import static com.ldtteam.common.CommonLdtTeamInit.COMMON_LOG;
import io.netty.buffer.Unpooled;

/**
 * Our wrapper for Forge network layer
 */
public class NetworkChannel extends SimpleChannel
{
    /**
     * Stolen from forge classes
     *
     * @see NetworkDirection
     */
    private static final int MAX_PACKET_SIZE = 1048576;

    /**
     * Stolen from super classes
     */
    private static final int INVALID_PAYLOAD_INDEX = Integer.MIN_VALUE;

    private static final UUID CLIENT_CACHE_KEY = EffectiveSide.get().isClient() ? UUID.randomUUID() : null;

    /**
     * Id of next registered message, also registered message count
     */
    private int id = 0;

    private final IndexedMessageCodec indexedCodec;
    private final NetworkInstance networkInstance;

    /**
     * Per socket address splitting cache
     */
    private final LoadingCache<UUID,
        Cache<Integer, ReceivingMessage>> receivingPartCache = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build(CacheLoader.from(() -> CacheBuilder.newBuilder()
                .concurrencyLevel(8)
                .initialCapacity(64)
                .expireAfterAccess(1, TimeUnit.MINUTES)
                .removalListener(notification -> {
                    if (notification.getCause() == RemovalCause.EXPIRED)
                    {
                        COMMON_LOG.error("Did not receive whole message in one minute?");
                    }
                })
                .build()));

    /**
     * Id of next splitting message
     */
    private final AtomicInteger nextSplittingId = new AtomicInteger();

    /**
     * @param modId unique channel name
     */
    public NetworkChannel(final String modId)
    {
        this(createInstance(modId, "main"));
    }

    /**
     * bouncer ctor
     */
    private NetworkChannel(final NetworkInstance networkInstance)
    {
        super(networkInstance);
        this.networkInstance = networkInstance;
        this.indexedCodec = getIndexedMessageCodec();

        registerMessage(SplittingPartMessage.class, SplittingPartMessage::new);
    }

    /**
     * Register a message into rawChannel.
     *
     * @param <MSG>    message class type
     * @param id       network id
     * @param msgClazz message class
     */
    public <MSG extends BaseAbstractMessage<MSG>> void registerMessage(final Class<MSG> msgClazz,
        final Function<FriendlyByteBuf, MSG> msgCtor)
    {
        if (id == (2 << 8) - 1)
        {
            throw new RuntimeException("exhausted network codec size");
        }

        registerMessage(id++, msgClazz, BaseAbstractMessage::toBytes, msgCtor, (msg, ctxIn) -> {
            final NetworkEvent.Context ctx = ctxIn.get();
            final LogicalSide packetOrigin = ctx.getDirection().getOriginationSide();

            ctx.setPacketHandled(true);
            if (msg.getExecutionSide() != null && packetOrigin.equals(msg.getExecutionSide()))
            {
                COMMON_LOG.warn("Receving {} at wrong side!", msg.getClass().getName());
                return;
            }

            // boolean param MUST equals true if packet arrived at logical server
            if (msg.runOnMainThread())
            {
                ctx.enqueueWork(() -> msg.onExecute(ctx, packetOrigin.equals(LogicalSide.CLIENT)));
            }
            else
            {
                msg.onExecute(ctx, packetOrigin.equals(LogicalSide.CLIENT));
            }
        });
    }

    private <MSG> void toBuffer(MSG msg, Consumer<Pair<FriendlyByteBuf, Integer>> bufferDispatcher)
    {
        final FriendlyByteBuf byteBuf = new FriendlyByteBuf(Unpooled.buffer());
        final int payloadIndex = encodeMessage(msg, byteBuf);

        if (payloadIndex != INVALID_PAYLOAD_INDEX || byteBuf.writerIndex() < MAX_PACKET_SIZE)
        {
            // dont split indexed payloads || small packets
            bufferDispatcher.accept(Pair.of(byteBuf, payloadIndex));
            return;
        }

        // whole data even with message discriminator
        final byte[] data = byteBuf.array();
        byteBuf.release();

        final int splittingId = nextSplittingId.getAndIncrement();
        int currentPayloadIndex = 0;

        while (currentPayloadIndex < data.length)
        {
            final int currentPayloadIndexEnd = Math.min(currentPayloadIndex + MAX_PACKET_SIZE, data.length);
            final SplittingPartMessage msgPart = new SplittingPartMessage(currentPayloadIndex,
                currentPayloadIndexEnd,
                data.length,
                splittingId,
                Arrays.copyOfRange(data, currentPayloadIndex, currentPayloadIndexEnd));

            // we encode the message in same way like any other message
            final FriendlyByteBuf byteBufPart = new FriendlyByteBuf(Unpooled.buffer());
            final int payloadIndexPart = encodeMessage(msgPart, byteBufPart);
            bufferDispatcher.accept(Pair.of(byteBufPart, payloadIndexPart));

            currentPayloadIndex += MAX_PACKET_SIZE;
        }
    }

    // ========== REDIRECT to our handling ==========
    // FORGE INLINE: from super, just make everything functional iface so it can consume more packets in row

    @Override
    public <MSG> void send(final PacketTarget target, final MSG message)
    {
        toVanillaPacket(message, target.getDirection(), target::send);
    }

    @Override
    public <MSG> void sendTo(final MSG message, final Connection manager, final NetworkDirection direction)
    {
        toVanillaPacket(message, direction, manager::send);
    }

    @Override
    public <MSG> void reply(final MSG message, final NetworkEvent.Context context)
    {
        toBuffer(message, buffer -> context.getPacketDispatcher().sendPacket(networkInstance.getChannelName(), buffer.getLeft()));
    }

    public <MSG> void toVanillaPacket(final MSG message, final NetworkDirection direction, final Consumer<Packet<?>> packetDispatcher)
    {
        toBuffer(message,
            buffer -> packetDispatcher.accept(direction.buildPacket(buffer, networkInstance.getChannelName()).getThis()));
    }

    // ========== REFLECTION cuz super has everything (package)private ==========

    private static NetworkInstance createInstance(final String modId, final String channelName)
    {
        final String version = ModList.get().getModContainerById(modId).get().getModInfo().getVersion().toString();
        return createInstance(new ResourceLocation(modId, channelName), () -> version, version::equals, version::equals);
    }

    private static NetworkInstance createInstance(final ResourceLocation name,
        final Supplier<String> networkProtocolVersion,
        final Predicate<String> clientAcceptedVersions,
        final Predicate<String> serverAcceptedVersions)
    {
        try
        {
            final Method m = NetworkRegistry.class
                .getDeclaredMethod("createInstance", ResourceLocation.class, Supplier.class, Predicate.class, Predicate.class);
            m.setAccessible(true);
            return NetworkInstance.class
                .cast(m.invoke(null, name, networkProtocolVersion, clientAcceptedVersions, serverAcceptedVersions));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private IndexedMessageCodec getIndexedMessageCodec()
    {
        try
        {
            final Field f = SimpleChannel.class.getDeclaredField("indexedCodec");
            f.setAccessible(true);
            return IndexedMessageCodec.class.cast(f.get(this));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private static final Method indexedCodecConsume;

    static
    {
        try
        {
            indexedCodecConsume =
                IndexedMessageCodec.class.getDeclaredMethod("consume", FriendlyByteBuf.class, int.class, Supplier.class);
            indexedCodecConsume.setAccessible(true);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private void consumeMessage(final FriendlyByteBuf byteBuf, final Context ctx)
    {
        consume(byteBuf, INVALID_PAYLOAD_INDEX, () -> ctx);
    }

    private void consume(final FriendlyByteBuf byteBuf, final int payloadIndex, final Supplier<Context> ctx)
    {
        try
        {
            indexedCodecConsume.invoke(indexedCodec, byteBuf, payloadIndex, ctx);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private record ReceivingMessage(byte[] data, AtomicInteger partsLeft)
    {
    }

    private class SplittingPartMessage extends BaseAbstractMessage<SplittingPartMessage>
    {
        private final int payloadIndexStart;
        private final int payloadIndexEnd;
        private final int totalPayloadLength;
        private final int splittingId;
        private final byte[] payload;

        SplittingPartMessage(final int payloadIndexStart,
            final int payloadIndexEnd,
            final int totalPayloadLength,
            final int splittingId,
            final byte[] payload)
        {
            super(null);
            this.payloadIndexStart = payloadIndexStart;
            this.payloadIndexEnd = payloadIndexEnd;
            this.totalPayloadLength = totalPayloadLength;
            this.splittingId = splittingId;
            this.payload = payload;
        }

        SplittingPartMessage(final FriendlyByteBuf buf)
        {
            super(buf);
            payloadIndexStart = buf.readVarInt();
            payloadIndexEnd = buf.readVarInt();
            totalPayloadLength = buf.readVarInt();
            splittingId = buf.readVarInt();
            payload = buf.readByteArray();
        }

        @Override
        protected void toBytes(FriendlyByteBuf buf)
        {
            buf.writeVarInt(payloadIndexStart);
            buf.writeVarInt(payloadIndexEnd);
            buf.writeVarInt(totalPayloadLength);
            buf.writeVarInt(splittingId);
            buf.writeByteArray(payload);
        }

        @Override
        protected boolean runOnMainThread()
        {
            return false; // this can execute in netty threads
        }

        @Override
        protected LogicalSide getExecutionSide()
        {
            return null;
        }

        @Override
        protected void onExecute(final Context ctx, final boolean isLogicalServer)
        {
            final ReceivingMessage rcvMsg;
            try
            {
                rcvMsg = receivingPartCache.getUnchecked(isLogicalServer ? ctx.getSender().getUUID() : CLIENT_CACHE_KEY)
                    .get(splittingId,
                        () -> new ReceivingMessage(new byte[totalPayloadLength],
                            new AtomicInteger(1 + totalPayloadLength / MAX_PACKET_SIZE)));
            }
            catch (ExecutionException e)
            {
                throw new RuntimeException(e);
            }

            synchronized (rcvMsg.data)
            {
                System.arraycopy(payload, 0, rcvMsg.data, payloadIndexStart, payloadIndexEnd - payloadIndexStart);
            }

            if (rcvMsg.partsLeft.decrementAndGet() != 0)
            {
                return;
            }

            final FriendlyByteBuf byteBuf = new FriendlyByteBuf(Unpooled.wrappedBuffer(rcvMsg.data));
            consumeMessage(byteBuf, ctx);
            byteBuf.release();
        }

        @Override
        protected NetworkChannel getNetwork()
        {
            return NetworkChannel.this;
        }
    }
}
