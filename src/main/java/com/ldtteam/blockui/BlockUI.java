package com.ldtteam.blockui;

import com.ldtteam.blockui.views.BOWindow;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod(BlockUI.MOD_ID)
public class BlockUI
{
    public static final String MOD_ID = "blockui";

    public BlockUI()
    {
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().addListener(this::onClientTick);
    }

    @SubscribeEvent
    public void onClientTick(final ClientTickEvent event)
    {
        if (event.phase == Phase.START && Screen.hasAltDown() && Screen.hasControlDown() && Screen.hasShiftDown()
            && InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_X))
        {
            new TestWindow().open();
        }
    }

    public class TestWindow extends BOWindow
    {
        public TestWindow()
        {
            super(new ResourceLocation(MOD_ID, "gui/test.xml"));
        }
    }
}
