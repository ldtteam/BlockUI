package com.ldtteam.common.language;

import com.ldtteam.blockui.mod.BlockUI;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import org.apache.commons.lang3.text.ExtendedMessageFormat;
import java.text.MessageFormat;
import java.util.Optional;

// TODO: vanilla based class, check every vanilla update
@SuppressWarnings("deprecation")
public class OurTranslatableComponent extends TranslatableComponent
{
    private Language lastLanguage = null;
    private String translatedKey = null;
    private MessageFormat messageFormat = null;

    public OurTranslatableComponent(final String key)
    {
        super(key);
    }

    public OurTranslatableComponent(final String key, final Object... args)
    {
        super(key, args);
    }

    @Override
    public String getContents()
    {
        final Language currentLanguage = Language.getInstance();
        if (lastLanguage != currentLanguage)
        {
            lastLanguage = currentLanguage;
            translatedKey = lastLanguage.getLanguageData().get(getKey());
            messageFormat = null;
        }

        if (translatedKey == null)
        {
            return getKey();
        }

        if (messageFormat == null)
        {
            messageFormat = new ExtendedMessageFormat(translatedKey, LangUtils.FORMAT_FACTORIES);
        }

        try
        {
            return messageFormat.format(getArgs());
        }
        catch (final ClassCastException e)
        {
            throw new RuntimeException("Cannot format argument of: " + getKey(), e);
        }
    }

    @Override
    public <T> Optional<T> visitSelf(final StyledContentConsumer<T> p_130682_, Style p_130683_)
    {
        // we need to skip super in TranslatableComponent
        return p_130682_.accept(p_130683_, this.getContents());
    }

    @Override
    public <T> Optional<T> visitSelf(final ContentConsumer<T> p_130681_)
    {
        // we need to skip super in TranslatableComponent
        return p_130681_.accept(this.getContents());
    }

    @Override
    public MutableComponent resolve(final CommandSourceStack p_131310_, final Entity p_131311_, final int p_131312_)
        throws CommandSyntaxException
    {
        // just cast to our class
        final TranslatableComponent result = (TranslatableComponent) super.resolve(p_131310_, p_131311_, p_131312_);
        return new OurTranslatableComponent(result.getKey(), result.getArgs());
    }

    @Override
    public OurTranslatableComponent plainCopy()
    {
        return new OurTranslatableComponent(getKey(), getArgs());
    }

    @Override
    public String toString()
    {
        return BlockUI.MOD_ID + super.toString();
    }

    @Override
    public boolean equals(Object obj)
    {
        return this == obj || (obj instanceof OurTranslatableComponent && super.equals(obj));
    }
}
