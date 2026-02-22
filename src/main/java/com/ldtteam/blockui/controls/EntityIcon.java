package com.ldtteam.blockui.controls;

import com.ldtteam.blockui.BOGuiGraphics;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneParams;
import com.ldtteam.blockui.controls.AbstractTextBuilder.AutomaticTooltipBuilder;
import com.ldtteam.blockui.controls.Tooltip.AutomaticTooltip;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;

import java.util.Optional;

/**
 * Control to render an entity as an icon
 */
public class EntityIcon extends Pane
{
    @Nullable
    private LivingEntity entity;
    private int          count = 1;
    private float yaw = 30;
    private float pitch = -10;
    private float headyaw = 0;

    public EntityIcon()
    {
        super();
    }

    public EntityIcon(final PaneParams params)
    {
        super(params);

        final Identifier entityName = params.getResource("entity");
        if (entityName != null)
        {
            setEntity(entityName);
        }

        this.count = params.getInteger("count", this.count);
        this.yaw = params.getFloat("yaw", this.yaw);
        this.pitch = params.getFloat("pitch", this.pitch);
        this.headyaw = params.getFloat("head", this.headyaw);
    }

    public void setEntity(@NotNull Identifier entityId)
    {
        final Optional<Holder.Reference<EntityType<?>>> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityId);
        entityType.ifPresentOrElse(e -> setEntity(e.value()), this::resetEntity);
    }

    public void setEntity(@NotNull EntityType<?> type)
    {
        final Entity entity = type.create(mc.level, EntitySpawnReason.LOAD);

        if (entity instanceof LivingEntity)
        {
            setEntity((LivingEntity) entity);
        }
        else
        {
            resetEntity();
        }
    }

    public void setEntity(@NotNull LivingEntity entity)
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
        final Matrix3x2fStack ms = target.guiGraphics().pose();

        if (this.entity != null)
        {
            final AABB bb = this.entity.getBoundingBox();
            final int scale = (int) (getHeight() / bb.getYsize() / 1.5);
            InventoryScreen.renderEntityInInventoryFollowsMouse(target.guiGraphics(), x, y, x+width, x+height, scale, 2.0f, (float) mx, (float) my, entity);
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
