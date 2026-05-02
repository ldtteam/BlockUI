package com.ldtteam.common.util;

import com.ldtteam.blockui.util.SafeError;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Mimic what codec would do, but vanilla doesn't coded that for some reason.
 */
public class CompoundTagToClassReflection
{
    public static void compoundToClassFields(final CompoundTag data, final Object target, final String errorContext)
    {
        final Map<String, Field> fields = getAllFields(target);
        for (final String fieldName : data.keySet())
        {
            final Field field = fields.get(fieldName);
            if (field == null)
            {
                SafeError.throwInDev(new IllegalArgumentException(errorContext + ": cannot find field: " + fieldName));
                continue;
            }
            if (!field.canAccess(target))
            {
                SafeError.throwInDev(new IllegalArgumentException(errorContext + ": cannot access field: " + fieldName));
                continue;
            }

            final Tag value = data.get(fieldName);

            switch (value)
            {
                case ByteArrayTag t -> setFieldObject(field, target, t.getAsByteArray(), errorContext);
                case IntArrayTag t -> setFieldObject(field, target, t.getAsIntArray(), errorContext);
                case LongArrayTag t -> setFieldObject(field, target, t.getAsLongArray(), errorContext);
                case StringTag t -> setFieldObject(field, target, t.value(), errorContext);
                case ByteTag t -> {
                    if (field.getType() == byte.class)
                    {
                        try
                        {
                            field.setByte(target, t.value());
                        }
                        catch (IllegalArgumentException | IllegalAccessException e)
                        {
                            SafeError.throwInDev(new RuntimeException(errorContext + ": trying to set byte field - " + fieldName, e));
                        }
                    }
                    else if (field.getType() == Byte.class)
                    {
                        setFieldObject(field, target, t.value(), errorContext);
                    }
                    else if (field.getType() == boolean.class)
                    {
                        try
                        {
                            field.setBoolean(target, t.value() != 0);
                        }
                        catch (IllegalArgumentException | IllegalAccessException e)
                        {
                            SafeError
                                .throwInDev(new RuntimeException(errorContext + ": trying to set boolean field - " + fieldName, e));
                        }
                    }
                    else if (field.getType() == Boolean.class)
                    {
                        setFieldObject(field, target, t.value() != 0, errorContext);
                    }
                }
                case DoubleTag t -> {
                    if (field.getType() == double.class)
                    {
                        try
                        {
                            field.setDouble(target, t.value());
                        }
                        catch (IllegalArgumentException | IllegalAccessException e)
                        {
                            SafeError
                                .throwInDev(new RuntimeException(errorContext + ": trying to set double field - " + fieldName, e));
                        }
                    }
                    else if (field.getType() == Double.class)
                    {
                        setFieldObject(field, target, t.value(), errorContext);
                    }
                }
                case FloatTag t -> {
                    if (field.getType() == float.class)
                    {
                        try
                        {
                            field.setFloat(target, t.value());
                        }
                        catch (IllegalArgumentException | IllegalAccessException e)
                        {
                            SafeError.throwInDev(new RuntimeException(errorContext + ": trying to set float field - " + fieldName, e));
                        }
                    }
                    else if (field.getType() == Float.class)
                    {
                        setFieldObject(field, target, t.value(), errorContext);
                    }
                }
                case IntTag t -> {
                    if (field.getType() == int.class)
                    {
                        try
                        {
                            field.setInt(target, t.value());
                        }
                        catch (IllegalArgumentException | IllegalAccessException e)
                        {
                            SafeError.throwInDev(new RuntimeException(errorContext + ": trying to set int field - " + fieldName, e));
                        }
                    }
                    else if (field.getType() == Integer.class)
                    {
                        setFieldObject(field, target, t.value(), errorContext);
                    }
                }
                case LongTag t -> {
                    if (field.getType() == long.class)
                    {
                        try
                        {
                            field.setLong(target, t.value());
                        }
                        catch (IllegalArgumentException | IllegalAccessException e)
                        {
                            SafeError.throwInDev(new RuntimeException(errorContext + ": trying to set long field - " + fieldName, e));
                        }
                    }
                    else if (field.getType() == Long.class)
                    {
                        setFieldObject(field, target, t.value(), errorContext);
                    }
                }
                case ShortTag t -> {
                    if (field.getType() == short.class)
                    {
                        try
                        {
                            field.setShort(target, t.value());
                        }
                        catch (IllegalArgumentException | IllegalAccessException e)
                        {
                            SafeError.throwInDev(new RuntimeException(errorContext + ": trying to set short field - " + fieldName, e));
                        }
                    }
                    else if (field.getType() == Short.class)
                    {
                        setFieldObject(field, target, t.value(), errorContext);
                    }
                }
                case CompoundTag t -> {
                    // TODO: this means nested object

                    // it can be final - just call recursion
                    // it can be present - just call recursion
                    // it can be null - either it has no-arg ctor -> recursion
                    // or we need special ctor '_ctor' nested compound -> ctor + recurion
                    // or sth more?

                    Object targetValue;
                    try
                    {
                        targetValue = field.get(target);
                    }
                    catch (IllegalArgumentException | IllegalAccessException e)
                    {
                        SafeError.throwInDev(new RuntimeException(errorContext + ": trying to get Object field - " + fieldName, e));
                        break;
                    }
                    if (targetValue == null)
                    {
                        final var ctors = field.getType().getDeclaredConstructors();
                        if (t.contains("_ctor"))
                        {
                            // TODO: find matching ctor
                        }
                        for (final var ctor : ctors)
                        {
                            if (ctor.getParameterTypes().length == 0)
                            {
                                try
                                {
                                    targetValue = ctor.newInstance();
                                }
                                catch (InstantiationException |
                                    IllegalAccessException |
                                    IllegalArgumentException |
                                    InvocationTargetException e)
                                {
                                    // continue
                                }
                            }
                        }
                        SafeError.requireNonNull(targetValue,
                            errorContext + ": trying to instantiate null target value - no matching constructor found");
                        try
                        {
                            field.set(target, targetValue);
                        }
                        catch (IllegalArgumentException | IllegalAccessException e)
                        {
                            SafeError.throwInDev(new RuntimeException(errorContext + ": trying to set Object field - " + fieldName, e));
                            break;
                        }
                    }

                    compoundToClassFields(t, targetValue, errorContext + " (inside field:" + fieldName + ")");
                }
                case ListTag t -> {
                    // TODO: this means field is
                    // array
                    // list
                    // can be final or not (if not final create appropriate list/array, if final must match length)
                }
                case EndTag _ -> {}
                case null -> {}
            }
        }
    }

    private static <T> void setFieldObject(final Field field, final Object target, final T value, final String errorContext)
    {
        if (field.getType() != value.getClass())
        {
            SafeError.throwInDev(new IllegalArgumentException(errorContext + ": field '%s' - expected type '%s' - was '%s'"
                .formatted(field.getName(), field.getType().getTypeName(), value.getClass().getTypeName())));
            return;
        }

        try
        {
            field.set(target, value);
        }
        catch (IllegalArgumentException | IllegalAccessException e)
        {
            SafeError.throwInDev(new RuntimeException(errorContext + ": trying to set field '%s' - expected type '%s' - was '%s'"
                .formatted(field.getName(), field.getType().getTypeName(), value.getClass().getTypeName()), e));
        }
        return;
    }

    private static Map<String, Field> getAllFields(final Object object)
    {
        final Map<String, Field> fields = new HashMap<>();
        Class<?> clazz = object.getClass();
        while (clazz != Object.class)
        {
            for (final Field field : clazz.getDeclaredFields())
            {
                fields.put(field.getName(), field);
            }
            clazz = clazz.getSuperclass();
        }
        return fields;
    }
}
