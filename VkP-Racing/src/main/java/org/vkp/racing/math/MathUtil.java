package org.vkp.racing.math;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class MathUtil {

	private MathUtil() {

	}

	public static Matrix4f calculateModelMatrix(Vector3f position, float rotation,
			float width, float height) {
		Matrix4f mMatrix = new Matrix4f();
		mMatrix.translation(position);
		mMatrix.rotate(rotation, 0.0f, 0.0f, 1.0f);
		mMatrix.scaleXY(width, height);
		return mMatrix;
	}

}
