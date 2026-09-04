package com.ldtteam.common.fakelevel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate.Sampler;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.material.FluidState;
import java.util.function.Predicate;

/**
 * Porting: class is relatively small, just check super class manually (all of missing methods are/were just aliases)
 */
public class FakeLevelChunkSection extends LevelChunkSection
{
    private static final int SECTION_WIDTH = 16;
    private static final int SECTION_HEIGHT = 16;

    private final FakeLevel<?> fakeLevel;
    private final int yIdx;
    private final ChunkPos chunkPos;

    /**
     * @param fakeLevel parent chunk
     * @param yIdx yLevel in chunk, multiply by section height
     */
    public FakeLevelChunkSection(final FakeLevel<?> fakeLevel, final int yIdx, final ChunkPos chunkPos)
    {
        super(null, null);
        this.fakeLevel = fakeLevel;
        this.yIdx = yIdx;
        this.chunkPos = chunkPos;
    }

    private BlockPos formGlobalPos(int x, int y, int z)
    {
        return new BlockPos(x + chunkPos.x() * SECTION_WIDTH, y + yIdx * SECTION_HEIGHT, z  + chunkPos.z() * SECTION_WIDTH);
    }

    @Override
    public BlockState setBlockState(int x, int y, int z, BlockState p_62995_, boolean p_62996_)
    {
        // this should return old value, but we don't allow changes
        return getBlockState(x, y, z);
    }

    @Override
    public BlockState getBlockState(int x, int y, int z)
    {
        return fakeLevel.getBlockState(formGlobalPos(x, y, z));
    }

    @Override
    public FluidState getFluidState(int x, int y, int z)
    {
        return fakeLevel.getFluidState(formGlobalPos(x, y, z));
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z)
    {
        return fakeLevel.getNoiseBiome(chunkPos.x(), yIdx * SECTION_HEIGHT, chunkPos.z());
    }

    @Override
    public PalettedContainerRO<Holder<Biome>> getBiomes()
    {
        // TODO: need our own? use clientLevel?
        return null;
    }

    @Override
    public PalettedContainer<BlockState> getStates()
    {
        // TODO: need our own
        return null;
    }

    @Override
    public boolean hasOnlyAir()
    {
        return false;
    }

    @Override
    public boolean isRandomlyTicking()
    {
        return false;
    }

    @Override
    public boolean isRandomlyTickingBlocks()
    {
        return false;
    }

    @Override
    public boolean isRandomlyTickingFluids()
    {
        return false;
    }

    @Override
    public boolean maybeHas(Predicate<BlockState> p_63003_)
    {
        // hard to say, so maybe yes
        return true;
    }

    @Override
    public int getSerializedSize()
    {
        // technically noop, vanilla uses it for network sync
        return 0;
    }

    @Override
    public void read(FriendlyByteBuf p_63005_)
    {
        // Noop
    }

    @Override
    public void readBiomes(FriendlyByteBuf p_275669_)
    {
        // Noop
    }

    @Override
    public void recalcBlockCounts()
    {
        // Noop
    }

    @Override
    public void release()
    {
        // Noop
    }

    @Override
    public void acquire()
    {
        // Noop
    }

    @Override
    public void fillBiomesFromNoise(BiomeResolver p_282075_, Sampler p_283084_, int p_282310_, int p_281510_, int p_283057_)
    {
        // Noop
    }

    @Override
    public void write(FriendlyByteBuf p_63012_)
    {
        // Noop
    }

    /*
    @Override
    public BlockState setBlockState(int p_62987_, int p_62988_, int p_62989_, BlockState p_62990_)
    {
        return super.setBlockState(p_62987_, p_62988_, p_62989_, p_62990_);
    }
    */
}
