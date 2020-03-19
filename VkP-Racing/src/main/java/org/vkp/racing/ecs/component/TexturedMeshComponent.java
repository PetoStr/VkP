package org.vkp.racing.ecs.component;

import java.util.BitSet;

import org.vkp.engine.mesh.TexturedMesh;

import lombok.Getter;

public class TexturedMeshComponent implements Component {

	public static final BitSet ID = new BitSet();
	static {
		ID.set(2);
	}

	@Getter
	private TexturedMesh texturedMesh;

	public TexturedMeshComponent(TexturedMesh texturedMesh) {
		this.texturedMesh = texturedMesh;
	}

	@Override
	public BitSet getId() {
		return ID;
	}

}
