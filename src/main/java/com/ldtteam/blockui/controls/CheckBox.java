package com.ldtteam.blockui.controls;

import com.ldtteam.blockui.PaneParams;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

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
     * Callback whenever the checked state changes.
     */
    private Consumer<Boolean> onCheckedChange;

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
        super.setHandler(this::onButtonClick);
    }

    /**
     * Constructor called by the xml loader.
     *
     * @param params PaneParams provided in the xml.
     */
    public CheckBox(final PaneParams params)
    {
        super(params);

        checkmarkImage = params.getResource("checkmark", img -> {});
        super.setHandler(this::onButtonClick);
    }

    /**
     * Internal handler for the button click.
     */
    private void onButtonClick(final Button button)
    {
        checked = !checked;
        final Consumer<Boolean> handler = onCheckedChange;
        if (handler != null)
        {
            handler.accept(checked);
        }
    }

    /**
     * Do not call this method. Checkboxes may not have a different button click implementation.
     */
    @Override
    public void setHandler(final ButtonHandler h)
    {
        // No-op, checkbox handler is explicit and may not be changed
    }

    /**
     * Set a callback handler for whenever the checked state changes.
     * Note that this callback is not fired when a manual invocation to {@link CheckBox#setChecked(boolean)} is called, only when the button itself is clicked.
     *
     * @param onCheckedChange the callback handler.
     */
    public void setOnCheckedChange(final Consumer<Boolean> onCheckedChange)
    {
        this.onCheckedChange = onCheckedChange;
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
    public void postDrawButton(
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
