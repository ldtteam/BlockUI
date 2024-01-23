package com.ldtteam.blockui.controls;

import com.ldtteam.blockui.PaneParams;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;

/**
 * Checkbox used for toggling a checkmark on and off.
 */
public class CheckBox extends ButtonImage
{
    /**
     * The image for the checkmark to render over the button.
     */
    protected ResourceLocation checkmarkImage;

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
     * @param loc ResourceLocation for the checkmark.
     */
    public void setCheckmarkImage(final ResourceLocation loc)
    {
        this.checkmarkImage = loc;
    }

    @Override
    public void postDrawBackground(
      final PoseStack ms,
      final ResourceLocation image,
      final int x,
      final int y,
      final int width,
      final int height,
      final int u,
      final int v,
      final int w,
      final int h,
      final int mapWidth,
      final int mapHeight)
    {
        if (checked)
        {
            blit(ms, checkmarkImage, x, y, width, height, u, v, w, h, mapWidth, mapHeight);
        }
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
