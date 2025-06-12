package com.ldtteam.common.fakelevel;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
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
    public BlockPos getSpawnPos()
    {
        return BlockPos.ZERO;
    }

    @Override
    public float getSpawnAngle()
    {
        return 0;
    }

    @Override
    public long getGameTime()
    {
        return vanillaLevelData.get().getGameTime();
    }

    @Override
    public long getDayTime()
    {
        return lightProvider.forceOwnLightLevel() ? lightProvider.getDayTime() : vanillaLevelData.get().getDayTime();
    }

    @Override
    public boolean isThundering()
    {
        return false;
    }

    @Override
    public boolean isRaining()
    {
        return false;
    }

    @Override
    public void setRaining(final boolean p_78171_)
    {
        // Noop
    }

    @Override
    public boolean isHardcore()
    {
        return false;
    }

    @Override
    public GameRules getGameRules()
    {
        return vanillaLevelData.get().getGameRules();
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
    public void setSpawn(final BlockPos pos, final float angle)
    {
        // Noop        
    }
}
