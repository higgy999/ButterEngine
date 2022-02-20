package me.toast.engine.world.systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import me.toast.engine.world.components.RenderComponent;
import me.toast.engine.world.components.TransformComponent;
import me.toast.engine.world.entities.Dragon;

public class DragonSystem extends EntitySystem {

    private ImmutableArray<Entity> entities;
    private final ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);

    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(TransformComponent.class, RenderComponent.class).get());
    }

    public void update(float deltaTime) {
        for (Entity entity : entities) {
            if (entity instanceof Dragon dragon) {
                TransformComponent tc = tm.get(dragon);
                tc.position.y += 2f * deltaTime;
            }
        }
    }
}
