package com.ldtteam.common.network;

import net.neoforged.fml.LogicalSide;

/**
 * Server (sender) -> Client (receiver) message
 */
public abstract class AbstractClientPlayMessage extends AbstractUnsidedPlayMessage implements IClientboundDistributor
{
    @Override
    protected LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }
}
