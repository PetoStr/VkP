package org.vkp.racing.scene;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vkp.engine.Camera;
import org.vkp.racing.ecs.component.Component;

import lombok.Getter;

public class Scene {

	@Getter
	private Camera camera;

	@Getter
	private Map<Integer, List<Component>> entities = new HashMap<>();

	private int newEntityId = 0;

	public Scene() {
		camera = new Camera();
	}

	public EntityBuilder createEntity() {
		return new EntityBuilder(newEntityId++, this);
	}

}
