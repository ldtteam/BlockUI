package com.ldtteam.blockui;

import com.ldtteam.blockui.views.BOWindow;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.PreeditEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.state.WindowRenderState;
import net.minecraft.network.chat.Component;
import org.joml.Matrix2f;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

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
    protected int framebufferWidth;
    protected int framebufferHeight;
    protected int absoluteMouseX;
    protected int absoluteMouseY;

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
    public void extractRenderState(final GuiGraphicsExtractor ms, final int mx, final int my, final float f)
    {
        if (ms.minecraft == null || !isOpen) // should never happen though
        {
            return;
        }
        final WindowRenderState windowState = ms.minecraft.gameRenderer.getGameRenderState().windowRenderState;

        absoluteMouseX = mx;
        absoluteMouseY = my;
        framebufferWidth = windowState.width; // ms.minecraft.getWindow().getWidth();
        framebufferHeight = windowState.height; // ms.minecraft.getWindow().getHeight();
        final int guiWidth = Math.max(framebufferWidth, Window.BASE_WIDTH);
        final int guiHeight = Math.max(framebufferHeight, Window.BASE_HEIGHT);

        mcScale = windowState.guiScale; // ms.minecraft.getWindow().getGuiScale();
        renderScale = window.getRenderType().calcRenderScale(ms.minecraft.getWindow(), window);

        width = window.getWidth();
        height = window.getHeight();
        x = Math.floor((guiWidth - width * renderScale) / 2.0d);
        y = Math.floor((guiHeight - height * renderScale) / 2.0d);

        // counter vanilla projection
        final Projection vanillaProjection = new Projection();
        // INLINE: this is copied from wherever vanilla is doing projection for GUI
        vanillaProjection.setupOrtho(1000.0F,
            11000.0F,
            (float) windowState.width / windowState.guiScale,
            (float) windowState.height / windowState.guiScale,
            true);

        final Matrix4f oldProjection = vanillaProjection.getMatrix(new Matrix4f());
        final Matrix4f oldViewModel = new Matrix4f().setTranslation(0.0F, 0.0F, -11000.0F);
        // INLINE: end

        final Matrix4f ourProjection = new Matrix4f().setOrtho(0.0F, framebufferWidth, framebufferHeight, 0.0F, vanillaProjection.zNear(), vanillaProjection.zFar());

        // if ever vanilla decides allows to use 1.21 system immediately do so
        // this hack is based on following facts at the time when it was written:
        // 1) the only thing that does matrix computation is vertex shader (VS)
        // 2) the said VS does classic vertex math: output = P * VM * input
        // 3) there is no other math (or scissoring stuff) evolder around old P * VM
        // 4) the old P and VM are properly copied from vanilla (INLINE above)
        // 5) the matrixes are stable enough to not under/overflow
        final Matrix4f hack = new Matrix4f();
        hack.mul(oldViewModel.invert()); // this internally does invertTranslation
        hack.mul(oldProjection.invertOrtho());
        hack.mul(ourProjection);

        final var newMs = new BOGuiGraphics.CountingMatrix3x2fStack(16);
        // inject hack
        newMs.mul(new Matrix3x2f(new Matrix2f(hack.m00(), hack.m01(), hack.m10(), hack.m11())));
        // our stuff, this assumes projection matrix is size of FB

        try
        {
            final double newMx = calcRelativeX(mx), newMy = calcRelativeY(my);
            final BOGuiGraphics target = new BOGuiGraphics(ms.minecraft, newMs, ms.guiRenderState, (int) newMx, (int) newMy);

            if (window.hasBlurredBackground() && ms.minecraft.screen == this && target.guiRenderState.firstStratumAfterBlur == Integer.MAX_VALUE)
            {
                target.blurBeforeThisStratum();
            }

            if (window.hasLightbox() && ms.minecraft.screen == this)
            {
                UiRenderMacros.fillGradient(target, 0, 0, framebufferWidth, framebufferHeight, -1072689136, -804253680);
                // super.extractTransparentBackground(target);
            }

            newMs.translate((float) x, (float) y);
            newMs.scale((float) renderScale, (float) renderScale);

            window.draw(target, newMx, newMy);

            if (ms.minecraft.screen == this)
            {
                int debugX = (int) (-x / renderScale) + 3;
                if (Pane.debugging)
                {
                    debugX = target.drawString(
                        "XML: %s Scaling: %s (vanilla: %.2f our: %.2f) "
                            .formatted(window.getXmlResourceLocation(), window.getRenderType().name(), mcScale, renderScale),
                        debugX,
                        -minecraft.font.lineHeight,
                        Color.getByName("white"));
                }
                ms.requestCursor(target.applyCursor(debugX));
            }

            target.nextStratum(); // TODO: simulate Z layering a bit, we really need to write sorted layering for us to be stable..

            window.drawLast(target, newMx, newMy);
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
    }

    @Override // INLINE: partial inline - completely remove any extraction
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        this.minecraft.gui.extractDeferredSubtitles();
    }

    @Override
    public boolean keyPressed(final KeyEvent event)
    {
        final int key = event.key();
        // keys without printable representation
        if (key >= 0 && key <= GLFW.GLFW_KEY_LAST)
        {
            try
            {
                return window.onKeyEvent(event);
            }
            catch (final Exception e)
            {
                final CrashReport crashReport = CrashReport.forThrowable(e, "KeyPressed event for BO screen");
                final CrashReportCategory category = crashReport.addCategory("BO screen key event details");
                category.setDetail("XML res loc", () -> window.getXmlResourceLocation().toString());
                category.setDetail("GLFW key value", () -> Integer.toString(event.input()));
                throw new ReportedException(crashReport);
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(final CharacterEvent event)
    {
        try
        {
            return window.onCharactedEvent(event);
        }
        catch (final Exception e)
        {
            final CrashReport crashReport = CrashReport.forThrowable(e, "CharTyped event for BO screen");
            final CrashReportCategory category = crashReport.addCategory("BO screen char event details");
            category.setDetail("XML res loc", () -> window.getXmlResourceLocation().toString());
            category.setDetail("Char value", () -> Character.toString(event.codepoint()));
            throw new ReportedException(crashReport);
        }
    }

    @Override
    public boolean preeditUpdated(final PreeditEvent event)
    {
        // TODO: implement this in text field
        return true;
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick)
    {
        final int keyCode = event.button();
        final double mx = calcRelativeX(event.x());
        final double my = calcRelativeY(event.y());
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
            category.setDetail("XML res loc", () -> Objects.toString(window.getXmlResourceLocation()));
            category.setDetail("GLFW mouse key value", () -> Integer.toString(keyCode));
            throw new ReportedException(crashReport);
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(final double mx, final double my, final double scrollHorizontalDiff, final double scrollVerticalDiff)
    {
        if (scrollVerticalDiff != 0)
        {
            try
            {
                return window.scrollInput(scrollHorizontalDiff * 10, scrollVerticalDiff * 10, calcRelativeX(mx), calcRelativeY(my));
            }
            catch (final Exception e)
            {
                final CrashReport crashReport = CrashReport.forThrowable(e, "MouseScroll event for BO screen");
                final CrashReportCategory category = crashReport.addCategory("BO screen scroll event details");
                category.setDetail("XML res loc", () -> window.getXmlResourceLocation().toString());
                category.setDetail("Scroll value", () -> Double.toString(scrollVerticalDiff));
                throw new ReportedException(crashReport);
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(final MouseButtonEvent event, final double deltaX, final double deltaY)
    {
        try
        {
            return window.onMouseDrag(calcRelativeX(event.x()), calcRelativeY(event.y()), 0, deltaX, deltaY);
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
    public boolean mouseReleased(final MouseButtonEvent event)
    {
        final int keyCode = event.button();
        if (keyCode == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            // Adjust coordinate to origin of window
            isMouseLeftDown = false;
            try
            {
                return window.onMouseReleased(calcRelativeX(event.x()), calcRelativeY(event.y()));
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

    /**
     * Get the open window here.
     * @return the window.
     */
    public BOWindow getWindow()
    {
        return window;
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

                    final LocalPlayer player = minecraft == null ? null : minecraft.player;
                    if (player != null && (!player.isAlive() || player.dead))
                    {
                        player.closeContainer();
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

    public int getFramebufferWidth()
    {
        return framebufferWidth;
    }

    public int getFramebufferHeight()
    {
        return framebufferHeight;
    }

    public int getAbsoluteMouseX()
    {
        return absoluteMouseX;
    }

    public int getAbsoluteMouseY()
    {
        return absoluteMouseY;
    }
}
