package org.vkp.racing.ecs.system;

import java.util.BitSet;
import java.util.List;

import org.vkp.racing.ecs.component.Component;

public interface GameSystem {

	default void prepare() {

	}

	void update(List<Component> components);

	default void finish() {

	}

	BitSet getRequiredComponents();

}
