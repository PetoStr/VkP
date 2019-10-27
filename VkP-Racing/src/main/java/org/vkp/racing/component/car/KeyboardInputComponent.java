package org.vkp.racing.component.car;

import java.util.ArrayList;
import java.util.List;

import org.vkp.engine.window.KeyCallback;
import org.vkp.engine.window.KeyListener;
import org.vkp.racing.component.InputComponent;
import org.vkp.racing.entity.Entity;
import org.vkp.racing.entity.Transform;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

import lombok.AllArgsConstructor;
import lombok.Data;

public class KeyboardInputComponent implements InputComponent, KeyListener {

	@Data
	@AllArgsConstructor
	static class KeyAction {
		int action;
		int key;
	}

	private List<KeyAction> recentKeyActions = new ArrayList<>();

	private CarPhysicsComponent carPhysicsComponent;

	public KeyboardInputComponent(CarPhysicsComponent carPhysicsComponent, KeyCallback keyCallback) {
		this.carPhysicsComponent = carPhysicsComponent;
		keyCallback.registerListener(this);
	}

	@Override
	public void update(Entity entity) {
		for (KeyAction keyAction : recentKeyActions) {
			if (keyAction.action == GLFW_PRESS) {
				handleKeyPress(entity, keyAction);
			} else if (keyAction.action == GLFW_RELEASE) {
				handleKeyRelease(entity, keyAction);
			}
		}
		recentKeyActions.clear();
	}

	private void handleKeyPress(Entity entity, KeyAction keyAction) {
		if (keyAction.key == GLFW_KEY_W) {
			carPhysicsComponent.accelerate();
		} else if (keyAction.key == GLFW_KEY_S) {
			carPhysicsComponent.reverse();
		} else if (keyAction.key == GLFW_KEY_A) {
			carPhysicsComponent.steerLeft();
			for (Transform child : entity.getTransform().getChildren()) {
				child.setRotation(-0.25f);
			}
		} else if (keyAction.key == GLFW_KEY_D) {
			carPhysicsComponent.steerRight();
			for (Transform child : entity.getTransform().getChildren()) {
				child.setRotation(0.25f);
			}
		}
	}

	private void handleKeyRelease(Entity entity, KeyAction keyAction) {
		if (keyAction.key == GLFW_KEY_W) {
			carPhysicsComponent.neutral();
		} else if (keyAction.key == GLFW_KEY_S) {
			carPhysicsComponent.neutral();
		} else if ((keyAction.key == GLFW_KEY_A)
				|| (keyAction.key == GLFW_KEY_D)) {
			carPhysicsComponent.noSteer();
			for (Transform child : entity.getTransform().getChildren()) {
				child.setRotation(0.0f);
			}
		}
	}

	@Override
	public void onKeyAction(int action, int key, int mods) {
		recentKeyActions.add(new KeyAction(action, key));
	}

}
