package org.vkp.racing.ecs.component;

import java.util.BitSet;

public class CarKeyboardComponent implements Component {

	public static final BitSet ID = new BitSet();
	static {
		ID.set(4);
	}

	@Override
	public BitSet getId() {
		return ID;
	}

}
