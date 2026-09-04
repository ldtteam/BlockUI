package com.ldtteam.blockui.controls;

import com.ldtteam.blockui.BOGuiGraphics;
import com.ldtteam.blockui.PaneParams;
import net.minecraft.resources.Identifier;

/**
 * Checkbox used for toggling a checkmark on and off.
 */
public class CheckBox extends ButtonImage
{
    /**
     * The image for the checkmark to render over the button.
     */
    protected Identifier checkmarkImage;
    protected ResolvedBlit resolvedCheckmarkImage;

    /**
     * Whether the button is checked or not.
     */
    private boolean checked = false;

    /**
     * Default constructor. Makes a small square button.
     */
    public CheckBox()
    {
        super();
    }

    /**
     * Constructor called by the xml loader.
     *
     * @param params PaneParams provided in the xml.
     */
    public CheckBox(final PaneParams params)
    {
        super(params);

        checkmarkImage = params.getResource("checkmark");
    }

    @Override
    public boolean handleClick(final double mx, final double my)
    {
        checked = !checked;
        super.handleClick(mx, my);
        return true;
    }

    /**
     * Set the checkmark image.
     *
     * @param loc Identifier for the checkmark.
     */
    public void setCheckmarkImage(final Identifier loc)
    {
        this.checkmarkImage = loc;
        requireNonNull(checkmarkImage, "Missing checkmark texture");
    }

    public Identifier getCheckmarkImage()
    {
        return checkmarkImage;
    }

    @Override
    public void postDrawBackground(final BOGuiGraphics target, final double mx, final double my)
    {
        requireNonNull(checkmarkImage, "Missing checkmark texture");

        if (!checked)
        {
            return;
        }

        if (resolvedCheckmarkImage == null)
        {
            resolvedCheckmarkImage = Image.resolveBlit(checkmarkImage);
        }

        resolvedCheckmarkImage.blit(target, x, y, width, height);
    }

    /**
     * Get if the checkbox is currently checked or not.
     *
     * @return true if so.
     */
    public boolean isChecked()
    {
        return checked;
    }

    /**
     * Set whether the checkbox is checked or not.
     *
     * @param checked the checked state.
     */
    public void setChecked(final boolean checked)
    {
        this.checked = checked;
    }
}
