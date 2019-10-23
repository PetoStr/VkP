package org.vkp.racing;

import org.vkp.engine.loader.ShapeLoader;
import org.vkp.engine.loader.ShapeType;
import org.vkp.engine.mesh.TexturedMesh;

import lombok.Getter;

@Getter
public class Assets {

	private ShapeLoader shapeLoader;

	private TexturedMesh redCar;

	public Assets(ShapeLoader shapeLoader) {
		this.shapeLoader = shapeLoader;
		redCar = shapeLoader.load(ShapeType.QUAD, "textures/red-car.png");
	}

}
