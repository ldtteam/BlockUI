package com.ldtteam.blockui.element;

import com.ldtteam.blockui.AbstractPane;
import com.ldtteam.blockui.AbstractPaneGroup;
import com.ldtteam.blockui.attribute.ColorDirection;
import com.ldtteam.blockui.context.RenderContext;
import com.ldtteam.blockui.element.shadow.AbstractDelegatedPane;
import com.ldtteam.blockui.parse.Parser;
import com.ldtteam.blockui.parse.ParsingCodec;
import com.ldtteam.blockui.util.Crash;
import com.ldtteam.blockui.util.Crash.CheckArgument;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.util.FastColor.ARGB32;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DelegatedShape<S extends DelegatedShape.ShapeType> extends AbstractDelegatedPane
{
    public static final List<ParsingCodec<? extends ShapeType>> SHAPE_CODEC_REGISTRY = new ArrayList<>()
    {
        {
            add(LineBox.CODEC);
            add(Rectangle.CODEC);
            add(Line.CODEC);
            add(VanillaTooltip.CODEC);
            add(VanillaTooltip.CODEC2);
        }
    };
    public static final String ID = "shape";
    public static final ParsingCodec<DelegatedShape<ShapeType>> CODEC = ParsingCodec.forDelegated(ID, DelegatedShape::new)
        .bifield("startColor", "endColor", "gradient", Parser.COLOR, null, null, (p, start, end) -> {
            p.setStartColor(start);
            p.setEndColor(end);
        })
        .field("colorDirection", Parser.COLOR_DIRECTION, DelegatedShape::setColorDirection)
        .requireNext()
        .typeField("shape", SHAPE_CODEC_REGISTRY, DelegatedShape::setShape);

    protected S shapeType;
    protected int startColorArgb = 0xff_00_00_00;
    protected int endColorArgb = 0xff_ff_ff_ff;
    protected ColorDirection colorDirection = ColorDirection.PRIMARY_DIAGONAL;

    public DelegatedShape(final AbstractPaneGroup parent)
    {
        super(parent);
    }

    @Override
    protected void drawSelf(final RenderContext ctx, final double mx, final double my)
    {
        if (ARGB32.alpha(startColorArgb) + ARGB32.alpha(endColorArgb) > 0)
        {
            Objects.requireNonNull(shapeType, "Forgot to set shape?");
            shapeType.draw(this, ctx);
        }
    }

    @Override
    protected void onAABBchange(final AABBchangeReason reason)
    {
        super.onAABBchange(reason);
        if (reason == AABBchangeReason.SIZE)
        {
            if (shapeType instanceof final LineWidthProvider ln && 2 * ln.getLineWidth() > Math.min(width, height))
            {
                throw Crash.argument("Cannot set size " + Math.min(width, height) +
                    " = min(pane width, pane height) < 2 * linewidth = " +
                    (2 * ln.getLineWidth()));
            }
        }
    }

    public Rectangle setRectangle()
    {
        return setShapeInternal(Rectangle.INSTANCE).getShape();
    }

    public LineBox setLineBox()
    {
        return setShapeInternal(new LineBox()).getShape();
    }

    public Line setLine()
    {
        return setShapeInternal(new Line()).getShape();
    }

    public void setVanillaTooltip()
    {
        setShapeInternal(VanillaTooltip.INSTANCE);
    }

    public VanillaTooltip setMutableVanillaTooltip()
    {
        return setShapeInternal(new VanillaTooltip()).getShape();
    }

    @SuppressWarnings("unchecked")
    protected <T extends ShapeType, R extends DelegatedShape<T>> R setShapeInternal(final T shapeType)
    {
        this.shapeType = (S) shapeType;
        return (R) this;
    }

    public DelegatedShape<S> setShape(final S shape)
    {
        this.shapeType = shape;
        return this;
    }

    public DelegatedShape<S> setColor(final int colorArgb)
    {
        this.startColorArgb = colorArgb;
        this.endColorArgb = colorArgb;
        return this;
    }

    public DelegatedShape<S> setStartColor(final int startColorArgb)
    {
        this.startColorArgb = startColorArgb;
        return this;
    }

    public DelegatedShape<S> setEndColor(final int endColorArgb)
    {
        this.endColorArgb = endColorArgb;
        return this;
    }

    public void setColorDirection(final ColorDirection colorDirection)
    {
        this.colorDirection = colorDirection;
    }

    public S getShape()
    {
        return shapeType;
    }

    public int getColor()
    {
        CheckArgument.equals(startColorArgb, endColorArgb, "Start != end color, cannot determine which one is the 'main' one");
        return startColorArgb;
    }

    public int getStartColor()
    {
        return startColorArgb;
    }

    public int getEndColor()
    {
        return endColorArgb;
    }

    public ColorDirection getColorDirection()
    {
        return colorDirection;
    }

    protected final boolean hasAlphaChannel()
    {
        return ARGB32.alpha(startColorArgb) + ARGB32.alpha(endColorArgb) < 0xff + 0xff;
    }

    public static class Shape<S extends DelegatedShape.ShapeType> extends DelegatedShape<S> implements RenderSetters, AccessibleId
    {
        public static final ParsingCodec<Shape<ShapeType>> CODEC =
            ParsingCodec.of(ID, Shape::new, AbstractPane.CODEC).copyFieldsFrom(DelegatedShape.CODEC);

        public Shape(final AbstractPaneGroup parent)
        {
            super(parent);
        }

        public static Shape<Rectangle> createRectangle(final AbstractPaneGroup parent)
        {
            final Shape<Rectangle> result = new Shape<>(parent);
            result.setRectangle();
            return result;
        }

        public static Shape<Rectangle> createVanillaLightbox(final AbstractPaneGroup parent)
        {
            final Shape<Rectangle> result = createRectangle(parent);
            result.colorDirection = ColorDirection.VERTICAL;
            result.startColorArgb = 0xc0101010;
            result.endColorArgb = 0xd0101010;
            return result;
        }

        public static Shape<LineBox> createLineBox(final AbstractPaneGroup parent, final int lineWidth)
        {
            final Shape<LineBox> result = new Shape<>(parent);
            result.setLineBox().setLineWidth(lineWidth);
            return result;
        }

        public static Shape<Line> createLine(final AbstractPaneGroup parent, final int lineWidth)
        {
            final Shape<Line> result = new Shape<>(parent);
            result.setLine().setLineWidth(lineWidth);
            return result;
        }

        public static Shape<VanillaTooltip> createVanillaTooltip(final AbstractPaneGroup parent)
        {
            final Shape<VanillaTooltip> result = new Shape<>(parent);
            result.setVanillaTooltip();
            result.startColorArgb = VanillaTooltip.BORDER_COLOR_START;
            result.endColorArgb = VanillaTooltip.BORDER_COLOR_END;
            return result;
        }

        public static Shape<VanillaTooltip> createMutableVanillaTooltip(final AbstractPaneGroup parent, final int backgroundColorArgb)
        {
            final Shape<VanillaTooltip> result = new Shape<>(parent);
            result.setMutableVanillaTooltip().setBackgroundColor(backgroundColorArgb);
            result.startColorArgb = VanillaTooltip.BORDER_COLOR_START;
            result.endColorArgb = VanillaTooltip.BORDER_COLOR_END;
            return result;
        }
    }

    public static abstract class ShapeType
    {
        protected abstract void draw(final DelegatedShape<?> pane, final RenderContext ctx);
    }

    public interface LineWidthProvider
    {
        int getLineWidth();
    }

    public static class LineBox extends ShapeType implements LineWidthProvider
    {
        public static final ParsingCodec<LineBox> CODEC =
            ParsingCodec.forType("lineBox", LineBox::new).field("lineWidth", Parser.INT, LineBox::setLineWidth);

        protected int lineWidth = 1;

        public LineBox()
        {
            // not public
        }

        @Override
        protected void draw(final DelegatedShape<?> pane, final RenderContext ctx)
        {
            if (2 * lineWidth > Math.min(pane.width, pane.height))
            {
                throw Crash.argument("Cannot set " + (2 * lineWidth) +
                    " = 2 * line width > min(pane width, pane height) = " +
                    Math.min(pane.width, pane.height));
            }

            final int r0 = ARGB32.red(pane.startColorArgb), g0 = ARGB32.green(pane.startColorArgb),
                b0 = ARGB32.blue(pane.startColorArgb), a0 = ARGB32.alpha(pane.startColorArgb), r1 = ARGB32.red(pane.endColorArgb),
                g1 = ARGB32.green(pane.endColorArgb), b1 = ARGB32.blue(pane.endColorArgb), a1 = ARGB32.alpha(pane.endColorArgb);
            switch (pane.colorDirection)
            {
                case PRIMARY_DIAGONAL ->
                {
                    // TODO: bake this math
                    final double alpha = Math.atan((double) pane.height / pane.width);
                    final float fullDiag = (float) Math.sqrt(pane.width * pane.width + pane.height * pane.height);
                    final float cornerProgress = calcDiagonalProgress(alpha, 0, pane.width) / fullDiag;
                    final float nearInnerProgress = calcDiagonalProgress(alpha, lineWidth, lineWidth) / fullDiag;
                    final float farInnerProgress = calcDiagonalProgress(alpha, lineWidth, pane.width - lineWidth) / fullDiag;

                    final int nearInner = ARGB32.lerp(nearInnerProgress, pane.startColorArgb, pane.endColorArgb);
                    final int farInner = ARGB32.lerp(farInnerProgress, pane.startColorArgb, pane.endColorArgb);
                    final int corner = ARGB32.lerp(cornerProgress, pane.startColorArgb, pane.endColorArgb);
                    final int cornerInv = ARGB32.lerp(1.0f - cornerProgress, pane.startColorArgb, pane.endColorArgb);
                    final int farInnerInv = ARGB32.lerp(1.0f - farInnerProgress, pane.startColorArgb, pane.endColorArgb);
                    final int nearInnerInv = ARGB32.lerp(1.0f - nearInnerProgress, pane.startColorArgb, pane.endColorArgb);

                    ctx.coloredTriangleFan(pane.hasAlphaChannel(), (buffer, m) -> {
                        buffer.vertex(m, pane.alignedX, pane.alignedY, 0).color(r0, g0, b0, a0).endVertex();
                        buffer.vertex(m, pane.alignedX, pane.alignedY + pane.height, 0).color(cornerInv).endVertex();
                        buffer.vertex(m, pane.alignedX + lineWidth, pane.alignedY + pane.height - lineWidth, 0)
                            .color(farInnerInv)
                            .endVertex();
                        buffer.vertex(m, pane.alignedX + lineWidth, pane.alignedY + lineWidth, 0).color(nearInner).endVertex();
                        buffer.vertex(m, pane.alignedX + pane.width - lineWidth, pane.alignedY + lineWidth, 0)
                            .color(farInner)
                            .endVertex();
                        buffer.vertex(m, pane.alignedX + pane.width, pane.alignedY, 0).color(corner).endVertex();
                    });
                    ctx.coloredTriangleFan(pane.hasAlphaChannel(), (buffer, m) -> {
                        buffer.vertex(m, pane.alignedX + pane.width, pane.alignedY + pane.height, 0).color(r1, g1, b1, a1).endVertex();
                        buffer.vertex(m, pane.alignedX + pane.width, pane.alignedY, 0).color(corner).endVertex();
                        buffer.vertex(m, pane.alignedX + pane.width - lineWidth, pane.alignedY + lineWidth, 0)
                            .color(farInner)
                            .endVertex();
                        buffer.vertex(m, pane.alignedX + pane.width - lineWidth, pane.alignedY + pane.height - lineWidth, 0)
                            .color(nearInnerInv)
                            .endVertex();
                        buffer.vertex(m, pane.alignedX + lineWidth, pane.alignedY + pane.height - lineWidth, 0)
                            .color(farInnerInv)
                            .endVertex();
                        buffer.vertex(m, pane.alignedX, pane.alignedY + pane.height, 0).color(cornerInv).endVertex();
                    });
                }
                case SECONDARY_DIAGONAL ->
                {
                    final double alpha = Math.atan((double) pane.height / pane.width);
                    final float fullDiag = (float) Math.sqrt(pane.width * pane.width + pane.height * pane.height);
                    final float cornerProgress = calcDiagonalProgress(alpha, 0, pane.width) / fullDiag;
                    final float nearInnerProgress = calcDiagonalProgress(alpha, lineWidth, lineWidth) / fullDiag;
                    final float farInnerProgress = calcDiagonalProgress(alpha, lineWidth, pane.width - lineWidth) / fullDiag;

                    final int nearInner = ARGB32.lerp(nearInnerProgress, pane.startColorArgb, pane.endColorArgb);
                    final int farInner = ARGB32.lerp(farInnerProgress, pane.startColorArgb, pane.endColorArgb);
                    final int corner = ARGB32.lerp(cornerProgress, pane.startColorArgb, pane.endColorArgb);
                    final int cornerInv = ARGB32.lerp(1.0f - cornerProgress, pane.startColorArgb, pane.endColorArgb);
                    final int farInnerInv = ARGB32.lerp(1.0f - farInnerProgress, pane.startColorArgb, pane.endColorArgb);
                    final int nearInnerInv = ARGB32.lerp(1.0f - nearInnerProgress, pane.startColorArgb, pane.endColorArgb);

                    ctx.coloredTriangleFan(pane.hasAlphaChannel(), (buffer, m) -> {
                        buffer.vertex(m, pane.alignedX + pane.width, pane.alignedY, 0).color(r0, g0, b0, a0).endVertex();
                        buffer.vertex(m, pane.alignedX, pane.alignedY, 0).color(corner).endVertex();
                        buffer.vertex(m, pane.alignedX + lineWidth, pane.alignedY + lineWidth, 0).color(farInner).endVertex();
                        buffer.vertex(m, pane.alignedX + pane.width - lineWidth, pane.alignedY + lineWidth, 0)
                            .color(nearInner)
                            .endVertex();
                        buffer.vertex(m, pane.alignedX + pane.width - lineWidth, pane.alignedY + pane.height - lineWidth, 0)
                            .color(farInnerInv)
                            .endVertex();
                        buffer.vertex(m, pane.alignedX + pane.width, pane.alignedY + pane.height, 0).color(cornerInv).endVertex();
                    });
                    ctx.coloredTriangleFan(pane.hasAlphaChannel(), (buffer, m) -> {
                        buffer.vertex(m, pane.alignedX, pane.alignedY + pane.height, 0).color(r1, g1, b1, a1).endVertex();
                        buffer.vertex(m, pane.alignedX, pane.alignedY, 0).color(corner).endVertex();
                        buffer.vertex(m, pane.alignedX + lineWidth, pane.alignedY + lineWidth, 0).color(farInner).endVertex();
                        buffer.vertex(m, pane.alignedX + lineWidth, pane.alignedY + pane.height - lineWidth, 0)
                            .color(nearInnerInv)
                            .endVertex();
                        buffer.vertex(m, pane.alignedX + pane.width - lineWidth, pane.alignedY + pane.height - lineWidth, 0)
                            .color(farInnerInv)
                            .endVertex();
                        buffer.vertex(m, pane.alignedX + pane.width, pane.alignedY + pane.height, 0).color(cornerInv).endVertex();
                    });
                }
                case HORIZONTAL ->
                {
                    final int leftColor = ARGB32.lerp((float) lineWidth / pane.width, pane.startColorArgb, pane.endColorArgb),
                        rightColor = ARGB32.lerp(1.0f - (float) lineWidth / pane.width, pane.startColorArgb, pane.endColorArgb);
                    final int rL = ARGB32.red(leftColor), gL = ARGB32.green(leftColor), bL = ARGB32.blue(leftColor),
                        aL = ARGB32.alpha(leftColor), rR = ARGB32.red(rightColor), gR = ARGB32.green(rightColor),
                        bR = ARGB32.blue(rightColor), aR = ARGB32.alpha(rightColor);

                    ctx.coloredTriangleFan(pane.hasAlphaChannel(), (buffer, m) -> {
                        buffer.vertex(m, pane.alignedX, pane.alignedY, 0).color(r0, g0, b0, a0).endVertex();
                        buffer.vertex(m, pane.alignedX, pane.alignedY + pane.height, 0).color(r0, g0, b0, a0).endVertex();
                        buffer.vertex(m, pane.alignedX + lineWidth, pane.alignedY + pane.height - lineWidth, 0)
                            .color(rL, gL, bL, aL)
                            .endVertex();
                        buffer.vertex(m, pane.alignedX + lineWidth, pane.alignedY + lineWidth, 0).color(rL, gL, bL, aL).endVertex();
                        buffer.vertex(m, pane.alignedX + pane.width - lineWidth, pane.alignedY + lineWidth, 0)
                            .color(rR, gR, bR, aR)
                            .endVertex();
                        buffer.vertex(m, pane.alignedX + pane.width, pane.alignedY, 0).color(r1, g1, b1, a1).endVertex();
                    });
                    ctx.coloredTriangleFan(pane.hasAlphaChannel(), (buffer, m) -> {
                        buffer.vertex(m, pane.alignedX + pane.width, pane.alignedY + pane.height, 0).color(r1, g1, b1, a1).endVertex();
                        buffer.vertex(m, pane.alignedX + pane.width, pane.alignedY, 0).color(r1, g1, b1, a1).endVertex();
                        buffer.vertex(m, pane.alignedX + pane.width - lineWidth, pane.alignedY + lineWidth, 0)
                            .color(rR, gR, bR, aR)
                            .endVertex();
                        buffer.vertex(m, pane.alignedX + pane.width - lineWidth, pane.alignedY + pane.height - lineWidth, 0)
                            .color(rR, gR, bR, aR)
                            .endVertex();
                        buffer.vertex(m, pane.alignedX + lineWidth, pane.alignedY + pane.height - lineWidth, 0)
                            .color(rL, gL, bL, aL)
                            .endVertex();
                        buffer.vertex(m, pane.alignedX, pane.alignedY + pane.height, 0).color(r0, g0, b0, a0).endVertex();
                    });
                }
                case VERTICAL ->
                {
                    final int upColor = ARGB32.lerp((float) lineWidth / pane.height, pane.startColorArgb, pane.endColorArgb),
                        downColor = ARGB32.lerp(1.0f - (float) lineWidth / pane.height, pane.startColorArgb, pane.endColorArgb);
                    final int rU = ARGB32.red(upColor), gU = ARGB32.green(upColor), bU = ARGB32.blue(upColor),
                        aU = ARGB32.alpha(upColor), rD = ARGB32.red(downColor), gD = ARGB32.green(downColor),
                        bD = ARGB32.blue(downColor), aD = ARGB32.alpha(downColor);

                    ctx.coloredTriangleFan(pane.hasAlphaChannel(), (buffer, m) -> {
                        buffer.vertex(m, pane.alignedX, pane.alignedY, 0).color(r0, g0, b0, a0).endVertex();
                        buffer.vertex(m, pane.alignedX, pane.alignedY + pane.height, 0).color(r1, g1, b1, a1).endVertex();
                        buffer.vertex(m, pane.alignedX + lineWidth, pane.alignedY + pane.height - lineWidth, 0)
                            .color(rD, gD, bD, aD)
                            .endVertex();
                        buffer.vertex(m, pane.alignedX + lineWidth, pane.alignedY + lineWidth, 0).color(rU, gU, bU, aU).endVertex();
                        buffer.vertex(m, pane.alignedX + pane.width - lineWidth, pane.alignedY + lineWidth, 0)
                            .color(rU, gU, bU, aU)
                            .endVertex();
                        buffer.vertex(m, pane.alignedX + pane.width, pane.alignedY, 0).color(r0, g0, b0, a0).endVertex();
                    });
                    ctx.coloredTriangleFan(pane.hasAlphaChannel(), (buffer, m) -> {
                        buffer.vertex(m, pane.alignedX + pane.width, pane.alignedY + pane.height, 0).color(r1, g1, b1, a1).endVertex();
                        buffer.vertex(m, pane.alignedX + pane.width, pane.alignedY, 0).color(r0, g0, b0, a0).endVertex();
                        buffer.vertex(m, pane.alignedX + pane.width - lineWidth, pane.alignedY + lineWidth, 0)
                            .color(rU, gU, bU, aU)
                            .endVertex();
                        buffer.vertex(m, pane.alignedX + pane.width - lineWidth, pane.alignedY + pane.height - lineWidth, 0)
                            .color(rD, gD, bD, aD)
                            .endVertex();
                        buffer.vertex(m, pane.alignedX + lineWidth, pane.alignedY + pane.height - lineWidth, 0)
                            .color(rD, gD, bD, aD)
                            .endVertex();
                        buffer.vertex(m, pane.alignedX, pane.alignedY + pane.height, 0).color(r1, g1, b1, a1).endVertex();
                    });
                }
            }
        }

        public LineBox setLineWidth(final int lineWidth)
        {
            this.lineWidth = lineWidth;
            return this;
        }

        public int getLineWidth()
        {
            return lineWidth;
        }
    }

    public static class Rectangle extends ShapeType
    {
        public static final Rectangle INSTANCE = new Rectangle();
        public static final ParsingCodec<Rectangle> CODEC = ParsingCodec.forType("rectangle", () -> INSTANCE);

        protected Rectangle()
        {
            // not public
        }

        @Override
        protected void draw(final DelegatedShape<?> pane, final RenderContext ctx)
        {
            final int r0 = ARGB32.red(pane.startColorArgb), g0 = ARGB32.green(pane.startColorArgb),
                b0 = ARGB32.blue(pane.startColorArgb), a0 = ARGB32.alpha(pane.startColorArgb), r1 = ARGB32.red(pane.endColorArgb),
                g1 = ARGB32.green(pane.endColorArgb), b1 = ARGB32.blue(pane.endColorArgb), a1 = ARGB32.alpha(pane.endColorArgb);
            ctx.coloredTriangleFan(pane.hasAlphaChannel(), (buffer, m) -> {
                switch (pane.colorDirection)
                {
                    case PRIMARY_DIAGONAL ->
                    {
                        final double alpha = Math.atan((double) pane.height / pane.width);
                        final float fullDiag = (float) Math.sqrt(pane.width * pane.width + pane.height * pane.height);
                        final float cornerProgress = calcDiagonalProgress(alpha, 0, pane.width) / fullDiag;
                        final int nearCornerColor = ARGB32.lerp(cornerProgress, pane.startColorArgb, pane.endColorArgb);
                        final int farCornerColor = ARGB32.lerp(1.0f - cornerProgress, pane.startColorArgb, pane.endColorArgb);

                        buffer.vertex(m, pane.alignedX, pane.alignedY, 0).color(r0, g0, b0, a0).endVertex();
                        buffer.vertex(m, pane.alignedX + pane.width, pane.alignedY, 0).color(nearCornerColor).endVertex();
                        buffer.vertex(m, pane.alignedX + pane.width, pane.alignedY + pane.height, 0).color(r1, g1, b1, a1).endVertex();
                        buffer.vertex(m, pane.alignedX, pane.alignedY + pane.height, 0).color(farCornerColor).endVertex();
                    }
                    case SECONDARY_DIAGONAL ->
                    {
                        final double alpha = Math.atan((double) pane.height / pane.width);
                        final float fullDiag = (float) Math.sqrt(pane.width * pane.width + pane.height * pane.height);
                        final float cornerProgress = calcDiagonalProgress(alpha, 0, pane.width) / fullDiag;
                        final int nearCornerColor = ARGB32.lerp(cornerProgress, pane.startColorArgb, pane.endColorArgb);
                        final int farCornerColor = ARGB32.lerp(1.0f - cornerProgress, pane.startColorArgb, pane.endColorArgb);

                        buffer.vertex(m, pane.alignedX, pane.alignedY, 0).color(nearCornerColor).endVertex();
                        buffer.vertex(m, pane.alignedX + pane.width, pane.alignedY, 0).color(r0, g0, b0, a0).endVertex();
                        buffer.vertex(m, pane.alignedX + pane.width, pane.alignedY + pane.height, 0).color(farCornerColor).endVertex();
                        buffer.vertex(m, pane.alignedX, pane.alignedY + pane.height, 0).color(r1, g1, b1, a1).endVertex();
                    }
                    case HORIZONTAL ->
                    {
                        buffer.vertex(m, pane.alignedX, pane.alignedY, 0).color(r0, g0, b0, a0).endVertex();
                        buffer.vertex(m, pane.alignedX + pane.width, pane.alignedY, 0).color(r1, g1, b1, a1).endVertex();
                        buffer.vertex(m, pane.alignedX + pane.width, pane.alignedY + pane.height, 0).color(r1, g1, b1, a1).endVertex();
                        buffer.vertex(m, pane.alignedX, pane.alignedY + pane.height, 0).color(r0, g0, b0, a0).endVertex();
                    }
                    case VERTICAL ->
                    {
                        buffer.vertex(m, pane.alignedX, pane.alignedY, 0).color(r0, g0, b0, a0).endVertex();
                        buffer.vertex(m, pane.alignedX + pane.width, pane.alignedY, 0).color(r0, g0, b0, a0).endVertex();
                        buffer.vertex(m, pane.alignedX + pane.width, pane.alignedY + pane.height, 0).color(r1, g1, b1, a1).endVertex();
                        buffer.vertex(m, pane.alignedX, pane.alignedY + pane.height, 0).color(r1, g1, b1, a1).endVertex();
                    }
                }
            });
        }
    }

    public static class Line extends ShapeType implements LineWidthProvider
    {
        public static final ParsingCodec<Line> CODEC =
            ParsingCodec.forType("line", Line::new).field("lineWidth", Parser.INT, Line::setLineWidth);

        protected int lineWidth = 1;

        protected Line()
        {
            // not public
        }

        @Override
        protected void draw(final DelegatedShape<?> pane, final RenderContext ctx)
        {
            if (lineWidth > Math.min(pane.width, pane.height))
            {
                throw Crash.argument("Cannot draw line width > min(pane width, pane height) = %d > %d".formatted(lineWidth,
                    Math.min(pane.width, pane.height)));
            }
            final int lwDown = lineWidth / 2, lwUp = lineWidth - lwDown;

            final int r0 = ARGB32.red(pane.startColorArgb), g0 = ARGB32.green(pane.startColorArgb),
                b0 = ARGB32.blue(pane.startColorArgb), a0 = ARGB32.alpha(pane.startColorArgb), r1 = ARGB32.red(pane.endColorArgb),
                g1 = ARGB32.green(pane.endColorArgb), b1 = ARGB32.blue(pane.endColorArgb), a1 = ARGB32.alpha(pane.endColorArgb);
            ctx.coloredTriangleFan(pane.hasAlphaChannel(), (buffer, m) -> {
                switch (pane.colorDirection)
                {
                    // TODO: fix diagonal corners
                    case PRIMARY_DIAGONAL ->
                    {
                        buffer.vertex(m, pane.alignedX + lwDown, pane.alignedY - lwDown, 0).color(r0, g0, b0, a0).endVertex();
                        buffer.vertex(m, pane.alignedX + lwDown + pane.width, pane.alignedY - lwDown + pane.height, 0)
                            .color(r1, g1, b1, a1)
                            .endVertex();
                        buffer.vertex(m, pane.alignedX - lwUp + pane.width, pane.alignedY + lwUp + pane.height, 0)
                            .color(r1, g1, b1, a1)
                            .endVertex();
                        buffer.vertex(m, pane.alignedX - lwUp, pane.alignedY + lwUp, 0).color(r0, g0, b0, a0).endVertex();
                    }
                    case SECONDARY_DIAGONAL ->
                    {
                        buffer.vertex(m, pane.alignedX - lwDown + pane.width, pane.alignedY - lwDown, 0)
                            .color(r0, g0, b0, a0)
                            .endVertex();
                        buffer.vertex(m, pane.alignedX + lwUp + pane.width, pane.alignedY + lwUp, 0).color(r0, g0, b0, a0).endVertex();
                        buffer.vertex(m, pane.alignedX + lwUp, pane.alignedY + lwUp + pane.height, 0)
                            .color(r1, g1, b1, a1)
                            .endVertex();
                        buffer.vertex(m, pane.alignedX - lwDown, pane.alignedY - lwDown + pane.height, 0)
                            .color(r1, g1, b1, a1)
                            .endVertex();
                    }
                    case HORIZONTAL ->
                    {
                        final int y = pane.alignedY + pane.height / 2;
                        buffer.vertex(m, pane.alignedX, y - lwDown, 0).color(r0, g0, b0, a0).endVertex();
                        buffer.vertex(m, pane.alignedX + pane.width, y - lwDown, 0).color(r1, g1, b1, a1).endVertex();
                        buffer.vertex(m, pane.alignedX + pane.width, y + lwUp, 0).color(r1, g1, b1, a1).endVertex();
                        buffer.vertex(m, pane.alignedX, y + lwUp, 0).color(r0, g0, b0, a0).endVertex();
                    }
                    case VERTICAL ->
                    {
                        final int x = pane.alignedX + pane.width / 2;
                        buffer.vertex(m, x - lwDown, pane.alignedY, 0).color(r0, g0, b0, a0).endVertex();
                        buffer.vertex(m, x - lwDown, pane.alignedY + pane.height, 0).color(r1, g1, b1, a1).endVertex();
                        buffer.vertex(m, x + lwUp, pane.alignedY + pane.height, 0).color(r1, g1, b1, a1).endVertex();
                        buffer.vertex(m, x + lwUp, pane.alignedY, 0).color(r0, g0, b0, a0).endVertex();
                    }
                }
            });
        }

        public Line setLineWidth(final int lineWidth)
        {
            this.lineWidth = lineWidth;
            return this;
        }

        public int getLineWidth()
        {
            return lineWidth;
        }
    }

    public static class VanillaTooltip extends ShapeType
    {
        public static final VanillaTooltip INSTANCE = new VanillaTooltip();
        public static final ParsingCodec<VanillaTooltip> CODEC = ParsingCodec.forType("vanillaTooltip", () -> INSTANCE);
        public static final ParsingCodec<VanillaTooltip> CODEC2 = ParsingCodec.forType("vanillaTooltipC", VanillaTooltip::new)
            .field("bgColor", Parser.COLOR, VanillaTooltip::setBackgroundColor);

        // TODO: update from net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil
        /** @see TooltipRenderUtil#BACKGROUND_COLOR */
        public static final int BACKGROUND_COLOR = 0xf0100010;
        /** @see TooltipRenderUtil#BORDER_COLOR_TOP */
        public static final int BORDER_COLOR_START = 0x505000ff;
        /** @see TooltipRenderUtil#BORDER_COLOR_BOTTOM */
        public static final int BORDER_COLOR_END = 0x5028007f;

        protected int backgroundColorArgb = BACKGROUND_COLOR;

        protected VanillaTooltip()
        {
            // not public
        }

        @Override
        protected void draw(final DelegatedShape<?> pane, final RenderContext ctx)
        {
            // intentionally ignore color direction

            // modified INLINE: should pixel perfectly match vanilla tooltip (ideally test against frozen background)
            ctx.coloredTriangleFan(ARGB32.alpha(backgroundColorArgb) < 0xff, (buffer, m) -> {
                final int r = ARGB32.red(backgroundColorArgb), g = ARGB32.green(backgroundColorArgb),
                    b = ARGB32.blue(backgroundColorArgb), a = ARGB32.alpha(backgroundColorArgb);

                buffer.vertex(m, pane.alignedX + 1, pane.alignedY + 1, 0).color(r, g, b, a).endVertex();
                buffer.vertex(m, pane.alignedX + 0, pane.alignedY + 1, 0).color(r, g, b, a).endVertex();
                buffer.vertex(m, pane.alignedX + 0, pane.alignedY + pane.height - 1, 0).color(r, g, b, a).endVertex();
                buffer.vertex(m, pane.alignedX + 1, pane.alignedY + pane.height - 1, 0).color(r, g, b, a).endVertex();
                buffer.vertex(m, pane.alignedX + 1, pane.alignedY + pane.height, 0).color(r, g, b, a).endVertex();
                buffer.vertex(m, pane.alignedX + pane.width - 1, pane.alignedY + pane.height, 0).color(r, g, b, a).endVertex();
                buffer.vertex(m, pane.alignedX + pane.width - 1, pane.alignedY + pane.height - 1, 0).color(r, g, b, a).endVertex();
                buffer.vertex(m, pane.alignedX + pane.width, pane.alignedY + pane.height - 1, 0).color(r, g, b, a).endVertex();
                buffer.vertex(m, pane.alignedX + pane.width, pane.alignedY + 1, 0).color(r, g, b, a).endVertex();
                buffer.vertex(m, pane.alignedX + pane.width - 1, pane.alignedY + 1, 0).color(r, g, b, a).endVertex();
                buffer.vertex(m, pane.alignedX + pane.width - 1, pane.alignedY + 0, 0).color(r, g, b, a).endVertex();
                buffer.vertex(m, pane.alignedX + 1, pane.alignedY + 0, 0).color(r, g, b, a).endVertex();
            });

            // drawLineRectGradient(1, 1, pane.width - 2, pane.height - 2, pane.startColorArgb, pane.endColorArgb, 1);
            final int r0 = ARGB32.red(pane.startColorArgb), g0 = ARGB32.green(pane.startColorArgb),
                b0 = ARGB32.blue(pane.startColorArgb), a0 = ARGB32.alpha(pane.startColorArgb), r1 = ARGB32.red(pane.endColorArgb),
                g1 = ARGB32.green(pane.endColorArgb), b1 = ARGB32.blue(pane.endColorArgb), a1 = ARGB32.alpha(pane.endColorArgb);
            final int lineWidth = 1, x = pane.alignedX + 1, y = pane.alignedY + 1, w = pane.width - 2, h = pane.height - 2;

            ctx.coloredTriangleFan(pane.hasAlphaChannel(), (buffer, m) -> {
                buffer.vertex(m, x, y, 0).color(r0, g0, b0, a0).endVertex();
                buffer.vertex(m, x, y + h, 0).color(r1, g1, b1, a1).endVertex();
                buffer.vertex(m, x + lineWidth, y + h - lineWidth, 0).color(r1, g1, b1, a1).endVertex();
                buffer.vertex(m, x + lineWidth, y + lineWidth, 0).color(r0, g0, b0, a0).endVertex();
                buffer.vertex(m, x + w - lineWidth, y + lineWidth, 0).color(r0, g0, b0, a0).endVertex();
                buffer.vertex(m, x + w, y, 0).color(r0, g0, b0, a0).endVertex();
            });
            ctx.coloredTriangleFan(pane.hasAlphaChannel(), (buffer, m) -> {
                buffer.vertex(m, x + w, y + h, 0).color(r1, g1, b1, a1).endVertex();
                buffer.vertex(m, x + w, y, 0).color(r0, g0, b0, a0).endVertex();
                buffer.vertex(m, x + w - lineWidth, y + lineWidth, 0).color(r0, g0, b0, a0).endVertex();
                buffer.vertex(m, x + w - lineWidth, y + h - lineWidth, 0).color(r1, g1, b1, a1).endVertex();
                buffer.vertex(m, x + lineWidth, y + h - lineWidth, 0).color(r1, g1, b1, a1).endVertex();
                buffer.vertex(m, x, y + h, 0).color(r1, g1, b1, a1).endVertex();
            });
        }

        public int getBackgroundColor()
        {
            return backgroundColorArgb;
        }

        public void setBackgroundColor(final int backgroundColorArgb)
        {
            if (this == INSTANCE)
            {
                Crash.crash(new RuntimeException("Trying to modify static tooltip instance"));
            }
            this.backgroundColorArgb = backgroundColorArgb;
        }
    }

    /**
     * @param  alpha    main rectangle diagonal to upper border angle
     * @param  vertDist projection point relative y
     * @param  horzDist projection point relative x
     * @return          diagonal progress € [0;1]
     */
    private static float calcDiagonalProgress(final double alpha, final int vertDist, final int horzDist)
    {
        if (vertDist == 0)
        {
            return (float) (horzDist * org.joml.Math.cos(alpha));
        }
        final double hypotenuse = Math.sqrt(horzDist * horzDist + vertDist * vertDist);
        final double innerAngle = alpha - Math.tan((double) vertDist / horzDist);
        return (float) (hypotenuse * org.joml.Math.cos(innerAngle));
    }
}
