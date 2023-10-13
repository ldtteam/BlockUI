package com.ldtteam.blockui.controls;

import com.google.gson.JsonParser;
import com.ldtteam.blockui.BOGuiGraphics;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.PaneParams;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class of blockIcons in our GUIs.
 */
public class BlockIcon extends Pane
{
    /**
     * BlockState represented in the blockIcon.
     */
    @Nullable private BlockState blockState;
    private ModelData modelData = ModelData.EMPTY;
    private float yaw = 45;
    private float pitch = -30;

    /**
     * Standard constructor instantiating the blockIcon without any additional settings.
     */
    public BlockIcon()
    {
        super();
    }

    /**
     * Constructor instantiating the blockIcon with specified parameters.
     *
     * @param params the parameters.
     */
    public BlockIcon(final PaneParams params)
    {
        super(params);

        final String blockName = params.getString("block");
        if (blockName != null)
        {
            final Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockName));
            if (block != null)
            {
                final BlockState state = parseBlockProperties(block, params.getString("properties"));
                setBlock(state, ModelData.EMPTY);
            }
        }
        this.yaw = params.getFloat("yaw", this.yaw);
        this.pitch = params.getFloat("pitch", this.pitch);
    }

    /**
     * Set the blockstate of the icon.
     *
     * @param blockStateIn the blockstate to set.
     * @param modelDataIn the modeldata to set.
     */
    public void setBlock(@Nullable final BlockState blockStateIn, @NotNull final ModelData modelDataIn)
    {
        this.blockState = blockStateIn;
        this.modelData = modelDataIn;

        if (onHover instanceof final Tooltip tooltip)
        {
            tooltip.setText(getToolTip());
        }
    }

    /**
     * Get the blockstate of the icon.
     *
     * @return the state of it.
     */
    @Nullable public BlockState getBlock()
    {
        return this.blockState;
    }

    public ModelData getModelData()
    {
        return this.modelData;
    }

    public void setYaw(final float yaw)
    {
        this.yaw = yaw;
    }

    public void setPitch(final float pitch)
    {
        this.pitch = pitch;
    }

    @Override
    public void drawSelf(final BOGuiGraphics ms, double mx, double my)
    {
        if (blockState != null)
        {
            final float scale = Math.min(this.getWidth(), this.getHeight()) / 2F;
            drawBlock(ms.pose(), this.blockState, this.modelData, x + getWidth() / 2F, y + scale, 0F, this.pitch, this.yaw, scale);
        }
    }

    @Override
    public void onUpdate()
    {
        if (onHover == null && blockState != null)
        {
            PaneBuilders.tooltipBuilder().hoverPane(this).build().setText(getToolTip());
        }
    }

    private List<MutableComponent> getToolTip()
    {
        if (blockState == null)
        {
            return Collections.emptyList();
        }

        final List<MutableComponent> result = new ArrayList<>();
        result.add(Component.translatable(blockState.getBlock().getDescriptionId()));

        for (final Property<?> property : blockState.getProperties())
        {
            result.add(Component.literal(property.value(blockState).toString()));
        }

        return result;
    }

    /**
     * This is only a very basic parser, mostly for test purposes.
     * @param block          the block.
     * @param propertiesText optional semicolon-separated list of name=value properties.
     * @return               the parsed blockstate.
     */
    private static BlockState parseBlockProperties(final Block block, @Nullable final String propertiesText)
    {
        BlockState state = block.defaultBlockState();

        if (propertiesText != null)
        {
            final String[] propertiesList = propertiesText.split(";");

            for (final String nameValue : propertiesList)
            {
                final String[] nameAndValue = nameValue.split("=", 2);

                final Property<?> property = block.getStateDefinition().getProperty(nameAndValue[0]);
                if (property != null)
                {
                    if (nameAndValue.length == 1)
                    {
                        state = state.cycle(property);
                    }
                    else
                    {
                        state = property.parseValue(JsonOps.INSTANCE, state, JsonParser.parseString(nameAndValue[1]))
                                .result().orElse(state);
                    }
                }
            }
        }

        return state;
    }
}
