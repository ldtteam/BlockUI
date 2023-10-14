package com.ldtteam.blockui.mod;

import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.BOGuiGraphics.BlockStateRenderingData;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.controls.Tooltip;
import com.ldtteam.blockui.views.BOWindow;
import com.ldtteam.blockui.views.ZoomDragView;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

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

            final Text text = PaneBuilders.textBuilder().append(Component.literal(ForgeRegistries.BLOCKS.getKey(block).toString())).colorName("black").build();
            text.setPosition(20, rowY);
            text.setSize(Tooltip.DEFAULT_MAX_WIDTH, Tooltip.DEFAULT_MAX_HEIGHT);
            text.recalcPreparedTextBox();
            text.setSize(text.getRenderedTextWidth() + 10, rowHeight);
            view.addChildPlain(text);

            int x = 20 + text.getWidth();
            for (final BlockState blockState : block.getStateDefinition().getPossibleStates())
            {
                final ItemIcon blockIcon = new ItemIcon();
                blockIcon.setPosition(x, rowY);
                blockIcon.setSize(16, 16);
                blockIcon.setBlockStateOverride(BlockStateRenderingData.of(blockState).withForcedBlockStateTooltip());

                view.addChildPlain(blockIcon);
                x += rowHeight;
            }

            rowY += rowHeight;
        }

        view.computeContentSize();
    }
}
