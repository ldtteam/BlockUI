package com.ldtteam.blockui.mod.container;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.InputHandler;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.controls.TextField;
import com.ldtteam.blockui.hooks.HookRegistries;
import com.ldtteam.blockui.hooks.TriggerMechanism;
import com.ldtteam.blockui.mod.BlockUI;
import com.ldtteam.blockui.mod.Log;
import com.ldtteam.blockui.views.BOWindow;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.blockui.views.ScrollingList.DataProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ContainerHook
{
    public static TagKey<BlockEntityType<?>> CONTAINER_TAG = ForgeRegistries.BLOCK_ENTITIES.tags().createTagKey(new ResourceLocation(BlockUI.MOD_ID, "container_gui"));

    public static void init()
    {
        if (true)
        {
            return;
        }

        final ResourceLocation gui_loc = new ResourceLocation(BlockUI.MOD_ID, "gui/container.xml");
        // TODO: properly support tag reloading
        for (final BlockEntityType<?> beType : ForgeRegistries.BLOCK_ENTITIES.tags().getTag(CONTAINER_TAG))
        {
            HookRegistries.BLOCK_ENTITY_HOOKS.register(beType,
                gui_loc,
                TriggerMechanism.getRayTrace(),
                (thing, type) -> true,
                ContainerHook::onContainerGuiOpen,
                null);
        }
    }

    public static void onContainerGuiOpen(final BlockEntity thing, final BOWindow window, final TriggerMechanism triggerType)
    {
        if (!Minecraft.getInstance().hasSingleplayerServer())
        {
            window.findPaneOfTypeByID("items", ScrollingList.class).off();
            window.findPaneOfTypeByID("filter", TextField.class).off();
            window.findPaneOfTypeByID("note", Text.class).setText(Component.translatable("blockui.container_gui.client_side_only"));
            return;
        }
        else
        {
            final IntegratedServer integratedServer = Minecraft.getInstance().getSingleplayerServer();
            final BlockEntity worldBlockEntity = integratedServer.getLevel(thing.getLevel().dimension())
                .getChunkAt(thing.getBlockPos())
                .getBlockEntity(thing.getBlockPos());

            final Container container = worldBlockEntity instanceof Container c ? c :
                (worldBlockEntity.getType() == BlockEntityType.ENDER_CHEST ? integratedServer.getPlayerList()
                    .getPlayer(integratedServer.getSingleplayerProfile().getId())
                    .getEnderChestInventory() : null);

            if (container == null)
            {
                HookRegistries.BLOCK_ENTITY_HOOKS.unregister(thing.getType(), triggerType);
                Log.getLogger()
                    .error("Removing container gui for type \"{}\" because it's not instance of Container class.",
                        ForgeRegistries.BLOCK_ENTITIES.getKey(thing.getType()));
            }

            final ContainerInfo containerInfo = new ContainerInfo(container);
            if (containerInfo.allItems.isEmpty())
            {
                window.findPaneOfTypeByID("items", ScrollingList.class).off();
                window.findPaneOfTypeByID("filter", TextField.class).off();
                window.findPaneOfTypeByID("note", Text.class).setText(Component.translatable("blockui.container_gui.empty"));
            }
            else
            {
                window.findPaneOfTypeByID("items", ScrollingList.class).setDataProvider(containerInfo);
                window.findPaneOfTypeByID("filter", TextField.class).setHandler(containerInfo);
            }
        }
    }

    private static class ContainerInfo implements DataProvider, InputHandler
    {
        private static final int TICKS_TO_WAIT_FOR_NO_INPUT = 20;

        private final List<ItemInfo> allItems = new ArrayList<>();
        private int ticksLeft = -1;
        private String filter = null;
        private List<ItemInfo> filteredItems = allItems;

        private ContainerInfo(final Container container)
        {
            for (int i = 0; i < container.getContainerSize(); i++)
            {
                final ItemStack isContainer = container.getItem(i);
                if (!isContainer.isEmpty())
                {
                    ItemInfo info = null;
                    for (final ItemInfo itemInfo : allItems)
                    {
                        if (isContainer.sameItem(itemInfo.is))
                        {
                            info = itemInfo;
                            break;
                        }
                    }
                    if (info == null)
                    {
                        allItems.add(new ItemInfo(new ItemStack(isContainer.getItem()), isContainer.getCount()));
                    }
                    else
                    {
                        info.count += isContainer.getCount();
                    }
                }
            }
        }

        @Override
        public void onInput(final TextField input)
        {
            final String newFilter = input.getText().strip();
            if (!newFilter.equals(filter))
            {
                ticksLeft = TICKS_TO_WAIT_FOR_NO_INPUT;
                filter = newFilter;
            }
        }

        @Override
        public int getElementCount() // also tick
        {
            if (ticksLeft >= 0)
            {
                ticksLeft--;
            }
            else
            {
                if (filter == null || filter.isEmpty())
                {
                    filteredItems = allItems;
                }
                else
                {
                    final Set<String> filterSet = Set.of(filter.split(" "));
                    filteredItems = allItems.stream()
                        .filter(info -> Set.of(info.is.getHoverName().getString().split(" ")).containsAll(filterSet)
                            || Set.of(ForgeRegistries.ITEMS.getKey(info.is.getItem()).getPath().split(" _")).containsAll(filterSet))
                        .toList();
                }
            }
            return filteredItems.size();
        }

        @Override
        public void updateElement(final int index, final Pane rowPane)
        {
            final ItemInfo info = filteredItems.get(index);
            final ItemIcon icon = rowPane.findPaneOfTypeByID("icon", ItemIcon.class);
            if (icon.getItem() != info.is)
            {
                icon.setItem(info.is);
                rowPane.findPaneOfTypeByID("name", Text.class).setText(info.is.getHoverName());
                rowPane.findPaneOfTypeByID("quantity", Text.class).setText(Component.literal(Integer.toString(info.count)));
            }
        }

        private static class ItemInfo
        {
            private final ItemStack is;
            private int count;

            private ItemInfo(final ItemStack is, final int count)
            {
                this.is = is;
                this.count = count;
            }
        }
    }
}
