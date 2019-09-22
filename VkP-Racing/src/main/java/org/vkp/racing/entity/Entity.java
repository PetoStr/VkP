package org.vkp.racing.entity;

import org.joml.Matrix4f;
import org.vkp.engine.mesh.TexturedMesh;
import org.vkp.racing.GameRenderer;
import org.vkp.racing.Scene;

public interface Entity {

	void update(Scene scene);

	void draw(GameRenderer renderer);

	Matrix4f getModelMatrix();

	TexturedMesh getTexturedMesh();

}
