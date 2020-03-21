package org.vkp.racing.ecs.component;

import java.util.BitSet;

public class BarrierComponent implements Component {

	public static final BitSet ID = new BitSet();
	static {
		ID.set(6);
	}

	@Override
	public BitSet getId() {
		return ID;
	}

}
