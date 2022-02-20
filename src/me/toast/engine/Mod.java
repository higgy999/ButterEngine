package me.toast.engine;

import com.badlogic.ashley.core.Engine;
import me.toast.engine.rendering.Renderer;
import me.toast.engine.scene.Camera;
import me.toast.engine.ui.UserInterface;
import me.toast.engine.window.Window;

public abstract class Mod {

    public static Mod LOADED_MOD;

    //We'll use this info later in the mod loading
    public String ID, NAME, DESCRIPTION, AUTHOR;

    public static Window Window;
    public static Engine Ashley;
    public static UserInterface Ultralight;
    public static Renderer RenderEngine;

    public Camera Camera;

    public Mod(String id, String name, String description, String author) {
        this.ID = id;
        this.NAME = name;
        this.DESCRIPTION = description;
        this.AUTHOR = author;

        LOADED_MOD = this;
    }

    //TODO: Make an event system

    //The lifecycle of the engine mod
    public static void Init() {
        Window = new Window(1280, 720);
        Ashley = new Engine();
        RenderEngine = new Renderer(Ashley);
        Ultralight = new UserInterface(Window);
        LOADED_MOD.M_Init();
    } //Initialize systems, texture, models, assets, etc.

    public static void Update(float dt) {
        Ashley.update(dt);
        Ultralight.Update();
        LOADED_MOD.M_Update(dt);
    } //Handle Input and then update game logic

    public static void Render() {
        RenderEngine.Render();
        Ultralight.Render();
        LOADED_MOD.M_Render();
    } //Draw Stuff

    public static void Cleanup() {
        Ashley.removeAllEntities();
        Ashley.removeAllSystems();
        Window.Cleanup();
        RenderEngine.Cleanup();
        Ultralight.Cleanup();
        AssetPool.CleanupAll();

        LOADED_MOD.M_Cleanup();
    } //Destroy systems, texture, models, assets, etc.

    public abstract void M_Init();
    public abstract void M_Update(float dt);
    public abstract void M_Render();
    public abstract void M_Cleanup();
}
