package com.ldtteam.blockui.common.codec;

import com.ldtteam.common.codec.XmlOps;
import com.ldtteam.common.codec.XmlValue;
import com.ldtteam.common.codec.XmlValue.XmlElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.junit.Test;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class XmlOpsTest
{

    // ===== Complex record with nested objects, lists, numerics =====
    private record Inventory(List<Item> items, int capacity)
    {
        static final Codec<Inventory> CODEC = RecordCodecBuilder.create(
            i -> i
                .group(Item.CODEC.listOf().fieldOf("items").forGetter(Inventory::items),
                    Codec.INT.fieldOf("capacity").forGetter(Inventory::capacity))
                .apply(i, Inventory::new));
    }

    private record Item(String name, int count, float weight)
    {
        static final Codec<Item> CODEC = RecordCodecBuilder.create(
            i -> i
                .group(Codec.STRING.fieldOf("name").forGetter(Item::name),
                    Codec.INT.fieldOf("count").forGetter(Item::count),
                    Codec.FLOAT.fieldOf("weight").forGetter(Item::weight))
                .apply(i, Item::new));
    }

    private record Player(String name, int health, Inventory inventory, List<Integer> scores)
    {
        static final Codec<Player> CODEC = RecordCodecBuilder.create(
            i -> i
                .group(Codec.STRING.fieldOf("name").forGetter(Player::name),
                    Codec.INT.fieldOf("health").forGetter(Player::health),
                    Inventory.CODEC.fieldOf("inventory").forGetter(Player::inventory),
                    Codec.INT.listOf().fieldOf("scores").forGetter(Player::scores))
                .apply(i, Player::new));
    }

    @Test
    public void roundtripComplexNestedRecord()
    {
        final Player player = new Player("Steve",
            100,
            new Inventory(List.of(new Item("sword", 1, 3.5f), new Item("shield", 1, 8.0f), new Item("potion", 5, 0.5f)), 36),
            List.of(100, 250, 42, 999));

        final DataResult<XmlValue> encoded = Player.CODEC.encodeStart(XmlOps.INSTANCE, player);
        assertTrue("Encoding failed: " + encoded.error(), encoded.isSuccess());

        final XmlValue xml = encoded.getOrThrow(AssertionError::new);
        System.out.println("=== roundtripComplexNestedRecord ===");
        System.out.println(XmlOps.toXmlString(xml).getOrThrow(AssertionError::new));

        final DataResult<Player> decoded = Player.CODEC.parse(XmlOps.INSTANCE, xml);
        assertTrue("Decoding failed: " + decoded.error(), decoded.isSuccess());

        assertEquals(player, decoded.getOrThrow(AssertionError::new));
    }

    // ===== Dispatch codec (polymorphism) =====

    private interface Shape
    {
        Codec<Shape> CODEC = Codec.STRING.dispatch("type", Shape::typeName, name -> switch (name)
        {
            case "circle" -> Circle.CODEC;
            case "rect" -> Rect.CODEC;
            default -> throw new IllegalArgumentException("Unknown shape: " + name);
        });

        String typeName();
    }

    private record Circle(double radius, String color) implements Shape
    {
        static final MapCodec<Circle> CODEC = RecordCodecBuilder.mapCodec(i -> i
            .group(Codec.DOUBLE.fieldOf("radius").forGetter(Circle::radius), Codec.STRING.fieldOf("color").forGetter(Circle::color))
            .apply(i, Circle::new));

        @Override
        public String typeName()
        {
            return "circle";
        }
    }

    private record Rect(double width, double height, String color) implements Shape
    {
        static final MapCodec<Rect> CODEC = RecordCodecBuilder.mapCodec(
            i -> i
                .group(Codec.DOUBLE.fieldOf("width").forGetter(Rect::width),
                    Codec.DOUBLE.fieldOf("height").forGetter(Rect::height),
                    Codec.STRING.fieldOf("color").forGetter(Rect::color))
                .apply(i, Rect::new));

        @Override
        public String typeName()
        {
            return "rect";
        }
    }

    @Test
    public void roundtripDispatchCodec()
    {
        final List<Shape> shapes = List.of(new Circle(5.0, "red"), new Rect(10.0, 20.0, "blue"), new Circle(2.5, "green"));
        final Codec<List<Shape>> codec = Shape.CODEC.listOf();

        final DataResult<XmlValue> encoded = codec.encodeStart(XmlOps.INSTANCE, shapes);
        assertTrue("Encoding failed: " + encoded.error(), encoded.isSuccess());
        System.out.println("=== roundtripDispatchCodec ===");
        System.out.println(XmlOps.toXmlString(encoded.getOrThrow(AssertionError::new)).getOrThrow(AssertionError::new));

        final DataResult<List<Shape>> decoded = encoded.flatMap(r -> codec.parse(XmlOps.INSTANCE, r));
        assertEquals(DataResult.success(shapes), decoded);
    }

    // ===== Cross-ops conversion: XML → JSON → XML roundtrip =====

    @Test
    public void convertBetweenXmlAndJson()
    {
        final Player player =
            new Player("Alex", 80, new Inventory(List.of(new Item("bow", 1, 2.0f), new Item("arrow", 64, 0.1f)), 27), List.of(10, 20));

        // Encode as XML
        final XmlValue xml = Player.CODEC.encodeStart(XmlOps.INSTANCE, player).getOrThrow(AssertionError::new);
        System.out.println("=== convertBetweenXmlAndJson ===");
        System.out.println(XmlOps.toXmlString(xml).getOrThrow(AssertionError::new));

        // Convert XML node tree → JavaOps
        final Object java = XmlOps.INSTANCE.convertTo(JavaOps.INSTANCE, xml);

        // Parse from JavaOps
        final Player fromJava = Player.CODEC.parse(JavaOps.INSTANCE, java).getOrThrow(AssertionError::new);
        assertEquals(player, fromJava);
    }

    // ===== Unbounded map with string keys and complex values =====

    @Test
    public void roundtripUnboundedMapComplex()
    {
        final Codec<Map<String, Item>> codec = Codec.unboundedMap(Codec.STRING, Item.CODEC);
        final Map<String, Item> data = Map.of("slot1", new Item("diamond", 64, 1.0f), "slot2", new Item("iron", 32, 2.5f));

        final DataResult<XmlValue> encoded = codec.encodeStart(XmlOps.INSTANCE, data);
        assertTrue("Encoding failed: " + encoded.error(), encoded.isSuccess());
        System.out.println("=== roundtripUnboundedMapComplex ===");
        System.out.println(XmlOps.toXmlString(encoded.getOrThrow(AssertionError::new)).getOrThrow(AssertionError::new));

        final DataResult<Map<String, Item>> decoded = encoded.flatMap(r -> codec.parse(XmlOps.INSTANCE, r));
        assertEquals(DataResult.success(data), decoded);
    }

    // ===== Optional fields, either codec =====

    private record Config(String host, int port, Optional<String> password, List<Long> timestamps)
    {
        static final Codec<Config> CODEC = RecordCodecBuilder.create(
            i -> i
                .group(Codec.STRING.fieldOf("host").forGetter(Config::host),
                    Codec.INT.fieldOf("port").forGetter(Config::port),
                    Codec.STRING.optionalFieldOf("password").forGetter(Config::password),
                    Codec.LONG.listOf().fieldOf("timestamps").forGetter(Config::timestamps))
                .apply(i, Config::new));
    }

    @Test
    public void roundtripOptionalFieldsPresent()
    {
        final Config config = new Config("localhost", 8080, Optional.of("secret"), List.of(1000L, 2000L, 3000L));

        final DataResult<XmlValue> encoded = Config.CODEC.encodeStart(XmlOps.INSTANCE, config);
        assertTrue("Encoding failed: " + encoded.error(), encoded.isSuccess());

        final DataResult<Config> decoded = encoded.flatMap(r -> Config.CODEC.parse(XmlOps.INSTANCE, r));
        assertEquals(DataResult.success(config), decoded);
    }

    @Test
    public void roundtripOptionalFieldsAbsent()
    {
        final Config config = new Config("example.com", 443, Optional.empty(), List.of(999L));

        final DataResult<XmlValue> encoded = Config.CODEC.encodeStart(XmlOps.INSTANCE, config);
        assertTrue("Encoding failed: " + encoded.error(), encoded.isSuccess());

        final DataResult<Config> decoded = encoded.flatMap(r -> Config.CODEC.parse(XmlOps.INSTANCE, r));
        assertEquals(DataResult.success(config), decoded);
    }

    // ===== ByteBuffer (Base64) roundtrip =====

    @Test
    public void roundtripByteBuffer()
    {
        final byte[] data = {0, 1, 2, 127, -128, -1, 42, 100};
        final Codec<ByteBuffer> codec = Codec.BYTE_BUFFER;

        final DataResult<XmlValue> encoded = codec.encodeStart(XmlOps.INSTANCE, ByteBuffer.wrap(data));
        assertTrue("Encoding failed: " + encoded.error(), encoded.isSuccess());

        final DataResult<ByteBuffer> decoded = encoded.flatMap(r -> codec.parse(XmlOps.INSTANCE, r));
        assertTrue("Decoding failed: " + decoded.error(), decoded.isSuccess());

        final byte[] result = new byte[decoded.getOrThrow(AssertionError::new).remaining()];
        decoded.getOrThrow(AssertionError::new).get(result);
        assertEquals(data.length, result.length);
        for (int i = 0; i < data.length; i++)
        {
            assertEquals(data[i], result[i]);
        }
    }

    // ===== Full RoundtripTest parity (same TestData as RoundtripTest) =====

    @Test
    public void writeReadXml()
    {
        // Uses the same complex TestData pattern from RoundtripTest
        final var data = new Player("TestPlayer",
            42,
            new Inventory(List.of(new Item("apple", 64, 0.25f), new Item("stone", 128, 4.0f)), 64),
            List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

        final DataResult<XmlValue> encoded = Player.CODEC.encodeStart(XmlOps.INSTANCE, data);
        final DataResult<Player> decoded = encoded.flatMap(r -> Player.CODEC.parse(XmlOps.INSTANCE, r));
        final DataResult<XmlValue> reEncoded = decoded.flatMap(r -> Player.CODEC.encodeStart(XmlOps.INSTANCE, r));

        assertEquals("read(write(x)) == x", DataResult.success(data), decoded);
        // Re-encode and decode again to verify stability
        final DataResult<Player> reDecoded = reEncoded.flatMap(r -> Player.CODEC.parse(XmlOps.INSTANCE, r));
        assertEquals("read(write(read(write(x)))) == x", DataResult.success(data), reDecoded);
    }

    // ===== "type" key becomes element tag name =====

    private record Entity(String type, String name, int level)
    {
        static final Codec<Entity> CODEC = RecordCodecBuilder.create(
            i -> i
                .group(Codec.STRING.fieldOf("type").forGetter(Entity::type),
                    Codec.STRING.fieldOf("name").forGetter(Entity::name),
                    Codec.INT.fieldOf("level").forGetter(Entity::level))
                .apply(i, Entity::new));
    }

    @Test
    public void typeKeyBecomesTagName()
    {
        final Entity entity = new Entity("Dragon", "Smaug", 99);

        final XmlValue xml = Entity.CODEC.encodeStart(XmlOps.INSTANCE, entity).getOrThrow(AssertionError::new);
        System.out.println("=== typeKeyBecomesTagName ===");
        System.out.println(XmlOps.toXmlString(xml).getOrThrow(AssertionError::new));

        // Verify the tag name IS "Dragon", not "object"
        assertTrue("Expected XmlElement", xml instanceof XmlElement);
        assertEquals("Dragon", ((XmlElement) xml).tag());
        assertEquals("Smaug", ((XmlElement) xml).getAttribute("name"));
        assertEquals("99", ((XmlElement) xml).getAttribute("level"));

        // Roundtrip
        final Entity decoded = Entity.CODEC.parse(XmlOps.INSTANCE, xml).getOrThrow(AssertionError::new);
        assertEquals(entity, decoded);
    }

    @Test
    public void typeKeyRoundtripsWithNestedTypedObjects()
    {
        // A list of entities with different type tags
        final List<Entity> entities =
            List.of(new Entity("Zombie", "Walker", 5), new Entity("Skeleton", "Bones", 8), new Entity("Dragon", "Alduin", 100));
        final Codec<List<Entity>> codec = Entity.CODEC.listOf();

        final XmlValue xml = codec.encodeStart(XmlOps.INSTANCE, entities).getOrThrow(AssertionError::new);
        System.out.println("=== typeKeyRoundtripsWithNestedTypedObjects ===");
        System.out.println(XmlOps.toXmlString(xml).getOrThrow(AssertionError::new));

        // Root should be a list element
        assertTrue("Expected XmlElement", xml instanceof XmlElement);
        assertEquals("list", ((XmlElement) xml).tag());

        final List<Entity> decoded = codec.parse(XmlOps.INSTANCE, xml).getOrThrow(AssertionError::new);
        assertEquals(entities, decoded);
    }

    @Test
    public void typeKeyWithNestedTypedMaps()
    {
        // Map containing typed objects as values
        record World(String type, Entity boss, List<Entity> mobs)
        {
            static final Codec<World> CODEC = RecordCodecBuilder.create(
                i -> i
                    .group(Codec.STRING.fieldOf("type").forGetter(World::type),
                        Entity.CODEC.fieldOf("boss").forGetter(World::boss),
                        Entity.CODEC.listOf().fieldOf("mobs").forGetter(World::mobs))
                    .apply(i, World::new));
        }

        final World world = new World("Dungeon",
            new Entity("Dragon", "Tiamat", 50),
            List.of(new Entity("Goblin", "Sneaky", 2), new Entity("Orc", "Grunk", 10)));

        final XmlValue xml = World.CODEC.encodeStart(XmlOps.INSTANCE, world).getOrThrow(AssertionError::new);
        System.out.println("=== typeKeyWithNestedTypedMaps ===");
        System.out.println(XmlOps.toXmlString(xml).getOrThrow(AssertionError::new));

        // Root should be <Dungeon>
        assertTrue(xml instanceof XmlElement);
        assertEquals("Dungeon", ((XmlElement) xml).tag());

        final World decoded = World.CODEC.parse(XmlOps.INSTANCE, xml).getOrThrow(AssertionError::new);
        assertEquals(world, decoded);
    }

    // ===== Recursive polymorphic tree: Z/A/B/C with dispatch + List<Z> children =====

    private interface Z
    {
        Codec<Z> CODEC = Codec.STRING.dispatch("type", Z::typeName, name -> switch (name)
        {
            case "A" -> A.CODEC;
            case "B" -> B.CODEC;
            case "C" -> C.CODEC;
            default -> throw new IllegalArgumentException("Unknown Z type: " + name);
        });

        String typeName();
    }

    private record A(String label, int value) implements Z
    {
        static final MapCodec<A> CODEC = RecordCodecBuilder
            .mapCodec(i -> i.group(Codec.STRING.fieldOf("label").forGetter(A::label), Codec.INT.fieldOf("value").forGetter(A::value))
                .apply(i, A::new));

        @Override
        public String typeName()
        {
            return "A";
        }
    }

    private static class B implements Z
    {
        static final MapCodec<B> CODEC = RecordCodecBuilder.mapCodec(i -> i
            .group(Codec.STRING.fieldOf("id").forGetter(b -> b.name), Z.CODEC.listOf().fieldOf("children").forGetter(b -> b.children))
            .apply(i, B::new));

        final String name;
        final List<Z> children;

        B(final String name, final List<Z> children)
        {
            this.name = name;
            this.children = children;
        }

        @Override
        public String typeName()
        {
            return "B";
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o) return true;
            if (!(o instanceof B b)) return false;
            return name.equals(b.name) && children.equals(b.children);
        }

        @Override
        public int hashCode()
        {
            return 31 * name.hashCode() + children.hashCode();
        }
    }

    private static class C extends B
    {
        static final MapCodec<C> CODEC = RecordCodecBuilder.mapCodec(
            i -> i
                .group(Codec.STRING.fieldOf("name").forGetter(c -> c.name),
                    Z.CODEC.listOf().fieldOf("children").forGetter(c -> c.children),
                    Codec.INT.fieldOf("priority").forGetter(c -> c.priority),
                    Codec.INT.listOf().fieldOf("tags").forGetter(c -> c.tags))
                .apply(i, C::new));

        final int priority;
        final List<Integer> tags;

        C(final String name, final List<Z> children, final int priority, final List<Integer> tags)
        {
            super(name, children);
            this.priority = priority;
            this.tags = tags;
        }

        @Override
        public String typeName()
        {
            return "C";
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o) return true;
            if (!(o instanceof C c)) return false;
            return name.equals(c.name) && children.equals(c.children) && priority == c.priority && tags.equals(c.tags);
        }

        @Override
        public int hashCode()
        {
            return 31 * (31 * (31 * name.hashCode() + children.hashCode()) + priority) + tags.hashCode();
        }
    }

    @Test
    public void recursivePolymorphicTree()
    {
        // A tree: C at root, with B and A children, B having its own A children
        final Z tree = new C("root",
            List.of(new A("leaf1", 10), new B("branch", List.of(new A("deep1", 20), new A("deep2", 30))), new A("leaf2", 40)),
            99,
            List.of(1, 2, 3));

        final DataResult<XmlValue> encoded = Z.CODEC.encodeStart(XmlOps.INSTANCE, tree);
        assertTrue("Encoding failed: " + encoded.error(), encoded.isSuccess());

        final XmlValue xml = encoded.getOrThrow(AssertionError::new);
        System.out.println("=== recursivePolymorphicTree ===");
        System.out.println(XmlOps.toXmlString(xml).getOrThrow(AssertionError::new));

        final DataResult<Z> decoded = Z.CODEC.parse(XmlOps.INSTANCE, xml);
        assertTrue("Decoding failed: " + decoded.error(), decoded.isSuccess());
        assertEquals(tree, decoded.getOrThrow(AssertionError::new));
    }

    @Test
    public void recursivePolymorphicList()
    {
        // A list of Z at the top level
        final List<Z> nodes = List.of(new A("solo", 1),
            new B("parent", List.of(new A("child", 2))),
            new C("complex", List.of(new B("nested", List.of(new A("innermost", 99)))), 5, List.of(10, 20, 30)));
        final Codec<List<Z>> codec = Z.CODEC.listOf();

        final DataResult<XmlValue> encoded = codec.encodeStart(XmlOps.INSTANCE, nodes);
        assertTrue("Encoding failed: " + encoded.error(), encoded.isSuccess());

        final XmlValue xml = encoded.getOrThrow(AssertionError::new);
        System.out.println("=== recursivePolymorphicList ===");
        System.out.println(XmlOps.toXmlString(xml).getOrThrow(AssertionError::new));

        final DataResult<List<Z>> decoded = codec.parse(XmlOps.INSTANCE, xml);
        assertTrue("Decoding failed: " + decoded.error(), decoded.isSuccess());
        assertEquals(nodes, decoded.getOrThrow(AssertionError::new));
    }

    // ===== TEXT_KEY tests =====

    private record TextHolder(String text, int score)
    {
        static final Codec<TextHolder> CODEC = RecordCodecBuilder.create(i -> i
            .group(Codec.STRING.fieldOf("text").forGetter(TextHolder::text), Codec.INT.fieldOf("score").forGetter(TextHolder::score))
            .apply(i, TextHolder::new));
    }

    @Test
    public void roundtripTextKey()
    {
        final TextHolder holder = new TextHolder("hello world", 42);

        final DataResult<XmlValue> encoded = TextHolder.CODEC.encodeStart(XmlOps.INSTANCE, holder);
        assertTrue("Encoding failed: " + encoded.error(), encoded.isSuccess());

        final XmlValue xml = encoded.getOrThrow(AssertionError::new);
        System.out.println("=== roundtripTextKey ===");
        System.out.println(XmlOps.toXmlString(xml).getOrThrow(AssertionError::new));

        // "text" should be stored as text content of the element
        assertTrue(xml instanceof XmlElement);
        final XmlElement elem = (XmlElement) xml;
        assertEquals("hello world", elem.getTextContent());
        assertEquals("42", elem.getAttribute("score"));

        final DataResult<TextHolder> decoded = TextHolder.CODEC.parse(XmlOps.INSTANCE, xml);
        assertTrue("Decoding failed: " + decoded.error(), decoded.isSuccess());
        assertEquals(holder, decoded.getOrThrow(AssertionError::new));
    }

    // ===== XML string → Node → XmlValue → object parsing tests =====

    private static XmlValue parseXml(final String xml)
    {
        try
        {
            final var factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true);
            final var doc = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
            return XmlOps.fromNode(doc.getDocumentElement());
        }
        catch (final Exception e)
        {
            throw new RuntimeException("Failed to parse XML: " + xml, e);
        }
    }

    @Test
    public void parsePlayerFromXmlString()
    {
        final XmlValue xml = parseXml("""
            <object name="Alex" health="80" scores="[10, 20, 30]">
              <inventory capacity="16">
                <items name="bow" count="1" weight="2.5"/>
              </inventory>
            </object>
            """);

        final Player player = Player.CODEC.parse(XmlOps.INSTANCE, xml).getOrThrow(AssertionError::new);
        assertEquals("Alex", player.name());
        assertEquals(80, player.health());
        assertEquals(16, player.inventory().capacity());
        assertEquals(List.of(new Item("bow", 1, 2.5f)), player.inventory().items());
        assertEquals(List.of(10, 20, 30), player.scores());
    }

    @Test
    public void parseDispatchedShapeFromXmlString()
    {
        final XmlValue xml = parseXml("""
            <circle radius="3.14" color="blue"/>
            """);

        final Shape shape = Shape.CODEC.parse(XmlOps.INSTANCE, xml).getOrThrow(AssertionError::new);
        assertEquals(new Circle(3.14, "blue"), shape);
    }

    @Test
    public void parseEntityWithTypeAsTagFromXmlString()
    {
        final XmlValue xml = parseXml("""
            <Skeleton name="Bonehead" level="15"/>
            """);

        final Entity entity = Entity.CODEC.parse(XmlOps.INSTANCE, xml).getOrThrow(AssertionError::new);
        assertEquals(new Entity("Skeleton", "Bonehead", 15), entity);
    }

    @Test
    public void parseTextContentFromXmlString()
    {
        final XmlValue xml = parseXml("""
            <object score="99">some text here</object>
            """);

        final TextHolder holder = TextHolder.CODEC.parse(XmlOps.INSTANCE, xml).getOrThrow(AssertionError::new);
        assertEquals(new TextHolder("some text here", 99), holder);
    }

    @Test
    public void parseRecursiveTreeFromXmlString()
    {
        final XmlValue xml = parseXml("""
            <C name="root" priority="5" tags="[1, 2]">
              <A label="leaf" value="42"/>
            </C>
            """);

        final Z tree = Z.CODEC.parse(XmlOps.INSTANCE, xml).getOrThrow(AssertionError::new);
        assertEquals(new C("root", List.of(new A("leaf", 42)), 5, List.of(1, 2)), tree);
    }

    @Test
    public void parseConfigFromXmlStringOptionalAbsent()
    {
        final XmlValue xml = parseXml("""
            <object host="db.local" port="5432" timestamps="[100, 200]"/>
            """);

        final Config config = Config.CODEC.parse(XmlOps.INSTANCE, xml).getOrThrow(AssertionError::new);
        assertEquals(new Config("db.local", 5432, Optional.empty(), List.of(100L, 200L)), config);
    }

    @Test
    public void parseConfigFromXmlStringOptionalPresent()
    {
        final XmlValue xml = parseXml("""
            <object host="db.local" port="5432" password="s3cret" timestamps="[100]"/>
            """);

        final Config config = Config.CODEC.parse(XmlOps.INSTANCE, xml).getOrThrow(AssertionError::new);
        assertEquals(new Config("db.local", 5432, Optional.of("s3cret"), List.of(100L)), config);
    }
}
