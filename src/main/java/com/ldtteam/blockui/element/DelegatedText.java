package com.ldtteam.blockui.element;

import com.ldtteam.blockui.AbstractPane;
import com.ldtteam.blockui.AbstractPaneGroup;
import com.ldtteam.blockui.attribute.Colors;
import com.ldtteam.blockui.attribute.RelativeAlignment;
import com.ldtteam.blockui.context.RenderContext;
import com.ldtteam.blockui.element.shadow.AbstractDelegatedPane;
import com.ldtteam.blockui.mod.BlockUI;
import com.ldtteam.blockui.parse.Parser;
import com.ldtteam.blockui.parse.ParsingCodec;
import com.ldtteam.blockui.util.Crash;
import com.ldtteam.blockui.util.text.EllipsisFormattedCharSequence;
import com.ldtteam.blockui.util.text.SpacerTextComponent;
import com.ldtteam.blockui.util.text.SpacerTextComponent.FormattedSpacerComponent;
import com.ldtteam.blockui.util.text.ToggleableTextComponent;
import com.ldtteam.blockui.util.text.ToggleableTextComponent.FormattedToggleableCharSequence;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import org.slf4j.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class DelegatedText extends AbstractDelegatedPane
{
    public static final String ID = "text";
    public static final ParsingCodec<DelegatedText> CODEC = ParsingCodec.forDelegated(ID, DelegatedText::new)
        .field("scale", Parser.FLOAT, DelegatedText::setScale)
        .field("textAlign", Parser.ALIGNMENT, DelegatedText::setTextAlignment)
        .field("shadow", Parser.BOOL, DelegatedText::setShadow)
        .field("linespace", Parser.INT, DelegatedText::setLinespace)
        .bifieldS("textColor",
            "hoverColor",
            "color",
            Parser.COLOR,
            () -> Colors.byString("white"),
            () -> Colors.byString("white"),
            (p, col, hoverCol) -> {
                p.setDefaultColor(col);
                p.setDefaultHoverColor(hoverCol);
            })
        .field("key", Parser.TRANSLATION, DelegatedText::setText);

    private static final Logger LOG = BlockUI.getLogger();
    protected List<MutableComponent> text = Collections.emptyList();

    protected float scale = 1.0f;
    protected RelativeAlignment textAlignment = RelativeAlignment.TOP_LEFT;
    protected boolean shadow = false;
    protected int linespace = 0;
    protected int defaultColor = Colors.byString("white");
    protected int defaultHoverColor = defaultColor;

    // rendering
    protected List<FormattedCharSequence> preparedText = null;
    protected int renderedTextWidth = 0;
    protected int renderedTextHeight = 0;

    public DelegatedText(final AbstractPaneGroup parent)
    {
        super(parent);
    }

    public List<MutableComponent> getText()
    {
        return text;
    }

    public void setText(final MutableComponent text)
    {
        setText(List.of(text));
    }

    public void setText(final List<MutableComponent> text)
    {
        for (final MutableComponent line : Objects.requireNonNull(text, "Use visible(false) instead of null text"))
        {
            Objects.requireNonNull(line, "Lines must be not null");
        }
        if (text.stream().allMatch(line -> line.getString().isBlank()))
        {
            LOG.warn("Trying to display empty text?!");
            Crash.argumentIfDev("Text is completely empty?!");
        }
        this.text = text;
        onTextChange(TextChangeReason.TEXT);
    }

    public float getScale()
    {
        return scale;
    }

    /**
     * @param scale text scale, ideally integer
     */
    public void setScale(final float scale)
    {
        this.scale = scale;
        onTextChange(TextChangeReason.SCALE);
    }

    public RelativeAlignment getTextAlignment()
    {
        return textAlignment;
    }

    /**
     * @param textAlignment text alignment inside this pane
     */
    public void setTextAlignment(final RelativeAlignment textAlignment)
    {
        this.textAlignment = textAlignment;
        onTextChange(TextChangeReason.ALIGNMENT);
    }

    public boolean isShadow()
    {
        return shadow;
    }

    /**
     * @param shadow true if should render with shadow
     */
    public void setShadow(final boolean shadow)
    {
        this.shadow = shadow;
        onTextChange(TextChangeReason.SHADOW);
    }

    public int getLinespace()
    {
        return linespace;
    }

    /**
     * @param linespace how many additional pixels should be added between each two lines
     */
    public void setLinespace(final int linespace)
    {
        this.linespace = linespace;
        onTextChange(TextChangeReason.LINESPACE);
    }

    public int getDefaultColor()
    {
        return defaultColor;
    }

    /**
     * @param defaultColorArgb default text color, used when text component has no style
     */
    public void setDefaultColor(final int defaultColorArgb)
    {
        this.defaultColor = defaultColorArgb;
        onTextChange(TextChangeReason.COLORS);
    }

    public int getDefaultHoverColor()
    {
        return defaultHoverColor;
    }

    /**
     * @param defaultHoverColorArgb default HOVER text color, used when text component has no style
     */
    public void setDefaultHoverColor(final int defaultHoverColorArgb)
    {
        this.defaultHoverColor = defaultHoverColorArgb;
        onTextChange(TextChangeReason.COLORS);
    }

    /**
     * @param defaultColorArgb used when text component has no style
     * @see                    #setDefaultColor(int)
     * @see                    #setDefaultHoverColor(int)
     */
    public void setDefaultColors(final int defaultColorArgb)
    {
        this.defaultColor = defaultColorArgb;
        this.defaultHoverColor = defaultColorArgb;
        onTextChange(TextChangeReason.COLORS);
    }

    /**
     * ALWAYS CALL SUPER if overriding!
     * 
     * @param reason what method caused this event
     */
    protected void onTextChange(final TextChangeReason reason)
    {
        if (reason.preparedTextInvalidated)
        {
            preparedText = null;
        }
    }

    @Override
    protected void onAABBchange(final AABBchangeReason reason)
    {
        super.onAABBchange(reason);
        if (reason == AABBchangeReason.SIZE)
        {
            onTextChange(TextChangeReason.PANE_SIZE);
        }
    }

    @Override
    protected void drawSelf(final RenderContext ctx, final double mx, final double my)
    {
        if (ctx.resourceReloaded || preparedText == null)
        {
            recalcPreparedText(ctx.mc.font);
        }
        drawSelf(compileRenderedText(ctx.mc.font), ctx, mx, my);
    }

    protected void drawSelf(final CompiledText compiledText, final RenderContext ctx, final double mx, final double my)
    {
        { // translate to rendering window
            int x = alignedX;
            switch (textAlignment.horizontal)
            {
                case LEFT ->
                    {}
                case MIDDLE -> x += (width - renderedTextWidth) / 2;
                case RIGHT -> x += width - renderedTextWidth;
            }

            int y = alignedY;
            switch (textAlignment.vertical)
            {
                case TOP ->
                    {}
                case MIDDLE -> y += (height - renderedTextHeight) / 2;
                case BOTTOM -> y += height - renderedTextHeight;
            }

            ctx.pushTranslate(x, y);
        }

        final boolean mustDisableFiltering = ctx.applyTextScale(scale);

        ctx.drawStringsInBatch(shadow,
            wasCursorInPane ? defaultHoverColor : defaultColor,
            RenderContext.NO_TEXT_BACKGROUND,
            DisplayMode.NORMAL,
            sink -> {
                int y = 0;
                for (int i = 0; i < compiledText.size(); i++)
                {
                    final FormattedCharSequence line = compiledText.lines.get(i);

                    if (line == FormattedCharSequence.EMPTY)
                    {
                        y += ctx.mc.font.lineHeight + linespace;
                        continue;
                    }
                    if (line instanceof final FormattedSpacerComponent spacer)
                    {
                        y += spacer.pixelHeight() + linespace;
                        continue;
                    }

                    final int x = switch (textAlignment.horizontal)
                    {
                        case LEFT -> 0;
                        case MIDDLE -> (int) ((renderedTextWidth - compiledText.lineWidths[i] * scale) / 2 / scale);
                        case RIGHT -> (int) ((renderedTextWidth - compiledText.lineWidths[i] * scale) / scale);
                    };

                    sink.render(line, x, y);
                    y += ctx.mc.font.lineHeight + linespace;
                }
            });

        if (mustDisableFiltering)
        {
            ctx.disableTextFiltering();
        }
        ctx.pop();
    }

    protected void recalcPreparedText(final Font font)
    {
        final int maxWidth = (int) (width / scale) - (shadow ? 1 : 0);
        preparedText = text.stream().flatMap(line -> {
            if (line.getContents() instanceof final SpacerTextComponent spacer)
            {
                return Stream.of(spacer.getVisualOrderText());
            }
            else if (line.getContents() instanceof final ToggleableTextComponent toggleable)
            {
                return font.split(toggleable.data(), maxWidth)
                    .stream()
                    .map(formatted -> new FormattedToggleableCharSequence(toggleable.condition(), formatted));
            }
            else if (line.getContents() == ComponentContents.EMPTY && line.getSiblings().isEmpty())
            {
                return Stream.of(FormattedCharSequence.EMPTY);
            }
            else
            {
                return font.split(line, maxWidth).stream();
            }
        }).toList();
    }

    protected CompiledText compileRenderedText(final Font font)
    {
        if (preparedText == null)
        {
            throw Crash.state("Forgot to call #recalcPreparedText(Font) ?");
        }

        // + Math.ceil(textScale) / textScale is to negate last pixel of vanilla font rendering
        final int maxHeight = (int) (height / scale) + 1;
        final int lineHeight = font.lineHeight + linespace;
        final int maxLineCount = maxHeight / lineHeight;

        final CompiledText result = new CompiledText(maxLineCount);

        int prepTextIdx = 0;
        int heightSum = 0;
        int widthMax = 0;
        // till can add && have sth to add && enough height
        while (result.size() < maxLineCount && prepTextIdx < preparedText.size() && heightSum + lineHeight <= maxHeight)
        {
            final FormattedCharSequence line = preparedText.get(prepTextIdx++);

            if (line instanceof final FormattedSpacerComponent spacer)
            {
                if (heightSum + spacer.pixelHeight() + linespace >= maxHeight)
                {
                    break; // spacer would cause height overflow
                }

                heightSum += spacer.pixelHeight() + linespace;
                result.lines.add(line);
                continue; // skip line post-process
            }
            else if (line instanceof final FormattedToggleableCharSequence toggleable)
            {
                if (toggleable.condition().getAsBoolean())
                {
                    result.lines.add(toggleable.data());
                }
                else
                {
                    continue; // nothing added -> next
                }
            }
            else
            {
                result.lines.add(line);
            }

            heightSum += lineHeight;

            final int idx = result.size() - 1;
            final int w = font.width(result.lines.get(idx));

            result.lineWidths[idx] = w;
            if (widthMax < w)
            {
                widthMax = w;
            }
        }

        // add elipsis since not everything is being rendered
        // TODO: or auto scrolling based on time?
        if (prepTextIdx < preparedText.size())
        {
            final int lastIdx = result.size() - 1;
            result.lines.set(lastIdx, new EllipsisFormattedCharSequence(result.lines.get(lastIdx), font, widthMax));
        }

        renderedTextWidth = (int) Math.ceil(widthMax * scale);
        renderedTextHeight = (int) Math.ceil((heightSum - 1 - linespace) * scale);
        return result;
    }

    public static class Text extends DelegatedText implements RenderSetters, AccessibleId
    {
        public static final ParsingCodec<Text> CODEC =
            ParsingCodec.of(ID, Text::new, AbstractPane.CODEC).copyFieldsFrom(DelegatedText.CODEC);

        public Text(final AbstractPaneGroup parent)
        {
            super(parent);
        }
    }

    protected record CompiledText(List<FormattedCharSequence> lines, int[] lineWidths)
    {
        protected CompiledText(final int maxSize)
        {
            this(new ArrayList<>(maxSize), new int[maxSize]);
        }

        protected int size()
        {
            return lines.size();
        }
    }

    protected enum TextChangeReason
    {
        PANE_SIZE(true),
        TEXT(true),
        SCALE(true),
        ALIGNMENT(false),
        SHADOW(true),
        LINESPACE(false),
        COLORS(false);

        final boolean preparedTextInvalidated;

        private TextChangeReason(final boolean preparedTextInvalidated)
        {
            this.preparedTextInvalidated = preparedTextInvalidated;
        }
    }
}
