package me.toast.engine.world.systems;

import com.badlogic.ashley.core.*;
import me.toast.engine.world.components.*;

public class MeshSystem extends EntitySystem {

    private final EntityListener listener = new EntityListener() {
        @Override public void entityAdded(Entity entity) {}
        @Override public void entityRemoved(Entity entity) {
            entity.getComponent(RenderComponent.class).mesh.Cleanup();
        }
    };

    public void addedToEngine(Engine engine) {
        engine.addEntityListener(listener);
    }

    public void removedFromEngine(Engine engine) {
        engine.removeEntityListener(listener);
    }
}
