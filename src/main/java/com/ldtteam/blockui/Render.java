package com.ldtteam.blockui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Render utility functions.
 */
public final class Render
{
    private Render()
    {
        // Hide default constructor
    }

    /**
     * Render an entity on a GUI.
     * @param poseStack matrix
     * @param x horizontal center position
     * @param y vertical bottom position
     * @param scale scaling factor
     * @param headYaw adjusts look rotation
     * @param yaw adjusts body rotation
     * @param pitch adjusts look rotation
     * @param entity the entity to render
     */
    public static void drawEntity(final PoseStack poseStack, final int x, final int y, final double scale,
                                  final float headYaw, final float yaw, final float pitch, final Entity entity)
    {
        final LivingEntity livingEntity = (entity instanceof LivingEntity) ? (LivingEntity) entity : null;
        final Minecraft mc = Minecraft.getInstance();
        if (entity.level == null) entity.level = mc.level;
        poseStack.pushPose();
        poseStack.translate((float) x, (float) y, 1050.0F);
        poseStack.scale(1.0F, 1.0F, -1.0F);
        poseStack.translate(0.0D, 0.0D, 1000.0D);
        poseStack.scale((float) scale, (float) scale, (float) scale);
        final Quaternion pitchRotation = Vector3f.XP.rotationDegrees(pitch);
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
        poseStack.mulPose(pitchRotation);
        final float oldYaw = entity.getYRot();
        final float oldPitch = entity.getXRot();
        final float oldYawOffset = livingEntity == null ? 0F : livingEntity.yBodyRot;
        final float oldPrevYawHead = livingEntity == null ? 0F : livingEntity.yHeadRotO;
        final float oldYawHead = livingEntity == null ? 0F : livingEntity.yHeadRot;
        entity.setYRot(180.0F + (float) headYaw);
        entity.setXRot(-pitch);
        if (livingEntity != null)
        {
            livingEntity.yBodyRot = 180.0F + yaw;
            livingEntity.yHeadRot = entity.getYRot();
            livingEntity.yHeadRotO = entity.getYRot();
        }
        final EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        pitchRotation.conj();
        dispatcher.overrideCameraOrientation(pitchRotation);
        dispatcher.setRenderShadow(false);
        final MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> dispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, poseStack, buffers, 0x00F000F0));
        buffers.endBatch();
        dispatcher.setRenderShadow(true);
        entity.setYRot(oldYaw);
        entity.setXRot(oldPitch);
        if (livingEntity != null)
        {
            livingEntity.yBodyRot = oldYawOffset;
            livingEntity.yHeadRotO = oldPrevYawHead;
            livingEntity.yHeadRot = oldYawHead;
        }
        poseStack.popPose();
    }
}
