package org.vkp.racing.scene;

import java.util.ArrayList;
import java.util.BitSet;
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

	public List<List<Component>> getRequiredEntities(BitSet requiredComponents) {
		List<List<Component>> foundEntities = new ArrayList<>();
		entities.forEach((entity, components) -> {
			BitSet componentIds = new BitSet();
			components.forEach(component -> componentIds.or(component.getId()));

			BitSet conjuction = new BitSet();
			conjuction.or(requiredComponents);

			conjuction.and(componentIds);
			if (conjuction.equals(requiredComponents)) {
				foundEntities.add(components);
			}
		});

		return foundEntities;
	}

	public EntityBuilder createEntity() {
		return new EntityBuilder(newEntityId++, this);
	}

}
