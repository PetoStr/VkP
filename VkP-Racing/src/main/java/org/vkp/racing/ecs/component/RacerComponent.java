package org.vkp.racing.ecs.component;

import java.util.BitSet;

import lombok.Getter;
import lombok.Setter;

public class RacerComponent implements Component {

	public static final BitSet ID = new BitSet();
	static {
		ID.set(8);
	}

	@Getter
	@Setter
	private CheckpointComponent nextCheckpoint;

	@Getter
	private float score;

	private long checkpointStartTime;

	public RacerComponent(CheckpointComponent nextCheckpoint) {
		this.nextCheckpoint = nextCheckpoint;
		checkpointStartTime = System.nanoTime();
	}

	public void passCheckpoint(CheckpointComponent checkpoint) {
		long now = System.nanoTime();
		float div = (float) ((now - checkpointStartTime) / 1e9);
		score += 1 / div;
		checkpointStartTime = now;
		nextCheckpoint = checkpoint.getNextCheckpoint();
	}

	@Override
	public BitSet getId() {
		return ID;
	}

}
