package com.ldtteam.blockui.controls;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneParams;
import com.ldtteam.blockui.views.View;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.blaze3d.platform.GlStateManager.LogicOp;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

/**
 * Class which can be used to add text fields to a pane.
 */
public class TextField extends Pane
{
    protected InputHandler handler;

    private static final int RECT_COLOR = -3_092_272;
    private static final int DEFAULT_MAX_TEXT_LENGTH = 32;
    // Attributes
    protected int maxTextLength = DEFAULT_MAX_TEXT_LENGTH;
    protected int textColor = 0xE0E0E0;
    protected int textColorDisabled = 0x707070;
    protected boolean shadow = true;
    @Nullable
    protected String tabNextPaneID = null;
    // Runtime
    protected String text = "";
    protected Filter filter;
    protected int cursorPosition = 0;
    protected int scrollOffset = 0;
    protected int selectionEnd = 0;
    protected int cursorBlinkCounter = 0;

    /**
     * Simple public constructor to instantiate.
     */
    public TextField()
    {
        super();
        // Required
    }

    /**
     * Public constructor to instantiate the field with params.
     *
     * @param params the parameters for the textField.
     */
    public TextField(final PaneParams params)
    {
        super(params);
        maxTextLength = params.getInteger("maxlength", maxTextLength);
        textColor = params.getColor("color", textColor);
        textColorDisabled = params.getColor("colordisabled", textColorDisabled);
        shadow = params.getBoolean("shadow", shadow);
        text = params.getString("textContent", text);
        tabNextPaneID = params.getString("tab");
    }

    public Filter getFilter()
    {
        return filter;
    }

    public void setFilter(final Filter f)
    {
        filter = f;
    }

    public String getText()
    {
        return text;
    }

    public void setText(final String s)
    {
        text = s.length() <= maxTextLength ? s : s.substring(0, maxTextLength);
        setCursorPosition(text.length());
    }

    public void setTextIgnoreLength(final String s)
    {
        text = s;
        setCursorPosition(text.length());
    }

    public int getInternalWidth()
    {
        return getWidth();
    }

    public int getMaxTextLength()
    {
        return maxTextLength;
    }

    public void setMaxTextLength(final int m)
    {
        maxTextLength = m;
    }

    public int getTextColor()
    {
        return textColor;
    }

    public void setTextColor(final int c)
    {
        textColor = c;
    }

    public int getTextColorDisabled()
    {
        return textColorDisabled;
    }

    public void setTextColorDisabled(final int c)
    {
        textColorDisabled = c;
    }

    @Nullable
    public String getTabNextPaneID()
    {
        return tabNextPaneID;
    }

    public void setTabNextPaneID(final String nextID)
    {
        tabNextPaneID = nextID;
    }

    public int getCursorPosition()
    {
        return cursorPosition;
    }

    public void setCursorPosition(final int pos)
    {
        cursorPosition = Mth.clamp(pos, 0, text.length());
        setSelectionEnd(cursorPosition);
    }

    /**
     * Move the cursor by an offset.
     *
     * @param offset the offset.
     */
    public void moveCursorBy(final int offset)
    {
        setCursorPosition(selectionEnd + offset);
    }

    public int getSelectionEnd()
    {
        return selectionEnd;
    }

    public void setSelectionEnd(final int pos)
    {
        selectionEnd = Mth.clamp(pos, 0, text.length());

        final int internalWidth = getInternalWidth();
        if (internalWidth > 0)
        {
            if (scrollOffset > text.length())
            {
                scrollOffset = text.length();
            }

            final String visibleString = mc.font.plainSubstrByWidth(text.substring(scrollOffset), internalWidth);
            final int rightmostVisibleChar = visibleString.length() + scrollOffset;

            if (selectionEnd == scrollOffset)
            {
                scrollOffset -= mc.font.plainSubstrByWidth(text, internalWidth, true).length();
            }

            if (selectionEnd > rightmostVisibleChar)
            {
                scrollOffset += selectionEnd - rightmostVisibleChar;
            }
            else if (selectionEnd <= scrollOffset)
            {
                scrollOffset -= scrollOffset - selectionEnd;
            }

            scrollOffset = Mth.clamp(scrollOffset, 0, text.length());
        }
    }

        public String getSelectedText()
    {
        final int start = Math.min(cursorPosition, selectionEnd);
        final int end = Math.max(cursorPosition, selectionEnd);
        return text.substring(start, end);
    }

    /**
     * Handle key event.
     *
     * @param c   the character.
     * @param key the key.
     * @return if it should be processed or not.
     */
    private boolean handleKey(final char c, final int key)
    {
        switch (key)
        {
            case GLFW.GLFW_KEY_BACKSPACE:
            case GLFW.GLFW_KEY_DELETE:
                return handleDelete(key);

            case GLFW.GLFW_KEY_HOME:
            case GLFW.GLFW_KEY_END:
                return handleHomeEnd(key);

            case GLFW.GLFW_KEY_RIGHT:
            case GLFW.GLFW_KEY_LEFT:
                return handleArrowKeys(key);

            case GLFW.GLFW_KEY_TAB:
                return handleTab();

            default:
                return handleChar(c);
        }
    }

    private boolean handleChar(final char c)
    {
        if (filter.isAllowedCharacter(c))
        {
            writeText(Character.toString(c));
            return true;
        }
        return false;
    }

    private boolean handleTab()
    {
        if (tabNextPaneID != null)
        {
            final Pane next = getWindow().findPaneByID(tabNextPaneID);
            if (next != null)
            {
                next.setFocus();
            }
        }
        return true;
    }

    private boolean handleArrowKeys(final int key)
    {
        final int direction = (key == GLFW.GLFW_KEY_LEFT) ? -1 : 1;


        if (Screen.hasShiftDown())
        {
            if (Screen.hasControlDown())
            {
                setSelectionEnd(getNthWordFromPos(direction, getSelectionEnd()));
            }
            else
            {
                setSelectionEnd(getSelectionEnd() + direction);
            }
        }
        else if (Screen.hasControlDown())
        {
            setCursorPosition(getNthWordFromCursor(direction));
        }
        else
        {
            moveCursorBy(direction);
        }
        return true;
    }

    private boolean handleHomeEnd(final int key)
    {
        final int position = (key == GLFW.GLFW_KEY_HOME) ? 0 : text.length();

        if (Screen.hasShiftDown())
        {
            setSelectionEnd(position);
        }
        else
        {
            setCursorPosition(position);
        }
        return true;
    }

    private boolean handleDelete(final int key)
    {
        final int direction = (key == GLFW.GLFW_KEY_BACKSPACE) ? -1 : 1;

        if (Screen.hasControlDown())
        {
            deleteWords(direction);
        }
        else
        {
            deleteFromCursor(direction);
        }

        return true;
    }

    @Override
    public void onFocus()
    {
        setCursorPosition(text.length());
        cursorBlinkCounter = 0;
    }

    /**
     * Draw itself at positions mx and my.
     */
    @Override
    public void drawSelf(final PoseStack ms, final double mx, final double my)
    {
        final int color = enabled ? textColor : textColorDisabled;
        final int drawWidth = getInternalWidth();
        final int drawX = x;
        final int drawY = y;

        // Determine the portion of the string that is visible on screen
        final String visibleString = mc.font.plainSubstrByWidth(text.substring(scrollOffset), drawWidth);

        final int relativeCursorPosition = cursorPosition - scrollOffset;
        int relativeSelectionEnd = selectionEnd - scrollOffset;
        final boolean cursorVisible = relativeCursorPosition >= 0 && relativeCursorPosition <= visibleString.length();
        final boolean cursorBeforeEnd = cursorPosition < text.length() || text.length() >= maxTextLength;

        // Enforce selection to the length limit of the visible string
        if (relativeSelectionEnd > visibleString.length())
        {
            relativeSelectionEnd = visibleString.length();
        }

        // Draw string up through cursor
        int textX = drawX;
        if (visibleString.length() > 0)
        {
            final String s1 = cursorVisible ? visibleString.substring(0, relativeCursorPosition) : visibleString;
            textX = drawString(ms, s1, textX, drawY, color, shadow);
        }

        int cursorX = textX;
        if (!cursorVisible)
        {
            cursorX = relativeCursorPosition > 0 ? (drawX + width) : drawX;
        }
        else if (cursorBeforeEnd && shadow)
        {
            textX -= 1;
            cursorX -= 1;
        }

        // Draw string after cursor
        if (visibleString.length() > 0 && cursorVisible && relativeCursorPosition < visibleString.length())
        {
            drawString(ms, visibleString.substring(relativeCursorPosition), textX, drawY, color, shadow);
        }

        // Should we draw the cursor this frame?
        if (isFocus() && cursorVisible && (cursorBlinkCounter / 6 % 2 == 0))
        {
            if (cursorBeforeEnd)
            {
                fill(ms, cursorX, drawY - 1, 1, 1 + mc.font.lineHeight, RECT_COLOR);
            }
            else
            {
                drawString(ms, "_", cursorX, drawY, color, shadow);
            }
        }

        // Draw selection
        if (relativeSelectionEnd != relativeCursorPosition)
        {
            final int selectedDrawX = drawX + mc.font.width(visibleString.substring(0, relativeSelectionEnd));

            int selectionStartX = Math.min(cursorX, selectedDrawX - 1);
            int selectionEndX = Math.max(cursorX, selectedDrawX - 1);

            if (selectionStartX > (x + width))
            {
                selectionStartX = x + width;
            }

            if (selectionEndX > (x + width))
            {
                selectionEndX = x + width;
            }

            final Matrix4f m = ms.last().pose();
            final Tesselator tessellator = Tesselator.getInstance();
            RenderSystem.setShaderColor(0.0F, 0.0F, 1.0F, 1.0F);
            RenderSystem.disableTexture();
            RenderSystem.enableColorLogicOp();
            RenderSystem.logicOp(LogicOp.OR_REVERSE);
            RenderSystem.setShader(GameRenderer::getPositionShader);

            final BufferBuilder vertexBuffer = tessellator.getBuilder();
            vertexBuffer.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
            vertexBuffer.vertex(m, selectionStartX, drawY - 1, 0.0f).endVertex();
            vertexBuffer.vertex(m, selectionStartX, drawY + 1 + mc.font.lineHeight, 0.0f).endVertex();
            vertexBuffer.vertex(m, selectionEndX, drawY + 1 + mc.font.lineHeight, 0.0f).endVertex();
            vertexBuffer.vertex(m, selectionEndX, drawY - 1, 0.0f).endVertex();
            tessellator.end();

            RenderSystem.disableColorLogicOp();
            RenderSystem.enableTexture();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Override
    public void putInside(final View view)
    {
        super.putInside(view);

        // Recompute scroll offset
        setSelectionEnd(selectionEnd);
    }

    @Override
    public boolean handleClick(final double mx, final double my)
    {
        if (mx < 0)
        {
            return false;
        }

        final String visibleString = mc.font.plainSubstrByWidth(text.substring(scrollOffset), getInternalWidth());
        final String trimmedString = mc.font.plainSubstrByWidth(visibleString, (int) mx);

        // Cache and restore scrollOffset when we change focus via click,
        // because onFocus() sets the cursor (and thus scroll offset) to the end.
        final int oldScrollOffset = scrollOffset;
        setFocus();
        scrollOffset = oldScrollOffset;
        setCursorPosition(trimmedString.length() + scrollOffset);
        return true;
    }

    @Override
    public boolean onKeyTyped(final char c, final int key)
    {
        if (Screen.isCopy(key))
        {
            mc.keyboardHandler.setClipboard(getSelectedText());
            return true;
        }
        else if (Screen.isCut(key))
        {
            mc.keyboardHandler.setClipboard(getSelectedText());
            writeText("");
            return true;
        }
        else if (Screen.isSelectAll(key))
        {
            setCursorPosition(text.length());
            setSelectionEnd(0);
            return true;
        }
        else if (Screen.isPaste(key))
        {
            writeText(mc.keyboardHandler.getClipboard());
            return true;
        }
        else
        {
            return handleKey(c, key);
        }
    }

    @Override
    public void onUpdate()
    {
        cursorBlinkCounter++;
    }

    /**
     * Write text into the field.
     *
     * @param str the string to write.
     */
    public void writeText(final String str)
    {
        final String filteredStr = filter.filter(str);

        final int insertAt = Math.min(cursorPosition, selectionEnd);
        final int insertEnd = Math.max(cursorPosition, selectionEnd);
        final int availableChars = (maxTextLength - text.length()) + (insertEnd - insertAt);

        if (availableChars < 0)
        {
            return;
        }

                final StringBuilder resultBuffer = new StringBuilder();
        if (text.length() > 0 && insertAt > 0)
        {
            resultBuffer.append(text.substring(0, insertAt));
        }

        final int insertedLength;
        if (availableChars < filteredStr.length())
        {
            resultBuffer.append(filteredStr.substring(0, availableChars));
            insertedLength = availableChars;
        }
        else
        {
            resultBuffer.append(filteredStr);
            insertedLength = filteredStr.length();
        }

        if (text.length() > 0 && insertEnd < text.length())
        {
            resultBuffer.append(text.substring(insertEnd));
        }

        text = resultBuffer.toString();
        moveCursorBy((insertAt - selectionEnd) + insertedLength);

        triggerHandler();
    }

    private void triggerHandler()
    {
        InputHandler delegatedHandler = handler;

        if (delegatedHandler == null && getWindow() instanceof InputHandler)
        {
            delegatedHandler = (InputHandler) getWindow();
        }

        if (delegatedHandler != null)
        {
            delegatedHandler.onInput(this);
        }
    }

    /**
     * Delete an amount of words.
     *
     * @param count the amount.
     */
    public void deleteWords(final int count)
    {
        if (text.length() != 0)
        {
            if (selectionEnd != cursorPosition)
            {
                this.writeText("");
            }
            else
            {
                deleteFromCursor(this.getNthWordFromCursor(count) - this.cursorPosition);
            }
        }
    }

    /**
     * Set the input handler for this textfield.
     *
     * @param h The new handler.
     */
    public void setHandler(final InputHandler h)
    {
        handler = h;
    }

    /**
     * Delete amount of words from cursor.
     *
     * @param count the amount.
     */
    public void deleteFromCursor(final int count)
    {
        if (text.length() == 0)
        {
            return;
        }

        if (selectionEnd != cursorPosition)
        {
            this.writeText("");
        }
        else
        {
            final boolean backwards = count < 0;
            final int start = backwards ? (this.cursorPosition + count) : this.cursorPosition;
            final int end = backwards ? this.cursorPosition : (this.cursorPosition + count);
                        String result = "";

            if (start > 0)
            {
                result = text.substring(0, start);
            }

            if (end < text.length())
            {
                result = result + text.substring(end);
            }

            text = result;

            if (backwards)
            {
                this.moveCursorBy(count);
            }
        }

        triggerHandler();
    }

    /**
     * Get the n'th word from a position.
     *
     * @param count the n.
     * @param pos   the position.
     * @return the length of the word.
     */
    public int getNthWordFromPos(final int count, final int pos)
    {
        final boolean reverse = count < 0;
        int position = pos;

        for (int i1 = 0; i1 < Math.abs(count); ++i1)
        {
            if (reverse)
            {
                while (position > 0 && text.charAt(position - 1) == ' ')
                {
                    --position;
                }
                while (position > 0 && text.charAt(position - 1) != ' ')
                {
                    --position;
                }
            }
            else
            {
                position = text.indexOf(' ', position);

                if (position == -1)
                {
                    position = text.length();
                }
                else
                {
                    while (position < text.length() && text.charAt(position) == ' ')
                    {
                        ++position;
                    }
                }
            }
        }

        return position;
    }

    /**
     * Get n'th word from cursor position.
     *
     * @param count the n.
     * @return the length.
     */
    public int getNthWordFromCursor(final int count)
    {
        return getNthWordFromPos(count, cursorPosition);
    }

    /**
     * Interface to filter words.
     */
    public interface Filter
    {
        /**
         * Apply the filter.
         *
         * @param s to the string.
         * @return the correct String.
         */
        String filter(String s);

        /**
         * Check if character is allowed.
         *
         * @param c character to test.
         * @return true if so.
         */
        boolean isAllowedCharacter(char c);
    }

    protected int drawString(final PoseStack ms, final String text, final float x, final float y, final int color, final boolean shadow)
    {
        if (shadow)
        {
            return mc.font.drawShadow(ms, text, x, y, color);
        }
        else
        {
            return mc.font.draw(ms, text, x, y, color);
        }
    }
}
