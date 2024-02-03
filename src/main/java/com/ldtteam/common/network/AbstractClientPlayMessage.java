package com.ldtteam.common.network;

import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;

/**
 * Server (sender) -> Client (receiver) message
 */
public abstract class AbstractClientPlayMessage extends AbstractPlayMessage<Player> implements IClientboundDistributor
{
    @Override
    protected LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }
}
