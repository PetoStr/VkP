package org.vkp.racing.ecs.component;

import java.util.BitSet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
public class CheckpointComponent implements Component {

	public static final BitSet ID = new BitSet();
	static {
		ID.set(7);
	}

	@Getter
	@Setter
	private CheckpointComponent nextCheckpoint;

	@Override
	public BitSet getId() {
		return ID;
	}

}
