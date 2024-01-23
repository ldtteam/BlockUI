package com.ldtteam.blockui.util.color;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IColourTest
{
    @Test
    public void testAll()
    {
        IColour col = new ColourRGBA(0x04030201);

        assertEquals(col.alphaF(), 1.0f / 255, 0.0f);

        assertEquals(col.red(), 4);
        assertEquals(col.green(), 3);
        assertEquals(col.blue(), 2);
        assertEquals(col.alpha(), 1);
        assertEquals(col.argb(), 0x01040302);
        assertEquals(col.rgba(), 0x04030201);

        col = col.asARGB();

        assertEquals(col.red(), 4);
        assertEquals(col.green(), 3);
        assertEquals(col.blue(), 2);
        assertEquals(col.alpha(), 1);
        assertEquals(col.argb(), 0x01040302);
        assertEquals(col.rgba(), 0x04030201);

        col = col.asQuartet();

        assertEquals(col.red(), 4);
        assertEquals(col.green(), 3);
        assertEquals(col.blue(), 2);
        assertEquals(col.alpha(), 1);
        assertEquals(col.argb(), 0x01040302);
        assertEquals(col.rgba(), 0x04030201);
    }
}
