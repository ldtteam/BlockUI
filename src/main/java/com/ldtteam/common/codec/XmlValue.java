package com.ldtteam.common.codec;

import com.mojang.serialization.DynamicOps;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Sealed interface representing an XML value in memory, used by {@link XmlOps}.
 * <h2>Variants</h2>
 * <ul>
 * <li>{@link XmlNull} — singleton representing absence (maps to {@link DynamicOps#empty()})
 * <li>{@link XmlText} — a primitive value stored as text (string, number, or boolean)
 * <li>{@link XmlElement} — a structured element with tag name, ordered attributes, and child nodes</ul>
 * <h2>Immutability</h2>
 * <p>All instances are immutable. Modification methods (e.g., {@code withAttribute}, {@code withChild}) return new instances. This
 * enables safe sharing across threads without synchronization.
 * <h2>Reserved Constants</h2>
 * <ul>
 * <li>{@link #TYPE_KEY} ({@code "type"}) — map key encoded as the element's tag name
 * <li>{@link #TEXT_KEY} ({@code "text"}) — map key encoded as inline text content, not an attribute
 * <li>{@link #CHILDREN_KEY} ({@code "children"}) — list key whose items are inlined as direct children</ul>
 *
 * @see XmlOps
 */
public sealed interface XmlValue permits XmlValue.XmlNull, XmlValue.XmlText, XmlValue.XmlElement
{
    XmlNull NULL = new XmlNull();

    /** Map key that becomes the element's tag name instead of being stored as an attribute. */
    String TYPE_KEY = "type";

    /**
     * Map key whose value is stored as inline text content (an {@link XmlText} child node). Enables output like
     * {@code <note>Hello</note>} instead of {@code <note text="Hello"/>}.
     */
    String TEXT_KEY = "text";

    /**
     * Map key whose list value is encoded as repeated {@code <children type="..."/>} child elements. A backward-compat fallback in
     * {@link XmlOps} collects all element children if no child has the tag "children".
     */
    String CHILDREN_KEY = "children";

    /**
     * Represents an absent/empty value.
     */
    record XmlNull() implements XmlValue
    {
        @Override
        public String toString()
        {
            return "XmlNull";
        }
    }

    /**
     * Represents a primitive value stored as text (string, number, boolean).
     */
    record XmlText(String value) implements XmlValue
    {
        public XmlText
        {
            Objects.requireNonNull(value, "value");
        }

        @Override
        public String toString()
        {
            return "XmlText(\"" + value + "\")";
        }
    }

    /**
     * Represents a structured XML element with a tag name, attributes (ordered), and child elements.
     * <p>When used as a map: attributes hold simple key-value pairs, children hold complex values (their tag name is the map key).
     * <p>When used as a list: children are the list items (tag name is irrelevant to the list itself, the parent's key provides the
     * tag for child items).
     */
    record XmlElement(String tag, Map<String, String> attributes, List<XmlValue> children) implements XmlValue
    {
        /** Default tag name for map/record elements that have no explicit type. */
        public static final String DEFAULT_TAG = "object";

        /** Tag name used for list elements. */
        public static final String LIST_TAG = "list";

        public XmlElement
        {
            Objects.requireNonNull(tag, "tag");
            attributes = Collections.unmodifiableMap(new LinkedHashMap<>(attributes));
            children = List.copyOf(children);
        }

        public XmlElement(final String tag)
        {
            this(tag, Map.of(), List.of());
        }

        public XmlElement(final String tag, final Map<String, String> attributes)
        {
            this(tag, attributes, List.of());
        }

        /**
         * Returns an attribute value, or null if absent.
         */
        @Nullable
        public String getAttribute(final String name)
        {
            return attributes.get(name);
        }

        /**
         * Returns a new element with an additional/updated attribute.
         */
        public XmlElement withAttribute(final String name, final String value)
        {
            final LinkedHashMap<String, String> newAttrs = new LinkedHashMap<>(attributes);
            newAttrs.put(name, value);
            return new XmlElement(tag, newAttrs, children);
        }

        /**
         * Returns a new element without the specified attribute.
         */
        public XmlElement withoutAttribute(final String name)
        {
            if (!attributes.containsKey(name))
            {
                return this;
            }
            final LinkedHashMap<String, String> newAttrs = new LinkedHashMap<>(attributes);
            newAttrs.remove(name);
            return new XmlElement(tag, newAttrs, children);
        }

        /**
         * Returns a new element with an additional child.
         */
        public XmlElement withChild(final XmlValue child)
        {
            final List<XmlValue> newChildren = new ArrayList<>(children);
            newChildren.add(child);
            return new XmlElement(tag, attributes, newChildren);
        }

        /**
         * Returns a new element with additional children.
         */
        public XmlElement withChildren(final List<XmlValue> additional)
        {
            final List<XmlValue> newChildren = new ArrayList<>(children);
            newChildren.addAll(additional);
            return new XmlElement(tag, attributes, newChildren);
        }

        /**
         * Returns a new element without children that match the given tag.
         */
        public XmlElement withoutChildrenByTag(final String childTag)
        {
            final List<XmlValue> newChildren =
                children.stream().filter(c -> !(c instanceof XmlElement e && e.tag.equals(childTag))).toList();
            return newChildren.size() == children.size() ? this : new XmlElement(tag, attributes, newChildren);
        }

        /**
         * Returns the concatenated text content (XmlText children), or null if none.
         */
        @Nullable
        public String getTextContent()
        {
            final StringBuilder sb = new StringBuilder();
            for (final XmlValue child : children)
            {
                if (child instanceof final XmlText t)
                {
                    sb.append(t.value());
                }
            }
            return sb.isEmpty() ? null : sb.toString();
        }

        /**
         * Returns a new element with its text content set (replaces any existing XmlText children).
         */
        public XmlElement withTextContent(final String text)
        {
            final List<XmlValue> newChildren = new ArrayList<>(children.stream().filter(c -> !(c instanceof XmlText)).toList());
            newChildren.add(0, new XmlText(text));
            return new XmlElement(tag, attributes, newChildren);
        }

        /**
         * Returns a new element with all XmlText children removed.
         */
        public XmlElement withoutTextContent()
        {
            final List<XmlValue> newChildren = children.stream().filter(c -> !(c instanceof XmlText)).toList();
            return new XmlElement(tag, attributes, newChildren);
        }

        /**
         * Returns a new element with a different tag name.
         */
        public XmlElement withTag(final String newTag)
        {
            return new XmlElement(newTag, attributes, children);
        }

        /**
         * Returns true if this is a list element.
         */
        public boolean isList()
        {
            return LIST_TAG.equals(tag);
        }

        /**
         * Returns child elements matching the given tag.
         */
        public List<XmlElement> getChildrenByTag(final String childTag)
        {
            return children.stream()
                .filter(c -> c instanceof XmlElement e && e.tag.equals(childTag))
                .map(c -> (XmlElement) c)
                .toList();
        }

        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder("XmlElement(<").append(tag);
            attributes.forEach((k, v) -> sb.append(' ').append(k).append("=\"").append(v).append('"'));
            if (children.isEmpty())
            {
                sb.append("/>");
            }
            else
            {
                sb.append("> ").append(children.size()).append(" children)");
            }
            return sb.toString();
        }
    }
}
