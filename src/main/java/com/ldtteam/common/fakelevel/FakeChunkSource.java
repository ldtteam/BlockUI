package com.ldtteam.common.fakelevel;

import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.lighting.LevelLightEngine;
import javax.annotation.Nullable;
import java.util.function.BooleanSupplier;

/**
 * Porting: class is relatively small, just check super class manually (all of missing methods are/were just aliases)
 */
public class FakeChunkSource extends ChunkSource
{
    private final FakeLevel fakeLevel;

    protected FakeChunkSource(final FakeLevel fakeLevel)
    {
        this.fakeLevel = fakeLevel;
    }

    @Override
    public FakeLevel getLevel()
    {
        return fakeLevel;
    }

    @Override
    @Nullable
    public ChunkAccess getChunk(final int x, final int z, final ChunkStatus chunkStatus, final boolean nonNull)
    {
        return fakeLevel.getChunk(x, z, chunkStatus, nonNull);
    }

    @Override
    public void tick(final BooleanSupplier p_202162_, final boolean p_202163_)
    {
        // noop
    }

    @Override
    public String gatherStats()
    {
        return fakeLevel.gatherChunkSourceStats();
    }

    @Override
    public int getLoadedChunksCount()
    {
        final int xCount = (fakeLevel.levelSource.getSizeX() + 15) / 16, zCount = (fakeLevel.levelSource.getSizeZ() + 15) / 16;
        return xCount * zCount;
    }

    @Override
    public LevelLightEngine getLightEngine()
    {
        return fakeLevel.getLightEngine();
    }

    /*
    @Override
    public LevelChunk getChunk(int p_62228_, int p_62229_, boolean p_62230_)
    {
        return super.getChunk(p_62228_, p_62229_, p_62230_);
    }

    @Override
    public LightChunk getChunkForLighting(int p_62241_, int p_62242_)
    {
        return super.getChunkForLighting(p_62241_, p_62242_);
    }

    @Override
    public LevelChunk getChunkNow(int p_62221_, int p_62222_)
    {
        return super.getChunkNow(p_62221_, p_62222_);
    }

    @Override
    public boolean hasChunk(int p_62238_, int p_62239_)
    {
        return super.hasChunk(p_62238_, p_62239_);
    }

    @Override
    public void setSpawnSettings(boolean p_62236_, boolean p_62237_)
    {
        super.setSpawnSettings(p_62236_, p_62237_);
    }

    @Override
    public void updateChunkForced(ChunkPos p_62233_, boolean p_62234_)
    {
        super.updateChunkForced(p_62233_, p_62234_);
    }

    @Override
    public void onLightUpdate(LightLayer p_63021_, SectionPos p_63022_)
    {
        super.onLightUpdate(p_63021_, p_63022_);
    }
    */
}
