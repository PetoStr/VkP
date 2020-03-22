package org.vkp.racing.math;

import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector2f;
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

	public static boolean testPolygonPolygonIntersection(Polygon a, Polygon b) {
		for (int x = 0; x < 2; x++) {
			Polygon cur = x == 0 ? a : b;
			List<Vector2f> points = cur.getPoints();

			for (int i1 = 0; i1 < points.size(); i1++) {
				int i2 = (i1 + 1) % points.size();
				Vector2f p1 = points.get(i1);
				Vector2f p2 = points.get(i2);

				Vector2f normal = new Vector2f(p2.y - p1.y, p1.x - p2.x);

				float minA = Float.POSITIVE_INFINITY;
				float maxA = Float.NEGATIVE_INFINITY;

				for (Vector2f p : a.getPoints()) {
					float projected = normal.x * p.x + normal.y * p.y;

					if (projected < minA) minA = projected;
					if (projected > maxA) maxA = projected;
				}

				float minB = Float.POSITIVE_INFINITY;
				float maxB = Float.NEGATIVE_INFINITY;

				for (Vector2f p : b.getPoints()) {
					float projected = normal.x * p.x + normal.y * p.y;

					if (projected < minB) minB = projected;
					if (projected > maxB) maxB = projected;
				}

				if (maxA < minB || maxB < minA) return false;
			}
		}

		return true;
	}

}
