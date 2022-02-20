package me.toast.engine.window;

import me.toast.engine.Mod;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    
    public long ID;
    public int Width, Height;
    public Input InputEvents;
    public GLCapabilities Capabilities;

    public Window(int width, int height) {
        this.Width = width; this.Height = height;
        // Set up an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
        
        ID = glfwCreateWindow(Width, Height, Mod.LOADED_MOD.NAME, NULL, NULL);
        if (ID == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        //This logic handles centering the window in the middle of the main monitor
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); //int pointer
            IntBuffer pHeight = stack.mallocInt(1); //int pointer

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(ID, pWidth, pHeight);
            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            // Center the window
            glfwSetWindowPos(ID, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(ID);
        // Enable v-sync
        glfwSwapInterval(1);
        //Desired FPS
        //glfwSwapInterval(144);

        // Make the window visible
        glfwShowWindow(ID);
        //Creates our OpenGL Context
        Capabilities = GL.createCapabilities();

        InputEvents = new Input();
        SetCallbacks();

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // Set the clear color
    }

    public boolean getShouldClose() { return glfwWindowShouldClose(ID); }
    public void setShouldClose(boolean value) { glfwSetWindowShouldClose(ID, value); }
    
    private void SetCallbacks() {
        // Set up a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(ID, InputEvents.KeyboardCallback);
        glfwSetCursorPosCallback(ID, InputEvents.MouseMoveCallback);
        glfwSetMouseButtonCallback(ID, InputEvents.MouseButtonsCallback);
        glfwSetScrollCallback(ID, InputEvents.MouseScrollCallback);

        glfwSetWindowSizeCallback(ID, InputEvents.WindowResizeCallback);
    }
    
    public void Cleanup() {
        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(ID);
        glfwDestroyWindow(ID);
        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }
}
