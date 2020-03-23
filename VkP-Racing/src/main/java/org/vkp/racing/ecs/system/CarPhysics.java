package org.vkp.racing.ecs.system;

import java.util.BitSet;
import java.util.List;

import org.vkp.racing.ecs.component.BarrierComponent;
import org.vkp.racing.ecs.component.CarPhysicsComponent;
import org.vkp.racing.ecs.component.Component;
import org.vkp.racing.ecs.component.Transform;
import org.vkp.racing.scene.Scene;

public class CarPhysics implements GameSystem {

	private Scene scene;

	public CarPhysics(Scene scene) {
		this.scene = scene;
	}

	@Override
	public void update(List<Component> components) {
		ComponentExtractor<ComponentGroup> componentExtractor = new ComponentExtractor<>();
		ComponentGroup componentGroup = new ComponentGroup();
		componentExtractor.extract(componentGroup, components);

		List<List<Component>> barriers = scene.getRequiredEntities(BarrierComponent.ID);
		barriers.forEach(barrier -> processBarrier(components, barrier, componentGroup));
	}

	@Override
	public BitSet getRequiredComponents() {
		BitSet bitSet = new BitSet();
		bitSet.or(Transform.ID);
		bitSet.or(CarPhysicsComponent.ID);
		return bitSet;
	}

	private void processBarrier(List<Component> entityComponents, List<Component> barrierComponents,
			ComponentGroup componentGroup) {
		Transform barrierTransform =
				(Transform) ComponentExtractor.extractOne(Transform.class, barrierComponents);
		if (barrierTransform == null) return;

		if (componentGroup.transform.intersects(barrierTransform)) {
			entityComponents.remove(componentGroup.carPhysics);
		}
	}

	private static class ComponentGroup {
		public Transform transform;
		public CarPhysicsComponent carPhysics;
	}

}
