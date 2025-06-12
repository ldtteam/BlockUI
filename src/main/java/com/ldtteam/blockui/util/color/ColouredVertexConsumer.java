package com.ldtteam.blockui.util.color;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;

/**
 * Wrapper for having default color for vertex consumer
 */
public class ColouredVertexConsumer implements VertexConsumer
{
    protected final VertexConsumer parent;
    public IColour defaultColor = null;

    public ColouredVertexConsumer(final VertexConsumer parent)
    {
        this.parent = parent;
    }

    @Override
    public ColouredVertexConsumer addVertex(final float x, final float y, final float z)
    {
        parent.addVertex(x, y, z);
        return this;
    }

    @Override
    public ColouredVertexConsumer setColor(final int r, final int g, final int b, final int a)
    {
        parent.setColor(r, g, b, a);
        return this;
    }

    /**
     * Applies previously set defaultColor, will shamelessly NPE if you forgot to set it
     */
    public ColouredVertexConsumer setDefaultColor()
    {
        defaultColor.writeIntoBuffer(this);
        return this;
    }

    @Override
    public ColouredVertexConsumer setUv(final float u, final float v)
    {
        parent.setUv(u, v);
        return this;
    }

    @Override
    public ColouredVertexConsumer setUv1(final int u, final int v)
    {
        parent.setUv1(u, v);
        return this;
    }

    @Override
    public ColouredVertexConsumer setUv2(final int u, final int v)
    {
        parent.setUv2(u, v);
        return this;
    }

    @Override
    public ColouredVertexConsumer setNormal(final float x, final float y, final float z)
    {
        parent.setNormal(x, y, z);
        return this;
    }

    @Override
    public ColouredVertexConsumer misc(final VertexFormatElement element, final int... values)
    {
        parent.misc(element, values);
        return this;
    }
}
