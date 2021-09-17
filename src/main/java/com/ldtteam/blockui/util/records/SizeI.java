package com.ldtteam.blockui.util.records;

public record SizeI(int width, int height)
{
    public MutableSizeI toMutable()
    {
        return new MutableSizeI(width, height);
    }

    public static class MutableSizeI
    {
        public int width;
        public int height;

        public MutableSizeI(final int width, final int height)
        {
            this.width = width;
            this.height = height;
        }

        public MutableSizeI()
        {
            this(0, 0);
        }
        
        public int width()
        {
            return width;
        }

        public int height()
        {
            return height;
        }

        public SizeI toImmutable()
        {
            return new SizeI(width, height);
        }
    }
}
