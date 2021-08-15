package me.toast.engine;

import test.game.DeathmatchMod;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Main {

    public void run() {
        new DeathmatchMod(1280, 720);

        Mod.LOADED_MOD.Init();
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // Set the clear color

        loop();

        Mod.LOADED_MOD.WINDOW.Destroy();
    }

    private void loop() {
        // Run the rendering loop until the user has attempted to close
        while ( !glfwWindowShouldClose(Mod.LOADED_MOD.WINDOW.ID) ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            Mod.LOADED_MOD.Update();

            Mod.LOADED_MOD.Render();

            Mod.LOADED_MOD.WINDOW.Update();
        }

        Mod.LOADED_MOD.ECS.removeAllEntities();
        Mod.LOADED_MOD.ECS.removeAllSystems();
        Mod.LOADED_MOD.Shutdown();
    }

    public static void main(String[] args) {
        new Main().run();
    }
}
