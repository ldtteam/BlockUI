package com.ldtteam.blockui;

import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.mod.Log;
import com.ldtteam.blockui.views.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Utilities to load xml files.
 */
public final class Loader extends SimplePreparableReloadListener<Map<ResourceLocation, PaneParams>>
{
    public static final Loader INSTANCE = new Loader();

    private final Map<ResourceLocation, Function<PaneParams, ? extends Pane>> paneFactories = new HashMap<>();

    private Map<ResourceLocation, PaneParams> xmlCache = new HashMap<>();

    private Loader()
    {
        register("view", View::new);
        register("group", Group::new);
        register("scrollgroup", ScrollingGroup::new);
        register("list", ScrollingList::new);
        register("text", Text::new);
        register("button", Button::construct);
        register("buttonimage", Button::construct); // TODO: remove, but we don't want to deal with xml changes now
        register("toggle", ToggleButton::new);
        register("label", Text::new); // TODO: remove, but we don't want to deal with xml changes now
        register("input", TextFieldVanilla::new);
        register("image", Image::new);
        register("imagerepeat", ImageRepeatable::new);
        register("box", Box::new);
        register("itemicon", ItemIcon::new);
        register("entityicon", EntityIcon::new);
        register("switch", SwitchView::new);
        register("dropdown", DropDownList::new);
        register("overlay", OverlayView::new);
        register("gradient", Gradient::new);
        register("zoomdragview", ZoomDragView::new);
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
        final ResourceLocation key = new ResourceLocation(name);

        if (paneFactories.containsKey(key))
        {
            throw new IllegalArgumentException("Duplicate pane type '" + name + "' when registering Pane class method.");
        }

        paneFactories.put(key, factoryMethod);
    }

    /**
     * Uses the loaded parameters to construct a new Pane tree
     *
     * @param params the parameters for the new pane and its children
     * @return the created Pane
     */
    private Pane createFromPaneParams(final PaneParams params)
    {
        final ResourceLocation paneType = new ResourceLocation(params.getType());

        if (paneFactories.containsKey(paneType))
        {
            return paneFactories.get(paneType).apply(params);
        }

        if (paneFactories.containsKey(new ResourceLocation(paneType.getPath())))
        {
            Log.getLogger().warn("Namespace override for " + paneType.getPath() + " not found. Using default.");
            return paneFactories.get(new ResourceLocation(paneType.getPath())).apply(params);
        }

        Log.getLogger().error("There is no factory method for " + paneType.getPath());
        return null;
    }

    /**
     * Create a pane from its xml parameters.
     *
     * @param params xml parameters.
     * @param parent parent view.
     * @return the new pane.
     */
    public static Pane createFromPaneParams(final PaneParams params, final View parent)
    {
        if ("layout".equalsIgnoreCase(params.getType()))
        {
            params.getResource("source", r -> createFromXMLFile(r, parent));
            return null;
        }

        if (parent instanceof BOWindow && params.getType().equals("window"))
        {
            ((BOWindow) parent).loadParams(params);
            parent.parseChildren(params);
            return parent;
        }
        else if (parent instanceof View && params.getType().equals("window")) // layout
        {
            parent.parseChildren(params);
            return parent;
        }
        else
        {
            params.setParentView(parent);
            final Pane pane = INSTANCE.createFromPaneParams(params);

            if (pane != null)
            {
                pane.putInside(parent);
                pane.parseChildren(params);
            }
            return pane;
        }
    }

    /**
     * Parse XML contains in a ResourceLocation into contents for a Window.
     *
     * @param resource xml as a {@link ResourceLocation}.
     * @param parent   parent view.
     */
    public static void createFromXMLFile(final ResourceLocation resource, final View parent)
    {
        if (INSTANCE.xmlCache.containsKey(resource))
        {
            try
            {
                createFromPaneParams(INSTANCE.xmlCache.get(resource), parent);
            }
            catch (Exception e)
            {
                throw new RuntimeException("Can't parse xml at: " + resource.toString(), e);
            }
        }
        else
        {
            throw new RuntimeException("Gui at \"" + resource.toString() + "\" was not found!");
            // TODO: create "missing gui" gui and don't crash?
        }
    }

    @Override
    protected Map<ResourceLocation, PaneParams> prepare(final ResourceManager rm, final ProfilerFiller profiler)
    {
        profiler.startTick();
        profiler.push("BlockUI-xml-lookup-parsing");

        final Map<ResourceLocation, PaneParams> foundXmls = new HashMap<>();
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

        rm.listResources("gui", pathStr -> pathStr.endsWith(".xml")).forEach(rl -> {
            final Document doc;
            try
            {
                final Resource res = rm.getResource(rl);
                doc = documentBuilder.parse(res.getInputStream());
            }
            catch (final IOException | SAXException e)
            {
                Log.getLogger().error("Failed to load xml at: " + rl.toString(), e);
                return;
            }

            doc.getDocumentElement().normalize();
            foundXmls.put(rl, new PaneParams(doc.getDocumentElement()));
        });

        profiler.pop();
        profiler.endTick();
        return foundXmls;
    }

    @Override
    protected void apply(final Map<ResourceLocation, PaneParams> foundXmls, final ResourceManager rm, final ProfilerFiller profiler)
    {
        xmlCache = foundXmls;
    }
}
