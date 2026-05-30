package com.ldtteam.common.codec;

import com.ldtteam.common.codec.XmlValue.XmlElement;
import com.ldtteam.common.codec.XmlValue.XmlNull;
import com.ldtteam.common.codec.XmlValue.XmlText;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.ListBuilder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import javax.annotation.Nullable;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * {@link DynamicOps} implementation for XML, using {@link XmlValue} as the in-memory representation.
 *
 * Encoding Model
 *
 * <p>Maps/records become {@link XmlElement} instances. Encoding dispatches per-value:
 *
 * <ul>
 *   <li><b>Simple values</b> (primitives, all-text lists) → XML attributes on the element
 *   <li><b>Complex values</b> (nested maps, typed lists) → child elements; the map key becomes the child's tag name
 *   <li><b>Typed lists</b> (dispatch polymorphism) → wrapped in a single child element whose tag is the key, with each item as a sub-child
 * </ul>
 *
 * Reserved Keys
 *
 * <ul>
 *   <li>{@code "type"} — encoded as the element's tag name (not an attribute). Used by dispatch codecs
 *   <li>{@code "text"} — encoded as inline text content (an {@link XmlValue.XmlText} child), not an attribute. Enables {@code <note>Hello</note>} style output
 *   <li>{@code "children"} — list items are inlined as repeated {@code <children type="..."/>} child elements rather than wrapped in a container. Used for polymorphic child lists
 * </ul>
 *
 * Primitive Lists
 *
 * <p>{@code IntStream} and {@code LongStream} are encoded as compact bracketed strings: {@code [1, 2, 3]}. These are stored as
 * attribute values. On read, any attribute matching {@code [...]} is parsed as a list. Plain string values that happen to match this
 * pattern are escaped with a {@code \} prefix on write.
 *
 * List/Scalar Ambiguity
 *
 * <p>A single child element with a given tag is indistinguishable from a one-element list in the XML structure. The codec layer
 * resolves this via type knowledge (calling {@code getList()} vs {@code getMap()}). The generic {@code entries()} stream treats single
 * children as scalar values.
 *
 * Thread Safety
 *
 * <p>This class is stateless and thread-safe. XML factory instances are cached per-thread via {@link ThreadLocal}.
 *
 * @see XmlValue
 * @see JsonOps
 */
public class XmlOps implements DynamicOps<XmlValue>
{
    public static final XmlOps INSTANCE = new XmlOps();

    private static final ThreadLocal<DocumentBuilderFactory> DOCUMENT_BUILDER_FACTORY = ThreadLocal.withInitial(() -> {
        try
        {
            final DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            return f;
        }
        catch (final ParserConfigurationException e)
        {
            throw new RuntimeException(e);
        }
    });

    private static final ThreadLocal<TransformerFactory> TRANSFORMER_FACTORY = ThreadLocal.withInitial(() -> {
        final TransformerFactory f = TransformerFactory.newInstance();
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        return f;
    });

    private XmlOps()
    {}

    // ========== Core ==========

    @Override
    public XmlValue empty()
    {
        return XmlNull.NULL;
    }

    @Override
    public XmlValue emptyMap()
    {
        return new XmlElement(XmlElement.DEFAULT_TAG);
    }

    @Override
    public XmlValue emptyList()
    {
        return new XmlElement(XmlElement.LIST_TAG);
    }

    // ========== Primitives ==========

    @Override
    public XmlValue createString(final String value)
    {
        return new XmlText(value);
    }

    @Override
    public XmlValue createNumeric(final Number i)
    {
        return new XmlText(i.toString());
    }

    @Override
    public XmlValue createBoolean(final boolean value)
    {
        return new XmlText(String.valueOf(value));
    }

    @Override
    public DataResult<String> getStringValue(final XmlValue input)
    {
        return input instanceof final XmlText t ? DataResult.success(t.value()) : DataResult.error(() -> "Not a string: " + input);
    }

    @Override
    public DataResult<Number> getNumberValue(final XmlValue input)
    {
        return input instanceof final XmlText t ? parseNumber(t.value()) : DataResult.error(() -> "Not a number: " + input);
    }

    @Override
    public DataResult<Boolean> getBooleanValue(final XmlValue input)
    {
        if (input instanceof final XmlText t)
        {
            if ("true".equals(t.value()))
            {
                return DataResult.success(true);
            }
            if ("false".equals(t.value()))
            {
                return DataResult.success(false);
            }
        }
        return DataResult.error(() -> "Not a boolean: " + input);
    }

    // ========== Map Operations ==========

    @Override
    public XmlValue createMap(final Stream<Pair<XmlValue, XmlValue>> map)
    {
        final List<Pair<XmlValue, XmlValue>> entries = map.toList();
        String tagName = XmlElement.DEFAULT_TAG;
        final LinkedHashMap<String, String> attrs = new LinkedHashMap<>();
        final List<XmlValue> children = new ArrayList<>();

        for (final Pair<XmlValue, XmlValue> entry : entries)
        {
            final String key = getStringOrNull(entry.getFirst());
            if (key == null)
            {
                continue;
            }
            final XmlValue value = entry.getSecond();

            if (XmlValue.TYPE_KEY.equals(key))
            {
                final String v = getStringOrNull(value);
                if (v != null && !v.isEmpty())
                {
                    tagName = v;
                }
            }
            else if (XmlValue.TEXT_KEY.equals(key))
            {
                final String v = getStringOrNull(value);
                if (v != null)
                {
                    children.add(0, new XmlText(v));
                }
            }
            else
            {
                appendEntry(attrs, children, key, value);
            }
        }
        return new XmlElement(tagName, attrs, children);
    }

    @Override
    public DataResult<Stream<Pair<XmlValue, XmlValue>>> getMapValues(final XmlValue input)
    {
        return input instanceof final XmlElement e ? DataResult.success(buildMapEntries(e)) :
            DataResult.error(() -> "Not an element: " + input);
    }

    @Override
    public DataResult<Consumer<BiConsumer<XmlValue, XmlValue>>> getMapEntries(final XmlValue input)
    {
        return input instanceof final XmlElement e ?
            DataResult.success(c -> buildMapEntries(e).forEach(p -> c.accept(p.getFirst(), p.getSecond()))) :
            DataResult.error(() -> "Not an element: " + input);
    }

    @Override
    public DataResult<MapLike<XmlValue>> getMap(final XmlValue input)
    {
        if (!(input instanceof final XmlElement element))
        {
            return DataResult.error(() -> "Not an element: " + input);
        }
        return DataResult.success(new MapLike<>()
        {
            @Nullable
            @Override
            public XmlValue get(final XmlValue key)
            {
                final String k = getStringOrNull(key);
                return k != null ? get(k) : null;
            }

            @Nullable
            @Override
            public XmlValue get(final String key)
            {
                if (XmlValue.TYPE_KEY.equals(key))
                {
                    return !XmlElement.DEFAULT_TAG.equals(element.tag()) ? new XmlText(element.tag()) : null;
                }
                if (XmlValue.TEXT_KEY.equals(key))
                {
                    final String tc = element.getTextContent();
                    return tc != null ? new XmlText(tc) : null;
                }
                final String attrValue = element.getAttribute(key);
                if (attrValue != null)
                {
                    return isBracketedList(attrValue) ? parseBracketedList(attrValue) : new XmlText(unescapeAttrValue(attrValue));
                }

                final List<XmlElement> matching = element.getChildrenByTag(key);
                if (matching.isEmpty())
                {
                    // Backward-compat fallback: if requesting "children" and no child has that tag,
                    // collect ALL element children as a list (legacy inline-typed format).
                    if (XmlValue.CHILDREN_KEY.equals(key) && !element.children().isEmpty())
                    {
                        final List<XmlValue> all = element.children().stream().filter(c -> c instanceof XmlElement).toList();
                        if (!all.isEmpty())
                        {
                            return new XmlElement(XmlElement.LIST_TAG, Map.of(), all);
                        }
                    }
                    return null;
                }
                if (matching.size() == 1)
                {
                    return unwrapChildAsValue(matching.get(0));
                }
                return new XmlElement(XmlElement.LIST_TAG,
                    Map.of(),
                    matching.stream().map(XmlOps::unwrapChildAsValue).map(v -> (XmlValue) v).toList());
            }

            @Override
            public Stream<Pair<XmlValue, XmlValue>> entries()
            {
                return buildMapEntries(element);
            }

            @Override
            public String toString()
            {
                return "MapLike[" + element + "]";
            }
        });
    }

    @Override
    public DataResult<XmlValue> mergeToMap(final XmlValue map, final XmlValue key, final XmlValue value)
    {
        if (!(map instanceof XmlElement) && !(map instanceof XmlNull))
        {
            return DataResult.error(() -> "mergeToMap called with not a map: " + map, map);
        }
        final String keyStr = getStringOrNull(key);
        if (keyStr == null)
        {
            return DataResult.error(() -> "key is not a string: " + key, map);
        }

        XmlElement result = map instanceof final XmlElement e ? e : new XmlElement(XmlElement.DEFAULT_TAG);

        if (XmlValue.TYPE_KEY.equals(keyStr))
        {
            final String v = getStringOrNull(value);
            if (v != null && !v.isEmpty())
            {
                result = result.withTag(v);
            }
            return DataResult.success(result);
        }
        if (XmlValue.TEXT_KEY.equals(keyStr))
        {
            final String v = getStringOrNull(value);
            if (v != null)
            {
                result = result.withoutTextContent().withTextContent(v);
            }
            return DataResult.success(result);
        }

        if (isSimpleValue(value) && !isValidXmlName(keyStr))
        {
            return DataResult.error(() -> "Invalid XML attribute name: " + keyStr, result);
        }

        result = removeEntryFrom(result, keyStr);
        return DataResult.success(appendToElement(result, keyStr, value));
    }

    @Override
    public DataResult<XmlValue> mergeToMap(final XmlValue map, final MapLike<XmlValue> values)
    {
        if (!(map instanceof XmlElement) && !(map instanceof XmlNull))
        {
            return DataResult.error(() -> "mergeToMap called with not a map: " + map, map);
        }

        XmlElement result = map instanceof final XmlElement e ? e : new XmlElement(XmlElement.DEFAULT_TAG);
        final List<XmlValue> missed = new ArrayList<>();

        for (final Pair<XmlValue, XmlValue> entry : values.entries().toList())
        {
            final String keyStr = getStringOrNull(entry.getFirst());
            if (keyStr == null)
            {
                missed.add(entry.getFirst());
                continue;
            }
            if (XmlValue.TYPE_KEY.equals(keyStr))
            {
                continue;
            }
            if (XmlValue.TEXT_KEY.equals(keyStr))
            {
                final String v = getStringOrNull(entry.getSecond());
                if (v != null)
                {
                    result = result.withoutTextContent().withTextContent(v);
                }
                continue;
            }
            result = appendToElement(removeEntryFrom(result, keyStr), keyStr, entry.getSecond());
        }

        if (!missed.isEmpty())
        {
            final XmlElement r = result;
            return DataResult.error(() -> "some keys are not strings: " + missed, r);
        }
        final XmlValue typeNode = values.get(XmlValue.TYPE_KEY);
        if (typeNode != null)
        {
            final String v = getStringOrNull(typeNode);
            if (v != null && !v.isEmpty())
            {
                result = result.withTag(v);
            }
        }
        return DataResult.success(result);
    }

    @Override
    public XmlValue remove(final XmlValue input, final String key)
    {
        if (!(input instanceof final XmlElement element))
        {
            return input;
        }
        if (XmlValue.TYPE_KEY.equals(key))
        {
            return element.withTag(XmlElement.DEFAULT_TAG);
        }
        if (XmlValue.TEXT_KEY.equals(key))
        {
            return element.withoutTextContent();
        }
        return removeEntryFrom(element, key);
    }

    // ========== List Operations ==========

    @Override
    public XmlValue createList(final Stream<XmlValue> input)
    {
        return new XmlElement(XmlElement.LIST_TAG, Map.of(), input.toList());
    }

    @Override
    public DataResult<Stream<XmlValue>> getStream(final XmlValue input)
    {
        if (input instanceof final XmlElement e && e.isList())
        {
            return DataResult.success(e.children().stream());
        }
        if (input instanceof final XmlText t && isBracketedList(t.value()))
        {
            return DataResult.success(parseBracketedItems(t.value()).stream().map(XmlText::new));
        }
        if (input instanceof final XmlElement e)
        {
            if (e.attributes().isEmpty() && !e.children().isEmpty())
            {
                return DataResult.success(e.children().stream());
            }
            return DataResult.success(Stream.of(input));
        }
        return DataResult.error(() -> "Not a list: " + input);
    }

    @Override
    public DataResult<Consumer<Consumer<XmlValue>>> getList(final XmlValue input)
    {
        if (input instanceof final XmlElement e && e.isList())
        {
            return DataResult.success(e.children()::forEach);
        }
        if (input instanceof final XmlText t && isBracketedList(t.value()))
        {
            final List<String> items = parseBracketedItems(t.value());
            return DataResult.success(c -> items.forEach(i -> c.accept(new XmlText(i))));
        }
        if (input instanceof final XmlElement e)
        {
            if (e.attributes().isEmpty() && !e.children().isEmpty())
            {
                return DataResult.success(e.children()::forEach);
            }
            return DataResult.success(c -> c.accept(input));
        }
        return DataResult.error(() -> "Not a list: " + input);
    }

    @Override
    public DataResult<XmlValue> mergeToList(final XmlValue list, final XmlValue value)
    {
        if (!(list instanceof XmlElement) && !(list instanceof XmlNull))
        {
            return DataResult.error(() -> "mergeToList called with not a list: " + list, list);
        }
        return DataResult.success(
            list instanceof final XmlElement e ? e.withChild(value) : new XmlElement(XmlElement.LIST_TAG, Map.of(), List.of(value)));
    }

    @Override
    public DataResult<XmlValue> mergeToList(final XmlValue list, final List<XmlValue> values)
    {
        if (!(list instanceof XmlElement) && !(list instanceof XmlNull))
        {
            return DataResult.error(() -> "mergeToList called with not a list: " + list, list);
        }
        if (values.isEmpty())
        {
            return DataResult.success(list instanceof XmlNull ? emptyList() : list);
        }
        return DataResult.success(
            list instanceof final XmlElement e ? e.withChildren(values) : new XmlElement(XmlElement.LIST_TAG, Map.of(), values));
    }

    // ========== Specialized Streams ==========

    @Override
    public DataResult<ByteBuffer> getByteBuffer(final XmlValue input)
    {
        if (!(input instanceof final XmlText t))
        {
            return DataResult.error(() -> "Not a byte buffer: " + input);
        }
        try
        {
            return DataResult.success(ByteBuffer.wrap(Base64.getDecoder().decode(t.value())));
        }
        catch (final IllegalArgumentException e)
        {
            return DataResult.error(() -> "Not valid Base64: " + t.value());
        }
    }

    @Override
    public XmlValue createByteList(final ByteBuffer input)
    {
        final byte[] bytes = new byte[input.remaining()];
        input.duplicate().get(bytes);
        return new XmlText(Base64.getEncoder().encodeToString(bytes));
    }

    @Override
    public DataResult<IntStream> getIntStream(final XmlValue input)
    {
        if (!(input instanceof final XmlText t) || !isBracketedList(t.value()))
        {
            return DataResult.error(() -> "Not an int list: " + input);
        }
        try
        {
            return DataResult.success(parseBracketedItems(t.value()).stream().mapToInt(Integer::parseInt));
        }
        catch (final NumberFormatException e)
        {
            return DataResult.error(() -> "Not an int list: " + input);
        }
    }

    @Override
    public XmlValue createIntList(final IntStream input)
    {
        return new XmlText(formatBracketedList(input.mapToObj(Integer::toString)));
    }

    @Override
    public DataResult<LongStream> getLongStream(final XmlValue input)
    {
        if (!(input instanceof final XmlText t) || !isBracketedList(t.value()))
        {
            return DataResult.error(() -> "Not a long list: " + input);
        }
        try
        {
            return DataResult.success(parseBracketedItems(t.value()).stream().mapToLong(Long::parseLong));
        }
        catch (final NumberFormatException e)
        {
            return DataResult.error(() -> "Not a long list: " + input);
        }
    }

    @Override
    public XmlValue createLongList(final LongStream input)
    {
        return new XmlText(formatBracketedList(input.mapToObj(Long::toString)));
    }

    // ========== Conversion ==========

    @Override
    public <U> U convertTo(final DynamicOps<U> outOps, final XmlValue input)
    {
        if (input instanceof XmlNull)
        {
            return outOps.empty();
        }
        if (input instanceof final XmlText t)
        {
            if ("true".equals(t.value()))
            {
                return outOps.createBoolean(true);
            }
            if ("false".equals(t.value()))
            {
                return outOps.createBoolean(false);
            }
            final DataResult<Number> num = parseNumber(t.value());
            if (num.isSuccess())
            {
                return outOps.createNumeric(num.getOrThrow(IllegalStateException::new));
            }
            return outOps.createString(t.value());
        }
        if (input instanceof final XmlElement e)
        {
            return e.isList() ? convertList(outOps, input) : convertMap(outOps, input);
        }
        return outOps.empty();
    }

    // ========== Builders ==========

    @Override
    public ListBuilder<XmlValue> listBuilder()
    {
        return new XmlListBuilder();
    }

    @Override
    public RecordBuilder<XmlValue> mapBuilder()
    {
        return new XmlRecordBuilder();
    }

    @Override
    public String toString()
    {
        return "XML";
    }

    // ========== DOM Conversion ==========

    /**
     * Converts an {@link XmlValue} tree to a W3C DOM {@link Node}. Useful for interop with standard XML APIs (XPath, XSLT, etc.).
     */
    public static Node toNode(final XmlValue value)
    {
        try
        {
            return toNode(value, createSecureDocumentBuilder().newDocument());
        }
        catch (final ParserConfigurationException e)
        {
            throw new RuntimeException("Failed to create XML Document", e);
        }
    }

    /**
     * Converts a W3C DOM {@link Node} to an {@link XmlValue} tree. Whitespace-only text nodes are skipped. Unknown node types become
     * {@link XmlNull}.
     */
    public static XmlValue fromNode(final Node node)
    {
        if (node instanceof final Text t)
        {
            final String c = t.getTextContent();
            return (c == null || c.isEmpty()) ? XmlNull.NULL : new XmlText(c);
        }
        if (node instanceof final Element elem)
        {
            final LinkedHashMap<String, String> attrs = new LinkedHashMap<>();
            final NamedNodeMap na = elem.getAttributes();
            for (int i = 0; i < na.getLength(); i++)
            {
                attrs.put(na.item(i).getNodeName(), na.item(i).getNodeValue());
            }
            final List<XmlValue> children = new ArrayList<>();
            final NodeList cn = elem.getChildNodes();
            for (int i = 0; i < cn.getLength(); i++)
            {
                final Node child = cn.item(i);
                if (child instanceof Text && child.getTextContent().isBlank())
                {
                    continue;
                }
                children.add(fromNode(child));
            }
            return new XmlElement(elem.getTagName(), attrs, children);
        }
        return XmlNull.NULL;
    }

    /**
     * Serializes an {@link XmlValue} to a pretty-printed XML string (no XML declaration).
     *
     * @return a {@link DataResult} containing the XML string, or an error if serialization fails
     */
    public static DataResult<String> toXmlString(final XmlValue value)
    {
        try
        {
            final Transformer tf = TRANSFORMER_FACTORY.get().newTransformer();
            tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            final StringWriter w = new StringWriter();
            tf.transform(new DOMSource(toNode(value)), new StreamResult(w));
            return DataResult.success(w.toString().trim());
        }
        catch (final Exception e)
        {
            return DataResult.error(() -> "Failed to serialize XML: " + e.getMessage());
        }
    }

    private static Node toNode(final XmlValue value, final Document doc)
    {
        if (value instanceof XmlNull)
        {
            return doc.createElement("null");
        }
        if (value instanceof final XmlText t)
        {
            return doc.createTextNode(t.value());
        }
        if (value instanceof final XmlElement element)
        {
            final Element e = doc.createElement(element.tag());
            element.attributes().forEach(e::setAttribute);
            for (final XmlValue child : element.children())
            {
                e.appendChild(toNode(child, doc));
            }
            return e;
        }
        return doc.createTextNode("");
    }

    private static javax.xml.parsers.DocumentBuilder createSecureDocumentBuilder() throws ParserConfigurationException
    {
        return DOCUMENT_BUILDER_FACTORY.get().newDocumentBuilder();
    }

    // ========== Internal Helpers ==========

    @Nullable
    private static String getStringOrNull(final XmlValue value)
    {
        return value instanceof final XmlText t ? t.value() : null;
    }

    private static boolean isSimpleValue(final XmlValue value)
    {
        if (value instanceof XmlText)
        {
            return true;
        }
        return value instanceof final XmlElement e && e.isList() && e.children().stream().allMatch(c -> c instanceof XmlText);
    }

    private static boolean hasTypedItems(final XmlElement list)
    {
        return list.children().stream().anyMatch(c -> c instanceof XmlElement e && !XmlElement.DEFAULT_TAG.equals(e.tag()));
    }

    private static String getSimpleValueString(final XmlValue value)
    {
        if (value instanceof final XmlText t)
        {
            return escapeAttrValue(t.value());
        }
        if (value instanceof final XmlElement e && e.isList())
        {
            return "[" +
                String.join(", ", e.children().stream().filter(c -> c instanceof XmlText).map(c -> ((XmlText) c).value()).toList()) +
                "]";
        }
        return "";
    }

    /**
     * Core encoding dispatch: appends a key-value pair to attrs/children lists (used by createMap).
     */
    private static void appendEntry(final Map<String, String> attrs,
        final List<XmlValue> children,
        final String key,
        final XmlValue value)
    {
        if (isSimpleValue(value))
        {
            attrs.put(key, getSimpleValueString(value));
        }
        else if (value instanceof final XmlElement list && list.isList())
        {
            if (hasTypedItems(list))
            {
                if (XmlValue.CHILDREN_KEY.equals(key))
                {
                    for (final XmlValue item : list.children())
                    {
                        if (item instanceof final XmlElement ce)
                        {
                            XmlElement child = ce.withTag(key);
                            if (!XmlElement.DEFAULT_TAG.equals(ce.tag()))
                            {
                                child = child.withAttribute(XmlValue.TYPE_KEY, ce.tag());
                            }
                            children.add(child);
                        }
                    }
                }
                else
                {
                    children.add(new XmlElement(key, Map.of(), list.children()));
                }
            }
            else
            {
                for (final XmlValue item : list.children())
                {
                    if (item instanceof final XmlElement ce)
                    {
                        children.add(ce.withTag(key));
                    }
                    else
                    {
                        children.add(new XmlElement(key, Map.of(XmlValue.TEXT_KEY, getSimpleValueString(item))));
                    }
                }
            }
        }
        else if (value instanceof final XmlElement elem)
        {
            XmlElement child = elem.withTag(key);
            if (!XmlElement.DEFAULT_TAG.equals(elem.tag()))
            {
                child = child.withAttribute(XmlValue.TYPE_KEY, elem.tag());
            }
            children.add(child);
        }
    }

    /**
     * Encodes a key-value pair into the element.
     * <ul>
     * <li>Simple values (text/number/bool) → stored as an attribute
     * <li>Lists → items inlined as repeated child elements with the key as tag
     * <li>Maps/elements → stored as a single child element with the key as tag</ul>
     * Special handling for {@link XmlValue#CHILDREN_KEY}: typed items become {@code <children type="..."/>} children to preserve type
     * info.
     */
    private static XmlElement appendToElement(final XmlElement element, final String key, final XmlValue value)
    {
        if (isSimpleValue(value))
        {
            return element.withAttribute(key, getSimpleValueString(value));
        }

        if (value instanceof final XmlElement list && list.isList())
        {
            if (hasTypedItems(list))
            {
                if (XmlValue.CHILDREN_KEY.equals(key))
                {
                    XmlElement result = element;
                    for (final XmlValue item : list.children())
                    {
                        if (item instanceof final XmlElement ce)
                        {
                            XmlElement child = ce.withTag(key);
                            if (!XmlElement.DEFAULT_TAG.equals(ce.tag()))
                            {
                                child = child.withAttribute(XmlValue.TYPE_KEY, ce.tag());
                            }
                            result = result.withChild(child);
                        }
                    }
                    return result;
                }
                return element.withChild(new XmlElement(key, Map.of(), list.children()));
            }
            final List<XmlValue> newChildren = new ArrayList<>(element.children());
            for (final XmlValue item : list.children())
            {
                if (item instanceof final XmlElement ce)
                {
                    newChildren.add(ce.withTag(key));
                }
                else
                {
                    newChildren.add(new XmlElement(key, Map.of(XmlValue.TEXT_KEY, getSimpleValueString(item))));
                }
            }
            return new XmlElement(element.tag(), element.attributes(), newChildren);
        }

        if (value instanceof final XmlElement elem)
        {
            XmlElement child = elem.withTag(key);
            if (!XmlElement.DEFAULT_TAG.equals(elem.tag()))
            {
                child = child.withAttribute(XmlValue.TYPE_KEY, elem.tag());
            }
            return element.withChild(child);
        }
        return element;
    }

    /**
     * Reconstructs map entries from an element's structure (inverse of encoding). Order: type key (from tag) → text key (from text
     * content) → attributes → grouped children.
     * <p>Note: a single child with a given tag is returned as a scalar value, not a one-element list. This is an inherent XML
     * ambiguity; the codec layer resolves it via type knowledge.
     */
    private Stream<Pair<XmlValue, XmlValue>> buildMapEntries(final XmlElement element)
    {
        final List<Pair<XmlValue, XmlValue>> entries = new ArrayList<>();
        if (!XmlElement.DEFAULT_TAG.equals(element.tag()))
        {
            entries.add(Pair.of(new XmlText(XmlValue.TYPE_KEY), new XmlText(element.tag())));
        }

        final String textContent = element.getTextContent();
        if (textContent != null)
        {
            entries.add(Pair.of(new XmlText(XmlValue.TEXT_KEY), new XmlText(textContent)));
        }

        for (final var attr : element.attributes().entrySet())
        {
            final String v = attr.getValue();
            entries.add(
                Pair.of(new XmlText(attr.getKey()), isBracketedList(v) ? parseBracketedList(v) : new XmlText(unescapeAttrValue(v))));
        }

        final LinkedHashMap<String, List<XmlElement>> groups = new LinkedHashMap<>();
        for (final XmlValue child : element.children())
        {
            if (child instanceof final XmlElement ce)
            {
                groups.computeIfAbsent(ce.tag(), k -> new ArrayList<>()).add(ce);
            }
        }

        for (final var group : groups.entrySet())
        {
            final List<XmlElement> items = group.getValue();
            if (items.size() == 1)
            {
                entries.add(Pair.of(new XmlText(group.getKey()), unwrapChildAsValue(items.get(0))));
            }
            else
            {
                entries.add(Pair.of(new XmlText(group.getKey()),
                    new XmlElement(XmlElement.LIST_TAG, Map.of(), items.stream().map(XmlOps::unwrapChildAsValue).toList())));
            }
        }
        return entries.stream();
    }

    private static XmlValue unwrapChildAsValue(final XmlElement child)
    {
        if (child.children().isEmpty() && child.attributes().size() == 1 && child.getAttribute(XmlValue.TEXT_KEY) != null)
        {
            return new XmlText(unescapeAttrValue(child.getAttribute(XmlValue.TEXT_KEY)));
        }
        final String typeAttr = child.getAttribute(XmlValue.TYPE_KEY);
        final String tag = (typeAttr != null && !typeAttr.isEmpty()) ? typeAttr : XmlElement.DEFAULT_TAG;
        return child.withTag(tag).withoutAttribute(XmlValue.TYPE_KEY);
    }

    /**
     * Removes the map entry identified by {@code key} from the element. Clears the key from both attributes and children.
     * Short-circuits if neither exists.
     */
    private static XmlElement removeEntryFrom(final XmlElement element, final String key)
    {
        if (element.getAttribute(key) == null && element.getChildrenByTag(key).isEmpty())
        {
            return element;
        }
        return element.withoutAttribute(key).withoutChildrenByTag(key);
    }

    /**
     * Parses a numeric string, narrowing to the smallest type that can represent the value exactly. Order: byte → short → int → long →
     * float → double. Codec consumers always call {@code .intValue()}, {@code .longValue()} etc., so the boxed type is safe.
     */
    private static DataResult<Number> parseNumber(final String text)
    {
        try
        {
            final BigDecimal bd = new BigDecimal(text);
            try
            {
                final long l = bd.longValueExact();
                if ((byte) l == l)
                {
                    return DataResult.success((byte) l);
                }
                if ((short) l == l)
                {
                    return DataResult.success((short) l);
                }
                if ((int) l == l)
                {
                    return DataResult.success((int) l);
                }
                return DataResult.success(l);
            }
            catch (final ArithmeticException e)
            {
                final double d = bd.doubleValue();
                return DataResult.success((float) d == d ? (float) d : d);
            }
        }
        catch (final NumberFormatException e)
        {
            return DataResult.error(() -> "Not a number: " + text);
        }
    }

    // ========== Bracketed List Format ==========
    // Compact encoding for IntStream/LongStream: "[1, 2, 3]" stored as an attribute value.
    // Only used for numeric primitive streams — never for arbitrary user strings.
    // Strings that match the [...] pattern are escaped with a \ prefix on write (see escapeAttrValue).

    private static boolean isBracketedList(final String v)
    {
        return v.startsWith("[") && v.endsWith("]");
    }

    /**
     * Escapes a plain string value before storing as an XML attribute. Prevents false-positive bracketed-list parsing on read. Values
     * starting with '[' + ']' or '\' get a '\' prefix.
     */
    private static String escapeAttrValue(final String value)
    {
        if (value.startsWith("\\") || isBracketedList(value))
        {
            return "\\" + value;
        }
        return value;
    }

    /**
     * Reverses {@link #escapeAttrValue} — strips leading '\' if present.
     */
    private static String unescapeAttrValue(final String value)
    {
        if (value.startsWith("\\"))
        {
            return value.substring(1);
        }
        return value;
    }

    private static List<String> parseBracketedItems(final String value)
    {
        final String inner = value.substring(1, value.length() - 1).trim();
        if (inner.isEmpty())
        {
            return List.of();
        }
        final String[] parts = inner.split(",");
        final List<String> items = new ArrayList<>(parts.length);
        for (final String p : parts)
        {
            items.add(p.trim());
        }
        return items;
    }

    private static XmlValue parseBracketedList(final String value)
    {
        return new XmlElement(XmlElement.LIST_TAG,
            Map.of(),
            parseBracketedItems(value).stream().map(XmlText::new).map(v -> (XmlValue) v).toList());
    }

    private static String formatBracketedList(final Stream<String> items)
    {
        return "[" + String.join(", ", items.toList()) + "]";
    }

    /**
     * Validates that a string is usable as an XML element/attribute name. Rejects: empty, names starting with "xml" (reserved per
     * spec), names with invalid characters. Allowed start chars: letter, underscore. Allowed continuation: letter, digit, underscore,
     * hyphen, dot. Colons are rejected (no namespace support).
     */
    private static boolean isValidXmlName(final String name)
    {
        if (name == null || name.isEmpty())
        {
            return false;
        }
        if (name.length() >= 3 && name.regionMatches(true, 0, "xml", 0, 3))
        {
            return false;
        }
        final char first = name.charAt(0);
        if (!Character.isLetter(first) && first != '_')
        {
            return false;
        }
        for (int i = 1; i < name.length(); i++)
        {
            final char c = name.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '_' && c != '-' && c != '.')
            {
                return false;
            }
        }
        return true;
    }

    // ========== List Builder ==========

    private class XmlListBuilder implements ListBuilder<XmlValue>
    {
        private DataResult<List<XmlValue>> builder = DataResult.success(new ArrayList<>(), Lifecycle.stable());

        @Override
        public DynamicOps<XmlValue> ops()
        {
            return XmlOps.this;
        }

        @Override
        public ListBuilder<XmlValue> add(final XmlValue value)
        {
            builder = builder.map(b -> {
                b.add(value);
                return b;
            });
            return this;
        }

        @Override
        public ListBuilder<XmlValue> add(final DataResult<XmlValue> value)
        {
            builder = builder.apply2stable((b, v) -> {
                b.add(v);
                return b;
            }, value);
            return this;
        }

        @Override
        public ListBuilder<XmlValue> withErrorsFrom(final DataResult<?> result)
        {
            builder = builder.flatMap(r -> result.map(v -> r));
            return this;
        }

        @Override
        public ListBuilder<XmlValue> mapError(final UnaryOperator<String> onError)
        {
            builder = builder.mapError(onError);
            return this;
        }

        @Override
        public DataResult<XmlValue> build(final XmlValue prefix)
        {
            final DataResult<XmlValue> result = builder.flatMap(b -> {
                if (!(prefix instanceof XmlElement) && !(prefix instanceof XmlNull))
                {
                    return DataResult.error(() -> "Cannot append a list to not a list: " + prefix, prefix);
                }
                final List<XmlValue> combined = new ArrayList<>();
                if (prefix instanceof final XmlElement e)
                {
                    combined.addAll(e.children());
                }
                combined.addAll(b);
                return DataResult.success(new XmlElement(XmlElement.LIST_TAG, Map.of(), combined), Lifecycle.stable());
            });
            builder = DataResult.success(new ArrayList<>(), Lifecycle.stable());
            return result;
        }
    }

    // ========== Record Builder ==========

    private class XmlRecordBuilder extends RecordBuilder.AbstractStringBuilder<XmlValue, XmlElement>
    {
        protected XmlRecordBuilder()
        {
            super(XmlOps.this);
        }

        @Override
        protected XmlElement initBuilder()
        {
            return new XmlElement(XmlElement.DEFAULT_TAG);
        }

        @Override
        protected XmlElement append(final String key, final XmlValue value, final XmlElement builder)
        {
            if (XmlValue.TYPE_KEY.equals(key))
            {
                final String v = getStringOrNull(value);
                if (v != null && !v.isEmpty())
                {
                    return builder.withTag(v);
                }
                return builder;
            }
            if (XmlValue.TEXT_KEY.equals(key))
            {
                final String v = getStringOrNull(value);
                if (v != null)
                {
                    return builder.withoutTextContent().withTextContent(v);
                }
                return builder;
            }
            return appendToElement(builder, key, value);
        }

        @Override
        protected DataResult<XmlValue> build(final XmlElement builder, final XmlValue prefix)
        {
            if (prefix == null || prefix instanceof XmlNull)
            {
                return DataResult.success(builder);
            }
            if (prefix instanceof final XmlElement pe)
            {
                final LinkedHashMap<String, String> attrs = new LinkedHashMap<>(pe.attributes());
                attrs.putAll(builder.attributes());
                final List<XmlValue> children = new ArrayList<>(pe.children());
                children.addAll(builder.children());
                final String tag = XmlElement.DEFAULT_TAG.equals(builder.tag()) ? pe.tag() : builder.tag();
                return DataResult.success(new XmlElement(tag, attrs, children));
            }
            return DataResult.error(() -> "mergeToMap called with not a map: " + prefix, prefix);
        }
    }
}
