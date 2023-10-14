package com.ldtteam.blockui.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

import javax.annotation.Nullable;

/**
 * Small single blockstate level wrapper. Lighting set to 10, full shading
 */
public class SingleBlockNeighborhood implements BlockAndTintGetter
{
    public BlockState blockState;

    @Override
    @Nullable
    public BlockEntity getBlockEntity(final BlockPos pos)
    {
        return null;
    }

    @Override
    public BlockState getBlockState(final BlockPos pos)
    {
        return BlockPos.ZERO.equals(pos) ? blockState : Blocks.AIR.defaultBlockState();
    }

    @Override
    public FluidState getFluidState(final BlockPos pos)
    {
        return getBlockState(pos).getFluidState();
    }

    @Override
    public int getHeight()
    {
        return 1;
    }

    @Override
    public int getMinBuildHeight()
    {
        return 0;
    }

    @Override
    public float getShade(final Direction direction, final boolean shade)
    {
        return 1;
    }

    @Override
    public LevelLightEngine getLightEngine()
    {
        throw new UnsupportedOperationException("Does anyone need LightEngine?");
    }

    @Override
    public int getBlockTint(final BlockPos pos, final ColorResolver colorResolver)
    {
        return BlockPos.ZERO.equals(pos) ? IClientFluidTypeExtensions.of(blockState.getFluidState()).getTintColor() : 0xff_ff_ff_ff; // argb
    }

    @Override
    public int getBrightness(final LightLayer lightLayer, final BlockPos pos)
    {
        return 10;
    }

    @Override
    public int getRawBrightness(final BlockPos pos, final int amount)
    {
        return 10;
    }
}