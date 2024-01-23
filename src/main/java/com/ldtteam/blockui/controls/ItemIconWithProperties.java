package com.ldtteam.blockui.controls;

import com.ldtteam.blockui.BOGuiGraphics;
import com.ldtteam.blockui.PaneParams;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Useful for overriding things like clock/compass textures. In xml defined through {@value #PARAM_PROPERTIES} key using nbt:
 * {<item registry key>:{<property name>:<float value>, ...}, ...}.
 * <p>
 * Special keys: {@value #NBT_CURRENT_ITEM} - refers to xml item (resolved during parsing not dynamic),
 * {@value #NBT_GENERIC_KEY} - generic properties
 * 
 * @see ItemProperties
 */
@SuppressWarnings("deprecation")
public class ItemIconWithProperties extends ItemIcon
{
    private static final String NBT_GENERIC_KEY = "_generic";
    private static final String NBT_CURRENT_ITEM = "_item";

    public static final String PARAM_PROPERTIES = "properties";

    protected final Map<ResourceLocation, ItemPropertyFunction> genericPropertyOverrides = new HashMap<>();
    protected final Map<Item, Map<ResourceLocation, ItemPropertyFunction>> itemPropertyOverrides = new HashMap<>();
    private Map<ResourceLocation, ItemPropertyFunction> currentItemOverrides = Collections.emptyMap();

    public ItemIconWithProperties()
    {
        super();
    }

    public ItemIconWithProperties(final PaneParams paneParams)
    {
        super(paneParams);

        final String data = paneParams.getString(PARAM_PROPERTIES);
        if (data != null && itemStack != null)
        {
            final CompoundTag tag;
            try
            {
                tag = TagParser.parseTag(data);
            }
            catch (CommandSyntaxException e)
            {
                throw new RuntimeException(data, null);
            }
            tag.getAllKeys().forEach(itemKey -> {
                if (tag.contains(itemKey, Tag.TAG_COMPOUND))
                {
                    final CompoundTag child = tag.getCompound(itemKey);
                    final var itemOverrides = NBT_GENERIC_KEY.equals(itemKey) ? genericPropertyOverrides :
                        itemPropertyOverrides.computeIfAbsent(NBT_CURRENT_ITEM.equals(itemKey) ? itemStack.getItem() :
                            ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemKey)), i -> new HashMap<>());

                    child.getAllKeys().forEach(key -> {
                        if (child.contains(key, Tag.TAG_ANY_NUMERIC))
                        {
                            final float value = child.getFloat(key); // intentionally ouf of lambda
                            itemOverrides.put(new ResourceLocation(key), (itemStack, level, entity, seee) -> value);
                        }
                    });
                }
            });

            onItemUpdate();
        }
    }

    /**
     * Short call for adding itemProperty to current item
     */
    public void addPropertyForCurrentItem(final ResourceLocation propertyKey, final ClampedItemPropertyFunction property)
    {
        itemPropertyOverrides
            .computeIfAbsent(Objects.requireNonNull(itemStack, "Call #setItem before this method").getItem(), item -> new HashMap<>())
            .put(propertyKey, property);
    }

    /**
     * @return modifiable all item-based overrides
     */
    public Map<Item, Map<ResourceLocation, ItemPropertyFunction>> getItemPropertyOverrides()
    {
        return itemPropertyOverrides;
    }

    /**
     * @return modifiable generic overrides
     */
    public Map<ResourceLocation, ItemPropertyFunction> getGenericPropertyOverrides()
    {
        return genericPropertyOverrides;
    }

    @Override
    public void drawSelf(final BOGuiGraphics target, final double mx, final double my)
    {
        if (isDataEmpty())
        {
            return;
        }
        if (currentItemOverrides.isEmpty() && genericPropertyOverrides.isEmpty())
        {
            super.drawSelf(target, mx, my);
            return;
        }
        final Item item = itemStack.getItem();

        // generic
        final Map<ResourceLocation, ItemPropertyFunction> oldGenericVals =
            genericPropertyOverrides.isEmpty() ? Collections.emptyMap() : new HashMap<>();
        genericPropertyOverrides.forEach((key, val) -> {
            oldGenericVals.put(key, ItemProperties.getProperty(item, key));
            ItemProperties.registerGeneric(key, val);
        });

        // item
        final Map<ResourceLocation, ItemPropertyFunction> oldItemVals =
            currentItemOverrides.isEmpty() ? Collections.emptyMap() : new HashMap<>();
        currentItemOverrides.forEach((key, val) -> {
            oldItemVals.put(key, ItemProperties.getProperty(item, key));
            ItemProperties.register(item, key, val);
        });

        super.drawSelf(target, mx, my);

        oldItemVals.forEach((key, val) -> ItemProperties.register(item, key, val));
        oldGenericVals.forEach((key, val) -> ItemProperties.registerGeneric(key, val));
    }

    @Override
    protected void onItemUpdate()
    {
        super.onItemUpdate();
        if (itemPropertyOverrides != null) // ctor race condition
        {
            currentItemOverrides = itemPropertyOverrides.getOrDefault(itemStack.getItem(), Collections.emptyMap());
        }
    }
}
