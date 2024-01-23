package com.ldtteam.blockui.controls;

import com.ldtteam.blockui.BOGuiGraphics;
import com.ldtteam.blockui.PaneParams;
import com.ldtteam.blockui.mod.Log;
import com.ldtteam.blockui.mod.item.BlockStateRenderingData;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Objects;

public class ItemIconWithBlockState extends ItemIcon
{
    public static final String PARAM_NBT = "nbt";

    /**
     * BlockState + BlockEntity ModelData override
     */
    @Nullable
    protected BlockStateRenderingData blockStateExtension = null;

    /**
     * If true then render both item and blockState, mostly internal
     */
    protected boolean renderItemAlongBlockState = false;

    /**
     * If true then render like item instead, mostly internal
     */
    protected boolean isBlockStateModelEmpty = false;

    /**
     * If true always append blockState properties in tooltip
     */
    protected boolean alwaysAddBlockStateTooltip = false;

    /**
     * Standard constructor instantiating the itemIcon without any additional settings.
     */
    public ItemIconWithBlockState()
    {
        super();
    }

    /**
     * Constructor instantiating the itemIcon with specified parameters.
     *
     * @param params the parameters.
     */
    public ItemIconWithBlockState(final PaneParams params)
    {
        super(params);

        final ItemStack newItemStack = itemStack;
        if (newItemStack != null)
        {
            final String nbt = params.getString(PARAM_NBT);
            if (nbt != null)
            {
                try
                {
                    newItemStack.setTag(TagParser.parseTag(nbt));
                }
                catch (final CommandSyntaxException e)
                {
                    Log.getLogger().error("Cannot parse item nbt", e);
                }
            }

            setItem(newItemStack);
        }

        this.alwaysAddBlockStateTooltip = params.getBoolean("alwaysAddBlockStateTooltip", alwaysAddBlockStateTooltip);
    }

    @Override
    public void setItem(final ItemStack itemStack)
    {
        super.setItem(itemStack);
        readBlockStateFromCurrentItemStack();
    }

    @Override
    public void clearDataAndScheduleTooltipUpdate()
    {
        super.clearDataAndScheduleTooltipUpdate();
        blockStateExtension = null;
        renderItemAlongBlockState = false;
        isBlockStateModelEmpty = false;
    }

    public boolean isBlockEmpty()
    {
        return blockStateExtension == null || isBlockStateModelEmpty;
    }

    @Override
    public boolean isDataEmpty()
    {
        return super.isDataEmpty() && isBlockEmpty();
    }

    @Override
    public void drawSelf(final BOGuiGraphics target, final double mx, final double my)
    {
        updateTooltipIfNeeded();
        if (isBlockEmpty())
        {
            super.drawSelf(target, mx, my);
            return;
        }
        
        final PoseStack ms = target.pose();
        ms.pushPose();
        ms.translate(x, y, 0.0f);
        ms.scale(this.getWidth() / DEFAULT_ITEMSTACK_SIZE, this.getHeight() / DEFAULT_ITEMSTACK_SIZE, 1.0f);

        if (renderItemAlongBlockState)
        {
            target.renderItem(itemStack, 0, 0);
        }
        target.renderBlockStateAsItem(blockStateExtension, itemStack);
        if (renderItemDecorations)
        {
            target.renderItemDecorations(itemStack, 0, 0);
        }

        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        ms.popPose();
    }

    @Override
    protected int modifyTooltipName(final List<Component> tooltipList, final TooltipFlag tooltipFlag, int nameOffset)
    {
        if (blockStateExtension != null)
        {
            final ResourceLocation key = ForgeRegistries.BLOCKS.getKey(blockStateExtension.blockState().getBlock());
            final String nameTKey = Util.makeDescriptionId("block", key);
            final MutableComponent name = Component.translatable(nameTKey);
            final MutableComponent nameKey = Component.literal(key.toString()).withStyle(ChatFormatting.DARK_GRAY);

            // add block name if present and differs from item
            if (!tooltipList.get(0).getString().equals(name.getString()) && !name.getString().equals(nameTKey))
            {
                tooltipList.add(nameOffset, name.withStyle(ChatFormatting.GRAY));
                nameOffset++;
            }

            // replace id with blockstate id
            for (int i = tooltipList.size() - 1; i >= 0; i--)
            {
                if (tooltipList.get(i).getContents() instanceof final LiteralContents literalContents &&
                    ResourceLocation.isValidResourceLocation(literalContents.text()))
                {
                    tooltipList.set(i, nameKey);
                    break;
                }
            }
        }
        return nameOffset;
    }

    @Override
    protected int appendTooltip(final List<Component> tooltipList, final TooltipFlag tooltipFlag, int prevTooltipSize)
    {
        // add blockstate info
        if (blockStateExtension != null && !blockStateExtension.blockState().getProperties().isEmpty() &&
            (tooltipFlag.isAdvanced() || alwaysAddBlockStateTooltip))
        {
            final boolean shouldFixPrevTooltipSize = alwaysAddBlockStateTooltip && prevTooltipSize == tooltipList.size();

            tooltipList.add(wrapShift(Component.empty(), !alwaysAddBlockStateTooltip));
            tooltipList.add(wrapShift(Component.translatable("blockui.tooltip.properties"), !alwaysAddBlockStateTooltip));

            final BlockState blockState = blockStateExtension.blockState();
            for (final Property<?> property : blockState.getProperties())
            {
                tooltipList.add(wrapShift(Component.literal("  " + property.getName() + " = " + getValueName(blockState, property))
                    .withStyle(ChatFormatting.GRAY), !alwaysAddBlockStateTooltip));
            }

            if (shouldFixPrevTooltipSize)
            {
                prevTooltipSize = tooltipList.size();
            }
        }
        return prevTooltipSize;
    }

    /**
     * @param alwaysRenderItem if true (and applicable) then will render itemStack before blockState rendering, mostly internal and
     *                         should not be changed from outside
     */
    public void setRenderItemAlongBlockState(final boolean alwaysRenderItem)
    {
        this.renderItemAlongBlockState = alwaysRenderItem;
    }

    /**
     * @return true if (applicable and) should render itemStack before blockState rendering
     */
    public boolean shouldRenderItemAlongBlockState()
    {
        return renderItemAlongBlockState;
    }

    /**
     * @param alwaysAddTooltip true for: forcibly show blockState properties in tooltip (otherwise on demand)
     */
    public void setAlwaysAddBlockStateTooltip(final boolean alwaysAddTooltip)
    {
        this.alwaysAddBlockStateTooltip = alwaysAddTooltip;
    }

    /**
     * @return true if forcibly show blockState properties in tooltip (otherwise on demand)
     */
    public boolean shouldAlwaysAddBlockStateTooltip()
    {
        return alwaysAddBlockStateTooltip;
    }

    /**
     * Overrides itemStack rendering with custom blockState. Sets itemStack from blockState
     */
    public void setBlockState(final BlockState blockState, @Nullable final BlockEntity blockEntity)
    {
        setBlockState(BlockStateRenderingData.of(blockState, blockEntity));
    }

    /**
     * Overrides itemStack rendering with custom blockState. Sets itemStack from blockState
     */
    public void setBlockState(final BlockStateRenderingData blockStateExtension)
    {
        setItemFromBlockState(blockStateExtension);
        setBlockStateWeak(blockStateExtension);
    }

    /**
     * Overrides itemstack rendering with custom blockstate. Does not check for itemStack block vs blockState equality
     */
    public void setBlockStateWeak(final BlockStateRenderingData blockStateExtension)
    {
        final ItemStack keepItemStack = Objects.requireNonNull(itemStack, "ItemStack must not be null when setting blockState");
        clearDataAndScheduleTooltipUpdate();
        itemStack = keepItemStack;

        this.blockStateExtension = blockStateExtension;

        final BlockState bs = blockStateExtension.blockState();
        // if it is invisible block without fluid or block entity then switch back to item rendering
        if (bs.getRenderShape() == RenderShape.INVISIBLE && blockStateExtension.blockEntity() == null)
        {
            if (bs.getFluidState().isEmpty())
            {
                this.isBlockStateModelEmpty = true;
            }
            // but if it is waterlogged then go for both?
            else if (bs.hasProperty(BlockStateProperties.WATERLOGGED))
            {
                this.renderItemAlongBlockState = true;
            }
        }
    }

    public BlockStateRenderingData getBlockState()
    {
        return blockStateExtension;
    }

    /**
     * Extracts blockstate/modeldata from current itemstack
     */
    protected void readBlockStateFromCurrentItemStack()
    {
        if (itemStack == null || !(itemStack.getItem() instanceof final BlockItem blockItem))
        {
            return;
        }

        BlockState blockstate = blockItem.getBlock().defaultBlockState();

        // parse block state
        final CompoundTag blockStateTag = itemStack.getTagElement(BlockItem.BLOCK_STATE_TAG);

        if (blockStateTag == null)
        {
            return;
        }

        final StateDefinition<Block, BlockState> statedefinition = blockstate.getBlock().getStateDefinition();

        for (final String propertyKey : blockStateTag.getAllKeys())
        {
            final Property<?> property = statedefinition.getProperty(propertyKey);
            if (property != null)
            {
                blockstate = updateState(blockstate, property, blockStateTag.getString(propertyKey));
            }
        }

        // try parsing blockentity
        final CompoundTag blockEntityTag = itemStack.getTagElement(BlockItem.BLOCK_ENTITY_TAG);
        BlockEntity be = null;
        if (blockEntityTag != null)
        {
            try
            {
                // use probably invalid pos
                be = BlockEntity.loadStatic(BlockStateRenderingData.ILLEGAL_BLOCK_ENTITY_POS, blockstate, blockEntityTag);
            }
            catch (final Exception e)
            {
                Log.getLogger().warn("Error while parsing blockentity data: " + blockEntityTag, e);
            }
        }

        setBlockStateWeak(BlockStateRenderingData.of(blockstate, be));
    }

    private static <T extends Comparable<T>> BlockState updateState(final BlockState state, final Property<T> property, final String valueName)
    {
        return property.getValue(valueName).map(value -> state.setValue(property, value)).orElse(state);
    }

    private static <T extends Comparable<T>> String getValueName(final BlockState blockState, final Property<T> property)
    {
        return property.getName(blockState.getValue(property));
    }
}
