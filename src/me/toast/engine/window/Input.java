package me.toast.engine.window;

import me.toast.engine.Mod;
import org.lwjgl.glfw.*;

import static org.lwjgl.opengl.GL11.glViewport;

public class Input {

    private final boolean[] keys = new boolean[GLFW.GLFW_KEY_LAST];
    private final boolean[] buttons = new boolean[GLFW.GLFW_MOUSE_BUTTON_LAST];

    public double mouseX, mouseY;
    public double scrollX, scrollY;

    public GLFWKeyCallback KeyboardCallback;
    public GLFWCursorPosCallback MouseMoveCallback;
    public GLFWMouseButtonCallback MouseButtonsCallback;
    public GLFWScrollCallback MouseScrollCallback;
    public GLFWWindowSizeCallback WindowResizeCallback;

    public Input() {
        KeyboardCallback = new GLFWKeyCallback() {
            public void invoke(long window, int key, int scancode, int action, int mods) {
                keys[key] = (action != GLFW.GLFW_RELEASE);
            }
        };

        MouseMoveCallback = new GLFWCursorPosCallback() {
            public void invoke(long window, double xpos, double ypos) {
                mouseX = xpos;
                mouseY = ypos;
            }
        };

        MouseButtonsCallback = new GLFWMouseButtonCallback() {
            public void invoke(long window, int button, int action, int mods) {
                buttons[button] = (action != GLFW.GLFW_RELEASE);
            }
        };

        MouseScrollCallback = new GLFWScrollCallback() {
            public void invoke(long window, double offsetx, double offsety) {
                scrollX += offsetx;
                scrollY += offsety;
            }
        };

        WindowResizeCallback = new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                if(Mod.LOADED_MOD.Window.Capabilities != null) {
                    Mod.LOADED_MOD.Window.Width = width;
                    Mod.LOADED_MOD.Window.Height = height;
                    glViewport(0, 0, width, height);
                    Mod.LOADED_MOD.Camera
                            .setProjection((float) Math.toRadians(45f), (float) width/ (float) height, 0.1f, 1000f);
                }
            }
        };
    }

    public boolean isKeyDown(int key) {
        return keys[key];
    }

    public boolean isButtonDown(int button) {
        return buttons[button];
    }

    public void SetMouseState(boolean lock) {
        GLFW.glfwSetInputMode(Mod.LOADED_MOD.Window.ID, GLFW.GLFW_CURSOR, lock ? GLFW.GLFW_CURSOR_DISABLED : GLFW.GLFW_CURSOR_NORMAL);
    }

    public boolean GetMouseState() {
        return GLFW.glfwGetInputMode(Mod.LOADED_MOD.Window.ID, GLFW.GLFW_CURSOR) != GLFW.GLFW_CURSOR_DISABLED;
    }
}
