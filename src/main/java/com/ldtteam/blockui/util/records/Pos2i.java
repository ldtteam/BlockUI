package com.ldtteam.blockui.util.records;

public interface Pos2i
{
    int x();

    int y();

    public static class MutablePos2i implements Pos2i
    {
        public int x;
        public int y;

        public MutablePos2i(final int x, final int y)
        {
            this.x = x;
            this.y = y;
        }

        public MutablePos2i()
        {
            this(0, 0);
        }

        public int x()
        {
            return x;
        }

        public int y()
        {
            return y;
        }

        public ImmutablePos2i toImmutable()
        {
            return new ImmutablePos2i(x, y);
        }
    }

    public static record ImmutablePos2i(int x, int y) implements Pos2i
    {
        public MutablePos2i toMutable()
        {
            return new MutablePos2i(x, y);
        }
    }
}
