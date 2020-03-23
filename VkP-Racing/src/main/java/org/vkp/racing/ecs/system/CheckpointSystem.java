package org.vkp.racing.ecs.system;

import java.util.BitSet;
import java.util.List;

import org.vkp.racing.ecs.component.CheckpointComponent;
import org.vkp.racing.ecs.component.Component;
import org.vkp.racing.ecs.component.RacerComponent;
import org.vkp.racing.ecs.component.Transform;
import org.vkp.racing.scene.Scene;

public class CheckpointSystem implements GameSystem {

	private Scene scene;

	public CheckpointSystem(Scene scene) {
		this.scene = scene;
	}

	@Override
	public void update(List<Component> components) {
		CarComponentGroup carGroup = new CarComponentGroup();
		ComponentExtractor.extract(carGroup, components);

		List<List<Component>> checkpoints = scene.getRequiredEntities(CheckpointComponent.ID);

		checkpoints.forEach(checkpoint -> {
			CheckpointComponentGroup checkpointGroup = new CheckpointComponentGroup();
			ComponentExtractor.extract(checkpointGroup, checkpoint);

			handleCheckpoint(checkpointGroup, carGroup);
		});
	}

	@Override
	public BitSet getRequiredComponents() {
		BitSet bitSet = new BitSet();
		bitSet.or(Transform.ID);
		bitSet.or(RacerComponent.ID);
		return bitSet;
	}

	private void handleCheckpoint(CheckpointComponentGroup checkpointGroup,
			CarComponentGroup carGroup) {
		if (carGroup.racer.getNextCheckpoint() != checkpointGroup.checkpoint) return;

		if (carGroup.transform.intersects(checkpointGroup.transform)) {
			carGroup.racer.passCheckpoint(checkpointGroup.checkpoint);
		}
	}

	private static class CarComponentGroup {
		public Transform transform;
		public RacerComponent racer;
	}

	private static class CheckpointComponentGroup {
		public Transform transform;
		public CheckpointComponent checkpoint;
	}

}
