package com.ldtteam.blockui.util;

public record Vec2i(int x, int y)
{
    public static final Vec2i EMPTY = new Vec2i(Integer.MIN_VALUE, Integer.MIN_VALUE);
}
