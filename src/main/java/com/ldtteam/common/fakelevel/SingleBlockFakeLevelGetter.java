package com.ldtteam.common.fakelevel;

import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;

/**
 * Simple implementation of {@link IFakeLevelBlockGetter} mostly for usage in methods where {@link Level} is needed for virtual
 * BE/entities etc.
 */
public class SingleBlockFakeLevelGetter implements IFakeLevelBlockGetter
{
    public BlockState blockState = null;
    public BlockEntity blockEntity = null;

    @Override
    public BlockEntity getBlockEntity(final BlockPos pos)
    {
        return blockEntity;
    }

    @Override
    public BlockState getBlockState(final BlockPos pos)
    {
        return blockState;
    }

    @Override
    public int getHeight()
    {
        return 1;
    }

    @Override
    public int getSizeX()
    {
        return 1;
    }

    @Override
    public int getSizeZ()
    {
        return 1;
    }

    @Override
    public void describeSelfInCrashReport(final CrashReportCategory category)
    {
        category.setDetail("Single block", blockState::toString);
        category.setDetail("Single block entity type",
            () -> blockEntity == null ? null : BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType()).toString());
    }

    /**
     * Creates simple fakeLevel instance
     * 
     * @param realLevel actual valid vanilla instance to provide eg. registries
     * @return new fakeLevel instance
     */
    public static FakeLevel<SingleBlockFakeLevelGetter> createSimpleInstance(final Level realLevel)
    {
        return new FakeLevel<>(new SingleBlockFakeLevelGetter(), IFakeLevelLightProvider.USE_CLIENT_LEVEL, realLevel, null, true);
    }

    /**
     * Do not forget to unset to prevent potential memory leaks
     *
     * @param blockState  related to blockEntity
     * @param blockEntity related to blockState
     * @param realLevel   actual valid vanilla instance to provide eg. registries
     * @return prepared {@link FakeLevel} instance
     * @see #unset(FakeLevel, BlockEntity)
     * @see FakeLevel#setEntities(Collection) FakeLevel#setEntities(Collection) if you want to add entities, do not forget to reset
     */
    public static void prepare(final FakeLevel<SingleBlockFakeLevelGetter> fakeLevel,
        final BlockState blockState,
        @Nullable final BlockEntity blockEntity,
        final Level realLevel)
    {
        fakeLevel.getLevelSource().blockEntity = blockEntity;
        fakeLevel.getLevelSource().blockState = blockState;
        fakeLevel.setRealLevel(realLevel);

        if (blockEntity != null)
        {
            blockEntity.setLevel(fakeLevel);
        }
    }

    /**
     * @param blockEntity to unlink level if needed
     * @see #prepare(FakeLevel, BlockState, BlockEntity, Level)
     */
    public static void unset(final FakeLevel<SingleBlockFakeLevelGetter> fakeLevel, @Nullable final BlockEntity blockEntity)
    {
        fakeLevel.getLevelSource().blockEntity = null;
        fakeLevel.getLevelSource().blockState = null;
        fakeLevel.setRealLevel(null);

        if (blockEntity != null)
        {
            blockEntity.setLevel(null);
        }
    }
}
