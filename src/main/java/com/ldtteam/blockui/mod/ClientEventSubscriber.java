package com.ldtteam.blockui.mod;

import com.ldtteam.blockui.BOScreen;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.hooks.HookManager;
import com.ldtteam.blockui.hooks.HookRegistries;
import com.ldtteam.blockui.mod.container.ContainerHook;
import com.ldtteam.blockui.util.records.SizeI;
import com.ldtteam.blockui.views.BOWindow;
import com.ldtteam.blockui.views.ScrollingList;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.InputEvent.MouseScrollingEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.ModMismatchEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class ClientEventSubscriber
{
    /**
     * Used to catch the renderWorldLastEvent in order to draw the debug nodes for pathfinding.
     *
     * @param event the catched event.
     */
    /* TODO: fixme
    public static void renderWorldLastEvent(@NotNull final RenderLevelLastEvent event)
    {
        final PoseStack ps = event.getPoseStack();
        final Vec3 viewPosition = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        ps.pushPose();
        ps.translate(-viewPosition.x(), -viewPosition.y(), -viewPosition.z());
        HookRegistries.render(ps, event.getPartialTick());
        ps.popPose();
    }*/

    /**
     * Used to catch the clientTickEvent.
     * Call renderer cache cleaning every 5 secs (100 ticks).
     *
     * @param event the catched event.
     */
    @SubscribeEvent
    public static void onClientTickEvent(final ClientTickEvent event)
    {
        if (event.phase == Phase.START && Screen.hasAltDown() && Screen.hasControlDown() && Screen.hasShiftDown())
        {
            if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_X))
            {
                new BOWindow(new ResourceLocation(BlockUI.MOD_ID, "gui/test.xml")).open();
            }
            else if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_C))
            {
                new BOWindow(new ResourceLocation(BlockUI.MOD_ID, "gui/test2.xml")).open();
            }
            else if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_Z))
            {
                final BOWindow boWindow3 = new BOWindow(new ResourceLocation(BlockUI.MOD_ID, "gui/test3.xml"));
                boWindow3.open();

                final ScrollingList list1 = boWindow3.findPaneOfTypeByID("list1", ScrollingList.class);
                list1.setDataProvider(new ScrollingList.DataProvider() {
                    @Override
                    public int getElementCount()
                    {
                        return 10;
                    }

                    @Override
                    public void updateElement(final int index, final Pane rowPane)
                    {
                        rowPane.findPaneByType(Text.class).setText(Component.literal("Hi " + index));
                    }
                });

                final ScrollingList list2 = boWindow3.findPaneOfTypeByID("list2", ScrollingList.class);
                list2.setDataProvider(new ScrollingList.DataProvider() {
                    @Override
                    public int getElementCount()
                    {
                        return 10;
                    }

                    @Override
                    public @Nullable SizeI getElementSize(final int index, final Pane rowPane)
                    {
                        return index % 2 == 0 ? new SizeI(100, 40) : null;
                    }

                    @Override
                    public void updateElement(final int index, final Pane rowPane)
                    {
                        rowPane.findPaneByType(Text.class).setText(Component.literal("Hi " + index));
                    }
                });
            }
        }

        if (event.phase == Phase.END && Minecraft.getInstance().level != null)
        {
            Minecraft.getInstance().getProfiler().push("hook_manager_tick");
            HookRegistries.tick(Minecraft.getInstance().level.getGameTime());
            Minecraft.getInstance().getProfiler().pop();
        }
    }

    /**
     * Used to catch the scroll when no gui is open.
     *
     * @param event the catched event.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseScrollEvent(final MouseScrollingEvent event)
    {
        // cancel in-game scrolling when raytraced gui has scrolling list
        event.setCanceled(HookManager.onScroll(event.getScrollDelta()));
    }

    /**
     * Hook test container gui.
     */
    @SubscribeEvent
    public static void onTagsUpdated(final TagsUpdatedEvent event)
    {
        ContainerHook.init();
    }

    @SubscribeEvent
    public static void renderOverlay(final RenderGuiOverlayEvent event)
    {
        if (Minecraft.getInstance().screen instanceof BOScreen && event.getOverlay() == VanillaGuiOverlay.CROSSHAIR.type())
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onModMismatch(final ModMismatchEvent event)
    {
        // there are no world data and rest is mod compat anyway
        event.markResolved(BlockUI.MOD_ID);
    }
}
