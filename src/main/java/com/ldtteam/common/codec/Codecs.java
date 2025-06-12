package com.ldtteam.common.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.Util;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;

public class Codecs
{
    /**
     * Lowercasing {@link Enum#name()} codec. Consider using array/registry-based variants if appropriate.
     */
    public static <E extends Enum<E>> Codec<E> forEnum(final Class<E> enumClass)
    {
        return Codec.STRING.comapFlatMap(str -> {
            try
            {
                return DataResult.success(Enum.valueOf(enumClass, str.toUpperCase(Locale.ROOT)));
            }
            catch (final IllegalArgumentException e)
            {
                return DataResult.error(() -> "Unknown " + enumClass
                    .getSimpleName() + ": " + str + ", must be one of: " + Arrays.toString(enumClass.getEnumConstants()));
            }
        }, rotMir -> rotMir.name().toLowerCase(Locale.ROOT));
    }

    /**
     * Size-wise unlimited array codec
     */
    public static <T> Codec<T[]> forArray(final Codec<T> elementCodec, final IntFunction<T[]> arrayFactory)
    {
        return forArray(elementCodec, arrayFactory, Integer.MAX_VALUE);
    }

    /**
     * Size-wise limited array codec
     */
    public static <T> Codec<T[]> forArray(final Codec<T> elementCodec, final IntFunction<T[]> arrayFactory, final int maxSize)
    {
        return Codec.list(elementCodec, 0, maxSize).xmap(list -> list.toArray(arrayFactory), Arrays::asList);
    }

    /**
     * Size-wise unlimited array codec
     */
    public static <T, B extends ByteBuf> StreamCodec<B, T[]> streamForArray(
        final StreamCodec<B, T> elementCodec,
        final IntFunction<T[]> arrayFactory)
    {
        return streamForArray(null, arrayFactory, Integer.MAX_VALUE);
    }

    /**
     * Size-wise limited array codec
     */
    public static <T, B extends ByteBuf> StreamCodec<B, T[]> streamForArray(
        final StreamCodec<B, T> elementCodec,
        final IntFunction<T[]> arrayFactory,
        final int maxSize)
    {
        return new StreamCodec<>()
        {
            public T[] decode(final B buf)
            {
                final T[] data = arrayFactory.apply(ByteBufCodecs.readCount(buf, maxSize));

                for (int i = 0; i < data.length; i++)
                {
                    data[i] = elementCodec.decode(buf);
                }

                return data;
            }

            public void encode(final B buf, final T[] data)
            {
                ByteBufCodecs.writeCount(buf, data.length, maxSize);

                for (final T element : data)
                {
                    elementCodec.encode(buf, element);
                }
            }
        };
    }

    /**
     * If data == emptyInstance then nothing is serialized
     */
    public static <A> Codec<A> withEmpty(final Codec<A> elementCodec, final A emptyInstance)
    {
        return new Codec<A>()
        {
            @Override
            public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix)
            {
                if (input == emptyInstance)
                {
                    // NbtOps are stupid when it comes to serializing empty tag
                    if (ops.empty() == NbtOps.INSTANCE.empty())
                    {
                        return DataResult.success(ops.createBoolean(true));
                    }
                    return DataResult.success(ops.empty());
                }
                return elementCodec.encode(input, ops, prefix);
            }

            @Override
            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input)
            {
                if (input == ops.empty() || (ops.empty() == NbtOps.INSTANCE.empty() && ops.getBooleanValue(input).result().orElse(false)))
                {
                    return DataResult.success(new Pair<>(emptyInstance, input));
                }
                return elementCodec.decode(ops, input);
            }
        };
    }

    /**
     * If data == emptyInstance then nothing is serialized
     */
    public static <A, B extends ByteBuf> StreamCodec<B, A> streamWithEmpty(final StreamCodec<B, A> elementCodec, final A emptyInstance)
    {
        return new StreamCodec<B, A>()
        {
            @Override
            public A decode(B buf)
            {
                final boolean isEmpty = buf.readBoolean();
                return isEmpty ? emptyInstance : elementCodec.decode(buf);
            }

            @Override
            public void encode(B buf, A data)
            {
                final boolean isEmpty = data == emptyInstance;
                buf.writeBoolean(isEmpty);
                if (!isEmpty)
                {
                    elementCodec.encode(buf, data);
                }
            }
        };
    }

    /**
     * StreamCodec that allows null values
     */
    public static <A, B extends ByteBuf> StreamCodec<B, A> wrapNullable(final StreamCodec<B, A> elementCodec)
    {
        return Util.<StreamCodec<B, A>, StreamCodec<B, A>>memoize(s -> streamWithEmpty(s, null)).apply(elementCodec);
    }

    /**
     * Field codec that allows null values
     */
    public static <O, C> RecordCodecBuilder<O, Optional<C>> wrapNullableField(final Codec<C> elementCodec, final String fieldName, final Function<O, C> fieldGetter)
    {
        // null-aware version of: return elementCodec.optionalFieldOf(fieldName, null).forGetter(fieldGetter);
        return elementCodec.optionalFieldOf(fieldName).forGetter(fieldGetter.andThen(Optional::ofNullable));
    }
}
