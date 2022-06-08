package com.ldtteam.common.language;

import com.ldtteam.blockui.mod.BlockUI;
import com.ldtteam.common.sideless.Sideless;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.text.FormatFactory;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;

@SuppressWarnings("deprecation")
public class LangUtils
{
    static final Map<String, Formatter<?>> FORMAT_FACTORIES = new HashMap<>();

    static
    {
        // example usage: new TranslatableComponent("this is {0, pName}", player)
        addFormatFactory("pNameRaw", Player.class, (player, stringBuf) -> stringBuf.append(player.getName().getContents()));
        addFormatFactory("pName", Player.class, (player, stringBuf) -> stringBuf.append(player.getDisplayName().getContents()));
        addFormatFactory("pGamemode",
            Player.class,
            (player, stringBuf) -> stringBuf.append(Sideless.getPlayerGameMode(player).getShortDisplayName().getContents()));
        addFormatFactory("pUUID", Player.class, (player, stringBuf) -> stringBuf.append(player.getUUID().toString()));
    }

    public static <T> void addFormatFactory(final String key, final Class<T> formatClazz, final BiConsumer<T, StringBuffer> formatter)
    {
        if (FORMAT_FACTORIES.containsKey(key))
        {
            BlockUI.MOD_LOG
                .warn("Duplicate format for key: {}, old value source: {}", key, FORMAT_FACTORIES.get(key).getClass().getName());
        }

        FORMAT_FACTORIES.put(key, new Formatter<>(formatClazz, formatter));
    }

    public static String translate(final String key, final Object... args)
    {
        return translateComp(key, args).getContents();
    }

    public static TranslatableComponent translateComp(final String key, final Object... args)
    {
        return new OurTranslatableComponent(key, args);
    }

    public static TextComponent stringComp(final String str)
    {
        return new TextComponent(str);
    }

    public static class Formatter<T>extends Format implements FormatFactory
    {
        private final Class<T> formatClazz;
        private final BiConsumer<T, StringBuffer> formatter;

        public Formatter(final Class<T> formatClazz, final BiConsumer<T, StringBuffer> formatter)
        {
            this.formatClazz = formatClazz;
            this.formatter = formatter;
        }

        @Override
        public StringBuffer format(final Object obj, final StringBuffer toAppendTo, final FieldPosition pos)
        {
            formatter.accept(formatClazz.cast(obj), toAppendTo);
            return toAppendTo;
        }

        @Override
        public Object parseObject(final String source, final ParsePosition pos)
        {
            throw new UnsupportedOperationException("not for parsing");
        }

        @Override
        public Format getFormat(final String name, final String arguments, final Locale locale)
        {
            return this;
        }
    }
}
