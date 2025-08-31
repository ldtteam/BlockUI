package com.ldtteam.blockui.attribute;

public enum ColorDirection
{
    VERTICAL,
    HORIZONTAL,
    PRIMARY_DIAGONAL,
    SECONDARY_DIAGONAL;

    public boolean isDiagonal()
    {
        return this == PRIMARY_DIAGONAL || this == SECONDARY_DIAGONAL;
    }
}
