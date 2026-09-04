package com.ldtteam.common.fakelevel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import java.util.function.Supplier;

/**
 * Porting: class is relatively small, just check super class manually (all of missing methods are/were just aliases)
 */
public class FakeLevelData implements WritableLevelData
{
    protected Supplier<LevelData> vanillaLevelData;
    protected final IFakeLevelLightProvider lightProvider;

    protected FakeLevelData(final Supplier<LevelData> vanillaLevelData, final IFakeLevelLightProvider lightProvider)
    {
        this.vanillaLevelData = vanillaLevelData;
        this.lightProvider = lightProvider;
    }

    @Override
    public LevelData.RespawnData getRespawnData()
    {
        return new RespawnData(new GlobalPos(vanillaLevelData.get().getRespawnData().dimension(), BlockPos.ZERO), 0, 0);
    }

    @Override
    public long getGameTime()
    {
        return vanillaLevelData.get().getGameTime();
    }

    @Override
    public boolean isHardcore()
    {
        return false;
    }

    @Override
    public Difficulty getDifficulty()
    {
        // would like peaceful but dont want to trigger entity remove in case someone actually manage to tick fake level
        return Difficulty.EASY;
    }

    @Override
    public boolean isDifficultyLocked()
    {
        return true;
    }

    @Override
    public void setSpawn(final LevelData.RespawnData respawnData)
    {
        // Noop
    }
}
