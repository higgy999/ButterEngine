package me.toast.engine.world.systems;

import me.toast.engine.Mod;

public class Systems {
    public static void AddEntitySystems() {
        Mod.Ashley.addSystem(new DragonSystem());
    }
}
