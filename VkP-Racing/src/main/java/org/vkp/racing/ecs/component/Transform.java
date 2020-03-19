package org.vkp.racing.ecs.component;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.vkp.racing.math.MathUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class Transform implements Component {

	public static final BitSet ID = new BitSet();
	static {
		ID.set(1);
	}

	@Getter
	@Setter
	private Vector3f position = new Vector3f();

	@Getter
	@Setter
	private float rotation;

	@Getter
	@Setter
	private float width;

	@Getter
	@Setter
	private float height;

	@Getter
	@Setter
	private Transform parent;

	@Getter
	@Setter
	private List<Transform> children = new ArrayList<>();

	public Matrix4f getModelMatrix() {
		// TODO use dirty flag
		Matrix4f mMatrix = MathUtil.calculateModelMatrix(position, rotation, width, height);
		if (parent != null) {
			mMatrix = new Matrix4f(parent.getModelMatrix()).mul(mMatrix);
		}
		return mMatrix;
	}

	@Override
	public BitSet getId() {
		return ID;
	}

}
