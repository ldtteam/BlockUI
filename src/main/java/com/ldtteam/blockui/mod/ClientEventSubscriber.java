package com.ldtteam.blockui.mod;

import com.ldtteam.blockui.AtlasManager;
import com.ldtteam.blockui.BOScreen;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.controls.Image;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.hooks.HookManager;
import com.ldtteam.blockui.hooks.HookRegistries;
import com.ldtteam.blockui.mod.container.ContainerHook;
import com.ldtteam.blockui.util.resloc.OutOfJarResourceLocation;
import com.ldtteam.blockui.views.BOWindow;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.InputEvent.MouseScrollingEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.util.function.Consumer;

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
    public static void onClientTickStart(final ClientTickEvent.Pre event)
    {
        if (Screen.hasAltDown() && Screen.hasControlDown() && Screen.hasShiftDown())
        {
            if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_X))
            {
                final BOWindow window = new BOWindow();
                int id = 0;

                final Button dumpAtlases = createTestGuiButton(id++, "Dump mod atlases to run folder", null);
                dumpAtlases.setHandler(b -> {
                    final Path dumpingFolder = Path.of("atlas_dump").toAbsolutePath().normalize();
                    Minecraft.getInstance().player.sendSystemMessage(Component.literal("Dumping atlases into: " + dumpingFolder.toString()));
                    AtlasManager.INSTANCE.dumpAtlases(dumpingFolder);
                });
                window.addChild(dumpAtlases);

                window.addChild(createTestGuiButton(id++, "General All-in-one", ResourceLocation.fromNamespaceAndPath(BlockUI.MOD_ID, "gui/test.xml"), parent -> {
                    parent.findPaneOfTypeByID("missing_out_of_jar", Image.class).setImage(OutOfJarResourceLocation.ofMinecraftFolder(BlockUI.MOD_ID, "missing_out_of_jar.png"), false);
                    parent.findPaneOfTypeByID("working_out_of_jar", Image.class).setImage(OutOfJarResourceLocation.of(BlockUI.MOD_ID, Path.of("../../src/test/resources/button.png")), false);
                }));
                window.addChild(createTestGuiButton(id++, "Tooltip Positioning", ResourceLocation.fromNamespaceAndPath(BlockUI.MOD_ID, "gui/test2.xml")));
                window.addChild(createTestGuiButton(id++, "ItemIcon To BlockState", ResourceLocation.fromNamespaceAndPath(BlockUI.MOD_ID, "gui/test3.xml"), BlockStateTestGui::setup));
                window.addChild(createTestGuiButton(id++, "Scrolling Lists", ResourceLocation.fromNamespaceAndPath(BlockUI.MOD_ID, "gui/test4.xml"), ScrollingListsGui::setup));

                final Text builderTest = new Text();
                builderTest.setSize(ButtonImage.DEFAULT_BUTTON_WIDTH * 2 + 20, ButtonImage.DEFAULT_BUTTON_HEIGHT);
                builderTest.setPosition(0, ((id + 1) / 2) * (builderTest.getHeight() + 10));
                PaneBuilders.textBuilder()
                    .append(Component.literal(BlockUI.MOD_ID))
                    .append(Component.literal(" - "))
                    .append(Component.literal(ModList.get().getModFileById(BlockUI.MOD_ID).versionString()))
                    .paragraphBreak()
                    .colorName("red")
                    .underlined()
                    .append(Component.translatable("blockui.tooltip.item_additional_info",
                        Component.translatable("key.keyboard.left.control")
                            .append(" + ")
                            .append(Component.translatable("key.keyboard.left.shift"))
                            .append(" + ")
                            .append(Component.translatable("key.keyboard.left.alt"))
                            .setStyle(Style.EMPTY.withItalic(true))))
                    .applyToPane(builderTest);
                window.addChild(builderTest);

                window.open();
            }
        }
    }

    @SubscribeEvent
    public static void onClientTickEnd(final ClientTickEvent.Post event)
    {
        if (Minecraft.getInstance().level != null)
        {
            Minecraft.getInstance().getProfiler().push("hook_manager_tick");
            HookRegistries.tick(Minecraft.getInstance().level.getGameTime());
            Minecraft.getInstance().getProfiler().pop();
        }
    }

    @SafeVarargs
    private static Button createTestGuiButton(final int order,
        final String name,
        final ResourceLocation testGuiResLoc,
        final Consumer<BOWindow>... setups)
    {
        final Button button = new ButtonImage();
        button.setPosition((order % 2) * (button.getWidth() + 20), (order / 2) * (button.getHeight() + 10));
        button.setText(Component.literal(name));
        button.setHandler(b -> {
            new BOWindow(testGuiResLoc)
            {
                @Override
                public void onOpened()
                {
                    super.onOpened();
                    for (final Consumer<BOWindow> setup : setups)
                    {
                        setup.accept(this);
                    }
                }
            }.openAsLayer();
        });
        return button;
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
        event.setCanceled(HookManager.onScroll(event.getScrollDeltaX(), event.getScrollDeltaY()));
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
    public static void renderOverlay(final RenderGuiLayerEvent.Pre event)
    {
        if (Minecraft.getInstance().screen instanceof BOScreen && event.getName().equals(VanillaGuiLayers.CROSSHAIR))
        {
            event.setCanceled(true);
        }
    }
}
