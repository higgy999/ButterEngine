package me.toast.engine;

import me.toast.dm.DeathmatchMod;
import me.toast.engine.utils.Time;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Main {

    public static void main(String[] args) {
        //System.setProperty("imgui.library.path", "./libs");

        new DeathmatchMod();

        Mod.Init();
            float deltaTime = 0f, endTime, beginTime = Time.getTime();
            while (!Mod.Window.getShouldClose()) {
                glfwPollEvents(); // Poll for window events. Callbacks will only be invoked during this call.
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

                Mod.Update(deltaTime);
                Mod.Render();

                glfwSwapBuffers(Mod.Window.ID); // swap the frame buffers

                endTime = Time.getTime();
                deltaTime = endTime - beginTime;
                beginTime = Time.getTime();
            }
        Mod.Cleanup();
    }
}
