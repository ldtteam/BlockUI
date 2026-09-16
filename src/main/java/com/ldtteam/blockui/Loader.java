package com.ldtteam.blockui;

import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.mod.Log;
import com.ldtteam.blockui.views.*;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Utilities to load xml files.
 */
public final class Loader extends SimplePreparableReloadListener<Map<ResourceLocation, PaneParams>>
{
    public static final Loader INSTANCE = new Loader();

    private static final String GUI_FOLDER = "gui";

    private static final String LAYOUT_ID = "layout";
    private static final String WINDOW_ID = "window";
    private static final String SLOT_ID = "slot";
    private static final String SLOT_ATTR = "slot";

    private final Map<String, Function<PaneParams, ? extends Pane>> paneFactories = new HashMap<>();

    private Map<ResourceLocation, PaneParams> xmlCache = new HashMap<>();

    private Loader()
    {
        register("view", View::new);
        register("group", Group::new);
        register("scrollgroup", ScrollingGroup::new);
        register("list", ScrollingList::new);
        register("text", Text::new);
        register("button", ButtonImage::new);
        register("toggle", ToggleButton::new);
        register("input", TextFieldVanilla::new);
        register("image", Image::new);
        register("box", Box::new);
        register("itemicon", ItemIcon::parse);
        register("entityicon", EntityIcon::new);
        register("switch", SwitchView::new);
        register("dropdown", DropDownList::new);
        register("overlay", OverlayView::new);
        register("gradient", Gradient::new);
        register("zoomdragview", ZoomDragView::new);
        register("checkbox", CheckBox::new);
    }

    /**
     * registers an element definition class so it can be used in
     * gui definition files
     *
     * @param name          the tag name of the element in the definition file
     * @param factoryMethod the constructor/method to create the element Pane
     */
    public void register(final String name, final Function<PaneParams, ? extends Pane> factoryMethod)
    {
        if (paneFactories.containsKey(name))
        {
            throw new IllegalArgumentException("Duplicate pane type '" + name + "' when registering Pane class method.");
        }

        paneFactories.put(name, factoryMethod);
    }

    /**
     * Parse XML contained in a ResourceLocation into contents for a Window.
     * <p>
     * The root tag of the XML file must be either {@code <window>} or {@code <layout>}.
     *
     * @param resource XML as a {@link ResourceLocation}.
     * @param parent   parent view.
     */
    @SuppressWarnings("UnusedReturnValue")
    public static Pane createFromXMLFile(final @NotNull ResourceLocation resource, final @NotNull View parent)
    {
        return createFromXMLFile(resource, parent, LayoutContext.EMPTY);
    }

    private static Pane createFromXMLFile(
        final @NotNull ResourceLocation resource,
        final @NotNull View parent,
        final @NotNull LayoutContext context)
    {
        final PaneParams rootParams = INSTANCE.xmlCache.get(resource);
        if (rootParams == null)
        {
            throw new RuntimeException("Gui at \"" + resource + "\" was not found!");
        }

        final String rootType = rootParams.getType();
        if (!WINDOW_ID.equalsIgnoreCase(rootType) && !LAYOUT_ID.equalsIgnoreCase(rootType))
        {
            throw new RuntimeException(
                "XML file \"" + resource + "\" has invalid root tag <" + rootType +
                ">. Only <window> or <layout> are valid root tags.");
        }

        try
        {
            return createFromPaneParams(rootParams, parent, context);
        }
        catch (final Exception e)
        {
            throw new RuntimeException("Can't parse xml at: " + resource, e);
        }
    }

    /**
     * Uses the loaded parameters to construct a new pane tree.
     * <p>
     * This method is for internal pane creation (children of views, scrolling list rows, etc.)
     * and does not enforce root tag restrictions. Use {@link #createFromXMLFile} to load a
     * full XML file, which enforces that the root tag is {@code <window>} or {@code <layout>}.
     *
     * @param params the parameters for the new pane and its children
     * @param parent parent view.
     * @return the new pane, or {@code null} for transparent directives ({@code <layout>}, {@code <slot>}).
     */
    public static Pane createFromPaneParams(final @NotNull PaneParams params, final @NotNull View parent)
    {
        return createFromPaneParams(params, parent, LayoutContext.EMPTY);
    }

    private static Pane createFromPaneParams(
        final @NotNull PaneParams params,
        final @NotNull View parent,
        final @NotNull LayoutContext context)
    {
        final String type = params.getType();

        // <layout> — transparent directive, never creates a pane
        if (LAYOUT_ID.equalsIgnoreCase(type))
        {
            final ResourceLocation source = params.getResource("source");
            if (source != null && !source.getPath().isEmpty())
            {
                // Use-site: <layout source="..."> — load referenced file with slot content and optional pos override
                createFromXMLFile(source, parent, buildLayoutContext(params, parent));
            }
            else
            {
                // File root: <layout> — expand single direct child transparently into parent, applying pos override if any
                final List<PaneParams> children = params.getChildren();
                if (children.size() > 1)
                {
                    throw new RuntimeException("Layout file root <layout> must have at most one direct child, found " + children.size());
                }
                if (!children.isEmpty())
                {
                    final Pane pane = createFromPaneParams(children.get(0), parent, context);
                    if (pane != null && context.hasPosOverride())
                    {
                        pane.setPosition(context.posOverride()[0], context.posOverride()[1]);
                    }
                }
            }
            return null;
        }

        // <slot> — inject caller-provided slot content, or fall back to the slot's own children
        if (SLOT_ID.equalsIgnoreCase(type))
        {
            final String slotName = params.getString("name", LayoutContext.DEFAULT_SLOT_KEY);
            final PaneParams slotContent = context.get(slotName);
            if (slotContent == null)
            {
                // No caller content for this slot — render fallback children if any
                parent.parseChildren(params, LayoutContext.EMPTY, child -> createFromPaneParams(child, parent, LayoutContext.EMPTY));
            }
            else
            {
                createFromPaneParams(slotContent, parent, LayoutContext.EMPTY);
            }
            return null;
        }

        // <window> root tag
        if (WINDOW_ID.equalsIgnoreCase(type))
        {
            if (parent instanceof final BOWindow window)
            {
                window.loadParams(params);
            }
            parent.parseChildren(params, context, child -> createFromPaneParams(child, parent, context));
            return parent;
        }

        // Normal pane creation
        params.setParentView(parent);

        final String name = params.getType();
        if (!INSTANCE.paneFactories.containsKey(name))
        {
            throw new RuntimeException(String.format("There is no factory method for %s", name));
        }

        final Pane pane = INSTANCE.paneFactories.get(name).apply(params);
        if (pane != null)
        {
            pane.putInside(parent);

            if (pane instanceof final View view)
            {
                view.parseChildren(params, context, child -> createFromPaneParams(child, view, context));
            }
        }
        return pane;
    }

    /**
     * Builds a {@link LayoutContext} from a {@code <layout source="...">} use-site tag.
     * Collects slot content from inline children, and extracts an optional pos override
     * from {@code pos}, {@code x}/{@code y} attributes on the use-site tag itself.
     * Children without a {@code slot} attribute are assigned to the {@value LayoutContext#DEFAULT_SLOT_KEY} slot.
     * Duplicate slot names produce a warning; the first child wins.
     */
    private static LayoutContext buildLayoutContext(final PaneParams layoutUseSiteTag, final View parent)
    {
        // Parse pos override from the use-site tag
        int[] posOverride = null;
        final String posAttr = layoutUseSiteTag.getString("pos");
        final String xAttr = layoutUseSiteTag.getString("x");
        final String yAttr = layoutUseSiteTag.getString("y");
        if (posAttr != null || xAttr != null || yAttr != null)
        {
            layoutUseSiteTag.setParentView(parent);
            final int[] resolved = {parent.x, parent.y};
            layoutUseSiteTag.getScaledInteger("pos", parent.getX(), parent.getY(), a -> {
                resolved[0] = a.get(0);
                resolved[1] = a.get(1);
            });
            if (xAttr != null)
            {
                layoutUseSiteTag.getScaledInteger("x", parent.getX(), parent.getY(), a -> resolved[0] = a.get(0));
            }
            if (yAttr != null)
            {
                layoutUseSiteTag.getScaledInteger("y", parent.getX(), parent.getY(), a -> resolved[1] = a.get(0));
            }
            posOverride = resolved;
        }

        // Collect slot content from inline children
        final List<PaneParams> children = layoutUseSiteTag.getChildren();
        if (children.isEmpty())
        {
            return new LayoutContext(Map.of(), posOverride);
        }

        final Map<String, PaneParams> map = new HashMap<>();
        for (final PaneParams child : children)
        {
            final String slotName = child.getString(SLOT_ATTR, LayoutContext.DEFAULT_SLOT_KEY);
            if (map.containsKey(slotName))
            {
                Log.getLogger().warn("Duplicate slot content for slot '{}' in <layout> tag, ignoring duplicate.", slotName);
                continue;
            }
            map.put(slotName, child);
        }
        return new LayoutContext(Collections.unmodifiableMap(map), posOverride);
    }

    @Override
    @NotNull
    protected Map<ResourceLocation, PaneParams> prepare(final @NotNull ResourceManager resourceManager, final ProfilerFiller profiler)
    {
        profiler.startTick();
        profiler.push("BlockUI-xml-lookup-parsing");

        final Map<ResourceLocation, PaneParams> foundXMLs = new HashMap<>();
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder documentBuilder;
        try
        {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        }
        catch (final ParserConfigurationException e)
        {
            profiler.pop();
            profiler.endTick();
            throw new RuntimeException(e);
        }

        resourceManager.listResources(GUI_FOLDER, rl -> rl.getPath().endsWith(".xml")).forEach((rl, res) -> {
            final Document doc;
            try (final InputStream is = res.open())
            {
                doc = documentBuilder.parse(is);
            }
            catch (final IOException | SAXException e)
            {
                Log.getLogger().error("Failed to load xml at: {}", rl.toString(), e);
                return;
            }

            doc.getDocumentElement().normalize();
            foundXMLs.put(rl, new PaneParams(doc.getDocumentElement()));
        });

        profiler.pop();
        profiler.endTick();
        return foundXMLs;
    }

    @Override
    protected void apply(final @NotNull Map<ResourceLocation, PaneParams> foundXMLs, final @NotNull ResourceManager resourceManager, final @NotNull ProfilerFiller profiler)
    {
        xmlCache = foundXMLs;
    }
}
