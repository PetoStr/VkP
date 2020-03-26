package org.vkp.racing.ecs.component;

import java.util.BitSet;

import org.joml.Vector2f;

import lombok.Getter;
import lombok.Setter;

public class RayComponent implements Component {

	public static final BitSet ID = new BitSet();
	static {
		ID.set(9);
	}

	@Getter
	@Setter
	private float measuredDistance;

	public Vector2f getDirection(float rotation) {
		float sin = (float) Math.sin(rotation);
		float cos = (float) Math.cos(rotation);
		return new Vector2f(cos, sin);
	}

	@Override
	public BitSet getId() {
		return ID;
	}

}
