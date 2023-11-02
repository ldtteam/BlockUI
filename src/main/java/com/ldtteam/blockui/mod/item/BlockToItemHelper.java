package com.ldtteam.blockui.mod.item;

import com.ldtteam.blockui.util.SingleBlockGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

/**
 * Methods for getting itemStack from blockState
 */
public class BlockToItemHelper
{
    public static final HitResult CLONE_ITEM_STACK_HIT_RESULT =
        new BlockHitResult(Vec3.atCenterOf(BlockPos.ZERO), Direction.DOWN, BlockPos.ZERO, false);

    /**
     * Same as {@link #getItemStackUsingPlayerPick(BlockState, BlockEntity, Player)} but liquids return buckets, fires flint&steel, etc.
     * 
     * @return result of enhanced simulatated middle mouse button click, empty if crashed or no pick result available
     */
    public static ItemStack getItemStack(final BlockState blockState, final BlockEntity blockEntity, final Player player)
    {
        // quick path air blocks
        if (blockState.getBlock() instanceof AirBlock)
        {
            return ItemStack.EMPTY;
        }
        final ItemStack result = getItemStackUsingPlayerPick(blockState, blockEntity, player);
        return result.isEmpty() ? getItem(blockState).getDefaultInstance() : result;
    }

    /**
     * Same as {@link #getItemStackUsingPlayerPick(BlockState, BlockEntity, Player)} but liquids return buckets, fires flint&steel, etc.
     * 
     * @param  serverLevel for fake player instance
     * @return             result of enhanced simulatated middle mouse button click, empty if crashed or no pick result available
     */
    public static ItemStack getItemStack(final BlockState blockState, final BlockEntity blockEntity, final ServerLevel serverLevel)
    {
        return getItemStack(blockState, blockEntity, FakePlayerFactory.getMinecraft(serverLevel));
    }

    /**
     * @return result of simulatated middle mouse button click, empty if crashed or no pick result available
     */
    public static ItemStack getItemStackUsingPlayerPick(final BlockState blockState, final BlockEntity blockEntity, final Player player)
    {
        return blockState.getCloneItemStack(CLONE_ITEM_STACK_HIT_RESULT, new SingleBlockGetter(blockState, blockEntity), BlockPos.ZERO, player);
    }

    private static Item getItem(final BlockState blockState)
    {
        final Block block = blockState.getBlock();
        if (block instanceof final LiquidBlock liquid)
        {
            return liquid.getFluid().getBucket();
        }
        else if (block instanceof final BubbleColumnBlock column)
        {
            return column.getFluidState(blockState).getType().getBucket();
        }
        else if (block instanceof BaseFireBlock)
        {
            return Items.FLINT_AND_STEEL;
        }

        return block.asItem();
    }
}
