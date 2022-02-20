package me.toast.engine;

import me.toast.dm.DeathmatchMod;
import me.toast.engine.utils.Time;

public class Main {

    public static void main(String[] args) {
        //System.setProperty("imgui.library.path", "./libs");

        new DeathmatchMod();

        Mod.Init();
            Time.Init();
            while (!Mod.Window.getShouldClose()) {
                Mod.Window.PollEvents();
                Mod.Window.Clear();

                Mod.Update(Time.deltaTime);
                Mod.Render();

                Mod.Window.SwapBuffers();

                Time.UpdateDeltaTime();
            }
        Mod.Cleanup();
    }
}
