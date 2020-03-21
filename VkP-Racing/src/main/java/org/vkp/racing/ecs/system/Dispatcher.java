package org.vkp.racing.ecs.system;

import java.util.BitSet;
import java.util.List;

import org.vkp.racing.scene.Scene;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Builder
public class Dispatcher {

	@Singular
	@Getter
	private List<GameSystem> gameSystems;

	@Singular
	@Getter
	private List<GameSystem> renderSystems;

	public Dispatcher(List<GameSystem> gameSystems, List<GameSystem> renderSystems) {
		this.gameSystems = gameSystems;
		this.renderSystems = renderSystems;
	}

	private void dispatch(Scene scene, List<GameSystem> systems) {
		scene.getEntities().forEach((entity, components) -> {
			BitSet componentIds = new BitSet();
			components.forEach(component -> componentIds.or(component.getId()));

			systems.forEach(system -> {
				BitSet conjuction = system.getRequiredComponents();
				conjuction.and(componentIds);
				if (conjuction.equals(system.getRequiredComponents())) {
					system.update(components);
				}
			});
		});
	}

	public void dispatchGameSystems(Scene scene) {
		dispatch(scene, gameSystems);
	}

	public void dispatchRenderSystems(Scene scene) {
		dispatch(scene, renderSystems);
	}

	public void prepareSystems() {
		gameSystems.forEach(GameSystem::prepare);
		renderSystems.forEach(GameSystem::prepare);
	}

	public void finishSystems() {
		gameSystems.forEach(GameSystem::finish);
		renderSystems.forEach(GameSystem::finish);
	}

}
