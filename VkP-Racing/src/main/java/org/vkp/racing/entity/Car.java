package org.vkp.racing.entity;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.vkp.engine.mesh.TexturedMesh;
import org.vkp.engine.texture.TextureInfo;
import org.vkp.engine.window.KeyListener;
import org.vkp.racing.Assets;
import org.vkp.racing.GameRenderer;
import org.vkp.racing.Scene;
import org.vkp.racing.math.MathUtil;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

import lombok.Getter;
import lombok.Setter;

public class Car implements Entity, Movable, KeyListener {

	@Getter
	@Setter
	private Vector3f position = new Vector3f();

	@Getter
	@Setter
	private float rotation;

	private float width;
	private float height;

	private float speed = 0.0f;

	private float acceleration = 0.0f;

	private float rotationSpeed = 0.0f;

	private TexturedMesh texturedMesh;

	public Car(Assets assets) {
		texturedMesh = assets.getRedCar();

		TextureInfo info = texturedMesh.getTexture().getTextureInfo();
		width = info.getWidth() / 7.0f;
		height = info.getHeight() / 7.0f;
	}

	@Override
	public void update(Scene scene) {
		rotate(rotationSpeed);
		speed += acceleration;
		move(speed);
	}

	@Override
	public void draw(GameRenderer renderer) {
		renderer.drawEntity(this);
	}

	@Override
	public void onKeyAction(int action, int key, int mods) {
		if (action == GLFW_PRESS) {
			if (key == GLFW_KEY_W && acceleration == 0.0f) {
				acceleration = 1.0f;
			} else if (key == GLFW_KEY_S && acceleration == 0.0f) {
				acceleration = -1.0f;
			} else if (key == GLFW_KEY_A && rotationSpeed == 0.0f) {
				rotationSpeed = -0.1f;
			} else if (key == GLFW_KEY_D && rotationSpeed == 0.0f) {
				rotationSpeed = 0.1f;
			}
		} else if (action == GLFW_RELEASE) {
			if (key == GLFW_KEY_W && acceleration != 0.0f) {
				acceleration = 0.0f;
			} else if (key == GLFW_KEY_S && acceleration != 0.0f) {
				acceleration = 0.0f;
			} else if (key == GLFW_KEY_A && rotationSpeed != 0.0f) {
				rotationSpeed = 0.0f;
			} else if (key == GLFW_KEY_D && rotationSpeed != 0.0f) {
				rotationSpeed = 0.0f;
			}
		}
	}

	@Override
	public Matrix4f getModelMatrix() {
		return MathUtil.calculateModelMatrix(position, rotation, width, height);
	}

	@Override
	public TexturedMesh getTexturedMesh() {
		return texturedMesh;
	}

	@Override
	public void move(float d) {
		position.x += d * Math.cos(rotation);
		position.y += d * Math.sin(rotation);
	}

	@Override
	public void rotate(float d) {
		rotation += d;
	}

}
