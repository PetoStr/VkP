package org.vkp.engine;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import lombok.Data;

@Data
public class Camera {

	private Vector3f position = new Vector3f();

	private Matrix4f viewMatrix = new Matrix4f();

	public void update() {
		viewMatrix.translation(position);
	}

	public void move(Vector3f d) {
		position.add(d);
	}

}
