package com.ldtteam.blockui.mod.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import java.util.Optional;

/**
 * Methods for getting itemStack from blockState
 */
public class BlockToItemHelper
{
    /**
     * Same as {@link #getItemStackUsingPlayerPick(BlockState, BlockEntity)} but liquids return buckets, fires flint&steel, etc.
     * 
     * @return result of enhanced simulatated middle mouse button click, empty if crashed or no pick result available
     */
    public static Optional<ItemStack> getItemStack(final BlockState blockState, final BlockEntity blockEntity)
    {
        // quick path air blocks
        if (blockState.getBlock() instanceof AirBlock)
        {
            return Optional.empty();
        }
        return getItemStackUsingPlayerPick(blockState, blockEntity).or(() -> Optional.ofNullable(getItem(blockState)).map(ItemStack::new));
    }

    /**
     * @return result of simulatated middle mouse button click, empty if crashed or no pick result available
     */
    public static Optional<ItemStack> getItemStackUsingPlayerPick(final BlockState blockState, final BlockEntity blockEntity)
    {
        try
        {
            return Optional
                .ofNullable(blockState.getCloneItemStack(BlockStateRenderingData.CLONE_ITEM_STACK_HIT_RESULT, new BlockGetter()
                {
                    @Override
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
                }, BlockPos.ZERO, null)).map(result -> result.isEmpty() ? null : result);
        }
        catch (final Exception e)
        {
            return Optional.empty();
        }
    }

    /**
     * @return null instead of air
     */
    private static Item getItem(final BlockState blockState)
    {
        final Block block = blockState.getBlock();
        if (block instanceof final LiquidBlock liquid)
        {
            return liquid.getFluid().getBucket();
        }
        else if (block instanceof BubbleColumnBlock)
        {
            return Items.WATER_BUCKET;
        }
        else if (block instanceof BaseFireBlock)
        {
            return Items.FLINT_AND_STEEL;
        }

        final Item asItem = block.asItem();
        return asItem == Items.AIR ? null : asItem;
    }
}
