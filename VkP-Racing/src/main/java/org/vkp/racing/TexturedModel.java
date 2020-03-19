package org.vkp.racing;

import org.joml.Matrix4f;
import org.vkp.engine.mesh.TexturedMesh;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TexturedModel {

	private TexturedMesh texturedMesh;
	private Matrix4f modelMatrix;

}
