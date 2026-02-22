package com.ldtteam.blockui.controls;

import com.ldtteam.blockui.BOGuiGraphics;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneParams;
import com.ldtteam.blockui.controls.AbstractTextBuilder.AutomaticTooltipBuilder;
import com.ldtteam.blockui.controls.Tooltip.AutomaticTooltip;
import com.ldtteam.blockui.mod.Log;
import com.ldtteam.blockui.mod.item.BlockStateRenderingData;
import com.ldtteam.blockui.util.SpacerTextComponent;
import com.ldtteam.blockui.util.ToggleableTextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.block.AirBlock;
import net.neoforged.neoforge.common.CreativeModeTabRegistry;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

        final Identifier itemName = params.getResource("item");
        if (itemName != null)
        {
            final Optional<Holder.Reference<Item>> item = BuiltInRegistries.ITEM.get(itemName);
            if (item.isPresent())
            {
                setItem(item.get().value().getDefaultInstance());
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
            blockStateExtension.blockState().item.blockEntity().saveToItem(itemStack, mc.level.registryAccess());
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
    public void drawSelf(final BOGuiGraphics target, final double mx, final double my)
    {
        updateTooltipIfNeeded();
        if (!isDataEmpty())
        {
            final Matrix3x2fStack ms = target.guiGraphics().pose();
            ms.pushMatrix();
            ms.translate(x, y);
            ms.scale(this.getWidth() / DEFAULT_ITEMSTACK_SIZE, this.getHeight() / DEFAULT_ITEMSTACK_SIZE);

            target.guiGraphics().renderItem(itemStack, 0, 0);
            if (renderItemDecorations)
            {
                target.guiGraphics().renderItemDecorations(mc.font, itemStack, 0, 0);
            }

            ms.popMatrix();
        }
    }

    @Override
    public void onUpdate()
    {
        if (onHover == null && !isItemEmpty())
        {
            new AutomaticTooltipBuilder().hoverPane(this).build();
            tooltipUpdateScheduled = true;
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
        if (mc.player.isCreative())
        {
            tooltipFlags = tooltipFlags.asCreative();
        }

        final List<Component> tooltipList = itemStack.getTooltipLines(TooltipContext.of(mc.level), mc.player, tooltipFlags);
        int nameOffset = 1;

        nameOffset = modifyTooltipName(tooltipList, tooltipFlags, nameOffset);

        int prevTooltipSize = tooltipList.size();
        if (tooltipFlags.advanced() && tooltipFlags.creative())
        {
            // add tags
            final int nameoffset = nameOffset + 1;
            BuiltInRegistries.ITEM.wrapAsHolder(itemStack.getItem())
                .tags()
                .forEach(tag -> tooltipList.add(nameoffset,
                    wrapShift(Component.literal("#" + tag.location()).withStyle(ChatFormatting.DARK_PURPLE))));

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

        prevTooltipSize = appendTooltip(tooltipList, tooltipFlags, prevTooltipSize);

        if (prevTooltipSize != tooltipList.size())
        {
            // add "show more info" text
            tooltipList.add(ToggleableTextComponent.ofNegated(mc::hasShiftDown, Component.empty()));
            tooltipList.add(ToggleableTextComponent.ofNegated(mc::hasShiftDown,
                Component.translatable("blockui.tooltip.item_additional_info", Component.translatable("key.keyboard.left.shift"))
                    .withStyle(ChatFormatting.GOLD)));
        }

        tooltipList.add(nameOffset, FIX_VANILLA_TOOLTIP);
        return tooltipList;
    }

    protected static MutableComponent wrapShift(final MutableComponent wrapped)
    {
        return ToggleableTextComponent.of(Minecraft.getInstance()::hasShiftDown, wrapped);
    }

    protected static MutableComponent wrapShift(final MutableComponent wrapped, final boolean shouldWrap)
    {
        return shouldWrap ? ToggleableTextComponent.of(Minecraft.getInstance()::hasShiftDown, wrapped) : wrapped;
    }
}
