package org.vkp.racing.ecs.system;

import java.util.BitSet;
import java.util.List;

import org.joml.Intersectionf;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.vkp.racing.ecs.component.BarrierComponent;
import org.vkp.racing.ecs.component.Component;
import org.vkp.racing.ecs.component.RayComponent;
import org.vkp.racing.ecs.component.Transform;
import org.vkp.racing.math.Polygon;
import org.vkp.racing.scene.Scene;

public class BarrierDistanceSystem implements GameSystem {

	private Scene scene;

	public BarrierDistanceSystem(Scene scene) {
		this.scene = scene;
	}

	@Override
	public void update(List<Component> components) {
		ComponentGroup group = new ComponentGroup();
		ComponentExtractor.extract(group, components);
		Transform transform = group.transform;

		BitSet bitSet = new BitSet();
		bitSet.or(BarrierComponent.ID);
		bitSet.or(Transform.ID);
		List<List<Component>> barriers = scene.getRequiredEntities(bitSet);

		Matrix4f modelMatrix = transform.getModelMatrix();
		Vector3f position = new Vector3f();
		Vector3f rotation = new Vector3f();
		modelMatrix.getTranslation(position);
		modelMatrix.getEulerAnglesZYX(rotation);

		float bestDist = Float.POSITIVE_INFINITY;
		Vector2f origin = new Vector2f(position.x, position.y);
		Vector2f direction = group.ray.getDirection(rotation.z);

		for (List<Component> barrier : barriers) {
			Transform barrierTransform =
					(Transform) ComponentExtractor.extractOne(Transform.class, barrier);

			Vector2f point = new Vector2f();
			if (checkIntersection(origin, direction, barrierTransform, point)) {
				float dist = point.distance(origin.x, origin.y);
				if (dist < bestDist) {
					bestDist = dist;
				}
			}
		}

		if (bestDist != Float.POSITIVE_INFINITY) {
			float scaleA = (float) Math.abs(30 / Math.cos(transform.getRotation()));
			float scaleB = (float) Math.abs(15 / Math.sin(transform.getRotation()));
			group.transform.setWidth(bestDist / Math.min(scaleA, scaleB));
		}

		group.ray.setMeasuredDistance(bestDist);
	}

	private boolean checkIntersection(Vector2f origin, Vector2f direction,
			Transform barrierTransform, Vector2f point) {
		Polygon barrierPolygon = barrierTransform.getPolygon();
		Vector2f[] vertices = barrierPolygon.getPoints().toArray(new Vector2f[0]);

		int idx = Intersectionf.intersectPolygonRay(vertices, origin.x, origin.y,
				direction.x, direction.y, point);

		return idx != -1;
	}

	@Override
	public BitSet getRequiredComponents() {
		BitSet bitSet = new BitSet();
		bitSet.or(Transform.ID);
		bitSet.or(RayComponent.ID);
		return bitSet;
	}

	private static class ComponentGroup {
		public Transform transform;
		public RayComponent ray;
	}

}
