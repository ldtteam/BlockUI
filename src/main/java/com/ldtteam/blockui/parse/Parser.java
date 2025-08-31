package com.ldtteam.blockui.parse;

import com.ldtteam.blockui.attribute.ColorDirection;
import com.ldtteam.blockui.attribute.RelativeAlignment;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public interface Parser<T>
{
    public static final Parser<String> SINGLE_WORD = null;
    public static final Parser<List<MutableComponent>> TRANSLATION = null; // if last char == .# then iterate numbers
    public static final Parser<Float> FLOAT = null;
    public static final Parser<Integer> INT = null;
    public static final Parser<Integer> COLOR = null;
    public static final Parser<Boolean> BOOL = null;
    public static final Parser<RelativeAlignment> ALIGNMENT = null;
    public static final Parser<ColorDirection> COLOR_DIRECTION = null;
    public static final Parser<ResourceLocation> RES_LOC = null;
    public static final Parser<ItemStack> ITEM_STACK = null;

    ParsingResult parse(String data);

    public interface ParsingResult
    {
        boolean hasErrored();

        Runnable getApplier();
    }

    public record ParsingError(String message, ErrorType type, @Nullable Throwable expection)
    {

    }

    public enum ErrorType
    {
        PARSE,
        VALIDATE_RANGE,
        VALIDATE_UI;
    }
}
