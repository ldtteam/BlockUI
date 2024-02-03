package com.ldtteam.common.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.LogicalSide;

/**
 * Client (sender) -> Server (receiver) message
 */
public abstract class AbstractServerPlayMessage extends AbstractPlayMessage<ServerPlayer> implements IServerboundDistributor
{
    @Override
    protected LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }
}
