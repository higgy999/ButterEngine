package me.toast.engine.world.entities;

import com.badlogic.ashley.core.Entity;
import me.toast.engine.Mod;
import me.toast.engine.world.components.RenderComponent;
import me.toast.engine.world.components.TransformComponent;

public class Dragon extends Entity {

    public RenderComponent meshCom;
    public TransformComponent transform;

    public Dragon(TransformComponent transform, RenderComponent mesh) {
        this.add(transform);
        this.add(mesh);

        this.transform = getComponent(TransformComponent.class);
        this.meshCom = this.getComponent(RenderComponent.class);

        Mod.Ashley.addEntity(this);
    }
}
