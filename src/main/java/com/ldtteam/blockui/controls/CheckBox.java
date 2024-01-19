package com.ldtteam.blockui.controls;

import com.ldtteam.blockui.PaneParams;
import com.ldtteam.blockui.support.ImageData;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Checkbox used for toggling a checkmark on and off.
 */
public class CheckBox extends ButtonImage
{
    /**
     * The image for the checked regular state.
     */
    @NotNull
    protected ImageData imageCheckedRegular = ImageData.MISSING;

    /**
     * The image for the checked hover state.
     */
    @NotNull
    protected ImageData imageCheckedHover = ImageData.MISSING;

    /**
     * The image for the checked disabled state.
     */
    @NotNull
    protected ImageData imageCheckedDisabled = ImageData.MISSING;

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

        imageCheckedRegular = loadImageInfo(params, "checked", "checked");
        imageCheckedHover = loadImageInfo(params, "checkedhighlight", "checkedhighlight");
        imageCheckedDisabled = loadImageInfo(params, "checkeddisabled", "checkeddisabled");
    }

    /**
     * Set the default image.
     *
     * @param loc     ResourceLocation for the image.
     * @param offsetX image x offset.
     * @param offsetY image y offset.
     * @param w       image width.
     * @param h       image height.
     */
    public void setCheckedImage(final ResourceLocation loc, final int offsetX, final int offsetY, final int w, final int h)
    {
        imageCheckedRegular = new ImageData(loc, offsetX, offsetY, w, h);
    }

    /**
     * Set the default image.
     *
     * @param loc ResourceLocation for the image.
     */
    public void setCheckedImage(final ResourceLocation loc, final boolean keepUv)
    {
        if (keepUv && !imageCheckedRegular.equals(ImageData.MISSING))
        {
            imageCheckedRegular = new ImageData(loc, imageCheckedRegular.offsetX(), imageCheckedRegular.offsetY(), imageCheckedRegular.width(), imageCheckedRegular.height());
        }
        else
        {
            imageCheckedRegular = new ImageData(loc, 0, 0, 0, 0);
        }
    }

    /**
     * Set the hover image.
     *
     * @param loc     ResourceLocation for the image.
     * @param offsetX image x offset.
     * @param offsetY image y offset.
     * @param w       image width.
     * @param h       image height.
     */
    public void setImageCheckedHighlight(final ResourceLocation loc, final int offsetX, final int offsetY, final int w, final int h)
    {
        imageCheckedHover = new ImageData(loc, offsetX, offsetY, w, h);
    }

    /**
     * Set the hover image.
     *
     * @param loc ResourceLocation for the image.
     */
    public void setImageCheckedHighlight(final ResourceLocation loc, final boolean keepUv)
    {
        if (keepUv && !imageCheckedHover.equals(ImageData.MISSING))
        {
            imageCheckedHover = new ImageData(loc, imageCheckedHover.offsetX(), imageCheckedHover.offsetY(), imageCheckedHover.width(), imageCheckedHover.height());
        }
        else
        {
            imageCheckedHover = new ImageData(loc, 0, 0, 0, 0);
        }
    }

    /**
     * Set the disabled image.
     *
     * @param loc     ResourceLocation for the image.
     * @param offsetX image x offset.
     * @param offsetY image y offset.
     * @param w       image width.
     * @param h       image height.
     */
    public void setImageCheckedDisabled(final ResourceLocation loc, final int offsetX, final int offsetY, final int w, final int h)
    {
        imageCheckedDisabled = new ImageData(loc, offsetX, offsetY, w, h);
    }

    /**
     * Set the disabled image.
     *
     * @param loc ResourceLocation for the image.
     */
    public void setImageCheckedDisabled(final ResourceLocation loc, final boolean keepUv)
    {
        if (keepUv && !imageCheckedDisabled.equals(ImageData.MISSING))
        {
            imageCheckedDisabled = new ImageData(loc, imageCheckedDisabled.offsetX(), imageCheckedDisabled.offsetY(), imageCheckedDisabled.width(), imageCheckedDisabled.height());
        }
        else
        {
            imageCheckedDisabled = new ImageData(loc, 0, 0, 0, 0);
        }
    }

    @Override
    public ImageData getImageToDraw()
    {
        if (checked)
        {
            if (!enabled && !imageCheckedDisabled.equals(ImageData.MISSING))
            {
                return imageCheckedDisabled;
            }
            else if (wasCursorInPane && !imageCheckedHover.equals(ImageData.MISSING))
            {
                return imageCheckedHover;
            }
            return imageCheckedRegular;
        }
        return super.getImageToDraw();
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
