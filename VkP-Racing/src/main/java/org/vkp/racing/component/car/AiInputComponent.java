package org.vkp.racing.component.car;

import org.vkp.racing.component.InputComponent;
import org.vkp.racing.entity.Entity;

public class AiInputComponent implements InputComponent {

	private CarPhysicsComponent carPhysicsComponent;

	private static final double[] ANGLES = { 0.0d, Math.PI / 2, Math.PI, 3 * Math.PI / 2 };

	private long startTime;

	private int currentAngleIndex;
	private boolean isDelay;

	public AiInputComponent(CarPhysicsComponent carPhysicsComponent) {
		this.carPhysicsComponent = carPhysicsComponent;
		startTime = System.nanoTime();
	}

	@Override
	public void update(Entity entity) {
		carPhysicsComponent.accelerate();
		long now = System.nanoTime();
		if (isDelay && now - startTime >= 1e9) {
			currentAngleIndex = (currentAngleIndex + 1) % ANGLES.length;
			isDelay = false;
		}
		float rotation = (float) (entity.getTransform().getRotation() + Math.PI);
		if (rotation < ANGLES[currentAngleIndex]
				|| (currentAngleIndex == 0 && rotation > ANGLES[ANGLES.length - 1])) {
			carPhysicsComponent.steerRight();
		} else if (!isDelay) {
			isDelay = true;
			startTime = now;
			carPhysicsComponent.noSteer();
		}
	}

}
