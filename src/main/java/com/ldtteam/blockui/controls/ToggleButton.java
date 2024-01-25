package com.ldtteam.blockui.controls;

import com.ldtteam.blockui.PaneParams;
import com.ldtteam.blockui.Parsers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.MutableComponent;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A Button pane for conveniently cycling through different states on click
 * with a shorthand to define different options quickly from the parameters.
 * 
 * TODO: rework in port
 */
public class ToggleButton extends ButtonImage
{
    private static final Pattern SHORT_TRANSLATION = Pattern.compile("(\\$[({]\\S+)\\.\\S+([})])\\|(\\$\\.[^$|\\s]+)");

    protected List<String> rawStates;
    protected List<MutableComponent> states;
    protected int active = 0;

    public ToggleButton(final PaneParams params)
    {
        super(params);
        setStateList(params.getString("options", ""));
    }

    /**
     * Creates a new toggleable vanilla button
     * @param options the available states as raw text strings
     */
    @Deprecated(forRemoval = true, since = "1.20.1")
    public ToggleButton(String... options)
    {
        setStateList(String.join("|", options));
    }

    /**
     * Creates a new custom image button
     * @param image the image to set as the button's background
     * @param options the available states as raw text strings
     */
    @Deprecated(forRemoval = true, since = "1.20.1")
    public ToggleButton(ResourceLocation image, String... options)
    {
        setImage(image, false);
        setStateList(String.join("|", options));
    }

    /**
     * Initializes the states and raw states from the given raw text
     * @param options the available options, delimited with a pipe (|) $. will copy the previous translation option
     */
    protected void setStateList(String options)
    {
        Matcher m = SHORT_TRANSLATION.matcher(options);
        while (m.find())
        {
            options = options.replace(m.group(3), m.group(1) + m.group(3).substring(1) + m.group(2));
            m = SHORT_TRANSLATION.matcher(options);
        }

        rawStates = Arrays.asList(options.split("\\s*\\|(?!\\|)\\s*"));
        states = rawStates.stream().map(option -> Parsers.TEXT.apply(option)).collect(Collectors.toList());

        if (!states.isEmpty())
        {
            setText(states.get(active));
        }
        else
        {
            clearText();
        }
    }

    /**
     * @param raw true if the result should not be localized
     * @return the list of states as strings
     */
    public List<String> getStateStrings(boolean raw)
    {
        return raw ? rawStates : states.stream().map(MutableComponent::getString).collect(Collectors.toList());
    }

    public List<MutableComponent> getStates()
    {
        return states;
    }

    /**
     * Reports if the given state pattern is currently the active one
     *
     * @param state the state or raw state to check against
     * @return true if the state is active
     */
    public boolean isActiveState(String state)
    {
        return states.get(active).getString().equals(state)
                 || rawStates.get(active).equals(state)
                 // take off the bracket for a translation string
                 || rawStates.get(active).substring(0, rawStates.get(active).length() - 1).endsWith(state);
    }

    /**
     * Attempts to set the active state displayed on the button via a raw text string
     *
     * @param state the state to set, if it exists as an option
     * @return whether the active state was changed
     */
    public boolean setActiveState(String state)
    {
        int index = -1;

        for (int i = 0; i < rawStates.size(); i++)
        {
            String s = rawStates.get(i);
            if (s.equals(state) || s.substring(0, s.length() - 1).endsWith(state))
            {
                index = i;
                break;
            }
        }

        if (index >= 0)
        {
            active = index;
            setText(states.get(active));
            return true;
        }
        else
        {
            clearText();
            return false;
        }
    }

    /**
     * Change the underlying button pane
     * @param button the new button pane to render
     */
    @Deprecated(forRemoval = true, since = "1.20.1")
    public void setButton(Button button)
    {
        // noop
    }

    @Deprecated(forRemoval = true, since = "1.20.1")
    public Button getButton()
    {
        return this;
    }

    @Override
    public boolean handleClick(final double mx, final double my)
    {
        if (!states.isEmpty())
        {
            active = (active + 1) % states.size();
            setText(states.get(active));
        }

        return super.handleClick(mx, my);
    }
}
