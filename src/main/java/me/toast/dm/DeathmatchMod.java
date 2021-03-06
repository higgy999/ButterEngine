package me.toast.dm;

import me.toast.engine.Mod;
import me.toast.engine.scene.Camera;
import me.toast.engine.world.components.TransformComponent;
import me.toast.engine.world.entities.Dragon;
import me.toast.engine.world.entities.UnitBox;
import org.joml.*;

import java.lang.Math;

import static org.lwjgl.glfw.GLFW.*;

public class DeathmatchMod extends Mod {

    public Dragon dragon;
    public UnitBox box;

    public DeathmatchMod() {
        super("DMTest",
                "Deathmatch Mod",
                "Simple multiplayer deathmatch test in ButterEngine. Use this as an example.",
                "LitlToast");
    }

    @Override
    public void M_Init() {
        dragon = new Dragon(new TransformComponent(new Vector3f(0, -5, -15), new Quaternionf(), new Vector3f(1)));
        box = new UnitBox(new TransformComponent(new Vector3f(0, -5, -15), new Quaternionf(), new Vector3f(1)));

        Cam = new Camera(new Vector3f(), new Vector3f());
        Cam.setProjection((float) Math.toRadians(90f), (float) Window.Width / (float) Window.Height, 0.1f, 1000f);
    }

        @Override
        public void M_Update(float deltaTime) {
            if (Window.InputEvents.isKeyDown(GLFW_KEY_ESCAPE))
                Window.setShouldClose(true);
        }

        @Override
        public void M_Render() {}

    @Override
    public void M_Cleanup() {}
}
