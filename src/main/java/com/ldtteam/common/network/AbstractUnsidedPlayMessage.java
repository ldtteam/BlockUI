package com.ldtteam.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Root class of message hierarchy serving as bouncer to vanilla networking.
 */
abstract class AbstractUnsidedPlayMessage implements CustomPacketPayload
{
    private final PlayMessageType<?> key;

    public AbstractUnsidedPlayMessage(final PlayMessageType<?> key)
    {
        this.key = key;
    }

    @Override
    public ResourceLocation id()
    {
        return key.id();
    }

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
}
