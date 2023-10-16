package com.ldtteam.blockui.mod.item;

import com.ldtteam.blockui.mod.Log;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.util.Lazy;
import java.util.Optional;
import java.util.function.Function;

/**
 * Holds blockstate rendering data for UIs. BlockState must match blockEntity
 */
public record BlockStateRenderingData(BlockState blockState,
    BlockEntity blockEntity,
    ModelData modelData,
    boolean renderItemDecorations,
    boolean alwaysAddBlockStateTooltip,
    Lazy<Optional<ItemStack>> playerPickedItemStack)
{
    public static final HitResult CLONE_ITEM_STACK_HIT_RESULT = new BlockHitResult(Vec3.atCenterOf(BlockPos.ZERO), Direction.DOWN, BlockPos.ZERO, false);
    public static final BlockPos ILLEGAL_BLOCK_ENTITY_POS = BlockPos.ZERO.below(1000);

    public BlockStateRenderingData(BlockState blockState,
        BlockEntity blockEntity,
        ModelData modelData,
        boolean renderItemDecorations,
        boolean alwaysAddBlockStateTooltip)
    {
        this(blockState,
            blockEntity,
            modelData,
            alwaysAddBlockStateTooltip,
            alwaysAddBlockStateTooltip,
            Lazy.of(() -> BlockToItemHelper.getItemStack(blockState, blockEntity)));
    }

    /**
     * @param blockState  blockState
     * @param blockEntity must match blockState
     */
    public static BlockStateRenderingData of(final BlockState blockState, final BlockEntity blockEntity)
    {
        return blockEntity == null ? of(blockState) : new BlockStateRenderingData(blockState, blockEntity, getModelData(blockState, blockEntity), true, false);
    }

    public static BlockStateRenderingData of(final BlockState blockState)
    {
        if (blockState.hasBlockEntity() && blockState.getBlock() instanceof final EntityBlock entityBlock)
        {
            final BlockEntity be = entityBlock.newBlockEntity(ILLEGAL_BLOCK_ENTITY_POS, blockState);
            if (be != null)
            {
                return of(blockState, be);
            }
        }
        return new BlockStateRenderingData(blockState, null, null, true, false);
    }

    public BlockStateRenderingData withItemDecorations()
    {
        return renderItemDecorations ? this : new BlockStateRenderingData(blockState, blockEntity, modelData, true, alwaysAddBlockStateTooltip, playerPickedItemStack);
    }

    public BlockStateRenderingData withoutItemDecorations()
    {
        return !renderItemDecorations ? this : new BlockStateRenderingData(blockState, blockEntity, modelData, false, alwaysAddBlockStateTooltip, playerPickedItemStack);
    }

    public BlockStateRenderingData withForcedBlockStateTooltip()
    {
        return alwaysAddBlockStateTooltip ? this : new BlockStateRenderingData(blockState, blockEntity, modelData, renderItemDecorations, true, playerPickedItemStack);
    }

    public BlockStateRenderingData withoutForcedBlockStateTooltip()
    {
        return !alwaysAddBlockStateTooltip ? this : new BlockStateRenderingData(blockState, blockEntity, modelData, renderItemDecorations, false, playerPickedItemStack);
    }

    public BlockStateRenderingData updateBlockEntity(final Function<BlockEntity, BlockEntity> updater)
    {
        final BlockEntity updated = updater.apply(blockEntity);
        return new BlockStateRenderingData(blockState,
            updated,
            getModelData(blockState, updated),
            renderItemDecorations,
            alwaysAddBlockStateTooltip,
            playerPickedItemStack);
    }

    public ModelData modelData()
    {
        return modelData == null ? ModelData.EMPTY : modelData;
    }

    private static ModelData getModelData(final BlockState blockState, final BlockEntity blockEntity)
    {
        ModelData model = ModelData.EMPTY;
        try
        {
            model = blockEntity.getModelData();
        }
        catch (final Exception e)
        {
            Log.getLogger().warn("Could not get model data for: " + blockState.toString(), e);
        }
        return model;
    }

    /**
     * @return best guess using player pick and similar methods
     */
    public Optional<ItemStack> itemStack()
    {
        return playerPickedItemStack.get();
    }
}
