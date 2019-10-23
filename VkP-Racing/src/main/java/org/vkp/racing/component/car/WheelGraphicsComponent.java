package org.vkp.racing.component.car;

import org.vkp.engine.loader.ShapeLoader;
import org.vkp.engine.loader.ShapeType;
import org.vkp.engine.mesh.TexturedMesh;
import org.vkp.engine.texture.Color;
import org.vkp.racing.Assets;
import org.vkp.racing.GameRenderer;
import org.vkp.racing.component.GraphicsComponent;
import org.vkp.racing.entity.Entity;

public class WheelGraphicsComponent implements GraphicsComponent {

	private TexturedMesh texturedMesh;

	public WheelGraphicsComponent(Assets assets) {
		ShapeLoader shapeLoader = assets.getShapeLoader();
		Color white = new Color(255, 255, 255);
		texturedMesh = shapeLoader.load(ShapeType.QUAD, white);
	}

	@Override
	public void draw(Entity entity, GameRenderer renderer) {
		renderer.drawEntity(entity);
	}

	@Override
	public TexturedMesh getTexturedMesh() {
		return texturedMesh;
	}

}
