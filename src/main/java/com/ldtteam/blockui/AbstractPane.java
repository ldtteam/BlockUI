package com.ldtteam.blockui;

import com.ldtteam.blockui.AbstractPaneGroup.RootPaneGroup;
import com.ldtteam.blockui.attribute.RelativeAlignment;
import com.ldtteam.blockui.context.DebugLevel;
import com.ldtteam.blockui.context.DelayedLevel;
import com.ldtteam.blockui.context.RenderContext;
import com.ldtteam.blockui.element.shadow.AbstractDelegatedPane;
import com.ldtteam.blockui.parse.Parser;
import com.ldtteam.blockui.parse.ParsingCodec;
import com.ldtteam.blockui.util.Crash;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Root ui element, anything in ui hiearchy extends this
 */
public abstract class AbstractPane
{
    public static final ParsingCodec<?> CODEC = ParsingCodec.forAbstract()
        .field("id", Parser.SINGLE_WORD, (p, id) -> p.id = AccessibleId.validateId(id))
        .bifield("x", "y", "pos", Parser.INT, 0, 0, RenderSetters::setPosInternal)
        .bifield("width", "height", "size", Parser.INT, null, null, AbstractPane::setSizeInternal)
        .field("alignment", Parser.ALIGNMENT, RenderSetters::setAlignmentInternal)
        .field("visible", Parser.BOOL, (p, vis) -> p.visible = vis);

    // hierarchy
    @Nullable
    protected String id = null;
    protected AbstractPaneGroup parent = null;
    protected final UiWindow window;

    // render (you can adjust these, but dont forget to call #onAABBchange() or comment why not to call it)
    private int x = 0; // subclasses 99.9% time should use aligned, feel free to protected if needed
    private int y = 0;
    protected int width = 1;
    protected int height = 1;
    protected RelativeAlignment alignment = RelativeAlignment.TOP_LEFT;
    // following should not call #onAABBchange()
    protected boolean visible = true;
    protected DelayedLevel renderLevel = DelayedLevel.NONE;

    // logic (you should NOT SET these from extending class, if so provided reason in comment)
    protected boolean wasCursorInPane = false;
    protected int alignedX = 0;
    protected int alignedY = 0;

    protected AbstractPane(final AbstractPaneGroup parent)
    {
        this.parent = Objects.requireNonNull(parent);
        this.window = parent.window;
        this.parent.onChildrenAdded(this);
    }

    @Deprecated(since = "FOR ROOT PANE GROUP ONLY")
    protected AbstractPane(final UiWindow window)
    {
        this.window = Objects.requireNonNull(window);
    }

    /**
     * Is a point relative to the parent's origin within the pane?
     *
     * @param  mx mouse x relative to parent
     * @param  my mouse y relative to parent
     * @return    true if the point is in the pane.
     */
    protected final boolean isPointInPane(final double mx, final double my)
    {
        return visible && mx >= x && mx < (x + width) && my >= y && my < (y + height);
    }

    /**
     * Draw the current Pane if visible.
     *
     * @param mx mouse x relative to parent
     * @param my mouse y relative to parent
     */
    protected final void draw(final RenderContext ctx, final double mx, final double my)
    {
        if (visible)
        {
            wasCursorInPane = isPointInPane(mx, my);

            // if not main phase wrap ourself for later
            if (renderLevel != DelayedLevel.NONE)
            {
                ctx.delayRendering(renderLevel, () -> draw0(ctx, mx, my));
            }
            else
            {
                draw0(ctx, mx, my);
            }
        }
        else
        {
            wasCursorInPane = false;
        }
    }

    /**
     * @param mx mouse x relative to parent
     * @param my mouse y relative to parent
     * @see      #draw(RenderContext, double, double) impl of draw method
     */
    private void draw0(final RenderContext ctx, final double mx, final double my)
    {
        drawSelf(ctx, mx, my);

        if (ctx.debugLevel.isEnabled(DebugLevel.BORDER) && !(this instanceof AbstractDelegatedPane) ||
            ctx.debugLevel.isEnabled(DebugLevel.SHADOW))
        {
            final int color = wasCursorInPane ? 0xFF00FF00 : 0xFF0000FF;

            ctx.drawLineRect(x, y, width, height, color);

            if (ctx.debugLevel.isEnabled(DebugLevel.ID) && wasCursorInPane)
            {
                ctx.drawStringBottomRight(id == null ? "$no_id" : id, alignedX + width, alignedY + height, color);
            }
        }
    }

    /**
     * Primary pane rendering function. Context is related to parent of this pane.
     * 
     * @param ctx parent related/managed context
     * @param mx  mouse x relative to parent
     * @param my  mouse y relative to parent
     */
    protected abstract void drawSelf(final RenderContext ctx, final double mx, final double my);

    /**
     * @param  terminalCondition pane predicate which should end search
     * @return                   DFS result (= first pane encountered)
     */
    @Nullable
    protected AbstractPane traverseHierarchy(final Predicate<AbstractPane> terminalCondition)
    {
        return terminalCondition.test(this) ? this : null;
    }

    /**
     * ALWAYS CALL SUPER if overriding!
     * 
     * @param reason what method caused this event
     */
    protected void onAABBchange(final AABBchangeReason reason)
    {
        if (parent == null)
        {
            throw Crash.state("Unrooted element, please set parent first");
        }
        if (width <= 0 || height <= 0)
        {
            throw Crash.argument("Size <= 0, w=%d, h=%d".formatted(width, height));
        }
        if (x < 0 || y < 0)
        {
            throw Crash.argument("Pos < 0, x=%d, y=%d".formatted(x, y));
        }

        alignedX = alignment.calculateX(parent.width, x, width);
        alignedY = alignment.calculateY(parent.height, y, height);

        if (alignedX + width > parent.width || alignedY + height > parent.height)
        {
            throw Crash.argument("AABB of this pane overflows from parent, w > parent: %d > %d, h > parent: %d > %d"
                .formatted(alignedX + width, parent.width, alignedY + height, parent.height));
        }
    }

    /**
     * Special snowflake event called after size change with old size as params. ALWAYS CALL SUPER if overriding!
     * 
     * @param oldWidth  width of this element before size event
     * @param oldHeight height of this element before size event
     */
    protected void onSizeChange(final int oldWidth, final int oldHeight)
    {}

    /**
     * Use with caution if current parent is {@link AbstractShadowGroupPane}.
     * 
     * @param parent if null call {@link #unlinkParentAndRemoveHover()}
     * @see          #unlinkParentAndRemoveHover()
     */
    public final void setParent(final AbstractPaneGroup newParent)
    {
        if (parent != null)
        {
            parent.onChildrenRemoved(this);
        }
        this.parent = newParent;
        parent.onChildrenAdded(this);
        onAABBchange(AABBchangeReason.POS);
    }

    public final void unlinkParentAndRemoveHover()
    {
        window.removeHoverFor(this);
        parent.onChildrenRemoved(this);
        this.parent = null;
    }

    public final AbstractPaneGroup getParent()
    {
        return parent;
    }

    public final UiWindow getWindow()
    {
        return window;
    }

    /**
     * @return true if cursor was in this pane last rendered frame
     */
    public final boolean wasCursorInPaneLastFrame()
    {
        return wasCursorInPane;
    }

    public interface RenderGetters
    {
        default int getX()
        {
            return ((AbstractPane) this).x;
        }

        default int getY()
        {
            return ((AbstractPane) this).y;
        }

        default int getWidth()
        {
            return ((AbstractPane) this).width;
        }

        default int getHeight()
        {
            return ((AbstractPane) this).height;
        }

        default RelativeAlignment getAlignment()
        {
            return ((AbstractPane) this).alignment;
        }

        default boolean isVisible()
        {
            return ((AbstractPane) this).visible;
        }
    }

    protected final void setSizeInternal(final int width, final int height)
    {
        if (this.width == width && this.height == height)
        {
            return;
        }

        final int oldW = this.width, oldH = this.height;
        this.width = width;
        this.height = height;
        onAABBchange(AABBchangeReason.SIZE);
        onSizeChange(oldW, oldH);
    }

    public sealed interface SizeSetters permits RenderSetters, RootPaneGroup
    {
        default SizeSetters adjustSize(final int widthDelta, final int heightDelta)
        {
            final AbstractPane thiz = ((AbstractPane) this);
            return setSize(thiz.width + widthDelta, thiz.height + heightDelta);
        }

        default SizeSetters setSize(final int width, final int height)
        {
            ((AbstractPane) this).setSizeInternal(width, height);
            return this;
        }
    }

    public non-sealed interface RenderSetters extends RenderGetters, SizeSetters
    {
        default RenderSetters setSizePercentOfParent(final float widthPercent, final float heightPercent)
        {
            final AbstractPane thiz = ((AbstractPane) this);
            setSize(Math.round(widthPercent * thiz.parent.width), Math.round(widthPercent * thiz.parent.height));
            return this;
        }

        default RenderSetters shrinkSizeWhileShifingPos(final int widthDelta, final int heightDelta)
        {
            adjustSize(widthDelta, heightDelta);
            return adjustPos(widthDelta / 2, heightDelta / 2);
        }

        default RenderSetters adjustPos(final int xDelta, final int yDelta)
        {
            final AbstractPane thiz = ((AbstractPane) this);
            return setPos(thiz.x + xDelta, thiz.y + yDelta);
        }

        default RenderSetters setPos(final int x, final int y)
        {
            setPosInternal((AbstractPane) this, x, y);
            return this;
        }

        private static void setPosInternal(final AbstractPane pane, final int x, final int y)
        {
            pane.x = x;
            pane.y = y;
            pane.onAABBchange(AABBchangeReason.POS);
        }

        default RenderSetters setAlignment(final RelativeAlignment alignment)
        {
            setAlignmentInternal(((AbstractPane) this), alignment);
            return this;
        }

        private static void setAlignmentInternal(final AbstractPane pane, final RelativeAlignment alignment)
        {
            pane.alignment = alignment;
            pane.onAABBchange(AABBchangeReason.ALIGNMENT);
        }

        default RenderSetters setVisible(final boolean visible)
        {
            ((AbstractPane) this).visible = visible;
            return this;
        }
    }

    public interface AccessibleId
    {
        private static String validateId(final String id)
        {
            if (id != null && id.isBlank())
            {
                throw Crash.argument("Id must be not blank");
            }
            return id;
        }

        default void setId(final String id)
        {
            ((AbstractPane) this).id = validateId(id);
        }

        default String getId()
        {
            return ((AbstractPane) this).id;
        }
    }

    protected enum AABBchangeReason
    {
        POS,
        SIZE,
        ALIGNMENT;
    }
}
