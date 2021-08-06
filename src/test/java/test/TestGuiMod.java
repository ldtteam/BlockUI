package test;

import com.ldtteam.blockui.Loader;
import com.ldtteam.blockui.views.Window;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod(TestGuiMod.MOD_ID)
public class TestGuiMod
{
    public static final String MOD_ID = "test_gui";

    public TestGuiMod()
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

    public class TestWindow extends Window
    {
        public TestWindow()
        {
            super(new ResourceLocation(MOD_ID, "gui/test.xml"));
        }
    }
}
