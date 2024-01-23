package com.ldtteam.blockui.controls;

import com.ldtteam.blockui.MatrixUtils;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneParams;
import com.ldtteam.blockui.controls.AbstractTextBuilder.AutomaticTooltipBuilder;
import com.ldtteam.blockui.controls.Tooltip.AutomaticTooltip;
import com.ldtteam.blockui.mod.Log;
import com.ldtteam.blockui.mod.item.BlockStateRenderingData;
import com.ldtteam.blockui.util.SpacerTextComponent;
import com.ldtteam.blockui.util.ToggleableTextComponent;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Class of itemIcons in our GUIs.
 */
public class ItemIcon extends Pane
{
    protected static final float DEFAULT_ITEMSTACK_SIZE = 16f;
    protected static final MutableComponent FIX_VANILLA_TOOLTIP = SpacerTextComponent.of(1);

    /**
     * ItemStack represented in the itemIcon.
     */
    @Nullable
    protected ItemStack itemStack;

    /**
     * If true then on next frame tooltip content will recompile
     */
    protected boolean tooltipUpdateScheduled = false;
    protected boolean renderItemDecorations = true;

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

        this.renderItemDecorations = params.getBoolean("renderItemDecorations", renderItemDecorations);
    }

    /**
     * Set the item of the icon. Will render as blockState if stack nbt contains blockState tag.
     *
     * @param itemStack the itemstack to set.
     */
    public void setItem(final ItemStack itemStack)
    {
        clearDataAndScheduleTooltipUpdate();
        this.itemStack = itemStack;
        onItemUpdate();
    }

    /**
     * Called when itemStack was changed
     */
    protected void onItemUpdate()
    {

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
     * @param renderDecorations true if should render itemStack decorations (enchantment foil, itemStack count, ...)
     */
    public void setRenderItemDecorations(final boolean renderDecorations)
    {
        this.renderItemDecorations = renderDecorations;
    }

    /**
     * @return true if shoudl render itemStack decorations (enchantment foil, itemStack count, ...)
     */
    public boolean renderItemDecorations()
    {
        return renderItemDecorations;
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
        clearDataAndScheduleTooltipUpdate();

        // intentionally not calling setItem cuz iconWithBlock would replace its internal data
        // end-users should use setBlockState of iconWithBlock if they want to set both
        itemStack = blockStateExtension.itemStack();
        if (itemStack.isEmpty() && !(blockStateExtension.blockState().getBlock() instanceof AirBlock))
        {
            Log.getLogger().warn("Cannot create proper itemStack for: " + blockStateExtension.blockState().toString());
        }
        if (!itemStack.isEmpty() && blockStateExtension.blockEntity() != null)
        {
            blockStateExtension.blockEntity().saveToItem(itemStack);
        }
        onItemUpdate();
    }

    /**
     * Resets all data in this item icon, effectively making it empty.
     */
    public void clearDataAndScheduleTooltipUpdate()
    {
        itemStack = null;
        tooltipUpdateScheduled = true;
    }

    protected boolean isItemEmpty()
    {
        return itemStack == null || itemStack.isEmpty();
    }

    public boolean isDataEmpty()
    {
        return isItemEmpty();
    }

    protected void updateTooltipIfNeeded()
    {
        if (tooltipUpdateScheduled)
        {
            if (onHover instanceof final AutomaticTooltip tooltip)
            {
                tooltip.setTextOld(getModifiedItemStackTooltip());
            }
            tooltipUpdateScheduled = false;
        }
    }

    @Override
    public void drawSelf(final PoseStack ms, final double mx, final double my)
    {
        updateTooltipIfNeeded();
        if (!isDataEmpty())
        {
            ms.pushPose();
            ms.translate(x, y, 0.0f);
            ms.scale(this.getWidth() / DEFAULT_ITEMSTACK_SIZE, this.getHeight() / DEFAULT_ITEMSTACK_SIZE, 1.0f);
            MatrixUtils.pushShaderMVstack(ms);

            Font font = IClientItemExtensions.DEFAULT.getFont(itemStack, IClientItemExtensions.FontContext.ITEM_COUNT);
            if (font == null)
            {
                font = mc.font;
            }

            mc.getItemRenderer().renderAndDecorateItem(itemStack, 0, 0);
            if (renderItemDecorations)
            {
                mc.getItemRenderer().renderGuiItemDecorations(font, itemStack, 0, 0);
            }

            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            MatrixUtils.popShaderMVstack();
            ms.popPose();
        }
    }

    @Override
    public void onUpdate()
    {
        if (onHover == null && !isItemEmpty())
        {
            new AutomaticTooltipBuilder().hoverPane(this).build().setTextOld(getModifiedItemStackTooltip());
        }
    }

    /**
     * @param tooltipList tooltip to modify
     * @param nameOffset points to element right after last name element
     * @return incremented name offset (if other names were added)
     */
    protected int modifyTooltipName(final List<Component> tooltipList, final TooltipFlag tooltipFlags, final int nameOffset)
    {
        return nameOffset;
    }

    /**
     * prevTooltipSize: This value if for determining whether to append "show more info" text or not.
     * If you add elements which are wrapped via ToggleableTextComponent (and want to show "show more info" text), then add their count to this value.
     * else if you want to hide the text then set this value to {@code tooltipList.size()}
     * 
     * @param tooltipList tooltip to modify
     * @param prevTooltipSize tooltip size before any modifications
     * @return new prevTooltipSize
     */
    protected int appendTooltip(final List<Component> tooltipList, final TooltipFlag tooltipFlags, final int prevTooltipSize)
    {
        return prevTooltipSize;
    }

    /**
     * Adds spacer and optional data
     *
     * INLINE: 
     * @see CreativeModeInventoryScreen#getTooltipFromContainerItem(ItemStack)
     */
    public List<Component> getModifiedItemStackTooltip()
    {
        if (isDataEmpty())
        {
            return Collections.emptyList();
        }

        TooltipFlag.Default tooltipFlags = mc.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;

        final List<Component> tooltipList = itemStack.getTooltipLines(mc.player, tooltipFlags);
        int nameOffset = 1;

        nameOffset = modifyTooltipName(tooltipList, tooltipFlags, nameOffset);

        int prevTooltipSize = tooltipList.size();
        if (tooltipFlags.isAdvanced() && mc.player.isCreative())
        {
            final Item item = itemStack.getItem();
            // add tags
            final int nameoffset = nameOffset + 1;
            ForgeRegistries.ITEMS.getHolder(item)
                .map(Holder::getTagKeys)
                .ifPresent(tags -> tags.forEach(tag -> tooltipList.add(nameoffset,
                    wrapShift(Component.literal("#" + tag.location()).withStyle(ChatFormatting.DARK_PURPLE)))));

            if (item.getItemCategory() != null)
            {
                tooltipList.add(nameOffset + 1, wrapShift(item.getItemCategory().getDisplayName().copy().withStyle(ChatFormatting.BLUE)));
            }
        }

        prevTooltipSize = appendTooltip(tooltipList, tooltipFlags, prevTooltipSize);

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

    protected static MutableComponent wrapShift(final MutableComponent wrapped)
    {
        return ToggleableTextComponent.of(Screen::hasShiftDown, wrapped);
    }

    protected static MutableComponent wrapShift(final MutableComponent wrapped, final boolean shouldWrap)
    {
        return shouldWrap ? ToggleableTextComponent.of(Screen::hasShiftDown, wrapped) : wrapped;
    }
}
