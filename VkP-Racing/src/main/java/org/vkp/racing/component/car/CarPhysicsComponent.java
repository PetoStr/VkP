package org.vkp.racing.component.car;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.vkp.racing.Scene;
import org.vkp.racing.component.PhysicsComponent;
import org.vkp.racing.entity.Entity;
import org.vkp.racing.entity.Transform;

import lombok.Getter;
import lombok.Setter;

public class CarPhysicsComponent implements PhysicsComponent {

	@Getter
	@Setter
	public float engineForce;

	@Getter
	@Setter
	public float steerAngle;

	private static final float CONST_DRAG = 0.04257f;
	private static final float CONST_ROLLING_RESISTANCE = 0.128f;
	private static final float WHEEL_BASE_HALF = 6.0f;

	private float speed;

	@Override
	public void update(Entity entity, Scene scene) {
		float rrForce = -CONST_ROLLING_RESISTANCE * speed;
		float dragForce = -CONST_DRAG * speed * speed;
		float tractionForce = engineForce;
		float longForce = rrForce + dragForce + tractionForce;
		float acceleration = longForce / 500.0f;
		speed += acceleration * 10;
		updateWheels(entity);
	}

	private void updateWheels(Entity entity) {
		Vector2f backWheelPos;
		Vector2f frontWheelPos;
		Transform transform = entity.getTransform();
		Vector2f carPosition = new Vector2f(transform.getPosition().x, transform.getPosition().y);
		float rotation = transform.getRotation();
		float angle = rotation + steerAngle;
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

}
