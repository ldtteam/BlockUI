package com.ldtteam.blockui.util;

import org.lwjgl.glfw.GLFW;

public record MouseButton(int glfwValue)
{
    public static final MouseButton LEFT = new MouseButton(GLFW.GLFW_MOUSE_BUTTON_LEFT);
    public static final MouseButton MIDDLE = new MouseButton(GLFW.GLFW_MOUSE_BUTTON_MIDDLE);
    public static final MouseButton RIGHT = new MouseButton(GLFW.GLFW_MOUSE_BUTTON_RIGHT);

    public static MouseButton byValue(final int glfwValue)
    {
        return switch (glfwValue)
        {
            case GLFW.GLFW_MOUSE_BUTTON_LEFT -> LEFT;
            case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> MIDDLE;
            case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> RIGHT;
            default -> new MouseButton(glfwValue);
        };
    }
}
