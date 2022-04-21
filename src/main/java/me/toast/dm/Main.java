package me.toast.dm;

import me.toast.engine.Mod;
import me.toast.engine.util.Time;

public class Main {

    public static void main(String[] args) {
        //System.setProperty("imgui.library.path", "./libs");

        new DeathmatchMod();

        Mod.Init();
            Time.Init();
            while (!Mod.Window.getShouldClose()) {
                Mod.Window.PollEvents();
                Mod.Ultralight.webController.update();
                Mod.Window.Clear();

                Mod.Update(Time.deltaTime);
                Mod.Render();

                Mod.Window.SwapBuffers();

                Time.UpdateDeltaTime();
            }
        Mod.Cleanup();
    }
}
