package com.ldtteam.common.network;

import net.neoforged.fml.LogicalSide;

/**
 * Client (sender) -> Server (receiver) message
 */
public abstract class AbstractServerPlayMessage extends AbstractUnsidedPlayMessage implements IServerboundDistributor
{
    @Override
    protected LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }
}
