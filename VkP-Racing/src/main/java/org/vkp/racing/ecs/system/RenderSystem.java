package org.vkp.racing.ecs.system;

import java.util.BitSet;
import java.util.List;

import org.joml.Matrix4f;
import org.vkp.engine.mesh.TexturedMesh;
import org.vkp.racing.GameRenderer;
import org.vkp.racing.TexturedModel;
import org.vkp.racing.ecs.component.Component;
import org.vkp.racing.ecs.component.TexturedMeshComponent;
import org.vkp.racing.ecs.component.Transform;

public class RenderSystem implements GameSystem {

	private GameRenderer gameRenderer;

	public RenderSystem(GameRenderer gameRenderer) {
		this.gameRenderer = gameRenderer;
	}

	@Override
	public void update(List<Component> components) {
		ComponentExtractor<ComponentGroup> componentExtractor = new ComponentExtractor<>();
		ComponentGroup componentGroup = new ComponentGroup();
		componentExtractor.extract(componentGroup, components);

		TexturedMesh texturedMesh = componentGroup.texturedMesh.getTexturedMesh();
		Matrix4f modelMatrix = componentGroup.transform.getModelMatrix();
		TexturedModel texturedModel = new TexturedModel(texturedMesh, modelMatrix);
		gameRenderer.drawTexturedModel(texturedModel);
	}

	@Override
	public BitSet getRequiredComponents() {
		BitSet bitSet = new BitSet();
		bitSet.or(Transform.ID);
		bitSet.or(TexturedMeshComponent.ID);
		return bitSet;
	}

	private static class ComponentGroup {
		public Transform transform;
		public TexturedMeshComponent texturedMesh;
	}

}
