package com.ldtteam.common.fakelevel;

import net.minecraft.core.BlockPos;
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
    public static final ThreadLocal<FakeLevel<SingleBlockFakeLevelGetter>> THREAD_LOCAL = new ThreadLocal<>();

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

    private static void prepareThreadLocal(final Level realLevel)
    {
        THREAD_LOCAL.set(new FakeLevel<>(new SingleBlockFakeLevelGetter(), IFakeLevelLightProvider.USE_CLIENT_LEVEL, realLevel, null, true));
    }

    /**
     * Do not forget to unset to prevent potential memory leaks
     *
     * @param state       related to blockEntity
     * @param blockEntity related to blockState
     * @param realLevel   actual valid vanilla instance to provide eg. registries
     * @return prepared {@link FakeLevel} instance
     * @see #unsetThreadLocal()
     * @see #unsetThreadLocal(BlockEntity)
     * @see FakeLevel#setEntities(Collection) FakeLevel#setEntities(Collection) if you want to add entities, do not forget to reset
     */
    public static FakeLevel<SingleBlockFakeLevelGetter> prepareThreadLocal(final BlockState state,
        @Nullable final BlockEntity blockEntity,
        final Level realLevel)
    {
        final FakeLevel<SingleBlockFakeLevelGetter> level = THREAD_LOCAL.get();
        if (level == null)
        {
            prepareThreadLocal(realLevel);
            return prepareThreadLocal(state, blockEntity, realLevel);
        }

        level.getLevelSource().blockEntity = blockEntity;
        level.getLevelSource().blockState = state;
        level.setRealLevel(realLevel);

        if (blockEntity != null)
        {
            blockEntity.setLevel(level);
        }

        return level;
    }

    /**
     * @see #prepareThreadLocal(Level)
     */
    public static void unsetThreadLocal()
    {
        unsetThreadLocal(null);
    }

    /**
     * @param blockEntity to unlink level if needed
     * @see #prepareThreadLocal(Level)
     */
    public static void unsetThreadLocal(@Nullable final BlockEntity blockEntity)
    {
        final FakeLevel<SingleBlockFakeLevelGetter> level = THREAD_LOCAL.get();
        level.getLevelSource().blockEntity = null;
        level.getLevelSource().blockState = null;
        level.setRealLevel(null);

        if (blockEntity != null)
        {
            blockEntity.setLevel(null);
        }
    }
}
