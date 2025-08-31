package com.ldtteam.blockui;

import com.ldtteam.blockui.AbstractPane.RenderSetters;
import com.ldtteam.blockui.AbstractPaneGroup.RootPaneGroup;
import com.ldtteam.blockui.context.DelayedLevel;
import com.ldtteam.blockui.context.gui.ScreenAdapter;
import com.ldtteam.blockui.element.DelegatedText;
import com.ldtteam.blockui.element.shadow.Tooltip;
import com.ldtteam.blockui.util.Crash;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

/**
 * API class, root ui element holding entire tree and few attributes
 */
public class UiWindow
{
    private final RootPaneGroup ui = new RootPaneGroup(this);
    private final ResourceLocation resourcePath;

    private Object renderingAdapter = null;
    private MutableComponent title = Component.empty();

    public UiWindow(final ResourceLocation resourcePath)
    {
        this.resourcePath = resourcePath;
    }

    /**
     * @param title window title, eg. used for narrator
     */
    public final void setTitle(final MutableComponent title)
    {
        this.title = title;
    }

    /**
     * @return window title, eg. used for narrator
     */
    public final MutableComponent getTitle()
    {
        return title;
    }

    /**
     * Immediately open this window as ui
     */
    public final void openAsPlayerGui()
    {
        final ScreenAdapter screen = new ScreenAdapter(this);
        setRenderingAdapter(screen);
        Minecraft.getInstance().pushGuiLayer(screen);
    }

    public final void closeIfPlayerGui()
    {
        if (!(renderingAdapter instanceof ScreenAdapter))
        {
            throw Crash.argument("Not a screen gui");
        }
        Minecraft.getInstance().popGuiLayer();
        setRenderingAdapter(null);
    }

    public final void openAsIngameOverlay()
    {
        // TODO: impl
    }

    public final void openAsEntityHover()
    {
        // TODO: impl
    }

    /**
     * @return since ids are unique then finding pane of type is just matter of casting
     */
    public final <T extends AbstractPane> Optional<T> findPaneOfTypeById(final String id, final Class<T> type)
    {
        final AbstractPane byId = ui.traverseHierarchy(p -> id.equals(p.id));
        return Optional.ofNullable(type.isInstance(byId) ? type.cast(byId) : null);
    }

    /**
     * @return since ids are unique then finding pane of type is just matter of casting
     */
    public final <T extends AbstractPane> Optional<T> findFirstPaneOfType(final Class<T> type)
    {
        final AbstractPane byType = ui.traverseHierarchy(p -> type.isInstance(p));
        return Optional.ofNullable(type.cast(byType));
    }

    /**
     * @param hoverSource when mouse is over source element then target will show
     * @param hoverTarget what should be shown when mouse is over source
     */
    public final <T extends AbstractPane & RenderSetters> void setHoverFor(final AbstractPane hoverSource, final T hoverTarget)
    {
        final AtomicReference<DelayedLevel> previousLevel = new AtomicReference<>();
        new HoverBuilder().startAction(() -> {
            previousLevel.set(hoverTarget.renderLevel);
            hoverTarget.renderLevel = DelayedLevel.HOVER;
            hoverTarget.setVisible(true);
        }).tickAction(source -> hoverTarget.wasCursorInPaneLastFrame()).stopAction(() -> {
            hoverTarget.renderLevel = previousLevel.get();
            hoverTarget.setVisible(false);
        }).buildForPane(hoverSource);
    }

    /**
     * @param hoverSource when mouse is over source element then target will show
     */
    public final Tooltip<?, DelegatedText> createVanillaTooltipFor(final AbstractPane hoverSource)
    {
        final var tooltip = Tooltip.vanillaTextTooltip(hoverSource.parent);
        new HoverBuilder().startAction(() -> tooltip.visible = true)
            .stopAction(() -> tooltip.visible = false)
            .buildForPane(hoverSource);
        return tooltip;
    }

    public final void removeHoverFor(final AbstractPane pane)
    {
        ui.hoverManager.remove(pane);
    }

    private void setRenderingAdapter(final Object adapter)
    {
        if (renderingAdapter != null)
        {
            throw Crash.state("Trying to open one window instance twice");
        }
        renderingAdapter = adapter;
    }

    public final ResourceLocation getUiResourcePath()
    {
        return resourcePath;
    }

    public final RootPaneGroup getRootPane()
    {
        return ui;
    }

    public final Object getRenderingAdapter()
    {
        return renderingAdapter;
    }

    public class HoverBuilder
    {
        private Runnable onHoverStartAction = null;
        private Predicate<AbstractPane> onHoverTickAction = null;
        private Runnable onHoverStopAction = null;

        /**
         * @param onHoverStartAction called when hover is opening
         */
        public HoverBuilder startAction(final Runnable onHoverStartAction)
        {
            this.onHoverStartAction = onHoverStartAction;
            return this;
        }

        /**
         * @param onHoverTickAction return true if should keep hover alive when mouse is not over source pane, param = source pane
         */
        public HoverBuilder tickAction(final Predicate<AbstractPane> onHoverTickAction)
        {
            this.onHoverTickAction = onHoverTickAction;
            return this;
        }

        /**
         * @param onHoverStopAction called when hover is closing
         */
        public HoverBuilder stopAction(final Runnable onHoverStopAction)
        {
            this.onHoverStopAction = onHoverStopAction;
            return this;
        }

        /**
         * @param pane source pane which will trigger hover action
         */
        public HoverBuilder buildForPane(final AbstractPane pane)
        {
            ui.hoverManager.add(pane, onHoverStartAction, onHoverTickAction, onHoverStopAction);
            return this;
        }
    }
}
