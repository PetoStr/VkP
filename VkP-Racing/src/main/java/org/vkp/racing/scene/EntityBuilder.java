package org.vkp.racing.scene;

import java.util.ArrayList;

import org.vkp.racing.ecs.component.Component;

public class EntityBuilder {

	private int entityId;

	private Scene scene;

	public EntityBuilder(int entityId, Scene scene) {
		this.entityId = entityId;
		this.scene = scene;

		scene.getEntities().put(entityId, new ArrayList<>());
	}

	public EntityBuilder with(Component component) {
		scene.getEntities().get(entityId).add(component);

		return this;
	}

	public int build() {
		return entityId;
	}

}
