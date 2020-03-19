package org.vkp.racing.ecs.component;

import java.util.BitSet;

import lombok.Getter;
import lombok.Setter;

public class CarAiComponent implements Component {

	public static final BitSet ID = new BitSet();
	static {
		ID.set(5);
	}

	@Getter
	@Setter
	private long startTime;

	@Getter
	@Setter
	private int currentAngleIndex;

	@Getter
	@Setter
	private boolean isDelay;

	@Override
	public BitSet getId() {
		return ID;
	}

}
