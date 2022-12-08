package com.ldtteam.blockui.hooks;

import java.util.function.DoubleSupplier;

/**
 * Triggers for opening guis
 */
public sealed class TriggerMechanism
{
    private static final RangeTriggerMechanism DISTANCE_TRIGGER = new RangeTriggerMechanism("dist", () -> 16.0d, 10, 0);
    private static final RayTraceTriggerMechanism RAY_TRACE_TRIGGER = new RayTraceTriggerMechanism("ray_trace", 1, 1);

    protected final String name;
    protected final int tickEveryXTicks;
    protected int priority;

    private TriggerMechanism(final String name, final int tickEveryXTicks, final int priority)
    {
        this.name = name;
        this.tickEveryXTicks = tickEveryXTicks;
        this.priority = priority;
    }

    /**
     * Gui will open once player points crosshair over target thing.
     * If satisfied then overrides any other trigger.
     *
     * @return ray trace trigger
     */
    public static RayTraceTriggerMechanism getRayTrace()
    {
        return RAY_TRACE_TRIGGER;
    }

    /**
     * Gui will open once player is 16 or less blocks close to target thing.
     *
     * @return 16 blocks distance trigger
     */
    public static RangeTriggerMechanism getDistance()
    {
        return DISTANCE_TRIGGER;
    }

    /**
     * Gui will open once player is x or less blocks close to target thing.
     *
     * @param blocks detection radius
     * @return <code>blocks</code> blocks distance trigger
     */
    public static RangeTriggerMechanism getDistance(final double blocks)
    {
        return new RangeTriggerMechanism(DISTANCE_TRIGGER.name, () -> blocks, DISTANCE_TRIGGER.tickEveryXTicks, DISTANCE_TRIGGER.priority);
    }

    /**
     * Overwrite current trigger priority.
     *
     * @param priority new priority
     */
    public void setPriority(final int priority)
    {
        this.priority = priority;
    }

    String getName()
    {
        return name;
    }

    /**
     * Used to lower performance-heavy triggers
     */
    boolean canTick(final long ticks)
    {
        return ticks % tickEveryXTicks == 0;
    }

    boolean isLowerPriority(final TriggerMechanism other)
    {
        return priority < other.priority;
    }

    public static final class RangeTriggerMechanism extends TriggerMechanism
    {
        private final DoubleSupplier rangeSupplier;

        private RangeTriggerMechanism(final String name, final DoubleSupplier rangeSupplier, final int tickEveryXTicks, final int priority)
        {
            super(name, tickEveryXTicks, priority);
            this.rangeSupplier = rangeSupplier;
        }

        public double getSearchRange()
        {
            return rangeSupplier.getAsDouble();
        }
    }

    public static final class RayTraceTriggerMechanism extends TriggerMechanism
    {
        private RayTraceTriggerMechanism(final String name, final int tickEveryXTicks, final int priority)
        {
            super(name, tickEveryXTicks, priority);
        }
    }
}
