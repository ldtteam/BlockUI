package com.ldtteam.common.fakelevel;

import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData.BlockEntityTagOutput;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate.Sampler;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.TickContainerAccess;
import net.neoforged.neoforge.model.data.ModelData;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Fake level fake chunk :D all data related methods must redirect to fake level Updating procedure is same as fakeLevel Porting info:
 * <ol>
 * <li>uncomment last method section</li>
 * <li>fix compile errors</li>
 * <li>add override for remaining methods and sort/implement them accordingly</li>
 * <li>comment last method section</li>
 * </ol>
 * <p>
 */
public class FakeChunk extends LevelChunk
{
    private final FakeLevel<?> fakeLevel;

    public static FakeChunk create(final FakeLevel<?> fakeLevel, final int x, final int z)
    {
        // 26.1 porting notes - we need to create this ourselves or we will get vanilla sections

        final ChunkPos chunkPos = new ChunkPos(x, z);
        final LevelChunkSection[] sections = new LevelChunkSection[fakeLevel.getSectionsCount()];
        for (int i = 0; i < sections.length; i++)
        {
            sections[i] = new FakeLevelChunkSection(fakeLevel, i, chunkPos);
        }

        final FakeChunk chunk = new FakeChunk(fakeLevel, new ChunkPos(x, z), sections);

        // set itself to cache
        fakeLevel.lastX = x;
        fakeLevel.lastZ = z;
        fakeLevel.lastChunk = chunk;

        return chunk;
    }

    private FakeChunk(final FakeLevel<?> fakeLevel, final ChunkPos pos, final LevelChunkSection[] sections)
    {
        super(fakeLevel, pos, UpgradeData.EMPTY, new LevelChunkTicks<>(), new LevelChunkTicks<>(), 0L, sections, null, null);
        this.fakeLevel = fakeLevel;
    }

    // ========================================
    // ========== REDIRECTED METHODS ==========
    // ========================================

    @Override
    public BlockState getBlockState(final BlockPos pos)
    {
        return fakeLevel.getBlockState(pos);
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(final BlockPos pos, final EntityCreationType creationMode)
    {
        return fakeLevel.getBlockEntity(pos);
    }

    @Override
    public FluidState getFluidState(final BlockPos pos)
    {
        return fakeLevel.getFluidState(pos);
    }

    @Override
    public FluidState getFluidState(final int bx, final int by, final int bz)
    {
        return getFluidState(new BlockPos(bx, by, bz));
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z)
    {
        return fakeLevel.getNoiseBiome(x, y, z);
    }

    @Override
    public Map<BlockPos, BlockEntity> getBlockEntities()
    {
        // TODO: this should ideally return only BEs in this chunk
        return fakeLevel.blockEntities;
    }

    @Override
    public Set<BlockPos> getBlockEntitiesPos()
    {
        return getBlockEntities().keySet();
    }

    @Override
    public ModelData getModelData(BlockPos pos)
    {
        return fakeLevel.getModelData(pos);
    }

    // ========================================
    // ======= NOOP UNSAFE NULL METHODS =======
    // ========================================

    // ========================================
    // ========== PERMANENT SETTINGS ==========
    // ========================================

    @Override
    public FullChunkStatus getFullStatus()
    {
        return FullChunkStatus.FULL;
    }

    @Override
    public ChunkStatus getPersistedStatus()
    {
        return ChunkStatus.FULL;
    }

    @Override
    public boolean isUnsaved()
    {
        return false;
    }

    @Override
    public boolean isUpgrading()
    {
        return false;
    }

    @Override
    public boolean isLightCorrect()
    {
        return true;
    }

    @Override
    public boolean canBeSerialized()
    {
        return false;
    }

    // ========================================
    // ========== HEIGHTMAP RELATED ===========
    // ========================================

    @Override
    public int getHeight(Types type, int x, int z)
    {
        return fakeLevel.getHeight(type, chunkPos.getBlockX(x), chunkPos.getBlockZ(z));
    }

    @Override
    public Collection<Entry<Types, Heightmap>> getHeightmaps()
    {
        // TODO: investigate..
        return Collections.emptyList();
    }

    @Override
    public Heightmap getOrCreateHeightmapUnprimed(Types p_62079_)
    {
        return null;
    }

    @Override
    public boolean hasPrimedHeightmap(Types p_187659_)
    {
        return false;
    }

    // ========================================
    // ============= NOOP METHODS =============
    // ========================================

    @Override
    public void addAndRegisterBlockEntity(BlockEntity p_156391_)
    {
        // Noop
    }

    @Override
    public TickContainerAccess<Block> getBlockTicks()
    {
        // Noop
        return BlackholeTickAccess.emptyContainer();
    }

    @Override
    public TickContainerAccess<Fluid> getFluidTicks()
    {
        // Noop
        return BlackholeTickAccess.emptyContainer();
    }

    @Override
    public void postProcessGeneration(ServerLevel level)
    {
        // Noop
    }

    @Override
    public void registerAllBlockEntitiesAfterLevelLoad()
    {
        // Noop
    }

    @Override
    public void removeBlockEntity(BlockPos p_62919_)
    {
        // Noop
    }

    @Override
    public void replaceBiomes(FriendlyByteBuf p_275574_)
    {
        // Noop
    }

    @Override
    public void replaceWithPacketData(FriendlyByteBuf p_187972_, Map<Types, long[]> p_187973_, Consumer<BlockEntityTagOutput> p_187974_)
    {
        // Noop
    }

    @Override
    public void setBlockEntity(BlockEntity p_156374_)
    {
        // Noop
    }

    @Override
    @javax.annotation.Nullable
    public BlockState setBlockState(BlockPos p_62865_, BlockState p_62866_, @Block.UpdateFlags int p_62867_)
    {
        // Noop
        return null;
    }

    @Override
    public void setFullStatus(Supplier<FullChunkStatus> p_62880_)
    {
        // Noop
    }

    @Override
    public void unpackTicks(long p_187986_)
    {
        // Noop
    }

    @Override
    public void addReferenceForStructure(Structure p_223007_, long p_223008_)
    {
        // Noop
    }

    @Override
    public void fillBiomesFromNoise(BiomeResolver p_187638_, Sampler p_187639_)
    {
        // Noop
    }

    @Override
    @javax.annotation.Nullable
    public CompoundTag getBlockEntityNbt(BlockPos p_62103_)
    {
        // Noop, for pending BEs only
        return null;
    }

    @Override
    public void setAllReferences(Map<Structure, LongSet> p_187663_)
    {
        // Noop
    }

    @Override
    public void setAllStarts(Map<Structure, StructureStart> p_62090_)
    {
        // Noop
    }

    @Override
    public void setBlockEntityNbt(CompoundTag p_62091_)
    {
        // Noop
    }

    @Override
    public void setLightCorrect(boolean p_62100_)
    {
        // Noop
    }

    @Override
    public void setStartForStructure(Structure p_223010_, StructureStart p_223011_)
    {
        // Noop
    }

    @Override
    public void setHeightmap(Types p_62083_, long[] p_62084_)
    {
        // Noop
    }

    @Override
    public void markUnsaved()
    {
        // Noop
    }

    @Override
    public void setUnsavedListener(UnsavedListener unsavedListener)
    {
        // Noop
    }

    @Override
    public void addPackedPostProcess(ShortList packedOffsets, int sectionIndex)
    {
        // Noop
    }

    // ========================================
    // ======== SUPER IS FINE METHODS =========
    // ========================================

    /*
    @Override
    public Level getLevel()
    {
        return super.getLevel();
    }

    @Override
    public void addEntity(Entity p_62826_)
    {
        super.addEntity(p_62826_);
    }

    @Override
    public void clearAllBlockEntities()
    {
        super.clearAllBlockEntities();
    }

    @Override
    @javax.annotation.Nullable
    public BlockEntity getBlockEntity(BlockPos p_62912_)
    {
        return super.getBlockEntity(p_62912_);
    }

    @Override
    public GameEventListenerRegistry getListenerRegistry(int p_251193_)
    {
        return super.getListenerRegistry(p_251193_);
    }

    @Override
    public PackedTicks getTicksForSerialization(long currentTick)
    {
        return super.getTicksForSerialization(currentTick);
    }

    @Override
    public boolean isEmpty()
    {
        return super.isEmpty();
    }

    @Override
    public void registerTickContainerInLevel(ServerLevel p_187959_)
    {
        super.registerTickContainerInLevel(p_187959_);
    }

    @Override
    public void runPostLoad()
    {
        super.runPostLoad();
    }

    @Override
    public void setLoaded(boolean p_62914_)
    {
        super.setLoaded(p_62914_);
    }

    @Override
    public void unregisterTickContainerFromLevel(ServerLevel p_187980_)
    {
        super.unregisterTickContainerFromLevel(p_187980_);
    }

    @Override
    public BiomeGenerationSettings carverBiome(Supplier<BiomeGenerationSettings> p_223015_)
    {
        return super.carverBiome(p_223015_);
    }

    @Override
    public void findBlocks(Predicate<BlockState> p_285343_, BiConsumer<BlockPos, BlockState> p_285030_)
    {
        super.findBlocks(p_285343_, p_285030_);
    }

    @Override
    public Map<Structure, LongSet> getAllReferences()
    {
        return super.getAllReferences();
    }

    @Override
    public Map<Structure, StructureStart> getAllStarts()
    {
        return super.getAllStarts();
    }

    @Override
    @javax.annotation.Nullable
    public BelowZeroRetrogen getBelowZeroRetrogen()
    {
        return super.getBelowZeroRetrogen();
    }

    @Override
    @javax.annotation.Nullable
    public BlendingData getBlendingData()
    {
        return super.getBlendingData();
    }

    @Override
    public int getHeight()
    {
        return super.getHeight();
    }

    @Override
    public LevelHeightAccessor getHeightAccessorForGeneration()
    {
        return super.getHeightAccessorForGeneration();
    }

    @Override
    public int getHighestFilledSectionIndex()
    {
        return super.getHighestFilledSectionIndex();
    }

    @Override
    public ChunkStatus getHighestGeneratedStatus()
    {
        return super.getHighestGeneratedStatus();
    }

    @Override
    public int getHighestSectionPosition()
    {
        return super.getHighestSectionPosition();
    }

    @Override
    public long getInhabitedTime()
    {
        return super.getInhabitedTime();
    }

    @Override
    public int getMinY()
    {
        return super.getMinY();
    }

    @Override
    public NoiseChunk getOrCreateNoiseChunk(Function<ChunkAccess, NoiseChunk> p_223013_)
    {
        return super.getOrCreateNoiseChunk(p_223013_);
    }

    @Override
    public ChunkPos getPos()
    {
        return super.getPos();
    }

    @Override
    public ShortList[] getPostProcessing()
    {
        return super.getPostProcessing();
    }

    @Override
    public LongSet getReferencesForStructure(Structure p_223017_)
    {
        return super.getReferencesForStructure(p_223017_);
    }

    @Override
    public ChunkSkyLightSources getSkyLightSources()
    {
        return super.getSkyLightSources();
    }

    @Override
    @javax.annotation.Nullable
    public StructureStart getStartForStructure(Structure p_223005_)
    {
        return super.getStartForStructure(p_223005_);
    }

    @Override
    public UpgradeData getUpgradeData()
    {
        return super.getUpgradeData();
    }

    @Override
    public boolean hasAnyStructureReferences()
    {
        return super.hasAnyStructureReferences();
    }

    @Override
    public void incrementInhabitedTime(long p_187633_)
    {
        super.incrementInhabitedTime(p_187633_);
    }

    @Override
    public void initializeLightSources()
    {
        super.initializeLightSources();
    }

    @Override
    public boolean isOldNoiseGeneration()
    {
        return super.isOldNoiseGeneration();
    }

    @Override
    public void markPosForPostprocessing(BlockPos p_62102_)
    {
        super.markPosForPostprocessing(p_62102_);
    }

    @Override
    public void setInhabitedTime(long p_62099_)
    {
        super.setInhabitedTime(p_62099_);
    }

    @Override
    public BlockHitResult clip(ClipContext p_45548_)
    {
        return super.clip(p_45548_);
    }

    @Override
    @javax.annotation.Nullable
    public BlockHitResult clipWithInteractionOverride(Vec3 p_45559_,
        Vec3 p_45560_,
        BlockPos p_45561_,
        VoxelShape p_45562_,
        BlockState p_45563_)
    {
        return super.clipWithInteractionOverride(p_45559_, p_45560_, p_45561_, p_45562_, p_45563_);
    }

    @Override
    public <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos p_151367_, BlockEntityType<T> p_151368_)
    {
        return super.getBlockEntity(p_151367_, p_151368_);
    }

    @Override
    public double getBlockFloorHeight(BlockPos p_45574_)
    {
        return super.getBlockFloorHeight(p_45574_);
    }

    @Override
    public double getBlockFloorHeight(VoxelShape p_45565_, Supplier<VoxelShape> p_45566_)
    {
        return super.getBlockFloorHeight(p_45565_, p_45566_);
    }

    @Override
    public Stream<BlockState> getBlockStates(AABB p_45557_)
    {
        return super.getBlockStates(p_45557_);
    }

    @Override
    public int getLightEmission(BlockPos p_45572_)
    {
        return super.getLightEmission(p_45572_);
    }

    @Override
    public BlockHitResult isBlockInLine(ClipBlockStateContext p_151354_)
    {
        return super.isBlockInLine(p_151354_);
    }

    @Override
    public int getMaxY()
    {
        return super.getMaxY();
    }

    @Override
    public int getMaxSectionY()
    {
        return super.getMaxSectionY();
    }

    @Override
    public int getMinSectionY()
    {
        return super.getMinSectionY();
    }

    @Override
    public int getSectionIndex(int p_151565_)
    {
        return super.getSectionIndex(p_151565_);
    }

    @Override
    public int getSectionIndexFromSectionY(int p_151567_)
    {
        return super.getSectionIndexFromSectionY(p_151567_);
    }

    @Override
    public int getSectionYFromSectionIndex(int p_151569_)
    {
        return super.getSectionYFromSectionIndex(p_151569_);
    }

    @Override
    public int getSectionsCount()
    {
        return super.getSectionsCount();
    }

    @Override
    public boolean isOutsideBuildHeight(BlockPos p_151571_)
    {
        return super.isOutsideBuildHeight(p_151571_);
    }

    @Override
    public boolean isOutsideBuildHeight(int p_151563_)
    {
        return super.isOutsideBuildHeight(p_151563_);
    }

    @Override
    public LevelChunkAuxiliaryLightManager getAuxLightManager(ChunkPos pos)
    {
        return super.getAuxLightManager(pos);
    }

    @Override
    @Nullable
    public AuxiliaryLightManager getAuxLightManager(BlockPos pos)
    {
        return super.getAuxLightManager(pos);
    }

    @Override
    public <T> T getData(Supplier<AttachmentType<T>> type)
    {
        return super.getData(type);
    }

    @Override
    public <T> boolean hasData(Supplier<AttachmentType<T>> type)
    {
        return super.hasData(type);
    }

    @Override
    public <T> @Nullable T setData(Supplier<AttachmentType<T>> type, T data)
    {
        return super.setData(type, data);
    }

    @Override
    public <T> T getData(AttachmentType<T> type)
    {
        return super.getData(type);
    }

    @Override
    public boolean hasData(AttachmentType<?> type)
    {
        return super.hasData(type);
    }

    @Override
    public <T> T setData(AttachmentType<T> type, T data)
    {
        return super.setData(type, data);
    }

    @Override
    public <T> Optional<T> getExistingData(AttachmentType<T> type)
    {
        return super.getExistingData(type);
    }

    @Override
    public boolean hasAttachments()
    {
        return super.hasAttachments();
    }

    @Override
    public <T> T removeData(AttachmentType<T> type)
    {
        return super.removeData(type);
    }

    @Override
    public <T> Optional<T> getExistingData(Supplier<AttachmentType<T>> type)
    {
        return super.getExistingData(type);
    }

    @Override
    public <T> @Nullable T removeData(Supplier<AttachmentType<T>> type)
    {
        return super.removeData(type);
    }

    @Override
    public AsField getAttachmentHolder()
    {
        return super.getAttachmentHolder();
    }

    @Override
    public <T> @org.jspecify.annotations.Nullable T getExistingDataOrNull(AttachmentType<T> type)
    {
        return super.getExistingDataOrNull(type);
    }

    @Override
    public <T> @org.jspecify.annotations.Nullable T getExistingDataOrNull(Supplier<AttachmentType<T>> type)
    {
        return super.getExistingDataOrNull(type);
    }

    @Override
    public void syncData(Supplier<? extends AttachmentType<?>> type)
    {
        super.syncData(type);
    }

    @Override
    public CompoundTag getBlockEntityNbtForSaving(BlockPos p_62932_, Provider p_323699_)
    {
        return super.getBlockEntityNbtForSaving(p_62932_, p_323699_);
    }

    @Override
    public LevelChunkSection getSection(int yIdx)
    {
        return super.getSection(yIdx);
    }

    @Override
    public void registerDebugValues(ServerLevel level, Registration registration)
    {
        super.registerDebugValues(level, registration);
    }

    @Override
    public void findBlocks(Predicate<BlockState> predicate,
        BiPredicate<BlockState, BlockPos> fineFilter,
        BiConsumer<BlockPos, BlockState> consumer)
    {
        super.findBlocks(predicate, fineFilter, consumer);
    }

    @Override
    public LevelChunkSection[] getSections()
    {
        return super.getSections();
    }

    @Override
    public boolean isYSpaceEmpty(int yStartInclusive, int yEndInclusive)
    {
        return super.isYSpaceEmpty(yStartInclusive, yEndInclusive);
    }

    @Override
    public PathElement problemPath()
    {
        return super.problemPath();
    }

    @Override
    public @org.jspecify.annotations.Nullable BlockState setBlockState(BlockPos pos, BlockState state)
    {
        return super.setBlockState(pos, state);
    }

    @Override
    public boolean tryMarkSaved()
    {
        return super.tryMarkSaved();
    }

    @Override
    public boolean isInsideBuildHeight(int blockY)
    {
        return super.isInsideBuildHeight(blockY);
    }

    @Override
    public boolean isInsideBuildHeight(BlockPos pos)
    {
        return super.isInsideBuildHeight(pos);
    }
    */
}
