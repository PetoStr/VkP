package org.vkp.racing.ecs.system;

import java.util.BitSet;
import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.vkp.racing.ecs.component.CarPhysicsComponent;
import org.vkp.racing.ecs.component.Component;
import org.vkp.racing.ecs.component.Transform;

public class CarMovementSystem implements GameSystem {

	@Override
	public void update(List<Component> components) {
		ComponentGroup componentGroup = new ComponentGroup();
		ComponentExtractor.extract(componentGroup, components);

		CarPhysicsComponent carPhysics = componentGroup.carPhysics;
		Transform transform = componentGroup.transform;
		float speed = carPhysics.getSpeed();
		float engineForce = carPhysics.getEngineForce();

		float direction = Math.signum(engineForce);
		float rrForce = -CarPhysicsComponent.CONST_ROLLING_RESISTANCE * speed;
		float dragForce = -CarPhysicsComponent.CONST_DRAG * speed * speed * direction;
		float tractionForce = engineForce;
		float longForce = rrForce + dragForce + tractionForce;
		float acceleration = longForce / 500.0f;
		carPhysics.setSpeed(speed + acceleration * 10);
		updateWheels(carPhysics, transform);
	}

	private void updateWheels(CarPhysicsComponent carPhysics, Transform transform) {
		Vector2f backWheelPos;
		Vector2f frontWheelPos;
		Vector2f carPosition = new Vector2f(transform.getPosition().x, transform.getPosition().y);

		float rotation = transform.getRotation();
		float speed = carPhysics.getSpeed();
		float angle = rotation + carPhysics.getSteerAngle();
		final float WHEEL_BASE_HALF = CarPhysicsComponent.WHEEL_BASE_HALF;

		frontWheelPos = new Vector2f(carPosition).add(
				new Vector2f(WHEEL_BASE_HALF, WHEEL_BASE_HALF)
					.mul(new Vector2f((float) Math.cos(rotation), (float) Math.sin(rotation))));
		backWheelPos = new Vector2f(carPosition).sub(
				new Vector2f(WHEEL_BASE_HALF, WHEEL_BASE_HALF)
					.mul(new Vector2f((float) Math.cos(rotation), (float) Math.sin(rotation))));

		frontWheelPos.add(new Vector2f(speed)
				.mul(new Vector2f((float) Math.cos(angle), (float) Math.sin(angle))));
		backWheelPos.add(new Vector2f(speed)
				.mul(new Vector2f((float) Math.cos(rotation), (float) Math.sin(rotation))));
		Vector2f newPos = new Vector2f(frontWheelPos).add(backWheelPos).mul(0.5f);
		transform.setPosition(new Vector3f(newPos, transform.getPosition().z));
		transform.setRotation((float) Math.atan2(frontWheelPos.y - backWheelPos.y,
				frontWheelPos.x - backWheelPos.x));
	}

	@Override
	public BitSet getRequiredComponents() {
		BitSet bitSet = new BitSet();
		bitSet.or(Transform.ID);
		bitSet.or(CarPhysicsComponent.ID);
		return bitSet;
	}

	private static class ComponentGroup {
		public Transform transform;
		public CarPhysicsComponent carPhysics;
	}

}
