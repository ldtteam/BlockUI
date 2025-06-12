package com.ldtteam.common.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

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
    public Type<?> type()
    {
        return key.id();
    }

    /**
     * Writes message data to buffer.
     *
     * @param buf fresh network payload
     */
    protected abstract void toBytes(final RegistryFriendlyByteBuf buf);
}
