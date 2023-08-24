package com.ldtteam.blockui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

/**
 * Helpful util methods when using Matrixes
 */
public class MatrixUtils
{
    /**
     * Private constructor to hide the public one.
     */
    private MatrixUtils()
    {
    }

    /**
     * @return last matrix Z translate value
     */
    public static float getLastMatrixTranslateZ(final PoseStack matrixStack)
    {
        return getMatrixTranslateZ(matrixStack.last().pose());
    }

    /**
     * @return matrix Z translate value
     */
    public static float getMatrixTranslateZ(final Matrix4f matrix)
    {
        return matrix.m23;
    }

    public static void pushShaderMVstack(final PoseStack pushWith)
    {
        final PoseStack ps = RenderSystem.getModelViewStack();
        ps.pushPose();
        ps.last().pose().multiply(pushWith.last().pose());
        ps.last().normal().mul(pushWith.last().normal());
        RenderSystem.applyModelViewMatrix();
    }

    public static void popShaderMVstack()
    {
        RenderSystem.getModelViewStack().popPose();
        RenderSystem.applyModelViewMatrix();
    }
}
