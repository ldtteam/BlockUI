package com.ldtteam.blockui;

import com.ldtteam.blockui.views.BOWindow;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.BitStorage;
import net.minecraft.util.SimpleBitStorage;
import net.minecraftforge.client.ForgeRenderTypes;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.OverlayRegistry;
import org.lwjgl.glfw.GLFW;

/**
 * Wraps MineCrafts GuiScreen for BlockOut's Window.
 */
public class BOScreen extends Screen
{
    protected double renderScale = 1.0d;
    protected double mcScale = 1.0d;
    protected BOWindow window;
    protected double x = 0;
    protected double y = 0;
    public static boolean isMouseLeftDown = false;
    protected boolean isOpen = false;
    private static final BitStorage ACCEPTED_KEY_PRESSED_MAP = new SimpleBitStorage(1, GLFW.GLFW_KEY_LAST + 1);

    static
    {
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_A, 1);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_C, 1);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_V, 1);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_X, 1);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_ESCAPE, 1);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_ENTER, 1);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_TAB, 1);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_BACKSPACE, 1);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_INSERT, 1);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_DELETE, 1);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_RIGHT, 1);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_LEFT, 1);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_DOWN, 1);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_UP, 1);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_PAGE_UP, 1);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_PAGE_DOWN, 1);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_HOME, 1);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_END, 1);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_CAPS_LOCK, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_SCROLL_LOCK, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_NUM_LOCK, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_PRINT_SCREEN, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_PAUSE, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_F1, 1);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_F2, 1);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_F3, 1);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_F4, 1);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_F5, 1);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_F6, 1);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_F7, 1);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_F8, 1);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_F9, 1);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_F10, 1);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_F11, 1);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_F12, 1);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_F13, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_F14, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_F15, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_F16, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_F17, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_F18, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_F19, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_F20, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_F21, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_F22, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_F23, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_F24, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_F25, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_KP_0, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_KP_1, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_KP_2, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_KP_3, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_KP_4, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_KP_5, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_KP_6, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_KP_7, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_KP_8, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_KP_9, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_KP_DECIMAL, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_KP_DIVIDE, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_KP_MULTIPLY, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_KP_SUBTRACT, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_KP_ADD, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_KP_ENTER, 1);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_KP_EQUAL, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_LEFT_SHIFT, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_LEFT_CONTROL, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_LEFT_ALT, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_LEFT_SUPER, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_RIGHT_SHIFT, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_RIGHT_CONTROL, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_RIGHT_ALT, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_RIGHT_SUPER, 0);
        ACCEPTED_KEY_PRESSED_MAP.set(GLFW.GLFW_KEY_MENU, 1);
    }

    /**
     * Create a GuiScreen from a BlockOut window.
     *
     * @param w blockout window.
     */
    public BOScreen(final BOWindow w)
    {
        super(Component.literal("Blockout GUI"));
        window = w;
    }

    @Override
    public void render(final PoseStack ms, final int mx, final int my, final float f)
    {
        if (minecraft == null || !isOpen) // should never happen though
        {
            return;
        }

        final double fbWidth = minecraft.getWindow().getWidth();
        final double fbHeight = minecraft.getWindow().getHeight();
        final double guiWidth = Math.max(fbWidth, 320.0d);
        final double guiHeight = Math.max(fbHeight, 240.0d);

        final float renderZlevel = MatrixUtils.getLastMatrixTranslateZ(ms);
        final float oldZ = minecraft.getItemRenderer().blitOffset;
        minecraft.getItemRenderer().blitOffset = renderZlevel;

        final boolean oldFilteringValue = ForgeRenderTypes.enableTextTextureLinearFiltering;
        ForgeRenderTypes.enableTextTextureLinearFiltering = false;

        mcScale = minecraft.getWindow().getGuiScale();
        renderScale = window.getRenderType().calcRenderScale(minecraft.getWindow(), window);

        if (window.hasLightbox())
        {
            width = (int) fbWidth;
            height = (int) fbHeight;
            super.renderBackground(ms);
        }

        width = window.getWidth();
        height = window.getHeight();
        x = Math.floor((guiWidth - width * renderScale) / 2.0d);
        y = Math.floor((guiHeight - height * renderScale) / 2.0d);

        // replace vanilla projection
        final PoseStack shaderPs = RenderSystem.getModelViewStack();
        final Matrix4f oldProjection = RenderSystem.getProjectionMatrix();
        RenderSystem.setProjectionMatrix(Matrix4f.orthographic(0.0F, (float) fbWidth, 0.0F, (float) fbHeight, -10000.0F, 50000.0F));
        shaderPs.pushPose();
        shaderPs.setIdentity();

        final PoseStack newMs = new PoseStack();
        newMs.translate(x, y, renderZlevel);
        newMs.scale((float) renderScale, (float) renderScale, 1.0f);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();

        try
        {
            window.draw(newMs, calcRelativeX(mx), calcRelativeY(my));
            window.drawLast(newMs, calcRelativeX(mx), calcRelativeY(my));
        }
        catch (final Exception e)
        {
            final CrashReport crashReport = CrashReport.forThrowable(e, "Rendering BO screen");
            final CrashReportCategory category = crashReport.addCategory("BO screen rendering details");
            category.setDetail("XML res loc", () -> window.getXmlResourceLocation().toString());
            category.setDetail("Scaling mode (window render type)", () -> window.getRenderType().name());
            category.setDetail("Vanilla gui scale", () -> Double.toString(mcScale));
            category.setDetail("BO gui scale", () -> Double.toString(renderScale));
            throw new ReportedException(crashReport);
        }
        finally
        {
            // restore vanilla state
            shaderPs.popPose();
            RenderSystem.setProjectionMatrix(oldProjection);
            RenderSystem.applyModelViewMatrix();

            minecraft.getItemRenderer().blitOffset = oldZ;
            ForgeRenderTypes.enableTextTextureLinearFiltering = oldFilteringValue;
        }
    }

    @Override
    public boolean keyPressed(final int key, final int scanCode, final int modifiers)
    {
        // keys without printable representation
        if (key >= 0 && key <= GLFW.GLFW_KEY_LAST)
        {
            try
            {
                return ACCEPTED_KEY_PRESSED_MAP.get(key) == 0 || window.onKeyTyped('\0', key);
            }
            catch (final Exception e)
            {
                final CrashReport crashReport = CrashReport.forThrowable(e, "KeyPressed event for BO screen");
                final CrashReportCategory category = crashReport.addCategory("BO screen key event details");
                category.setDetail("XML res loc", () -> window.getXmlResourceLocation().toString());
                category.setDetail("GLFW key value", () -> Integer.toString(key));
                throw new ReportedException(crashReport);
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(final char ch, final int key)
    {
        try
        {
            return window.onKeyTyped(ch, key);
        }
        catch (final Exception e)
        {
            final CrashReport crashReport = CrashReport.forThrowable(e, "CharTyped event for BO screen");
            final CrashReportCategory category = crashReport.addCategory("BO screen char event details");
            category.setDetail("XML res loc", () -> window.getXmlResourceLocation().toString());
            category.setDetail("Char value", () -> Character.toString(ch));
            throw new ReportedException(crashReport);
        }
    }

    @Override
    public boolean mouseClicked(final double mxIn, final double myIn, final int keyCode)
    {
        final double mx = calcRelativeX(mxIn);
        final double my = calcRelativeY(myIn);
        try
        {
            if (keyCode == GLFW.GLFW_MOUSE_BUTTON_LEFT)
            {
                // Adjust coordinate to origin of window
                isMouseLeftDown = true;
                return window.click(mx, my);
            }
            else if (keyCode == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
            {
                return window.rightClick(mx, my);
            }
        }
        catch (final Exception e)
        {
            final CrashReport crashReport = CrashReport.forThrowable(e, "MousePressed event for BO screen");
            final CrashReportCategory category = crashReport.addCategory("BO screen mouse event details");
            category.setDetail("XML res loc", () -> window.getXmlResourceLocation().toString());
            category.setDetail("GLFW mouse key value", () -> Integer.toString(keyCode));
            throw new ReportedException(crashReport);
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(final double mx, final double my, final double scrollDiff)
    {
        if (scrollDiff != 0)
        {
            try
            {
                return window.scrollInput(scrollDiff * 10, calcRelativeX(mx), calcRelativeY(my));
            }
            catch (final Exception e)
            {
                final CrashReport crashReport = CrashReport.forThrowable(e, "MouseScroll event for BO screen");
                final CrashReportCategory category = crashReport.addCategory("BO screen scroll event details");
                category.setDetail("XML res loc", () -> window.getXmlResourceLocation().toString());
                category.setDetail("Scroll value", () -> Double.toString(scrollDiff));
                throw new ReportedException(crashReport);
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(final double xIn, final double yIn, final int speed, final double deltaX, final double deltaY)
    {
        try
        {
            return window.onMouseDrag(calcRelativeX(xIn), calcRelativeY(yIn), speed, deltaX, deltaY);
        }
        catch (final Exception e)
        {
            final CrashReport crashReport = CrashReport.forThrowable(e, "MouseDragged event for BO screen");
            final CrashReportCategory category = crashReport.addCategory("BO screen mouse event details");
            category.setDetail("XML res loc", () -> window.getXmlResourceLocation().toString());
            throw new ReportedException(crashReport);
        }
    }

    @Override
    public boolean mouseReleased(final double mxIn, final double myIn, final int keyCode)
    {
        if (keyCode == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            // Adjust coordinate to origin of window
            isMouseLeftDown = false;
            try
            {
                return window.onMouseReleased(calcRelativeX(mxIn), calcRelativeY(myIn));
            }
            catch (final Exception e)
            {
                final CrashReport crashReport = CrashReport.forThrowable(e, "MouseReleased event for BO screen");
                final CrashReportCategory category = crashReport.addCategory("BO screen mouse event details");
                category.setDetail("XML res loc", () -> window.getXmlResourceLocation().toString());
                category.setDetail("GLFW mouse key value", () -> Integer.toString(keyCode));
                throw new ReportedException(crashReport);
            }
        }
        return false;
    }

    @Override
    public void init()
    {
        minecraft.keyboardHandler.setSendRepeatsToGui(true);
        OverlayRegistry.enableOverlay(ForgeIngameGui.CROSSHAIR_ELEMENT, false);
    }

    @Override
    public void tick()
    {
        try
        {
            if (minecraft != null)
            {
                if (!isOpen)
                {
                    window.onOpened();
                    isOpen = true;
                }
                else
                {
                    window.onUpdate();

                    if (!minecraft.player.isAlive() || minecraft.player.dead)
                    {
                        minecraft.player.closeContainer();
                    }
                }
            }
        }
        catch (final Exception e)
        {
            final CrashReport crashReport = CrashReport.forThrowable(e, "Ticking/Updating BO screen");
            final CrashReportCategory category = crashReport.addCategory("BO screen update details");
            category.setDetail("XML res loc", () -> window.getXmlResourceLocation().toString());
            category.setDetail("Is opened", () -> Boolean.toString(isOpen));
            throw new ReportedException(crashReport);
        }
    }

    @Override
    public void removed()
    {
        try
        {
            window.onClosed();
        }
        catch (final Exception e)
        {
            final CrashReport crashReport = CrashReport.forThrowable(e, "Closing BO screen");
            final CrashReportCategory category = crashReport.addCategory("BO screen closing details");
            category.setDetail("XML res loc", () -> window.getXmlResourceLocation().toString());
            category.setDetail("Is opened", () -> Boolean.toString(isOpen));
            throw new ReportedException(crashReport);
        }
        finally
        {
            BOWindow.clearFocus();
            minecraft.keyboardHandler.setSendRepeatsToGui(false);
            OverlayRegistry.enableOverlay(ForgeIngameGui.CROSSHAIR_ELEMENT, true);
        }
    }

    @Override
    public boolean isPauseScreen()
    {
        return window.doesWindowPauseGame();
    }

    /**
     * Converts X from event to unscaled and unscrolled X for child in relative (top-left) coordinates.
     */
    private double calcRelativeX(final double xIn)
    {
        return (xIn * mcScale - x) / renderScale;
    }

    /**
     * Converts Y from event to unscaled and unscrolled Y for child in relative (top-left) coordinates.
     */
    private double calcRelativeY(final double yIn)
    {
        return (yIn * mcScale - y) / renderScale;
    }

    public double getRenderScale()
    {
        return renderScale;
    }

    public double getVanillaGuiScale()
    {
        return mcScale;
    }
}
