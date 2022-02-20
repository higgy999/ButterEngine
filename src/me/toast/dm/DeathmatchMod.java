package me.toast.dm;

import me.toast.engine.AssetPool;
import me.toast.engine.Mod;
import me.toast.engine.rendering.*;
import me.toast.engine.scene.Camera;
import me.toast.engine.world.components.RenderComponent;
import me.toast.engine.world.components.TransformComponent;
import me.toast.engine.world.entities.RenderEntity;
import me.toast.engine.world.systems.MeshSystem;
import org.joml.*;

import java.lang.Math;

import static org.lwjgl.glfw.GLFW.*;

public class DeathmatchMod extends Mod {

    public Mesh mesh;
    public RenderEntity test;

    MeshSystem meshSystem = new MeshSystem();

    public DeathmatchMod() {
        super("DMTest",
                "Deathmatch Mod",
                "Simple multiplayer deathmatch test in ButterEngine. Use this as an example.",
                "LitlToast");
    }

    @Override
    public void M_Init() {
        mesh = Model.LoadScene("dragon.obj", AssetPool.getShader("Mesh"), AssetPool.getMaterial("dragon"))[0];
        test = new RenderEntity(new TransformComponent(new Vector3f(0, -5, -15), new Quaternionf(), new Vector3f(1)), new RenderComponent(mesh));

        Camera = new Camera(new Vector3f(), new Quaternionf());
        Camera.setProjection((float) Math.toRadians(90f), (float) Window.Width / (float) Window.Height, 0.1f, 1000f);

        Ashley.addSystem(meshSystem);
    }

        @Override
        public void M_Update(float dt) {
            if (Window.InputEvents.isKeyDown(GLFW_KEY_ESCAPE))
                Window.setShouldClose(true);

            if (Window.InputEvents.isButtonDown(GLFW_MOUSE_BUTTON_LEFT))
                Window.InputEvents.SetMouseState(true);
            if (Window.InputEvents.isButtonDown(GLFW_MOUSE_BUTTON_RIGHT))
                Window.InputEvents.SetMouseState(false);

            test.transform.position.y += 2f * dt;
        }

        @Override
        public void M_Render() {}

    @Override
    public void M_Cleanup() {}
}
