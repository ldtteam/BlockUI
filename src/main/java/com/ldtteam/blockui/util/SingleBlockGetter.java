package com.ldtteam.blockui.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import javax.annotation.Nullable;

/**
 * Small single blockstate level wrapper
 */
public class SingleBlockGetter implements LevelReader
{
    // TODO: stupid level class hierarchy
    public BlockState blockState = null;
    public BlockEntity blockEntity = null;

    public SingleBlockGetter(final BlockState blockState, final BlockEntity blockEntity)
    {
        this.blockState = blockState;
        this.blockEntity = blockEntity;
    }

    public SingleBlockGetter(final BlockState blockState)
    {
        this(blockState, null);
    }

    public SingleBlockGetter()
    {
        this(null, null);
    }

    @Override
    @Nullable
    public BlockEntity getBlockEntity(final BlockPos pos)
    {
        return BlockPos.ZERO.equals(pos) ? blockEntity : null;
    }

    @Override
    public BlockState getBlockState(final BlockPos pos)
    {
        return BlockPos.ZERO.equals(pos) ? blockState : Blocks.VOID_AIR.defaultBlockState();
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

    /**
     * Small single blockstate level wrapper. Lighting set to 10, full shading
     */
    public static class SingleBlockNeighborhood extends SingleBlockGetter implements BlockAndTintGetter
    {
        public SingleBlockNeighborhood(final BlockState blockState, final BlockEntity blockEntity)
        {
            super(blockState, blockEntity);
        }

        public SingleBlockNeighborhood(final BlockState blockState)
        {
            super(blockState, null);
        }

        public SingleBlockNeighborhood()
        {
            super(null, null);
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
            return colorResolver.getColor(ServerLifecycleHooks.getCurrentServer().registryAccess().registryOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS), pos.getX(), pos.getZ());
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
}
