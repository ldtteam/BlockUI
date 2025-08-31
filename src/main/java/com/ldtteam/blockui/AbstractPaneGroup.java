package com.ldtteam.blockui;

import com.ldtteam.blockui.context.HoverManager;
import com.ldtteam.blockui.context.RenderContext;
import com.ldtteam.blockui.parse.ParsingCodec;
import com.ldtteam.blockui.util.Crash.CheckArgument;
import com.ldtteam.blockui.util.MouseButton;
import com.ldtteam.blockui.util.MouseEventCallback;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;

/**
 * Pane without renderable content, groups sub-tree content
 */
public abstract class AbstractPaneGroup extends AbstractPane
{
    // intentional private to maintain order consistency as much as possible
    // TODO: ideally we have a joint list which allows iteration on groups without instanceof
    private final List<AbstractPane> childs = new ArrayList<>();
    private final List<AbstractPaneGroup> groups = new ArrayList<>();

    protected AbstractPaneGroup(final AbstractPaneGroup parent)
    {
        super(parent);
    }

    /**
     * For root pane being special snowflake
     */
    @SuppressWarnings("deprecation")
    private AbstractPaneGroup(final UiWindow window)
    {
        super(window);
    }

    /**
     * Only {@link AbstractPane} should call this, overrides are fine till they call super
     */
    protected void onChildrenAdded(final AbstractPane child)
    {
        childs.add(child);
        if (child instanceof final AbstractPaneGroup group)
        {
            groups.add(group);
        }
    }

    /**
     * Only {@link AbstractPane} should call this, overrides are fine till they call super
     */
    protected void onChildrenRemoved(final AbstractPane child)
    {
        childs.remove(child);
        if (child instanceof final AbstractPaneGroup group)
        {
            groups.remove(group);
        }
    }

    protected List<AbstractPane> getChildsView()
    {
        return Collections.unmodifiableList(childs);
    }

    /**
     * @param    moveWhat   which child should be moved
     * @param    moveBefore before what child should be moveWhat moved
     * @implNote            use with caution when moveWhat is {@link AbstractPaneGroup}
     */
    protected void moveChildBeforeAnother(final AbstractPane moveWhat, final AbstractPane moveBefore)
    {
        // this entire mechanism assumes there is an order consistency in child/group lists

        final int idxWhat = childs.indexOf(moveWhat);
        final int idxBefore = childs.indexOf(moveBefore);
        CheckArgument.notEquals(idxWhat, -1, "moveWhat is not child of this group");
        CheckArgument.notEquals(idxBefore, -1, "moveBefore is not child of this group");

        if (idxWhat > idxBefore)
        {
            childs.add(idxBefore, childs.remove(idxWhat));

            // TODO: this is not ideal, but it's at least sth (when moved thingy is group then group order might change)
            if (moveWhat instanceof final AbstractPaneGroup groupWhat && moveBefore instanceof final AbstractPaneGroup groupBefore)
            {
                moveChildGroupBeforeAnother(groupWhat, groupBefore);
            }
        }
    }

    /**
     * @see #moveChildBeforeAnother(AbstractPane, AbstractPane)
     */
    private void moveChildGroupBeforeAnother(final AbstractPaneGroup moveWhat, final AbstractPaneGroup moveBefore)
    {
        final int idxWhat = groups.indexOf(moveWhat);
        final int idxBefore = groups.indexOf(moveBefore);
        CheckArgument.notEquals(idxWhat, -1, "group moveWhat is not child of this group");
        CheckArgument.notEquals(idxBefore, -1, "group moveBefore is not child of this group");

        if (idxWhat > idxBefore)
        {
            groups.add(idxBefore, groups.remove(idxWhat));
        }
    }

    @Override
    protected AbstractPane traverseHierarchy(final Predicate<AbstractPane> terminalCondition)
    {
        if (terminalCondition.test(this))
        {
            return this;
        }
        for (final AbstractPane c : childs)
        {
            final AbstractPane result = c.traverseHierarchy(terminalCondition);
            if (result != null)
            {
                return result;
            }
        }
        return null;
    }

    @Override
    protected void drawSelf(final RenderContext ctx, final double mx, final double my)
    {
        ctx.pushTranslate(alignedX, alignedY);

        final double mxC = mx - alignedX, myC = my - alignedY;
        for (final AbstractPane c : childs)
        {
            c.draw(ctx, mxC, myC);
        }

        ctx.pop();
    }

    private boolean eventReverseIterator(final double mx, final double my, final MouseEventCallback callback)
    {
        final ListIterator<AbstractPaneGroup> it = groups.listIterator(groups.size());
        final double mxChild = mx - alignedX;
        final double myChild = my - alignedY;
        while (it.hasPrevious())
        {
            final AbstractPaneGroup child = it.previous();
            if (child.isPointInPane(mxChild, myChild))
            {
                return callback.accept(child, mxChild, myChild);
            }
        }
        return false;
    }

    /**
     * Called on mouse button release
     * 
     * @param  button mouse button, can be compared with static instance
     * @param  mx     mouse x relative to parent
     * @param  my     mouse y relative to parent
     * @return        true if event was used or propagation needs to be stopped
     */
    protected boolean processMouseButton(final MouseButton button, final double mx, final double my)
    {
        return eventReverseIterator(mx, my, (child, mxChild, myChild) -> child.processMouseButton(button, mxChild, myChild));
    }

    /**
     * @param  wheel minus for down, plus for up
     * @param  mx    mouse x relative to parent
     * @param  my    mouse y relative to parent
     * @return       true if event was used or propagation needs to be stopped
     */
    protected boolean processMouseScroll(final double wheel, final double mx, final double my)
    {
        return eventReverseIterator(mx, my, (child, mxChild, myChild) -> child.processMouseScroll(wheel, mxChild, myChild));
    }

    /**
     * @param  mx     mouse start x relative to parent
     * @param  my     mouse start y relative to parent
     * @param  deltaX relative x
     * @param  deltaY relative y
     * @return        true if event was used or propagation needs to be stopped
     */
    protected boolean processMouseDrag(final double mx, final double my, final double deltaX, final double deltaY)
    {
        return eventReverseIterator(mx, my, (child, mxChild, myChild) -> child.processMouseDrag(mxChild, myChild, deltaX, deltaY));
    }

    public static class PaneGroup extends AbstractPaneGroup implements RenderSetters, AccessibleId
    {
        public static final String ID = "group";
        public static final ParsingCodec<PaneGroup> CODEC = ParsingCodec.of(ID, PaneGroup::new, AbstractPane.CODEC).parseChilds();

        public PaneGroup(final AbstractPaneGroup parent)
        {
            super(parent);
        }
    }

    public final static class RootPaneGroup extends AbstractPaneGroup implements SizeSetters
    {
        final HoverManager hoverManager = new HoverManager();
        // TODO: pane focus

        public RootPaneGroup(final UiWindow window)
        {
            super(window);
            super.parent = this;
        }

        // manually add size getters

        public int getWidth()
        {
            return width;
        }

        public int getHeight()
        {
            return height;
        }

        public void renderWithoutMouse(final RenderContext ctx)
        {
            final int illegalMousePos = Integer.MIN_VALUE / 256;
            render(ctx, illegalMousePos, illegalMousePos);
        }

        public void render(final RenderContext ctx, final double mx, final double my)
        {
            super.draw(ctx, mx, my);
            hoverManager.tick();
        }
    }
}
