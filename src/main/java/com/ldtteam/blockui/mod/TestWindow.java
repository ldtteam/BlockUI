package com.ldtteam.blockui.mod;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.BlockIcon;
import com.ldtteam.blockui.views.BOWindow;
import com.ldtteam.blockui.views.DropDownList;
import com.ldtteam.blockui.views.ScrollingList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

import java.util.List;

/**
 * Simple test window for BlockUI.
 */
class TestWindow extends BOWindow
{
    /**
     * Constructor.
     *
     * @param resource layout resource.
     */
    public TestWindow(final ResourceLocation resource)
    {
        super(resource);

        fillBlockStates();
    }

    private void fillBlockStates()
    {
        final DropDownList blockStatesList = findPaneOfTypeByID("blockstates", DropDownList.class);
        if (blockStatesList == null)
        {
            return;
        }

        final Block block = Blocks.POLISHED_DIORITE_STAIRS;
        final List<BlockState> states = block.getStateDefinition().getPossibleStates();

        blockStatesList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return states.size();
            }

            @Override
            public void updateElement(int index, Pane rowPane)
            {
                final BlockIcon icon = rowPane.findPaneByType(BlockIcon.class);
                if (icon != null)
                {
                    icon.setBlock(states.get(index), ModelData.EMPTY);
                }
            }
        });
        blockStatesList.setSelectedIndex(0);
    }
}
