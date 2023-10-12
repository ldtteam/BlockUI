package com.ldtteam.blockui.util.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A fake level containing a single block at {@link BlockPos#ZERO}.
 */
final class SingleBlockLevel implements BlockAndTintGetter
{
    private final Level level;
    private final BlockState block;

    public SingleBlockLevel(final Level level, final BlockState block)
    {
        this.level = level;
        this.block = block;
    }

    @Override
    public float getShade(@NotNull final Direction pDirection, final boolean pShade)
    {
        return level.getShade(pDirection, pShade);
    }

    @NotNull
    @Override
    public LevelLightEngine getLightEngine()
    {
        return level.getLightEngine();
    }

    @Override
    public int getBrightness(@NotNull final LightLayer pLightType, @NotNull final BlockPos pBlockPos)
    {
        return 15;
    }

    @Override
    public int getBlockTint(@NotNull final BlockPos pPos, @NotNull final ColorResolver pColorResolver)
    {
        if (pPos.equals(BlockPos.ZERO))
        {
            return IClientFluidTypeExtensions.of(block.getFluidState()).getTintColor();
        }
        return -1;
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(@NotNull final BlockPos pPos)
    {
        return null;
    }

    @NotNull
    @Override
    public BlockState getBlockState(@NotNull final BlockPos pPos)
    {
        return pPos.equals(BlockPos.ZERO) ? block : Blocks.VOID_AIR.defaultBlockState();
    }

    @NotNull
    @Override
    public FluidState getFluidState(@NotNull final BlockPos pPos)
    {
        return getBlockState(pPos).getFluidState();
    }

    @Override
    public int getHeight()
    {
        return level.getHeight();
    }

    @Override
    public int getMinBuildHeight()
    {
        return level.getMinBuildHeight();
    }
}
