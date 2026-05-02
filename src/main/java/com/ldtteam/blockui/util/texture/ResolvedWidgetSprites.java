package com.ldtteam.blockui.util.texture;

import com.ldtteam.blockui.UiRenderMacros.ResolvedBlit;
import com.ldtteam.blockui.util.color.ColourQuartet4f;
import com.ldtteam.blockui.util.color.IColour;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.resources.Identifier;
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
    //public static IColour FOCUSED_MODULATOR = new ColourQuartet4f(1.1f, 1.1f, 1.1f, 1.0f);
    // TODO: cannot do more than byte max (ie. 255), need shader and buffer support to use this
    public static IColour FOCUSED_MODULATOR = new ColourQuartet4f(1.0f, 1.0f, 1.0f, 1.0f);
    public static IColour NORMAL_MODULATOR = new ColourQuartet4f(0.91f, 0.91f, 0.91f, 1.0f);
    public static IColour DISABLED_MODULATOR = new ColourQuartet4f(0.5f, 0.5f, 0.5f, 1.0f);

    /**
     * @return resolve given sprites using given resolver
     */
    public static ResolvedWidgetSprites fromUnresolved(final WidgetSprites widgetSprites,
        final Function<Identifier, ResolvedBlit> resolver)
    {
        final Map<Identifier, ResolvedBlit> resolved = new HashMap<>();
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
     */
    public ResolvedBlit getAndPrepare(final boolean isEnabled, final boolean isFocused)
    {
        if (isEnabled)
        {
            if (isFocused)
            {
                return ifSameBlitModulateColor(enabled, enabledFocused, FOCUSED_MODULATOR);
            }
            else
            {
                return enabled.withColorModulation(NORMAL_MODULATOR);
            }
        }
        else
        {
            if (isFocused)
            {
                return ifSameBlitModulateColor(enabled, disabledFocused, DISABLED_MODULATOR);
            }
            else
            {
                return ifSameBlitModulateColor(enabled, disabled, DISABLED_MODULATOR);
            }
        }
    }

    private static ResolvedBlit ifSameBlitModulateColor(final ResolvedBlit test, final ResolvedBlit compared, final IColour modulator)
    {
        return compared == test ? compared.withColorModulation(modulator) : compared;
    }
}
