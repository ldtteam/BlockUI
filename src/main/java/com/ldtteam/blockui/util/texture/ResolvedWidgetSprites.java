package com.ldtteam.blockui.util.texture;

import com.ldtteam.blockui.UiRenderMacros.ResolvedBlit;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.resources.ResourceLocation;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Just as {@link WidgetSprites} but resolved
 */
public record ResolvedWidgetSprites(ResolvedBlit enabled,
    ResolvedBlit disabled,
    ResolvedBlit enabledFocused,
    ResolvedBlit disabledFocused)
{
    public static float FOCUSED_MODULATOR = 1.1f;
    public static float DISABLED_MODULATOR = 0.5f;
    public static float DISABLED_FOCUSED_MODULATOR = 0.6f;

    /**
     * @return resolve given sprites using given resolver
     */
    public static ResolvedWidgetSprites fromUnresolved(final WidgetSprites widgetSprites,
        final Function<ResourceLocation, ResolvedBlit> resolver)
    {
        final Map<ResourceLocation, ResolvedBlit> resolved = new HashMap<>();
        final ResolvedBlit defaultEnabledBlit = resolver.apply(Objects.requireNonNull(widgetSprites.enabled(), "Forgot to put null check somewhere?"));
        resolved.put(null, defaultEnabledBlit);
        resolved.put(widgetSprites.enabled(), defaultEnabledBlit);

        return new ResolvedWidgetSprites(defaultEnabledBlit,
            resolved.computeIfAbsent(widgetSprites.disabled(), resolver),
            resolved.computeIfAbsent(widgetSprites.enabledFocused(), resolver),
            resolved.computeIfAbsent(widgetSprites.disabledFocused(), resolver));
    }

    /**
     * @param isEnabled whether element is interactive
     * @param isFocused whether element is hovered/focused
     * @return correct blit and also applies shader color
     * @see RenderSystem#setShaderColor(float, float, float, float)
     */
    public ResolvedBlit getAndPrepare(final boolean isEnabled, final boolean isFocused)
    {
        if (isEnabled)
        {
            if (isFocused)
            {
                ifSameShaderColor(enabled, enabledFocused, FOCUSED_MODULATOR);
                return enabledFocused;
            }
            else
            {
                return enabled;
            }
        }
        else
        {
            if (isFocused)
            {
                ifSameShaderColor(enabled, disabledFocused, DISABLED_MODULATOR);
                return disabledFocused;
            }
            else
            {
                ifSameShaderColor(enabled, disabled, DISABLED_MODULATOR);
                return disabled;
            }
        }
    }

    private static void ifSameShaderColor(final ResolvedBlit a, final ResolvedBlit b, final float rgb)
    {
        if (a == b)
        {
            RenderSystem.setShaderColor(rgb, rgb, rgb, 1.0F);
        }
    }
}
