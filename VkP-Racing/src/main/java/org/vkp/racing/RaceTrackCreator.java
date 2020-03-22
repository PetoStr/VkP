package org.vkp.racing;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.vkp.engine.loader.ShapeLoader;
import org.vkp.engine.loader.ShapeType;
import org.vkp.engine.mesh.TexturedMesh;
import org.vkp.engine.texture.Color;
import org.vkp.racing.ecs.component.BarrierComponent;
import org.vkp.racing.ecs.component.TexturedMeshComponent;
import org.vkp.racing.ecs.component.Transform;
import org.vkp.racing.scene.Scene;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RaceTrackCreator {

	private Scene scene;

	private Assets assets;

	private final List<Transform> roadTransforms = new ArrayList<>();
	private final List<Transform> roadCornerTransforms = new ArrayList<>();

	public void createRaceTrack() {
		ShapeLoader shapeLoader = assets.getShapeLoader();
		Color lightGrey = new Color(211, 211, 211);
		TexturedMesh wallTexturedMesh = shapeLoader.load(ShapeType.QUAD, lightGrey);

		prepareRoadTransforms();
		prepareRoadCornerTransforms();

		roadTransforms.forEach(transform -> createRoad(transform, wallTexturedMesh));
		roadCornerTransforms.forEach(transform -> createRoadCorner(transform, wallTexturedMesh));

		roadTransforms.clear();
		roadCornerTransforms.clear();
	}

	private void prepareRoadTransforms() {
		Transform transform = new Transform();
		transform.setWidth(80.0f);
		transform.setHeight(200.0f);

		transform.setPosition(new Vector3f(100.0f, 400.0f, 0.0f));
		roadTransforms.add(copyTransform(transform));

		transform.setPosition(new Vector3f(780.0f, 400.0f, 0.0f));
		roadTransforms.add(copyTransform(transform));

		transform.setRotation((float) (Math.PI / 2));
		for (int i = 0; i < 3; i++) {
			transform.setPosition(new Vector3f(240.0f + i * 200.0f, 260.0f, 0.0f));
			roadTransforms.add(copyTransform(transform));
			transform.setPosition(new Vector3f(240.0f + i * 200.0f, 540.0f, 0.0f));
			roadTransforms.add(copyTransform(transform));
		}
	}

	private void prepareRoadCornerTransforms() {
		Transform transform = new Transform();
		transform.setWidth(80.0f);
		transform.setHeight(80.0f);

		float[] x = { 100.0f, 780.0f, 100.0f, 780.0f };
		float[] y = { 260.0f, 260.0f, 540.0f, 540.0f };
		float[] rotation = { 0.0f, (float) (Math.PI / 2), (float) -(Math.PI / 2), (float) Math.PI };

		for (int i = 0; i < 4; i++) {
			transform.setRotation(rotation[i]);
			transform.setPosition(new Vector3f(x[i], y[i], 0.0f));
			roadCornerTransforms.add(copyTransform(transform));
		}
	}

	private Transform copyTransform(Transform transform) {
		Transform res = new Transform();
		res.setPosition(transform.getPosition());
		res.setRotation(transform.getRotation());
		res.setWidth(transform.getWidth());
		res.setHeight(transform.getHeight());

		return res;
	}

	private void createRoad(Transform transform, TexturedMesh wallTexturedMesh) {
		Transform roadTransform = copyTransform(transform);
		Transform leftWallTransform = new Transform();
		leftWallTransform.setParent(roadTransform);
		leftWallTransform.setPosition(new Vector3f(-0.5f, 0.0f, 0.0f));
		leftWallTransform.setWidth(0.05f);
		leftWallTransform.setHeight(1.0f);

		Transform rightWallTransform = new Transform();
		rightWallTransform.setParent(roadTransform);
		rightWallTransform.setPosition(new Vector3f(0.5f, 0.0f, 0.0f));
		rightWallTransform.setWidth(0.05f);
		rightWallTransform.setHeight(1.0f);

		roadTransform.getChildren().add(leftWallTransform);
		roadTransform.getChildren().add(rightWallTransform);

		scene.createEntity()
			.with(new TexturedMeshComponent(assets.getRoad()))
			.with(roadTransform)
			.build();

		scene.createEntity()
			.with(new TexturedMeshComponent(wallTexturedMesh))
			.with(leftWallTransform)
			.with(new BarrierComponent())
			.build();

		scene.createEntity()
			.with(new TexturedMeshComponent(wallTexturedMesh))
			.with(rightWallTransform)
			.with(new BarrierComponent())
			.build();
	}

	private void createRoadCorner(Transform transform, TexturedMesh wallTexturedMesh) {
		Transform roadTransform = copyTransform(transform);

		Transform wallTransform = new Transform();
		wallTransform.setParent(roadTransform);
		wallTransform.setRotation((float) (Math.PI / 4.0f));
		wallTransform.setWidth(0.05f);
		wallTransform.setHeight(1.4142f);

		roadTransform.getChildren().add(wallTransform);

		scene.createEntity()
			.with(new TexturedMeshComponent(assets.getRoadCorner()))
			.with(roadTransform)
			.build();

		scene.createEntity()
			.with(new TexturedMeshComponent(wallTexturedMesh))
			.with(wallTransform)
			.with(new BarrierComponent())
			.build();
	}

}
