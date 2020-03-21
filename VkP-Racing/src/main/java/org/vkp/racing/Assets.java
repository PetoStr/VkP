package org.vkp.racing;

import org.vkp.engine.loader.ShapeLoader;
import org.vkp.engine.loader.ShapeType;
import org.vkp.engine.mesh.TexturedMesh;

import lombok.Getter;

@Getter
public class Assets {

	private ShapeLoader shapeLoader;

	private TexturedMesh redCar;
	private TexturedMesh road;
	private TexturedMesh roadCorner;

	public Assets(ShapeLoader shapeLoader) {
		this.shapeLoader = shapeLoader;
		loadAssets();
	}

	private void loadAssets() {
		redCar = shapeLoader.load(ShapeType.QUAD, "textures/red-car.png");
		road = shapeLoader.load(ShapeType.QUAD, "textures/road.png");
		roadCorner = shapeLoader.load(ShapeType.QUAD, "textures/road-corner.png");
	}

}
