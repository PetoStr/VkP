package org.vkp.racing.ecs.component;

import java.util.BitSet;

import lombok.Getter;
import lombok.Setter;

public class CarPhysicsComponent implements Component {

	public static final BitSet ID = new BitSet();
	static {
		ID.set(3);
	}

	public static final float CONST_DRAG = 0.04257f;
	public static final float CONST_ROLLING_RESISTANCE = 0.128f;
	public static final float WHEEL_BASE_HALF = 6.0f;

	@Getter
	@Setter
	private float engineForce;

	@Getter
	@Setter
	private float steerAngle;

	@Getter
	@Setter
	private float speed;


	public void steerLeft() {
		steerAngle = -0.25f;
	}

	public void steerRight() {
		steerAngle = 0.25f;
	}

	public void noSteer() {
		steerAngle = 0.0f;
	}

	public void accelerate() {
		engineForce = 1.5f;
	}

	public void reverse() {
		engineForce = -1.5f;
	}

	public void neutral() {
		engineForce = 0.0f;
	}

	@Override
	public BitSet getId() {
		return ID;
	}

}
