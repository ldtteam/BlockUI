package com.ldtteam.blockui.controls;

import com.ldtteam.blockui.BOGuiGraphics;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.PaneParams;
import com.ldtteam.blockui.mod.Log;
import com.ldtteam.blockui.mod.item.BlockStateRenderingData;
import com.ldtteam.blockui.util.SpacerTextComponent;
import com.ldtteam.blockui.util.ToggleableTextComponent;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.common.CreativeModeTabRegistry;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class of itemIcons in our GUIs.
 */
public class ItemIcon extends Pane
{
    private static final float DEFAULT_ITEMSTACK_SIZE = 16f;
    private static final MutableComponent FIX_VANILLA_TOOLTIP = SpacerTextComponent.of(1);

    /**
     * ItemStack represented in the itemIcon.
     */
    private ItemStack itemStack;

    /**
     * BlockState + BlockEntity ModelData override
     */
    private BlockStateRenderingData blockStateExtension;
    private boolean renderItemAlongBlockState = false;

    private boolean tooltipUpdateScheduled = false;

    /**
     * Standard constructor instantiating the itemIcon without any additional settings.
     */
    public ItemIcon()
    {
        super();
    }

    /**
     * Constructor instantiating the itemIcon with specified parameters.
     *
     * @param params the parameters.
     */
    public ItemIcon(final PaneParams params)
    {
        super(params);

        final String itemName = params.getString("item");
        if (itemName != null)
        {
            final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
            if (item != null)
            {
                final ItemStack newItemStack = item.getDefaultInstance();

                final String nbt = params.getString("nbt");
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
        }
    }

    /**
     * @param renderItemAlongBlockState if true (and applicable) then will render itemStack before blockState rendering
     */
    public void setRenderItemAlongBlockState(final boolean renderItemAlongBlockState)
    {
        this.renderItemAlongBlockState = renderItemAlongBlockState;
    }

    /**
     * @return true if (applicable and) should render itemStack before blockState rendering
     */
    public boolean renderItemAlongBlockState()
    {
        return renderItemAlongBlockState;
    }

    /**
     * Set the item of the icon. Will render as blockState if stack nbt contains blockState tag.
     *
     * @param itemStack the itemstack to set.
     */
    public void setItem(final ItemStack itemStack)
    {
        this.itemStack = itemStack;
        clearBlockStateAndUpdateTooltip();
        readBlockStateFromCurrentItemStack();
    }

    /**
     * Get the itemstack of the icon.
     *
     * @return the stack of it.
     */
    public ItemStack getItem()
    {
        return this.itemStack;
    }

    /**
     * Sets itemStack from blockState.
     * 
     * @see #setItem(ItemStack) equivalent of setItem(ItemStack)
     */
    public void setItemFromBlockState(final BlockState blockState, @Nullable final BlockEntity blockEntity)
    {
        setItemFromBlockState(BlockStateRenderingData.of(blockState, blockEntity));
    }

    /**
     * Sets itemStack from blockState.
     * 
     * @see #setItem(ItemStack) equivalent of setItem(ItemStack)
     */
    public void setItemFromBlockState(final BlockStateRenderingData blockStateExtension)
    {
        clearBlockStateAndUpdateTooltip();

        itemStack = blockStateExtension.itemStack();
        if (itemStack.isEmpty() && !(blockStateExtension.blockState().getBlock() instanceof AirBlock))
        {
            Log.getLogger().warn("Cannot create proper itemStack for: " + blockStateExtension.blockState().toString());
        }
        if (!itemStack.isEmpty() && blockStateExtension.blockEntity() != null)
        {
            blockStateExtension.blockEntity().saveToItem(itemStack);
        }
    }

    /**
     * Overrides itemStack rendering with custom blockState.
     * Sets itemStack from blockState
     */
    public void setBlockStateOverride(final BlockState blockState, @Nullable final BlockEntity blockEntity)
    {
        setBlockStateOverride(BlockStateRenderingData.of(blockState, blockEntity));
    }

    /**
     * Overrides itemStack rendering with custom blockState.
     * Sets itemStack from blockState
     */
    public void setBlockStateOverride(final BlockStateRenderingData blockStateExtension)
    {
        setItemFromBlockState(blockStateExtension);
        setBlockStateWeakOverride(blockStateExtension);
    }

    /**
     * Overrides itemstack rendering with custom blockstate. Does not check for itemStack block vs blockState equality
     */
    public void setBlockStateWeakOverride(final BlockStateRenderingData blockStateExtension)
    {
        clearBlockStateAndUpdateTooltip();
        this.blockStateExtension = blockStateExtension;
        
        final BlockState bs = blockStateExtension.blockState();
        // if it is invisible block without fluid or block entity then switch back to item rendering
        if (bs.getRenderShape() == RenderShape.INVISIBLE && blockStateExtension.blockEntity() == null)
        {
            if (bs.getFluidState().isEmpty())
            {
                this.blockStateExtension = null;
            }
            // but if it is waterlogged then go for both?
            else if (bs.hasProperty(BlockStateProperties.WATERLOGGED))
            {
                this.renderItemAlongBlockState = true;
            }
        }
    }

    public void clearBlockStateAndUpdateTooltip()
    {
        blockStateExtension = null;
        renderItemAlongBlockState = false;
        tooltipUpdateScheduled = true;
    }

    public BlockStateRenderingData getBlockStateExtension()
    {
        return blockStateExtension;
    }

    public boolean isItemEmpty()
    {
        return (itemStack == null || itemStack.isEmpty()) && blockStateExtension == null;
    }

    @Override
    public void drawSelf(final BOGuiGraphics target, final double mx, final double my)
    {
        if (tooltipUpdateScheduled)
        {
            if (onHover instanceof final Tooltip tooltip)
            {
                tooltip.setTextOld(getModifiedItemStackTooltip());
            }
            tooltipUpdateScheduled = false;
        }

        if (!isItemEmpty())
        {
            final PoseStack ms = target.pose();
            ms.pushPose();
            ms.translate(x, y, 0.0f);
            ms.scale(this.getWidth() / DEFAULT_ITEMSTACK_SIZE, this.getHeight() / DEFAULT_ITEMSTACK_SIZE, 1.0f);

            if (blockStateExtension == null)
            {
                target.renderItem(itemStack, 0, 0);
            }
            else
            {
                if (renderItemAlongBlockState)
                {
                    target.renderItem(itemStack, 0, 0);
                }
                target.renderBlockStateAsItem(blockStateExtension, itemStack);
            }

            if (blockStateExtension == null || blockStateExtension.renderItemDecorations())
            {
                target.renderItemDecorations(itemStack, 0, 0);
            }

            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            ms.popPose();
        }
    }

    @Override
    public void onUpdate()
    {
        if (onHover == null && !isItemEmpty())
        {
            PaneBuilders.tooltipBuilder().hoverPane(this).build().setTextOld(getModifiedItemStackTooltip());
        }
    }

    /**
     * Adds spacer and optional data
     *
     * INLINE: 
     * @see CreativeModeInventoryScreen#getTooltipFromContainerItem(ItemStack)
     */
    public List<Component> getModifiedItemStackTooltip()
    {
        final boolean isItemPresent = itemStack != null && !itemStack.isEmpty();
        if (!isItemPresent && blockStateExtension == null)
        {
            return Collections.emptyList();
        }

        TooltipFlag.Default tooltipFlags = mc.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
        if (mc.player.isCreative())
        {
            tooltipFlags = tooltipFlags.asCreative();
        }

        final List<Component> tooltipList = isItemPresent ? itemStack.getTooltipLines(mc.player, tooltipFlags) : new ArrayList<>();
        int nameOffset = 1;

        if (blockStateExtension != null)
        {
            final ResourceLocation key = ForgeRegistries.BLOCKS.getKey(blockStateExtension.blockState().getBlock());
            final String nameTKey = Util.makeDescriptionId("block", key);
            final MutableComponent name = Component.translatable(nameTKey);
            final MutableComponent nameKey = Component.literal(key.toString()).withStyle(ChatFormatting.DARK_GRAY);

            if (tooltipList.isEmpty())
            {
                tooltipList.add(name);
                tooltipList.add(nameKey);
            }
            else
            {
                // add block name if present and differs from item
                if (!tooltipList.get(0).getString().equals(name.getString()) && !name.getString().equals(nameTKey))
                {
                    tooltipList.add(1, name.withStyle(ChatFormatting.GRAY));
                    nameOffset++;
                }

                // replace id with blockstate id
                for (int i = tooltipList.size() - 1; i >= 0; i--)
                {
                    if (tooltipList.get(i).getContents() instanceof final LiteralContents literalContents && ResourceLocation.isValidResourceLocation(literalContents.text()))
                    {
                        tooltipList.set(i, nameKey);
                        break;
                    }
                }
            }
        }

        int prevTooltipSize = tooltipList.size();
        if (tooltipFlags.advanced() && tooltipFlags.creative() && isItemPresent)
        {
            // add tags
            final int nameoffset = nameOffset + 1;
            ForgeRegistries.ITEMS.getHolder(itemStack.getItem())
                .map(Holder::getTagKeys)
                .ifPresent(tags -> tags.forEach(tag -> tooltipList.add(nameoffset,
                    wrapShift(Component.literal("#" + tag.location()).withStyle(ChatFormatting.DARK_PURPLE)))));

            // add creative tabs
            int i = nameOffset + 1;
            final ItemStack defaultStack = itemStack.getItem().getDefaultInstance();
            for (final CreativeModeTab tab : CreativeModeTabRegistry.getSortedCreativeModeTabs())
            {
                if (tab.contains(defaultStack))
                {
                    tooltipList.add(i++, wrapShift(tab.getDisplayName().copy().withStyle(ChatFormatting.BLUE)));
                }
            }
        }

        // add blockstate info
        if (blockStateExtension != null && !blockStateExtension.blockState().getProperties().isEmpty() &&
            (tooltipFlags.advanced() || blockStateExtension.alwaysAddBlockStateTooltip()))
        {
            final boolean shouldFixPrevTooltipSize = blockStateExtension.alwaysAddBlockStateTooltip() && prevTooltipSize == tooltipList.size();

            tooltipList.add(wrapShift(Component.empty(), !blockStateExtension.alwaysAddBlockStateTooltip()));
            tooltipList.add(wrapShift(Component.translatable("blockui.tooltip.properties"), !blockStateExtension.alwaysAddBlockStateTooltip()));

            final BlockState blockState = blockStateExtension.blockState();
            for (final Property<?> property : blockState.getProperties())
            {
                tooltipList.add(wrapShift(Component.literal("  " + property.getName() + " = " + getValueName(blockState, property))
                    .withStyle(ChatFormatting.GRAY), !blockStateExtension.alwaysAddBlockStateTooltip()));
            }

            if (shouldFixPrevTooltipSize)
            {
                prevTooltipSize = tooltipList.size();
            }
        }

        if (prevTooltipSize != tooltipList.size())
        {
            // add "show more info" text
            tooltipList.add(ToggleableTextComponent.ofNegated(Screen::hasShiftDown, Component.empty()));
            tooltipList.add(ToggleableTextComponent.ofNegated(Screen::hasShiftDown,
                Component.translatable("blockui.tooltip.item_additional_info", Component.translatable("key.keyboard.left.shift"))
                    .withStyle(ChatFormatting.GOLD)));
        }

        tooltipList.add(nameOffset, FIX_VANILLA_TOOLTIP);
        return tooltipList;
    }

    private static MutableComponent wrapShift(final MutableComponent wrapped)
    {
        return ToggleableTextComponent.of(Screen::hasShiftDown, wrapped);
    }

    private static MutableComponent wrapShift(final MutableComponent wrapped, final boolean shouldWrap)
    {
        return shouldWrap ? ToggleableTextComponent.of(Screen::hasShiftDown, wrapped) : wrapped;
    }

    /**
     * Extracts blockstate/modeldata from current itemstack
     */
    protected void readBlockStateFromCurrentItemStack()
    {
        if (!(itemStack.getItem() instanceof final BlockItem blockItem))
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
            catch (Exception e)
            {
                Log.getLogger().warn("Error while parsing blockentity data: " + blockEntityTag, e);
            }
        }

        setBlockStateWeakOverride(BlockStateRenderingData.of(blockstate, be));
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
