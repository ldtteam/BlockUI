package com.ldtteam.blockui.mod.item;

import com.ldtteam.blockui.mod.Log;
import com.ldtteam.common.util.BlockToItemHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.Nullable;
import java.util.function.Function;

/**
 * Holds blockstate rendering data for UIs. BlockState must match blockEntity
 */
public record BlockStateRenderingData(BlockState blockState,
    @Nullable BlockEntity blockEntity,
    ModelData modelData,
    boolean modelNeedsRotationFix,
    Lazy<ItemStack> playerPickedItemStack)
{
    public static final BlockPos ILLEGAL_BLOCK_ENTITY_POS = BlockPos.ZERO.below(1000);

    private BlockStateRenderingData(final BlockState blockState,
        final BlockEntity blockEntity,
        final ModelData modelData,
        final boolean modelNeedsRotationFix)
    {
        this(blockState,
            blockEntity,
            modelData,
            modelNeedsRotationFix,
            Lazy.of(() -> BlockToItemHelper.getItemStack(blockState, blockEntity, Minecraft.getInstance().player)));
    }

    private BlockStateRenderingData(final BlockState blockState, final BlockEntity blockEntity, final ModelData modelData)
    {
        this(blockState, blockEntity, modelData, checkModelForYrotation(blockState));
    }

    /**
     * @return captures blockstate in given level at given pos in current time (now)
     */
    public static BlockStateRenderingData of(final Level level, final BlockPos pos, final Player player)
    {
        final BlockState blockState = level.getBlockState(pos);
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        final ItemStack itemStack = BlockToItemHelper.getItemStack(level, pos, player);

        return new BlockStateRenderingData(blockState,
            blockEntity,
            getModelData(blockState, blockEntity),
            checkModelForYrotation(blockState),
            Lazy.of(() -> itemStack));
    }

    /**
     * @param blockEntity must match blockState
     */
    public static BlockStateRenderingData of(final BlockState blockState, @Nullable final BlockEntity blockEntity)
    {
        return blockEntity == null ? of(blockState) : new BlockStateRenderingData(blockState, blockEntity, getModelData(blockState, blockEntity));
    }

    /**
     * If blockState should have blockEntity then a new fresh empty one will be created. Use {@link #of(BlockState, BlockEntity)} everywhere possible
     */
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
        return new BlockStateRenderingData(blockState, null, null);
    }

    /**
     * Useful when you want to update blockEntity. Keeps modelData in sync
     */
    public BlockStateRenderingData updateBlockEntity(final Function<BlockEntity, BlockEntity> updater)
    {
        final BlockEntity updated = updater.apply(blockEntity);
        return new BlockStateRenderingData(blockState, updated, getModelData(blockState, updated), modelNeedsRotationFix);
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
    public ItemStack itemStack()
    {
        return playerPickedItemStack.get();
    }

    /**
     * @return true if model contains only Y axis rotations
     */
    public static boolean checkModelForYrotation(final BlockState blockState)
    {
        // TODO: port 21.6 this is completely gone
        // find out how to detect whether blockState is being rendered by model:block/cross
        // or why cross models have rotation issues
        return false;
    }
}
