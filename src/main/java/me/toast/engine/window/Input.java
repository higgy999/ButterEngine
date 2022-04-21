package me.toast.engine.window;

import me.toast.engine.Mod;
import org.lwjgl.glfw.*;

import static org.lwjgl.opengl.GL11.glViewport;

public class Input {

    private final boolean[] keys = new boolean[GLFW.GLFW_KEY_LAST];
    private final boolean[] buttons = new boolean[GLFW.GLFW_MOUSE_BUTTON_LAST];

    //TODO: This should be private with getters and setters for GLFW calls to actually change things
    public float mouseX, mouseY;
    public float scrollX, scrollY;

    public GLFWKeyCallback KeyboardCallback;
    public GLFWCharCallback CharCallback;
    public GLFWCursorPosCallback MouseMoveCallback;
    public GLFWMouseButtonCallback MouseButtonsCallback;
    public GLFWScrollCallback MouseScrollCallback;
    public GLFWWindowSizeCallback WindowResizeCallback;
    public GLFWWindowFocusCallback WindowFocusCallback;

    public Input() {
        KeyboardCallback = new GLFWKeyCallback() {
            public void invoke(long window, int key, int scancode, int action, int mods) {
                keys[key] = (action != GLFW.GLFW_RELEASE);
            }
        };

        MouseMoveCallback = new GLFWCursorPosCallback() {
            public void invoke(long window, double xpos, double ypos) {
                mouseX = (float) xpos;
                mouseY = (float) ypos;
            }
        };

        MouseButtonsCallback = new GLFWMouseButtonCallback() {
            public void invoke(long window, int button, int action, int mods) {
                buttons[button] = (action != GLFW.GLFW_RELEASE);
            }
        };

        MouseScrollCallback = new GLFWScrollCallback() {
            public void invoke(long window, double offsetx, double offsety) {
                scrollX += (float) offsetx;
                scrollY += (float) offsety;
            }
        };

        WindowResizeCallback = new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                if(Mod.Window.Capabilities != null) {
                    Mod.Window.Width = width;
                    Mod.Window.Height = height;
                    glViewport(0, 0, width, height);
                    Mod.Cam.setProjection((float) Math.toRadians(45f), (float) width/ (float) height, 0.1f, 1000f);
                    if (Mod.Ultralight != null) {
                        Mod.Ultralight.webController.resize(width, height);
                        //TODO: Fix Ultralight Scaling
                    }
                }
            }
        };

        CharCallback = new GLFWCharCallback() {
            @Override
            public void invoke(long window, int codepoint) {

            }
        };

        WindowFocusCallback = new GLFWWindowFocusCallback() {
            @Override
            public void invoke(long window, boolean focused) {

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
        GLFW.glfwSetInputMode(Mod.Window.ID, GLFW.GLFW_CURSOR, lock ? GLFW.GLFW_CURSOR_DISABLED : GLFW.GLFW_CURSOR_NORMAL);
    }

    public boolean GetMouseState() {
        return GLFW.glfwGetInputMode(Mod.Window.ID, GLFW.GLFW_CURSOR) != GLFW.GLFW_CURSOR_DISABLED;
    }
}
