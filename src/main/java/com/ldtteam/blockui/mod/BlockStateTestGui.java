package com.ldtteam.blockui.mod;

import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.ItemIconWithBlockState;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.controls.Tooltip;
import com.ldtteam.blockui.mod.item.BlockStateRenderingData;
import com.ldtteam.blockui.views.BOWindow;
import com.ldtteam.blockui.views.ZoomDragView;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.ForgeRegistries;

/**
 * Sets up gui for all blockstates
 */
public class BlockStateTestGui
{
    public static void setup(final BOWindow window)
    {
        final ZoomDragView view = window.findFirstPaneByType(ZoomDragView.class);
        final int rowHeight = 18;

        int rowY = 0;
        for (final Block block : ForgeRegistries.BLOCKS)
        {
            final ItemIcon icon = new ItemIcon();
            icon.setPosition(0, rowY);
            icon.setSize(16, 16);
            icon.setItem(new ItemStack(block));
            view.addChildPlain(icon);

            final ItemIcon pickIcon = new ItemIcon();
            pickIcon.setPosition(16, rowY);
            pickIcon.setSize(16, 16);
            view.addChildPlain(pickIcon);

            final Text text = PaneBuilders.textBuilder()
                .append(Component.literal(ForgeRegistries.BLOCKS.getKey(block).toString()))
                .colorName(BlockStateRenderingData.checkModelForYrotation(block.defaultBlockState()) ? "red" : "black")
                .build();
            text.setPosition(35, rowY);
            text.setSize(Tooltip.DEFAULT_MAX_WIDTH, Tooltip.DEFAULT_MAX_HEIGHT);
            text.recalcPreparedTextBox();
            text.setSize(text.getRenderedTextWidth() + 10, rowHeight);
            view.addChildPlain(text);

            int x = 35 + text.getWidth();
            for (final BlockState blockState : block.getStateDefinition().getPossibleStates())
            {
                final ItemIconWithBlockState blockIcon = new ItemIconWithBlockState();
                blockIcon.setPosition(x, rowY);
                blockIcon.setSize(16, 16);
                blockIcon.setAlwaysAddBlockStateTooltip(true);
                blockIcon.setBlockState(blockState, null);

                if (pickIcon.isDataEmpty() && blockIcon.getBlockState() != null)
                {
                    pickIcon.setItemFromBlockState(blockIcon.getBlockState());
                }

                view.addChildPlain(blockIcon);
                x += rowHeight;
            }

            rowY += rowHeight;
        }

        view.computeContentSize();
    }
}
