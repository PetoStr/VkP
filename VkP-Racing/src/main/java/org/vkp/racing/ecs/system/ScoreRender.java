package org.vkp.racing.ecs.system;

import java.util.BitSet;
import java.util.List;

import org.joml.Vector3f;
import org.vkp.racing.GameRenderer;
import org.vkp.racing.ecs.component.Component;
import org.vkp.racing.ecs.component.RacerComponent;
import org.vkp.racing.ecs.component.Transform;

public class ScoreRender implements GameSystem {

	private GameRenderer gameRenderer;

	public ScoreRender(GameRenderer gameRenderer) {
		this.gameRenderer = gameRenderer;
	}

	@Override
	public void update(List<Component> components) {
		ComponentGroup componentGroup = new ComponentGroup();
		ComponentExtractor.extract(componentGroup, components);

		Transform transform = componentGroup.transform;
		Vector3f position = new Vector3f(transform.getPosition());
		position.x -= transform.getWidth();
		position.y -= transform.getHeight() * 1.5f;
		String text = "Score: " + componentGroup.racer.getScore();

		gameRenderer.drawText(text, position.x, position.y, 80.0f);
	}

	@Override
	public BitSet getRequiredComponents() {
		BitSet bitSet = new BitSet();
		bitSet.or(Transform.ID);
		bitSet.or(RacerComponent.ID);
		return bitSet;
	}

	private static class ComponentGroup {
		public Transform transform;
		public RacerComponent racer;
	}

}
