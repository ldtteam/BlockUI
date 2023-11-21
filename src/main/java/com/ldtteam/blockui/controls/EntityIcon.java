package com.ldtteam.blockui.controls;

import com.ldtteam.blockui.BOGuiGraphics;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneParams;
import com.ldtteam.blockui.controls.AbstractTextBuilder.AutomaticTooltipBuilder;
import com.ldtteam.blockui.controls.Tooltip.AutomaticTooltip;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Control to render an entity as an icon
 */
public class EntityIcon extends Pane
{
    @Nullable
    private Entity entity;
    private int count = 1;
    private float yaw = 30;
    private float pitch = -10;
    private float headyaw = 0;

    public EntityIcon()
    {
        super();
    }

    public EntityIcon(@NotNull PaneParams params)
    {
        super(params);

        final String entityName = params.getString("entity");
        if (entityName != null)
        {
            setEntity(new ResourceLocation(entityName));
        }

        this.count = params.getInteger("count", this.count);
        this.yaw = params.getFloat("yaw", this.yaw);
        this.pitch = params.getFloat("pitch", this.pitch);
        this.headyaw = params.getFloat("head", this.headyaw);
    }

    public void setEntity(@NotNull ResourceLocation entityId)
    {
        final EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityId);
        if (entityType != null)
        {
            setEntity(entityType);
        }
        else
        {
            resetEntity();
        }
    }

    public void setEntity(@NotNull EntityType<?> type)
    {
        final Entity entity = type.create(mc.level);

        if (entity != null)
        {
            setEntity(entity);
        }
        else
        {
            resetEntity();
        }
    }

    public void setEntity(@NotNull Entity entity)
    {
        this.entity = entity;
        if (onHover instanceof final AutomaticTooltip tooltip)
        {
            tooltip.setText(this.entity.getDisplayName());
        }
    }

    public void resetEntity()
    {
        this.entity = null;
        if (onHover instanceof final AutomaticTooltip tooltip)
        {
            tooltip.clearText();
        }
    }

    public void setCount(final int count)
    {
        this.count = count;
    }

    public void setYaw(final float yaw)
    {
        this.yaw = yaw;
    }

    public void setPitch(final float pitch)
    {
        this.pitch = pitch;
    }

    @Override
    public void drawSelf(final BOGuiGraphics target, final double mx, final double my)
    {
        final PoseStack ms = target.pose();

        if (this.entity != null)
        {
            ms.pushPose();
            ms.translate(x, y, -50);

            final AABB bb = this.entity.getBoundingBox();
            final float scale = (float) (getHeight() / bb.getYsize() / 1.5);
            final int cx = (getWidth() / 2);
            final int by = getHeight();
            final int offsetY = 2;
            drawEntity(ms, cx, by - offsetY, scale, this.headyaw, this.yaw, this.pitch, this.entity);

            if (this.count != 1)
            {
                String s = String.valueOf(this.count);
                ms.translate(getWidth(), getHeight(), 100.0D);
                ms.scale(0.75F, 0.75F, 0.75F);
                MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                mc.font.drawInBatch(s,
                        (float) (-4 - mc.font.width(s)),
                        (float) (-mc.font.lineHeight),
                        16777215,
                        true,
                        ms.last().pose(),
                        buffer,
                        Font.DisplayMode.NORMAL,
                        0,
                        15728880);
                buffer.endBatch();
            }

            ms.popPose();
        }
    }

    @Override
    public void onUpdate()
    {
        if (this.onHover == null && this.entity != null)
        {
            new AutomaticTooltipBuilder().hoverPane(this).build().setText(this.entity.getDisplayName());
        }
    }
}
