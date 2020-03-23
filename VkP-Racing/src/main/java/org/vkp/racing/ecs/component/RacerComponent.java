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
	private int score;

	public RacerComponent(CheckpointComponent nextCheckpoint) {
		this.nextCheckpoint = nextCheckpoint;
	}

	public void passCheckpoint(CheckpointComponent checkpoint) {
		nextCheckpoint = checkpoint.getNextCheckpoint();
		score++;
	}

	@Override
	public BitSet getId() {
		return ID;
	}

}
