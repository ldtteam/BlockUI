package com.ldtteam.blockui.mod;

import com.ldtteam.blockui.UiWindow;
import com.ldtteam.blockui.context.RenderContextType;
import com.ldtteam.blockui.element.DelegatedShape.Shape;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ClientEventSubscriber
{
    @SubscribeEvent
    public static void renderEnd(final RenderTickEvent event)
    {
        if (event.phase == Phase.END)
        {
            RenderContextType.applyToAll(ctx -> ctx.resourceReloaded = false);
        }
    }

    @SubscribeEvent
    public static void guiTest(final ScreenEvent.Render.Post event)
    {
        RenderContextType.GUI.grab(event.getGuiGraphics());

        final Shape<?> test = Shape.createVanillaTooltip(new UiWindow(new ResourceLocation(BlockUI.MOD_ID, "asd")).getRootPane());
        test.getWindow().getRootPane().setSize(event.getScreen().width, event.getScreen().height);

        test.setSize(30, 50);
        test.getWindow().getRootPane().render(RenderContextType.GUI, event.getMouseX(), event.getMouseY());

        /*
        final Shape<?> test = new Shape<>(new UiWindow().getRootPane());
        test.getWindow().getRootPane().setSize(event.getScreen().width, event.getScreen().height);
        
        test.setRectangle();
        test.setSize(130, 260);
        
        test.setPos(50, 100);
        test.setColorDirection(ColorDirection.VERTICAL);
        test.getWindow().draw(RenderContextType.GUI, event.getMouseX(), event.getMouseY());
        
        test.setPos(200, 100);
        test.setColorDirection(ColorDirection.HORIZONTAL);
        test.getWindow().draw(RenderContextType.GUI, event.getMouseX(), event.getMouseY());
        
        test.setPos(350, 100);
        test.setColorDirection(ColorDirection.PRIMARY_DIAGONAL);
        test.getWindow().draw(RenderContextType.GUI, event.getMouseX(), event.getMouseY());
        
        test.setPos(500, 100);
        test.setColorDirection(ColorDirection.SECONDARY_DIAGONAL);
        test.getWindow().draw(RenderContextType.GUI, event.getMouseX(), event.getMouseY());
        
        test.setLineBox().setLineWidth(50);
        test.setSize(130, 260);
        
        test.setPos(50, 100);
        test.setColorDirection(ColorDirection.VERTICAL);
        test.getWindow().draw(RenderContextType.GUI, event.getMouseX(), event.getMouseY());
        
        test.setPos(200, 100);
        test.setColorDirection(ColorDirection.HORIZONTAL);
        test.getWindow().draw(RenderContextType.GUI, event.getMouseX(), event.getMouseY());
        
        test.setPos(350, 100);
        test.setColorDirection(ColorDirection.PRIMARY_DIAGONAL);
        test.getWindow().draw(RenderContextType.GUI, event.getMouseX(), event.getMouseY());
        
        test.setPos(500, 100);
        test.setColorDirection(ColorDirection.SECONDARY_DIAGONAL);
        test.getWindow().draw(RenderContextType.GUI, event.getMouseX(), event.getMouseY());
        */

        RenderContextType.GUI.tickAndRelease();
    }
}
