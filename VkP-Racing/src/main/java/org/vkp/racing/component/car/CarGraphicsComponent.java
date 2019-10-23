package org.vkp.racing.component.car;

import org.vkp.engine.mesh.TexturedMesh;
import org.vkp.racing.Assets;
import org.vkp.racing.GameRenderer;
import org.vkp.racing.component.GraphicsComponent;
import org.vkp.racing.entity.Entity;

public class CarGraphicsComponent implements GraphicsComponent {

	private TexturedMesh texturedMesh;

	public CarGraphicsComponent(Assets assets) {
		texturedMesh = assets.getRedCar();
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
