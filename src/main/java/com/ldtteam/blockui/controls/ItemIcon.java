package com.ldtteam.blockui.controls;

import com.ldtteam.blockui.MatrixUtils;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.PaneParams;
import com.ldtteam.blockui.util.SpacerTextComponent;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.ForgeRegistries;

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
    }

    /**
     * Set the item of the icon.
     *
     * @param itemStackIn the itemstack to set.
     */
    public void setItem(final ItemStack itemStackIn)
    {
        this.itemStack = itemStackIn;
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

    @Override
    public void drawSelf(final PoseStack ms, final double mx, final double my)
    {
        if (itemStack != null && !itemStack.isEmpty())
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
            mc.getItemRenderer().renderGuiItemDecorations(font, itemStack, 0, 0);

            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            MatrixUtils.popShaderMVstack();
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

    public List<Component> getModifiedItemStackTooltip()
    {
        if (itemStack == null)
        {
            return Collections.emptyList();
        }

        final List<Component> result = window.getScreen().getTooltipFromItem(itemStack);
        result.add(1, FIX_VANILLA_TOOLTIP);
        return result;
    }
}
