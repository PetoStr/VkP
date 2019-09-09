package org.vkp.engine.model;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.vkp.engine.mesh.Mesh;
import org.vkp.engine.texture.Texture;

import lombok.Getter;
import lombok.Setter;

public class Model {

	@Getter
	private Vector3f position = new Vector3f();

	@Getter
	private float width = 1.0f;

	@Getter
	private float height = 1.0f;

	@Getter
	@Setter
	private Mesh mesh;

	@Setter
	@Getter
	private Texture texture;

	private Matrix4f modelMatrix = new Matrix4f();

	private boolean shouldUpdateModelMatrix = true;

	public Model(Mesh mesh, Texture texture) {
		this.mesh = mesh;
		this.texture = texture;
	}

	public void move(Vector3f d) {
		position.add(d);
		shouldUpdateModelMatrix = true;
	}

	public void setPosition(Vector3f position) {
		this.position = position;
		shouldUpdateModelMatrix = true;
	}

	public void setWidth(float width) {
		this.width = width;
		shouldUpdateModelMatrix = true;
	}

	public void setHeight(float height) {
		this.height = height;
		shouldUpdateModelMatrix = true;
	}

	public Matrix4f getModelMatrix() {
		if (shouldUpdateModelMatrix) {
			updateModelMatrix();
			shouldUpdateModelMatrix = false;
		}

		return modelMatrix;
	}

	private void updateModelMatrix() {
		modelMatrix.translation(position);
		modelMatrix.scaleXY(width, height);
	}

}
