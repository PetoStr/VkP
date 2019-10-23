package org.vkp.racing.entity;

import org.vkp.engine.mesh.TexturedMesh;
import org.vkp.racing.GameRenderer;
import org.vkp.racing.Scene;
import org.vkp.racing.component.GraphicsComponent;
import org.vkp.racing.component.InputComponent;
import org.vkp.racing.component.PhysicsComponent;

import lombok.Getter;
import lombok.Setter;

public class Entity {

	@Getter
	@Setter
	private Transform transform;

	private GraphicsComponent graphicsComponent;
	private PhysicsComponent physicsComponent;
	private InputComponent inputComponent;

	public Entity(Transform transform,
			GraphicsComponent graphicsComponent,
			PhysicsComponent physicsComponent,
			InputComponent inputComponent) {
		this.transform = transform;
		this.graphicsComponent = graphicsComponent;
		this.physicsComponent = physicsComponent;
		this.inputComponent = inputComponent;
	}

	public void update(Scene scene) {
		inputComponent.update(this);
		physicsComponent.update(this, scene);
	}

	public void draw(GameRenderer renderer) {
		graphicsComponent.draw(this, renderer);
	}

	public TexturedMesh getTexturedMesh() {
		return graphicsComponent.getTexturedMesh();
	}

}
