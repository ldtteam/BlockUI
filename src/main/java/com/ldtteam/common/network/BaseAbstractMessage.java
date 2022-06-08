package com.ldtteam.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.NetworkEvent.Context;
import org.jetbrains.annotations.Nullable;

/**
 * Abstract class for all network messages
 */
public abstract class BaseAbstractMessage<T extends BaseAbstractMessage<T>>
{
    @SuppressWarnings("unchecked")
    private final T thiz = (T) this;

    /**
     * Remainder of that you have to create "fromBytes" ctor
     * 
     * @param buf buffer when "fromBytes" ctor, null otherwise
     */
    protected BaseAbstractMessage(final FriendlyByteBuf buf)
    {
        // force ctor as fromBytes method
    }

    /**
     * Writes message data to buffer.
     *
     * @param buf network data byte buffer
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
     * Executes message action.
     *
     * @param ctxIn           network context of incoming message
     * @param isLogicalServer whether message arrived at logical server side
     */
    protected abstract void onExecute(final Context ctx, final boolean isLogicalServer);

    /**
     * Implementing class in using mod should call {@link #createNetwork(String)} and store it into static field, which in turn should
     * be returned by this method.
     * 
     * @return mod network instance
     */
    protected abstract NetworkChannel getNetwork();

    /**
     * @param  modId implementing mod id
     * @return       new network instance
     */
    protected NetworkChannel createNetwork(final String modId)
    {
        return new NetworkChannel(modId);
    }

    /**
     * @return override with false if you can process given message offthread, can be based on message content ie. this is queried
     *         before onExecute call
     */
    protected boolean runOnMainThread()
    {
        return true;
    }

    /**
     * Replies to given context. Client sends to server. Server sends to message origin (one specific client).
     * 
     * @param ctx
     */
    public T sendReply(final Context ctx)
    {
        getNetwork().reply(this, ctx);
        return thiz;
    }

    /**
     * Sends to server.
     */
    public T sendToServer()
    {
        getNetwork().sendToServer(this);
        return thiz;
    }

    /**
     * Sends to player.
     *
     * @param player target player
     */
    public T sendToPlayer(final ServerPlayer player)
    {
        getNetwork().send(PacketDistributor.PLAYER.with(() -> player), this);
        return thiz;
    }

    /**
     * Sends to everyone in dimension.
     *
     * @param level target dimension
     */
    public T sendToDimension(final ResourceKey<Level> level)
    {
        getNetwork().send(PacketDistributor.DIMENSION.with(() -> level), this);
        return thiz;
    }

    /**
     * Sends to everyone in circle made using given target point.
     *
     * @param pos target position and radius
     * @see       PacketDistributor.TargetPoint
     */
    public T sendToPosition(final PacketDistributor.TargetPoint pos)
    {
        getNetwork().send(PacketDistributor.NEAR.with(() -> pos), this);
        return thiz;
    }

    /**
     * Sends to everyone.
     */
    public T sendToEveryone()
    {
        getNetwork().send(PacketDistributor.ALL.noArg(), this);
        return thiz;
    }

    /**
     * Sends to everyone (excluding given entity) who is in certain range from entity's pos.
     *
     * @param entity target entity, excluded
     */
    public T sendToTrackingEntity(final Entity entity)
    {
        getNetwork().send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), this);
        return thiz;
    }

    /**
     * Sends to everyone (including given entity) who is in certain range from entity's pos.
     *
     * @param entity target entity, included
     */
    public T sendToTrackingEntityAndSelf(final Entity entity)
    {
        getNetwork().send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), this);
        return thiz;
    }

    /**
     * Sends to everyone in given chunk.
     *
     * @param chunk target chunk
     */
    public T sendToTrackingChunk(final LevelChunk chunk)
    {
        getNetwork().send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), this);
        return thiz;
    }
}
