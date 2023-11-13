package com.ldtteam.blockui.controls;

import com.ldtteam.blockui.Alignment;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneParams;
import com.ldtteam.blockui.util.cursor.Cursor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

/**
 * Base button class.
 * Has a handler for when the button is clicked.
 */
public abstract class Button extends AbstractTextElement
{
    protected ButtonHandler handler;

    /**
     * Default constructor.
     */
    public Button()
    {
        super();
        this.cursor = Cursor.HAND;
    }

    /**
     * Constructor used when loading from xml.
     *
     * @param params PaneParams from xml file.
     */
    public Button(final PaneParams params)
    {
        super(params);
        this.cursor = this.cursor == Cursor.DEFAULT ? Cursor.HAND : this.cursor;
    }

    /**
     * Construct a button from the parameters according to set out text defaults
     */
    public Button(
      final PaneParams params,
      final Alignment alignment,
      final int enabledColor,
      final int hoverColor,
      final int disabledColor,
      final boolean hasShadow,
      final boolean shouldWrap)
    {
        super(params, alignment, enabledColor, hoverColor, disabledColor, hasShadow, shouldWrap);
        this.cursor = this.cursor == Cursor.DEFAULT ? Cursor.HAND : this.cursor;
    }

    /**
     * Construct a button according to set out text defaults
     */
    public Button(
      final Alignment alignment,
      final int enabledColor,
      final int hoverColor,
      final int disabledColor,
      final boolean hasShadow,
      final boolean shouldWrap)
    {
        super(alignment, enabledColor, hoverColor, disabledColor, hasShadow, shouldWrap);
        this.cursor = Cursor.HAND;
    }

    /**
     * Set the button handler for this button.
     *
     * @param h The new handler.
     */
    public void setHandler(final ButtonHandler h)
    {
        handler = h;
    }

    /**
     * Play click sound and find the proper handler.
     *
     * @param mx mouse X coordinate, relative to Pane's top-left
     * @param my mouse Y coordinate, relative to Pane's top-left
     */
    @Override
    public boolean handleClick(final double mx, final double my)
    {
        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f));

        ButtonHandler delegatedHandler = handler;

        if (delegatedHandler == null)
        {
            // If we do not have a designated handler, find the closest ancestor that is a Handler
            for (Pane p = parent; p != null; p = p.getParent())
            {
                if (p instanceof ButtonHandler)
                {
                    delegatedHandler = (ButtonHandler) p;
                    break;
                }
            }
        }

        if (delegatedHandler != null)
        {
            delegatedHandler.onButtonClicked(this);
        }
        return true;
    }

    /**
     * Selects and constructs a Button type based on its parameters
     * @param params the patameters
     * @return a freshly constructed Button
     * @deprecated use {@link ButtonImage}
     */
    @Deprecated(forRemoval = true, since = "1.20.2")
    public static Button construct(PaneParams params)
    {
        return new ButtonImage(params);
    }
}
