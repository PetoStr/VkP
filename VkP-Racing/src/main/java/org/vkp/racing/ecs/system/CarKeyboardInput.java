package org.vkp.racing.ecs.system;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.vkp.engine.window.KeyCallback;
import org.vkp.engine.window.KeyListener;
import org.vkp.racing.ecs.component.CarPhysicsComponent;
import org.vkp.racing.ecs.component.Component;
import org.vkp.racing.ecs.component.CarKeyboardComponent;
import org.vkp.racing.ecs.component.Transform;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

import lombok.AllArgsConstructor;
import lombok.Data;

public class CarKeyboardInput implements GameSystem, KeyListener {

	@Data
	@AllArgsConstructor
	static class KeyAction {
		int action;
		int key;
	}

	private List<KeyAction> recentKeyActions = new ArrayList<>();

	public CarKeyboardInput(KeyCallback keyCallback) {
		keyCallback.registerListener(this);
	}

	@Override
	public void update(List<Component> components) {
		ComponentExtractor<ComponentGroup> componentExtractor = new ComponentExtractor<>();
		ComponentGroup componentGroup = new ComponentGroup();
		componentExtractor.extract(componentGroup, components);

		for (KeyAction keyAction : recentKeyActions) {
			if (keyAction.action == GLFW_PRESS) {
				handleKeyPress(componentGroup, keyAction);
			} else if (keyAction.action == GLFW_RELEASE) {
				handleKeyRelease(componentGroup, keyAction);
			}
		}
	}

	@Override
	public void finish() {
		recentKeyActions.clear();
	}

	private void handleKeyPress(ComponentGroup componentGroup, KeyAction keyAction) {
		if (keyAction.key == GLFW_KEY_W) {
			componentGroup.carPhysics.accelerate();
		} else if (keyAction.key == GLFW_KEY_S) {
			componentGroup.carPhysics.reverse();
		} else if (keyAction.key == GLFW_KEY_A) {
			componentGroup.carPhysics.steerLeft();
			for (Transform child : componentGroup.transform.getChildren()) {
				child.setRotation(-0.25f);
			}
		} else if (keyAction.key == GLFW_KEY_D) {
			componentGroup.carPhysics.steerRight();
			for (Transform child : componentGroup.transform.getChildren()) {
				child.setRotation(0.25f);
			}
		}
	}

	private void handleKeyRelease(ComponentGroup componentGroup, KeyAction keyAction) {
		if (keyAction.key == GLFW_KEY_W) {
			componentGroup.carPhysics.neutral();
		} else if (keyAction.key == GLFW_KEY_S) {
			componentGroup.carPhysics.neutral();
		} else if ((keyAction.key == GLFW_KEY_A)
				|| (keyAction.key == GLFW_KEY_D)) {
			componentGroup.carPhysics.noSteer();
			for (Transform child : componentGroup.transform.getChildren()) {
				child.setRotation(0.0f);
			}
		}
	}

	@Override
	public void onKeyAction(int action, int key, int mods) {
		recentKeyActions.add(new KeyAction(action, key));
	}

	@Override
	public BitSet getRequiredComponents() {
		BitSet bitSet = new BitSet();
		bitSet.or(CarKeyboardComponent.ID);
		bitSet.or(CarPhysicsComponent.ID);
		bitSet.or(Transform.ID);
		return bitSet;
	}

	private static class ComponentGroup {
		public Transform transform;
		public CarPhysicsComponent carPhysics;
	}

}
