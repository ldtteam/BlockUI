package com.ldtteam.blockui.controls;

import com.ldtteam.blockui.BOGuiGraphics;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.PaneParams;
import com.ldtteam.blockui.mod.Log;
import com.ldtteam.blockui.util.SpacerTextComponent;
import com.ldtteam.blockui.util.ToggleableTextComponent;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.CreativeModeTabRegistry;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

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
    private BlockStateExtension blockStateExtension;

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
                setItem(item.getDefaultInstance());
            }
        }

        final String nbt = params.getString("nbt");
        if (nbt != null && itemStack != null)
        {
            try
            {
                itemStack.setTag(TagParser.parseTag(nbt));
                processBlockStateFromCurrentItemStack();
            }
            catch (final CommandSyntaxException e)
            {
                Log.getLogger().error("Cannot parse item nbt", e);
            }
        }
    }

    /**
     * Set the item of the icon.
     *
     * @param itemStackIn the itemstack to set.
     */
    public void setItem(final ItemStack itemStackIn)
    {
        this.itemStack = itemStackIn;
        processBlockStateFromCurrentItemStack();
        if (onHover instanceof final Tooltip tooltip)
        {
            tooltip.setTextOld(getModifiedItemStackTooltip());
        }
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
     * Overrides itemstack rendering with custom blockstate
     * 
     * @see #setBlockStateOverride(BlockState, ModelData) for BlockEntity model data
     */
    public void setBlockStateOverride(final BlockState blockState)
    {
        setBlockStateOverride(blockState, null);
    }

    /**
     * Overrides itemstack rendering with custom blockstate
     */
    public void setBlockStateOverride(final BlockState blockState, final ModelData modelData)
    {
        setBlockStateOverride(blockState, modelData, true);
    }

    /**
     * Overrides itemstack rendering with custom blockstate
     */
    public void setBlockStateOverride(final BlockState blockState, final ModelData modelData, final boolean renderItemDecorations)
    {
        this.blockStateExtension = new BlockStateExtension(blockState, modelData, renderItemDecorations);
    }

    @Override
    public void drawSelf(final BOGuiGraphics target, final double mx, final double my)
    {
        final PoseStack ms = target.pose();

        if (itemStack != null && !itemStack.isEmpty())
        {
            ms.pushPose();
            ms.translate(x, y, 0.0f);
            ms.scale(this.getWidth() / DEFAULT_ITEMSTACK_SIZE, this.getHeight() / DEFAULT_ITEMSTACK_SIZE, 1.0f);

            if (blockStateExtension == null)
            {
                target.renderItem(itemStack, 0, 0);
            }
            else
            {
                target.renderBlockStateAsItem(blockStateExtension.blockState, blockStateExtension.modelData(), itemStack);
            }

            if (blockStateExtension == null || blockStateExtension.renderItemDecorations)
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
        if (onHover == null && itemStack != null && !itemStack.isEmpty())
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
        if (itemStack == null)
        {
            return Collections.emptyList();
        }

        TooltipFlag.Default tooltipFlags = mc.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
        if (mc.player.isCreative())
        {
            tooltipFlags = tooltipFlags.asCreative();
        }

        final List<Component> result = itemStack.getTooltipLines(mc.player, tooltipFlags);
        final ItemStack defaultStack = itemStack.getItem().getDefaultInstance();

        if (tooltipFlags.advanced() && tooltipFlags.creative())
        {
            // add tags
            ForgeRegistries.ITEMS.getHolder(itemStack.getItem())
                .map(Holder::getTagKeys)
                .ifPresent(tags -> tags
                    .forEach(tag -> result.add(1, wrapShift(Component.literal("#" + tag.location()).withStyle(ChatFormatting.DARK_PURPLE)))));

            // add creative tabs
            int i = 1;
            for (final CreativeModeTab tab : CreativeModeTabRegistry.getSortedCreativeModeTabs())
            {
                if (tab.contains(defaultStack))
                {
                    result.add(i++, wrapShift(tab.getDisplayName().copy().withStyle(ChatFormatting.BLUE)));
                }
            }

            // add blockstate info
            if (blockStateExtension != null)
            {
                result.add(wrapShift(Component.empty()));
                result.add(wrapShift(Component.translatable("blockui.tooltip.properties")));

                final BlockState blockState = blockStateExtension.blockState;
                for (final Property<?> property : blockState.getProperties())
                {
                    result.add(wrapShift(Component.literal("  " + property.getName() + " = " + getValueName(blockState, property)).withStyle(ChatFormatting.GRAY)));
                }
            }

            // add "show more info" text
            result.add(ToggleableTextComponent.ofNegated(Screen::hasShiftDown, Component.empty()));
            result.add(ToggleableTextComponent.ofNegated(Screen::hasShiftDown,
                Component.translatable("blockui.tooltip.item_additional_info", Component.translatable("key.keyboard.left.shift"))
                    .withStyle(ChatFormatting.GOLD)));
        }

        result.add(1, FIX_VANILLA_TOOLTIP);
        return result;
    }

    private static MutableComponent wrapShift(final MutableComponent wrapped)
    {
        return ToggleableTextComponent.of(Screen::hasShiftDown, wrapped);
    }

    /**
     * Extracts blockstate/modeldata from current itemstack
     */
    protected void processBlockStateFromCurrentItemStack()
    {
        // remove old extension
        blockStateExtension = null;

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
        ModelData modelData = null;
        if (blockEntityTag != null)
        {
            try
            {
                // use probably invalid pos
                final BlockEntity be = BlockEntity.loadStatic(BlockPos.ZERO.below(1000), blockstate, blockEntityTag);
                if (be != null)
                {
                    modelData = be.getModelData();
                }
            }
            catch (Exception e)
            {
                Log.getLogger().warn("Error while parsing blockentity data", e);
            }
        }

        blockStateExtension = new BlockStateExtension(blockstate, modelData, true);
    }

    private static <T extends Comparable<T>> BlockState updateState(final BlockState state, final Property<T> property, final String valueName)
    {
        return property.getValue(valueName).map(value -> state.setValue(property, value)).orElse(state);
    }

    private static <T extends Comparable<T>> String getValueName(final BlockState blockState, final Property<T> property)
    {
        return property.getName(blockState.getValue(property));
    }

    public static record BlockStateExtension(BlockState blockState, @Nullable ModelData modelData, boolean renderItemDecorations)
    {
        @Override
        public ModelData modelData()
        {
            return modelData == null ? ModelData.EMPTY : modelData;
        }
    }
}
