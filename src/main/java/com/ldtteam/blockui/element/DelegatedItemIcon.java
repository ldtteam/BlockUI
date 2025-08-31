package com.ldtteam.blockui.element;

import com.ldtteam.blockui.AbstractPane;
import com.ldtteam.blockui.AbstractPaneGroup;
import com.ldtteam.blockui.context.RenderContext;
import com.ldtteam.blockui.element.shadow.AbstractDelegatedPane;
import com.ldtteam.blockui.parse.Parser;
import com.ldtteam.blockui.parse.ParsingCodec;
import com.ldtteam.blockui.util.Crash.CheckArgument;
import com.ldtteam.blockui.util.text.SpacerTextComponent;
import com.ldtteam.blockui.util.text.ToggleableTextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.common.CreativeModeTabRegistry;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import java.util.Collections;
import java.util.List;

public class DelegatedItemIcon extends AbstractDelegatedPane
{
    public static final int VANILLA_ITEM_SIZE = 16;
    private static final MutableComponent FIX_VANILLA_TOOLTIP = SpacerTextComponent.of(1);

    public static final String ID = "itemIcon";
    public static final ParsingCodec<DelegatedItemIcon> CODEC = ParsingCodec.forDelegated(ID, DelegatedItemIcon::new)
        .field("path", Parser.ITEM_STACK, DelegatedItemIcon::setItemStack)
        .field("altStackCount", Parser.SINGLE_WORD, DelegatedItemIcon::setAlternativeStackCount);

    @Nullable
    protected String altStackCount = null;
    protected ItemStack itemStack;
    private boolean isTooltipCreationScheduled = false;

    public DelegatedItemIcon(final AbstractPaneGroup parent)
    {
        super(parent);
    }

    public void setAlternativeStackCount(@Nullable final String altStackCount)
    {
        this.altStackCount = altStackCount;
    }

    public void setItemStack(final ItemStack itemStack)
    {
        this.itemStack = CheckArgument.notNull(itemStack, "Use visible(false) instead");
    }

    @Nullable
    public String getAlternativeStackCount()
    {
        return altStackCount;
    }

    public ItemStack getItemStack()
    {
        return itemStack;
    }

    public void replaceHoverWithVanillaTooltip()
    {
        isTooltipCreationScheduled = true;
    }

    @Override
    protected void drawSelf(final RenderContext ctx, final double mx, final double my)
    {
        if (isTooltipCreationScheduled)
        {
            isTooltipCreationScheduled = false;
            window.createVanillaTooltipFor(this).getContentPane().setText(getModifiedItemStackTooltip(ctx.mc));
        }

        ctx.pushScaleNoZ((float) width / VANILLA_ITEM_SIZE, (float) height / VANILLA_ITEM_SIZE);
        ctx.renderItem(itemStack, alignedX, alignedY, altStackCount);
        ctx.pop();
    }

    public static class ItemIcon extends DelegatedItemIcon implements RenderSetters, AccessibleId
    {
        public static final ParsingCodec<ItemIcon> CODEC = ParsingCodec.of(ID, ItemIcon::new, AbstractPane.CODEC)
            .copyFieldsFrom(DelegatedItemIcon.CODEC)
            .field("itemScale", Parser.INT, ItemIcon::setSizeToScaleOfVanilla);

        public ItemIcon(final AbstractPaneGroup parent)
        {
            super(parent);
        }

        public ItemIcon setSizeToScaleOfVanilla(final int scale)
        {
            setSize(VANILLA_ITEM_SIZE * scale, VANILLA_ITEM_SIZE * scale);
            return this;
        }
    }

    protected List<MutableComponent> getModifiedItemStackTooltip(final Minecraft mc)
    {
        if (itemStack == null)
        {
            return Collections.emptyList();
        }

        TooltipFlag.Default tooltipFlags = mc.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
        if (mc.player != null && mc.player.isCreative())
        {
            tooltipFlags = tooltipFlags.asCreative();
        }

        // TODO: validate each mc version that component type only has MutableComponent as child
        @SuppressWarnings({"rawtypes", "unchecked"})
        final List<MutableComponent> result = (List) itemStack.getTooltipLines(mc.player, tooltipFlags);
        final ItemStack defaultStack = itemStack.getItem().getDefaultInstance();

        if (tooltipFlags.advanced() && tooltipFlags.creative())
        {
            ForgeRegistries.ITEMS.getHolder(itemStack.getItem())
                .map(Holder::getTagKeys)
                .ifPresent(tags -> tags.forEach(
                    tag -> result.add(1, wrapShift(Component.literal("#" + tag.location()).withStyle(ChatFormatting.DARK_PURPLE)))));

            int i = 1;
            for (final CreativeModeTab tab : CreativeModeTabRegistry.getSortedCreativeModeTabs())
            {
                if (!tab.hasSearchBar() && tab.contains(defaultStack))
                {
                    result.add(i++, wrapShift(tab.getDisplayName().copy().withStyle(ChatFormatting.BLUE)));
                }
            }
        }

        result.add(1, FIX_VANILLA_TOOLTIP);
        return result;
    }

    private static MutableComponent wrapShift(final MutableComponent wrapped)
    {
        return ToggleableTextComponent.of(Screen::hasShiftDown, wrapped);
    }
}
