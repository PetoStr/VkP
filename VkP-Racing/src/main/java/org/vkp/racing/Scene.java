package org.vkp.racing;

import java.util.ArrayList;
import java.util.List;

import org.vkp.engine.Camera;
import org.vkp.racing.entity.Entity;

import lombok.Data;

@Data
public class Scene {

	private Camera camera;

	private List<Entity> entities = new ArrayList<>();

	public Scene() {
		camera = new Camera();
	}

}
