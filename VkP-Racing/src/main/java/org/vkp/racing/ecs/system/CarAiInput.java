package org.vkp.racing.ecs.system;

import java.util.BitSet;
import java.util.List;

import org.vkp.racing.ecs.component.CarAiComponent;
import org.vkp.racing.ecs.component.CarPhysicsComponent;
import org.vkp.racing.ecs.component.Component;
import org.vkp.racing.ecs.component.Transform;

public class CarAiInput implements GameSystem {

	private static final double[] ANGLES = { 0.0d, Math.PI / 2, Math.PI, 3 * Math.PI / 2 };

	@Override
	public void update(List<Component> components) {
		ComponentExtractor<ComponentGroup> componentExtractor = new ComponentExtractor<>();
		ComponentGroup componentGroup = new ComponentGroup();
		componentExtractor.extract(componentGroup, components);

		CarAiComponent carAi = componentGroup.carAi;
		Transform transform = componentGroup.transform;
		CarPhysicsComponent carPhysics = componentGroup.carPhysics;

		carPhysics.accelerate();
		long now = System.nanoTime();
		if (carAi.isDelay() && now - carAi.getStartTime() >= 1e9) {
			carAi.setCurrentAngleIndex((carAi.getCurrentAngleIndex() + 1) % ANGLES.length);
			carAi.setDelay(false);
		}
		float rotation = (float) (transform.getRotation() + Math.PI);
		if (rotation < ANGLES[carAi.getCurrentAngleIndex()]
				|| (carAi.getCurrentAngleIndex() == 0 && rotation > ANGLES[ANGLES.length - 1])) {
			carPhysics.steerRight();
		} else if (!carAi.isDelay()) {
			carAi.setDelay(true);
			carAi.setStartTime(now);
			carPhysics.noSteer();
		}
	}

	@Override
	public BitSet getRequiredComponents() {
		BitSet bitSet = new BitSet();
		bitSet.or(CarAiComponent.ID);
		bitSet.or(CarPhysicsComponent.ID);
		bitSet.or(Transform.ID);
		return bitSet;
	}

	private static class ComponentGroup {
		public CarAiComponent carAi;
		public Transform transform;
		public CarPhysicsComponent carPhysics;
	}

}
