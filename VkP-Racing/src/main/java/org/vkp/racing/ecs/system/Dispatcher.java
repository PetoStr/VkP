package org.vkp.racing.ecs.system;

import java.util.BitSet;
import java.util.List;

import org.vkp.racing.ecs.component.Component;
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
		systems.forEach(system -> {
			BitSet requiredComponents = system.getRequiredComponents();
			List<List<Component>> entities = scene.getRequiredEntities(requiredComponents);
			entities.forEach(system::update);
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
