package com.ldtteam.blockui;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Carries slot content and an optional position override during layout expansion.
 * Each slot name maps to exactly ONE PaneParams (slots are unique).
 * {@code null} slot name in the caller is normalized to {@link #DEFAULT_SLOT_KEY}.
 * {@code posOverride} is non-null when the use-site {@code <layout>} tag specifies a {@code pos}, {@code x}, or {@code y} attribute,
 * in which case the layout file's single root child has its position replaced with these values.
 */
public record LayoutContext(Map<String, PaneParams> slotContent, @Nullable int[] posOverride)
{
    public static final LayoutContext EMPTY = new LayoutContext(Map.of(), null);

    static final String DEFAULT_SLOT_KEY = "default";

    @Nullable
    public PaneParams get(final String slotName)
    {
        return slotContent.get(slotName);
    }

    public boolean hasPosOverride()
    {
        return posOverride != null;
    }
}