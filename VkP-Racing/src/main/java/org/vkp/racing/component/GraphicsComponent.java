package org.vkp.racing.component;

import org.vkp.engine.mesh.TexturedMesh;
import org.vkp.racing.GameRenderer;
import org.vkp.racing.entity.Entity;

public interface GraphicsComponent {

	void draw(Entity entity, GameRenderer renderer);

	TexturedMesh getTexturedMesh();

}
