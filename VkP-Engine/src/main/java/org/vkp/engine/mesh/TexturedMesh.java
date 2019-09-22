package org.vkp.engine.mesh;

import org.vkp.engine.texture.Texture;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TexturedMesh {

	private Mesh mesh;
	private Texture texture;

}
