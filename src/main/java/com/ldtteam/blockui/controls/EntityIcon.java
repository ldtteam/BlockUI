package com.ldtteam.blockui.controls;

import com.ldtteam.blockui.BOGuiGraphics;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneParams;
import com.ldtteam.blockui.controls.AbstractTextBuilder.AutomaticTooltipBuilder;
import com.ldtteam.blockui.controls.Tooltip.AutomaticTooltip;
import com.ldtteam.common.util.CompoundTagToClassReflection;
import com.mojang.math.Axis;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.BiConsumer;

/**
 * Control to render an entity as an icon
 */
public class EntityIcon<STATE extends EntityIcon.EntityIconState> extends Pane
{
    @Nullable
    protected STATE entityState;
    protected Transformation transformation = new Transformation();
    protected int count = 1;

    private Component tooltipCachedComponent = null;

    public EntityIcon()
    {
        super();
    }

    @SuppressWarnings("unchecked")
    public EntityIcon(final PaneParams params)
    {
        super(params);

        this.count = params.getInteger("count", this.count);

        final Identifier entityName = params.getResource("entity");
        if (entityName == null)
        {
            return;
        }

        EntityRenderState ers = new EntityRenderState();
        ers.entityType = BuiltInRegistries.ENTITY_TYPE.get(entityName).get().value();

        if (ers.entityType == EntityType.MANNEQUIN || ers.entityType == EntityType.PLAYER)
        {
            requireNonNull(null, "Cannot load avatar entityType");
            ers = new AvatarRenderState();
        }
        else
        {
            // TODO: this doesn't allow player skins
            ers = mc.getEntityRenderDispatcher().getRenderer(ers).createRenderState();
        }

        final CompoundTag ersDataRaw = params.getCompoundTag("renderState");
        if (ersDataRaw != null)
        {
            CompoundTagToClassReflection.compoundToClassFields(ersDataRaw, ers, "Error parsing renderState (" + getXmlRelatedId() + ")");
        }
        setEntityState((STATE) new StaticState<EntityRenderState>(ers));

        final CompoundTag transformationRaw = params.getCompoundTag("transformation");
        if (transformationRaw != null)
        {
            transformation.overrideCameraAngle = new Quaternionf();
            CompoundTagToClassReflection.compoundToClassFields(transformationRaw, transformation, "Error parsing ransformation (" + getXmlRelatedId() + ")");
            return;
        }

        // compute some meaningful default

        // TODO: port 26.1 mirality to verify compat with 1.21 code
        // missing auto scaling - check if vanilla has something to calc that from ERS
        final float yaw = params.getFloat("yaw", 30);
        final float pitch = params.getFloat("pitch", -10);
        final float headyaw = params.getFloat("head", 0);

        transformation.rotation = Axis.ZP.rotationDegrees(180.0F);
        transformation.rotation.mul(Axis.XP.rotationDegrees(pitch));

        if (ers instanceof final LivingEntityRenderState lers)
        {
            lers.yRot = 180.0f + headyaw;
            lers.xRot = -pitch;
            lers.bodyRot = 180.0f + yaw;
            // yHeadRot is not used for lers?
        }

        transformation.overrideCameraAngle = Axis.XP.rotationDegrees(pitch).conjugate();

        // poseStack.scale((float) scale, (float) scale, (float) scale);
        // final Quaternionf pitchRotation = Axis.XP.rotationDegrees(pitch);
        // poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        // poseStack.mulPose(pitchRotation);
        // entity.setYRot(180.0F + (float) headYaw);
        // entity.setXRot(-pitch);
        // if (livingEntity != null)
        // {
        //     livingEntity.yBodyRot = 180.0F + yaw;
        //     livingEntity.yHeadRot = entity.getYRot();
        //     livingEntity.yHeadRotO = entity.getYRot();
        // }
        // pitchRotation.conjugate();
        // dispatcher.overrideCameraOrientation(pitchRotation);
    }

    public void setEntityState(@Nullable final STATE entityState)
    {
        this.entityState = entityState;
        if (entityState == null && this.onHover instanceof AutomaticTooltip)
        {
            setHoverPane(null);
            tooltipCachedComponent = null;
        }
    }

    public STATE getEntityState()
    {
        return entityState;
    }

    public void setCount(final int count)
    {
        this.count = count;
    }

    public int getCount()
    {
        return count;
    }

    public void setTransformation(final Transformation transformation)
    {
        this.transformation = transformation;
    }

    public Transformation getTransformation()
    {
        return transformation;
    }

    @Override
    public void drawSelf(final BOGuiGraphics target, final double mx, final double my)
    {
        final Matrix3x2fStack ms = target.pose();

        if (this.entityState != null)
        {
            ms.pushMatrix();
            ms.translate(x, y);

            final EntityRenderState ers = entityState.entityRenderState(mc.getEntityRenderDispatcher());

            if (ers.nameTag != null)
            {
                tooltipCachedComponent = ers.nameTag;
                ers.nameTag = null;
            }

            final ScreenRectangle renderBox = target.calcTransformedPaneBounds(this);

            target.entity(ers,
                transformation.scale,
                transformation.translation,
                transformation.rotation,
                transformation.overrideCameraAngle,
                renderBox.left(),
                renderBox.top(),
                renderBox.right(),
                renderBox.bottom());

            if (this.count != 1)
            {
                final String amount = String.valueOf(count);
                ms.translate(getWidth(), getHeight());
                target.text(mc.font, amount, -4 - mc.font.width(amount), -mc.font.lineHeight, -1, true);
            }

            ms.popMatrix();
        }
    }

    @Override
    public void onUpdate()
    {
        if (this.onHover == null && this.tooltipCachedComponent != null)
        {
            new AutomaticTooltipBuilder().hoverPane(this).append(tooltipCachedComponent).build();
        }
    }

    public interface EntityIconState
    {
        EntityRenderState entityRenderState(EntityRenderDispatcher entityRenderDispatcher);
    }

    public record StaticState<ERS extends EntityRenderState>(ERS entityRenderState) implements EntityIconState
    {
        @Override
        public EntityRenderState entityRenderState(final EntityRenderDispatcher entityRenderDispatcher)
        {
            return entityRenderState;
        }
    }

    public static class DynamicState<E extends Entity, ERS extends EntityRenderState, ER extends EntityRenderer<E, ERS>> implements EntityIconState
    {
        public static final BiConsumer<? extends Entity, ? extends EntityRenderState> COMMON_RENDER_STATE_POSTPROCESSING = (entity, ers) -> {
            ers.shadowPieces.clear();
            ers.nameTag = entity.getCustomName();
        };

        private final E entity;
        private ERS entityRenderState;
        private final BiConsumer<E, ERS> entityRenderStateAdjuster;

        public DynamicState(final E entity, @Nullable final BiConsumer<E, ERS> entityRenderStateAdjuster)
        {
            this.entity = entity;
            this.entityRenderStateAdjuster = entityRenderStateAdjuster == null ? this::commonRenderStateAdjustments : entityRenderStateAdjuster;
        }

        public void commonRenderStateAdjustments(final E entity, final ERS entityRenderState)
        {
            // vanilla
            entityRenderState.shadowPieces.clear();
            entityRenderState.outlineColor = 0;

            // ours
            entityRenderState.nameTag = entity.getCustomName();
        }

        @SuppressWarnings("unchecked")
        @Override
        public EntityRenderState entityRenderState(final EntityRenderDispatcher entityRenderDispatcher)
        {
            final ER renderer = (ER) entityRenderDispatcher.getRenderer(entity);
            entityRenderState = renderer.createRenderState(entity, 1.0f);
            entityRenderStateAdjuster.accept(entity, entityRenderState);
            return entityRenderState;
        }
    }

    public class Transformation
    {
        public float scale = 10.0f;
        public Vector3f translation = new Vector3f();
        public Quaternionf rotation = new Quaternionf();
        @Nullable
        public Quaternionf overrideCameraAngle = null;
    }
}
