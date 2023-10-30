package com.ldtteam.blockui.mod.item;

import com.ldtteam.blockui.mod.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.Nullable;
import java.util.function.Function;

/**
 * Holds blockstate rendering data for UIs. BlockState must match blockEntity
 */
public record BlockStateRenderingData(BlockState blockState,
    @Nullable BlockEntity blockEntity,
    ModelData modelData,
    boolean renderItemDecorations,
    boolean alwaysAddBlockStateTooltip,
    boolean modelNeedsRotationFix,
    Lazy<ItemStack> playerPickedItemStack)
{
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
            checkModelForYrotation(blockState),
            Lazy.of(() -> BlockToItemHelper.getItemStack(blockState, blockEntity, Minecraft.getInstance().player)));
    }

    /**
     * @param blockEntity must match blockState
     */
    public static BlockStateRenderingData of(final BlockState blockState, @Nullable final BlockEntity blockEntity)
    {
        return blockEntity == null ? of(blockState) :
            new BlockStateRenderingData(blockState, blockEntity, getModelData(blockState, blockEntity), true, false);
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
        return new BlockStateRenderingData(blockState, null, null, true, false);
    }

    /**
     * @return will enable itemStack decorations like enchantment foil or itemStack count
     */
    public BlockStateRenderingData withItemDecorations()
    {
        return renderItemDecorations ? this :
            new BlockStateRenderingData(blockState, blockEntity, modelData, true, alwaysAddBlockStateTooltip, modelNeedsRotationFix, playerPickedItemStack);
    }

    /**
     * @return will disable itemStack decorations like enchantment foil or itemStack count
     */
    public BlockStateRenderingData withoutItemDecorations()
    {
        return !renderItemDecorations ? this :
            new BlockStateRenderingData(blockState, blockEntity, modelData, false, alwaysAddBlockStateTooltip, modelNeedsRotationFix, playerPickedItemStack);
    }

    /**
     * @return will forcibly show blockState properties in tooltip
     */
    public BlockStateRenderingData withForcedBlockStateTooltip()
    {
        return alwaysAddBlockStateTooltip ? this :
            new BlockStateRenderingData(blockState, blockEntity, modelData, renderItemDecorations, true, modelNeedsRotationFix, playerPickedItemStack);
    }

    /**
     * @return will on-demand show blockState properties in tooltip
     */
    public BlockStateRenderingData withoutForcedBlockStateTooltip()
    {
        return !alwaysAddBlockStateTooltip ? this :
            new BlockStateRenderingData(blockState, blockEntity, modelData, renderItemDecorations, false, modelNeedsRotationFix, playerPickedItemStack);
    }

    /**
     * Useful when you want to update blockEntity. Keeps modelData in sync
     */
    public BlockStateRenderingData updateBlockEntity(final Function<BlockEntity, BlockEntity> updater)
    {
        final BlockEntity updated = updater.apply(blockEntity);
        return new BlockStateRenderingData(blockState, updated, getModelData(blockState, updated), renderItemDecorations, alwaysAddBlockStateTooltip, modelNeedsRotationFix, Lazy.of(() -> BlockToItemHelper.getItemStack(blockState, blockEntity, Minecraft.getInstance().player)));
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
        final ModelResourceLocation modelResLoc = BlockModelShaper.stateToModelLocation(blockState);
        final ModelBakery modelBakery =
            Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getModelManager().getModelBakery();
        final UnbakedModel model = modelBakery.getModel(modelResLoc);
        final BlockModel blockModel = model instanceof final BlockModel bm ? bm :
            (model instanceof final MultiVariant mv ?
                modelBakery.modelResources.get(ModelBakery.MODEL_LISTER.idToFile(mv.getVariants().get(0).getModelLocation())) :
                null);

        if (blockModel == null || blockModel.getElements().isEmpty())
        {
            return false;
        }

        for (final BlockElement element : blockModel.getElements())
        {
            if (element.rotation == null || element.rotation.axis() != Direction.Axis.Y)
            {
                return false;
            }
        }

        if (blockState.hasProperty(BlockStateProperties.AXIS))
        {
            return blockState.getValue(BlockStateProperties.AXIS) == Axis.Y;
        }

        if (blockState.hasProperty(BlockStateProperties.FACING))
        {
            final Direction facing = blockState.getValue(BlockStateProperties.FACING);
            return facing == Direction.UP || facing == Direction.DOWN;
        }

        return true;
    }
}
