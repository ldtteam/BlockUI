package com.ldtteam.blockui;

import com.ldtteam.blockui.mod.Log;
import com.ldtteam.blockui.views.View;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.MutableComponent;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Special parameters for the panes.
 */
public class PaneParams
{
    private final Map<String, Object> propertyCache = new HashMap<>();
    private final List<PaneParams>    children;
    private final Node                node;
    private       View                parentView;

    /**
     * Instantiates the pane parameters.
     *
     * @param n the node.
     */
    public PaneParams(final Node n)
    {
        node = n;
        children = new ArrayList<>(node.getChildNodes().getLength());
    }

    /**
     * Get the node type.
     *
     * @return the name of the node.
     */
    public String getType()
    {
        return node.getNodeName();
    }

    /**
     * Get the parent for this pane.
     *
     * @return the parent.
     */
    public View getParentView()
    {
        return parentView;
    }

    /**
     * Set the parent for this pane.
     *
     * @param parent the new parent.
     */
    public void setParentView(final View parent)
    {
        parentView = parent;
    }

    /**
     * Get the width of the parent, if any. Defaults to 0 if no parent has been set.
     *
     * @return the width.
     */
    public int getParentWidth()
    {
        return parentView != null ? parentView.getInteriorWidth() : 0;
    }

    /**
     * Get the height of the parent, if any. Defaults to 0 if no parent has been set.
     *
     * @return the height.
     */
    public int getParentHeight()
    {
        return parentView != null ? parentView.getInteriorHeight() : 0;
    }

    /**
     * Get the left position of the parent, if any. Defaults to 0 if no parent has been set.
     *
     * @return the left position.
     */
    public int getParentLeft()
    {
        return parentView != null ? parentView.x : 0;
    }

    /**
     * Get the top position of the parent, if any. Defaults to 0 if no parent has been set.
     *
     * @return the top position.
     */
    public int getParentTop()
    {
        return parentView != null ? parentView.y : 0;
    }

    public List<PaneParams> getChildren()
    {
        if (!children.isEmpty()) return children;

        Node child = node.getFirstChild();
        while (child != null)
        {
            if (child.getNodeType() == Node.ELEMENT_NODE)
            {
                children.add(new PaneParams(child));
            }
            child = child.getNextSibling();
        }

        return children;
    }

        public String getText()
    {
        return node.getTextContent().trim();
    }

    private Node getAttribute(final String name)
    {
        return node.getAttributes().getNamedItem(name);
    }

    public boolean hasAttribute(final String name)
    {
        return node.getAttributes().getNamedItem(name) != null;
    }

    /**
     * Finds an attribute by name from the XML node
     * and parses it using the provided parser method
     * @param name the attribute name to search for
     * @param parser the parser to convert the attribute to its property
     * @param def the default value if none can be found
     * @param <T> the type of value to work with
     * @return the parsed value
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String name, Function<String, T> parser, T def)
    {
        T result = null;

        if (propertyCache.containsKey(name))
        {
            try
            {
                result = (T) propertyCache.get(name);
                return result != null ? result : def;
            }
            catch (ClassCastException cce)
            {
                Log.getLogger().warn("Invalid property: previous value of key does not match type.");
            }
        }

        final Node attr = getAttribute(name);
        if (attr != null) result = parser.apply(attr.getNodeValue());

        propertyCache.put(name, result);
        return result != null ? result : def;
    }

    /**
     * Get the string attribute.
     *
     * @param name the name to search.
     * @return the attribute.
     */
    @Nullable
    public String getString(final String name)
    {
        return getString(name, null);
    }

    /**
     * Get the String attribute from the name and revert to the default if not present.
     *
     * @param name      the name.
     * @param def the default value if none can be found
     * @return the String.
     */
    public String getString(final String name, final String def)
    {
        return getProperty(name, String::toString, def);
    }

    /**
     * Get the resource location from the name
     * @param name the attribute name
     * @param def  the default value to fall back to
     * @return the parsed resource location
     */
    public ResourceLocation getResource(final String name, final ResourceLocation def)
    {
        return getProperty(name, Parsers.RESOURCE, def);
    }

    /**
     * Get the resource location from the name
     * @param name the attribute name
     * @param def  the default value to fall back to
     * @return the parsed resource location
     * @deprecated use {@link PaneParams#getResource(String, ResourceLocation)} instead.
     */
    @Deprecated(forRemoval = true, since = "1.20.1")
    public ResourceLocation getResource(final String name, final String def)
    {
        return getResource(name, new ResourceLocation(def));
    }

    /**
     * Get the resource location from the name
     * @param name the attribute name
     * @return the parsed resource location
     */
    public ResourceLocation getResource(final String name)
    {
        return getResource(name, (ResourceLocation) null);
    }

    /**
     * Get the resource location from the name and load it
     * @param name the attribute name
     * @param loader a method to act upon the resource if it is not blank or null
     * @return the parsed resource location (or null if it couldn't be parsed)
     */
    @Nullable
    public ResourceLocation getResource(final String name, final Consumer<ResourceLocation> loader)
    {
        final ResourceLocation rl = getResource(name);
        if (rl != null && !rl.getPath().isEmpty())
        {
            loader.accept(rl);
            return rl;
        }
        return null;
    }

    /**
     * Get the text content with potential newlines from the name.
     *
     * @param name the name
     * @return the parsed and localized list
     */
    public List<MutableComponent> getMultilineText(final String name)
    {
        return getMultilineText(name, Collections.emptyList());
    }

    /**
     * Get the text content with potential newlines from the name and revert to the default if not present.
     *
     * @param name the name
     * @param def the default value if none can be found
     * @return the parsed and localized list
     */
    public List<MutableComponent> getMultilineText(final String name, List<MutableComponent> def)
    {
        return getProperty(name, Parsers.MULTILINE, def);
    }

    /**
     * Get the localized String attribute from the name and revert to the default if not present.
     *
     * @param name      the name.
     * @param def the default value if none can be found
     * @return the localized text component.
     */
    public MutableComponent getTextComponent(final String name, final MutableComponent def)
    {
        return getProperty(name, Parsers.TEXT, def);
    }

    /**
     * Get the integer attribute from name and revert to the default if not present.
     *
     * @param name     the name.
     * @param def the default value if none can be found
     * @return the int.
     */
    public int getInteger(final String name, final int def)
    {
        return getProperty(name, Parsers.INT, def);
    }

    /**
     * Get the float attribute from name and revert to the default if not present.
     *
     * @param name     the name.
     * @param def the default value if none can be found
     * @return the float.
     */
    public float getFloat(final String name, final float def)
    {
        return getProperty(name, Parsers.FLOAT, def);
    }

    /**
     * Get the double attribute from name and revert to the default if not present.
     *
     * @param name     the name.
     * @param def the default value if none can be found
     * @return the double.
     */
    public double getDouble(final String name, final double def)
    {
        return getProperty(name, Parsers.DOUBLE, def);
    }

    /**
     * Get the boolean attribute from name and revert to the default if not present.
     *
     * @param name     the name.
     * @param def the default value if none can be found
     * @return the boolean.
     */
    public boolean getBoolean(final String name, final boolean def)
    {
        return getProperty(name, Parsers.BOOLEAN, def);
    }

    /**
     * Get the boolean attribute from name and class and revert to the default if not present.
     *
     * @param name      the name.
     * @param clazz     the class.
     * @param def the default value if none can be found
     * @param <T>       the type of class.
     * @return the enum attribute.
     */
    public <T extends Enum<T>> T getEnum(final String name, final Class<T> clazz, final T def)
    {
        return getProperty(name, Parsers.ENUM(clazz), def);
    }

    /**
     * Get the scalable integer attribute from name and revert to the default if not present.
     *
     * @param name  the name
     * @param scale the total value to be a fraction of
     * @param def the default value if none can be found
     * @return the parsed value
     */
    public int getScaledInteger(String name, final int scale, final int def)
    {
        return getProperty(name, Parsers.SCALED(scale), def);
    }

    /**
     * Parses two scalable values and processes them through an applicant
     *
     * @param name the attribute name to search for
     * @param scaleX the first fraction total
     * @param scaleY the second fraction total
     * @param applier the method to utilise the result values
     */
    public void getScaledInteger(final String name, final int scaleX, final int scaleY, Consumer<List<Integer>> applier)
    {
        List<Integer> results = Parsers.SCALED(scaleX, scaleY).apply(getString(name));
        if (results != null) applier.accept(results);
    }

    /**
     * Get the color attribute from name and revert to the default if not present.
     *
     * @param name the name.
     * @param def  the default value if none can be found
     * @return int color value.
     */
    public int getColor(final String name, final int def)
    {
        return getProperty(name, Parsers.COLOR, def);
    }

    /**
     * Fetches a property and runs the result through a given method.
     * Commonly used for shorthand properties.
     * @param name the name of the attribute to retrieve
     * @param parser the parser applied to each part
     * @param parts the maximum number of parts to fill to if less are given
     * @param applier the method to utilise the parsed values
     * @param <T> the type of each part
     */
    public <T> void applyShorthand(String name, Function<String, T> parser, int parts, Consumer<List<T>> applier)
    {
        List<T> results = Parsers.shorthand(parser, parts).apply(getString(name));
        if (results != null) applier.accept(results);
    }

    /**
     * Checks if any of attribute names are present and return first found, else return default.
     *
     * @param def the default value if none can be found
     * @param attributes attributes names to check
     * @return first found attribute or default
     */
    public String hasAnyAttribute(final String def, final String... attributes)
    {
        final NamedNodeMap nodeMap = node.getAttributes();
        for (final String attr : attributes)
        {
            if (nodeMap.getNamedItem(attr) != null) // inlined hasAttribute
            {
                return attr;
            }
        }
        return def;
    }
}
