package org.vkp.racing.ecs.component;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.vkp.racing.math.MathUtil;
import org.vkp.racing.math.Polygon;

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

	public boolean intersects(Transform other) {
		Matrix4f thisModelMatrix = getModelMatrix();
		Matrix4f otherModelMatrix = other.getModelMatrix();
		Vector4f[] thisCorners = {
				new Vector4f(-0.5f, 0.5f, 0.0f, 1.0f).mul(thisModelMatrix),
				new Vector4f(-0.5f, -0.5f, 0.0f, 1.0f).mul(thisModelMatrix),
				new Vector4f(0.5f, -0.5f, 0.0f, 1.0f).mul(thisModelMatrix),
				new Vector4f(0.5f, 0.5f, 0.0f, 1.0f).mul(thisModelMatrix),
		};
		Vector4f[] otherCorners = {
				new Vector4f(-0.5f, 0.5f, 0.0f, 1.0f).mul(otherModelMatrix),
				new Vector4f(-0.5f, -0.5f, 0.0f, 1.0f).mul(otherModelMatrix),
				new Vector4f(0.5f, -0.5f, 0.0f, 1.0f).mul(otherModelMatrix),
				new Vector4f(0.5f, 0.5f, 0.0f, 1.0f).mul(otherModelMatrix),
		};

		Polygon a = new Polygon();
		Polygon b = new Polygon();
		for (int i = 0; i < thisCorners.length; i++) {
			a.getPoints().add(new Vector2f(thisCorners[i].x, thisCorners[i].y));
			b.getPoints().add(new Vector2f(otherCorners[i].x, otherCorners[i].y));
		}

		return MathUtil.testPolygonPolygonIntersection(a, b);
	}

	@Override
	public BitSet getId() {
		return ID;
	}

}
