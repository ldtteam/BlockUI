package com.ldtteam.blockui.mod.config;

import com.ldtteam.blockui.mod.BlockUI;
import com.ldtteam.common.config.AbstractConfiguration;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;

/**
 * Client-side BlockUI configuration.
 */
public class BlockUIClientConfiguration extends AbstractConfiguration
{
    public final BooleanValue highContrastCount;

    public BlockUIClientConfiguration(final Builder builder)
    {
        super(builder, BlockUI.MOD_ID);

        createCategory("visuals");
        highContrastCount = defineBoolean("highContrastCount", false);
        finishCategory();
    }
}
