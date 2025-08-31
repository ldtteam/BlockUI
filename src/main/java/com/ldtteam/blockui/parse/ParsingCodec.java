package com.ldtteam.blockui.parse;

import com.ldtteam.blockui.AbstractPane;
import com.ldtteam.blockui.AbstractPaneGroup;
import com.ldtteam.blockui.parse.PaneBiApplier.PaneApplier;
import com.ldtteam.blockui.parse.PaneBiApplier.ShadowPaneAccessor;
import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/*
 * TODO: process in insert order, beware nulls
 */
public class ParsingCodec<T>
{
    @Nullable
    private final ParsingCodec<?> parent;
    @Nullable
    private final Function<AbstractPaneGroup, T> paneFactory;
    @Nullable
    private final String id;

    private final Map<String, Parser<?>> fieldParsers = new HashMap<>();

    private ParsingCodec(final ParsingCodec<?> parent, final Function<AbstractPaneGroup, T> paneFactory, final String id)
    {
        this.parent = parent;
        this.paneFactory = paneFactory;
        this.id = id;
    }

    // abstract
    public static <U extends AbstractPane> ParsingCodec<U> forAbstract()
    {
        return new ParsingCodec<>(null, null, null);
    }

    // abstract + parent
    public static <U extends AbstractPane> ParsingCodec<U> forAbstract(final ParsingCodec<?> parent)
    {
        return new ParsingCodec<>(parent, null, null);
    }

    // normal pane
    public static <U extends AbstractPane> ParsingCodec<U> of(final String id,
        final Function<AbstractPaneGroup, U> paneFactory,
        final ParsingCodec<?> parent)
    {
        return new ParsingCodec<>(parent, paneFactory, id);
    }

    // delegated pane
    public static <U extends AbstractPane> ParsingCodec<U> forDelegated(final String id,
        final Function<AbstractPaneGroup, U> paneFactory)
    {
        return new ParsingCodec<>(null, paneFactory, id);
    }

    public static <U> ParsingCodec<U> forType(final String id, final Supplier<U> factory)
    {
        return new ParsingCodec<>(null, p -> factory.get(), id);
    }

    public <U> ParsingCodec<T> field(final String id, final Parser<U> parser, final PaneApplier<T, U> applier)
    {
        return this;
    }

    /**
     * @param idFirst       first field id
     * @param idSecond      second field id
     * @param idCombined    combined field id, will split using space into first + second
     * @param parser        parsing function
     * @param firstDefault  default value for first field, if null => second null (= if first parsed => second must be present)
     * @param secondDefault default value for second field, must be null if first is null
     * @param applier       pane setter
     */
    public <U> ParsingCodec<T> bifield(final String idFirst,
        final String idSecond,
        final String idCombined,
        final Parser<U> parser,
        final U firstDefault,
        final U secondDefault,
        final PaneBiApplier<T, U> applier)
    {
        return this;
    }

    /**
     * @see #bifield(String, String, String, Parser, Object, Object, PaneBiApplier)
     */
    public <U> ParsingCodec<T> bifieldS(final String idFirst,
        final String idSecond,
        final String idCombined,
        final Parser<U> parser,
        final Supplier<U> firstDefault,
        final Supplier<U> secondDefault,
        final PaneBiApplier<T, U> applier)
    {
        return this;
    }

    public <U> ParsingCodec<T> typeField(final String id,
        final List<ParsingCodec<? extends U>> values,
        final PaneApplier<T, ? extends U> applier)
    {
        // parser is string
        return this;
    }

    public ParsingCodec<T> parseChilds()
    {
        // must process shadow first, more like ignore shadow ids
        return this;
    }

    public <U extends AbstractPane> ParsingCodec<T> shadowSingleChild(final String idSuffix, final PaneApplier<T, U> applier)
    {
        // can be without idsuffix
        // is arbitrary - use codec for normal pane
        return this;
    }

    public <U extends AbstractPane> ParsingCodec<T> shadowElement(final String idSuffix,
        final ParsingCodec<U> codec,
        final ShadowPaneAccessor<T, U> applier)
    {
        return this;
    }

    public ParsingCodec<T> copyFieldsFrom(final ParsingCodec<?> codec)
    {
        return this;
    }

    public ParsingCodec<T> requireNext()
    {
        return this;
    }
}
