package org.vkp.racing.scene;

import java.util.ArrayList;

import org.vkp.racing.ecs.component.BarrierComponent;
import org.vkp.racing.ecs.component.Component;

public class EntityBuilder {

	private int entityId;

	private Scene scene;

	private boolean isBarrier;

	public EntityBuilder(int entityId, Scene scene) {
		this.entityId = entityId;
		this.scene = scene;

		scene.getEntities().put(entityId, new ArrayList<>());
		if (isBarrier) scene.getWalls().add(entityId);
	}

	public EntityBuilder with(Component component) {
		scene.getEntities().get(entityId).add(component);

		if (component instanceof BarrierComponent) isBarrier = true;

		return this;
	}

	public int build() {
		return entityId;
	}

}
