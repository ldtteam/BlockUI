package com.ldtteam.blockui.views;

import com.ldtteam.blockui.BOGuiGraphics;
import com.ldtteam.blockui.MouseEventCallback;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneParams;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.Mth;

/**
 * Zoomable and scrollable "online map"-like view
 */
public class ZoomDragView extends View
{
    protected double scrollX = 0d;
    protected double scrollY = 0d;
    protected double scale = 1d;

    protected int contentHeight = 0;
    protected int contentWidth = 0;

    protected double dragFactor = 1d;
    protected boolean dragEnabled = true;

    protected double zoomFactor = 1.1d;
    protected boolean zoomEnabled = true;
    protected double minScale = 0.2d;
    protected double maxScale = 2d;

    /**
     * Required default constructor.
     */
    public ZoomDragView()
    {
        super();
    }

    /**
     * Constructs a View from PaneParams.
     *
     * @param params Params for the Pane.
     */
    public ZoomDragView(final PaneParams params)
    {
        super(params);
        dragFactor = params.getDouble("dragfactor", dragFactor);
        dragEnabled = params.getBoolean("dragenabled", dragEnabled);
        zoomFactor = params.getDouble("zoomfactor", zoomFactor);
        zoomEnabled = params.getBoolean("zoomenabled", zoomEnabled);
        minScale = params.getDouble("minscale", minScale);
        maxScale = params.getDouble("maxscale", maxScale);
    }

    @Override
    protected boolean childIsVisible(final Pane child)
    {
        return calcInverseAbsoluteX(child.getX()) < getInteriorWidth() && calcInverseAbsoluteY(child.getY()) < getInteriorHeight()
            && calcInverseAbsoluteX(child.getX() + child.getWidth()) >= 0 && calcInverseAbsoluteY(child.getY() + child.getHeight()) >= 0;
    }

    /**
     * Converts X of child to scaled and scrolled X in absolute coordinates.
     */
    public double calcInverseAbsoluteX(final double xIn)
    {
        return xIn * scale - scrollX;
    }

    /**
     * Converts Y of child to scaled and scrolled Y in absolute coordinates.
     */
    public double calcInverseAbsoluteY(final double yIn)
    {
        return yIn * scale - scrollY;
    }

    /**
     * Converts X from event to unscaled and unscrolled X for child in relative (top-left) coordinates.
     */
    public double calcRelativeX(final double xIn)
    {
        return (xIn - x + scrollX) / scale + x;
    }

    /**
     * Converts Y from event to unscaled and unscrolled Y for child in relative (top-left) coordinates.
     */
    public double calcRelativeY(final double yIn)
    {
        return (yIn - y + scrollY) / scale + y;
    }

    @Override
    public void parseChildren(final PaneParams params)
    {
        super.parseChildren(params);
        computeContentSize();
    }

    @Override
    public void addChild(final Pane child)
    {
        super.addChild(child);
        computeContentSize();
    }

    /**
     * Compute the height in pixels of the container.
     */
    public void computeContentSize()
    {
        contentHeight = 0;
        contentWidth = 0;

        for (final Pane child : children)
        {
            if (child != null)
            {
                contentHeight = Math.max(contentHeight, child.getY() + child.getHeight());
                contentWidth = Math.max(contentWidth, child.getX() + child.getWidth());
            }
        }

        // Recompute scroll
        setScrollY(scrollY);
        setScrollX(scrollX);
    }

    /**
     * @return based on current contentHeight and scale
     */
    public double getMaxScrollY()
    {
        return Math.max(0, (double) contentHeight * scale - getHeight());
    }

    /**
     * @return based on current contentWidth and scale
     */
    public double getMaxScrollX()
    {
        return Math.max(0, (double) contentWidth * scale - getWidth());
    }

    protected void abstractDrawSelfPre(final PoseStack ms, final double mx, final double my)
    {
    }

    protected void abstractDrawSelfPost(final PoseStack ms, final double mx, final double my)
    {
    }

    @Override
    public void drawSelf(final BOGuiGraphics target, final double mx, final double my)
    {
        final PoseStack ms = target.pose();

        scissorsStart(ms, contentWidth, contentHeight);

        ms.pushPose();
        ms.translate(-scrollX, -scrollY, 0.0d);
        ms.translate((1 - scale) * x, (1 - scale) * y, 0.0d);
        ms.scale((float) scale, (float) scale, 1.0f);
        abstractDrawSelfPre(ms, mx, my);
        super.drawSelf(target, calcRelativeX(mx), calcRelativeY(my));
        abstractDrawSelfPost(ms, mx, my);
        ms.popPose();

        scissorsEnd(target);
    }

    @Override
    public void drawSelfLast(final BOGuiGraphics target, final double mx, final double my)
    {
        final PoseStack ms = target.pose();

        scissorsStart(ms, contentWidth, contentHeight);

        ms.pushPose();
        ms.translate(-scrollX, -scrollY, 0.0d);
        ms.translate((1 - scale) * x, (1 - scale) * y, 0.0d);
        ms.scale((float) scale, (float) scale, 1.0f);
        super.drawSelfLast(target, calcRelativeX(mx), calcRelativeY(my));
        ms.popPose();

        scissorsEnd(target);
    }

    public void setScrollY(final double offset)
    {
        scrollY = Mth.clamp(offset, 0, getMaxScrollY());
    }

    public void setScrollX(final double offset)
    {
        scrollX = Mth.clamp(offset, 0, getMaxScrollX());
    }

    @Override
    public boolean onMouseDrag(final double startX, final double startY, final int speed, final double x, final double y)
    {
        final boolean childResult = super.onMouseDrag(startX, startY, speed, calcRelativeX(x), calcRelativeY(y));
        if (!childResult && dragEnabled)
        {
            setScrollX(scrollX - x * dragFactor);
            setScrollY(scrollY - y * dragFactor);
            return true;
        }
        return childResult;
    }

    @Override
    public boolean scrollInput(final double wheel, final double mx, final double my)
    {
        final boolean childResult = super.scrollInput(wheel, mx, my);
        if (!childResult && zoomEnabled)
        {
            final double childX = mx - x;
            final double childY = my - y;
            final double oldX = (childX + scrollX) / scale;
            final double oldY = (childY + scrollY) / scale;
            scale = wheel < 0 ? scale / zoomFactor : scale * zoomFactor;
            scale = Mth.clamp(scale, minScale, maxScale);
            setScrollX(oldX * scale - childX);
            setScrollY(oldY * scale - childY);
            return true;
        }
        return childResult;
    }

    @Override
    public boolean mouseEventProcessor(final double mx,
        final double my,
        final MouseEventCallback panePredicate,
        final MouseEventCallback eventCallbackPositive,
        final MouseEventCallback eventCallbackNegative)
    {
        return super.mouseEventProcessor(calcRelativeX(mx), calcRelativeY(my), panePredicate, eventCallbackPositive, eventCallbackNegative);
    }

    /**
     * Add child without recomputing content box. You always should recomputed box when done with adding children.
     *
     * @param child child element
     * @see #addChild(Pane)
     * @see #computeContentSize()
     */
    public void addChildPlain(final Pane child)
    {
        super.addChild(child);
    }

    public double getDragFactor()
    {
        return dragFactor;
    }

    public void setDragFactor(final double dragFactor)
    {
        if (dragFactor < 1.0d)
        {
            throw new IllegalArgumentException("dragFactor can't be less than one");
        }
        this.dragFactor = dragFactor;
    }

    public boolean isDragEnabled()
    {
        return dragEnabled;
    }

    public void enableDrag()
    {
        setDragEnabled(true);
    }

    public void disableDrag()
    {
        setDragEnabled(false);
    }

    public void setDragEnabled(final boolean dragEnabled)
    {
        this.dragEnabled = dragEnabled;
    }

    public double getZoomFactor()
    {
        return zoomFactor;
    }

    public void setZoomFactor(final double zoomFactor)
    {
        if (zoomFactor < 1.0d)
        {
            throw new IllegalArgumentException("zoomFactor can't be less than one");
        }
        this.zoomFactor = zoomFactor;
    }

    public boolean isZoomEnabled()
    {
        return zoomEnabled;
    }

    public void enableZoom()
    {
        setZoomEnabled(true);
    }

    public void disableZoom()
    {
        setZoomEnabled(false);
    }

    public void setZoomEnabled(final boolean zoomEnabled)
    {
        this.zoomEnabled = zoomEnabled;
    }

    public double getMinScale()
    {
        return minScale;
    }

    public void setMinScale(final double minScale)
    {
        if (minScale > maxScale)
        {
            throw new IllegalArgumentException("minScale can't be greater than maxScale");
        }
        this.minScale = minScale;
    }

    public double getMaxScale()
    {
        return maxScale;
    }

    public void setMaxScale(final double maxScale)
    {
        if (maxScale < minScale)
        {
            throw new IllegalArgumentException("maxScale can't be less than minScale");
        }
        this.maxScale = maxScale;
    }

    public double getScrollX()
    {
        return scrollX;
    }

    public double getScrollY()
    {
        return scrollY;
    }

    /**
     * Automatically clamped between minScale and maxScale.
     * @see #scrollInput(double, double, double) for zooming at position
     */
    public void setScaleRaw(final double scale)
    {
        this.scale = Mth.clamp(scale, minScale, maxScale);
    }

    public double getScale()
    {
        return scale;
    }

    public int getContentHeight()
    {
        return contentHeight;
    }

    public int getContentWidth()
    {
        return contentWidth;
    }
}
