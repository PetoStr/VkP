package org.vkp.racing;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.vkp.engine.loader.ShapeLoader;
import org.vkp.engine.loader.ShapeType;
import org.vkp.engine.mesh.TexturedMesh;
import org.vkp.engine.texture.Color;
import org.vkp.racing.ecs.component.BarrierComponent;
import org.vkp.racing.ecs.component.CheckpointComponent;
import org.vkp.racing.ecs.component.TexturedMeshComponent;
import org.vkp.racing.ecs.component.Transform;
import org.vkp.racing.scene.Scene;

import lombok.Getter;

public class RaceTrackCreator {

	private Scene scene;

	private Assets assets;

	private final List<Transform> roadTransforms = new ArrayList<>();
	private final List<Transform> roadCornerTransforms = new ArrayList<>();

	private CheckpointComponent previousCheckpoint;

	@Getter
	private CheckpointComponent firstCheckpoint;

	public RaceTrackCreator(Scene scene, Assets assets) {
		this.scene = scene;
		this.assets = assets;
	}

	public void createRaceTrack() {
		ShapeLoader shapeLoader = assets.getShapeLoader();
		Color lightGrey = new Color(211, 211, 211);
		TexturedMesh wallTexturedMesh = shapeLoader.load(ShapeType.QUAD, lightGrey);

		prepareRoadTransforms();
		prepareRoadCornerTransforms();

		roadTransforms.forEach(transform -> createRoad(transform, wallTexturedMesh,
				wallTexturedMesh));
		roadCornerTransforms.forEach(transform -> createRoadCorner(transform, wallTexturedMesh));

		roadTransforms.clear();
		roadCornerTransforms.clear();
	}

	private void prepareRoadTransforms() {
		Transform transform = new Transform();
		transform.setWidth(80.0f);
		transform.setHeight(200.0f);

		transform.setRotation((float) (Math.PI / 2));
		for (int i = 0; i < 3; i++) {
			transform.setPosition(new Vector3f(240.0f + i * 200.0f, 260.0f, 0.0f));
			roadTransforms.add(copyTransform(transform));
		}

		transform.setRotation((float) Math.PI);
		transform.setPosition(new Vector3f(780.0f, 400.0f, 0.0f));
		roadTransforms.add(copyTransform(transform));

		transform.setRotation((float) -(Math.PI / 2));
		for (int i = 2; i >= 0; i--) {
			transform.setPosition(new Vector3f(240.0f + i * 200.0f, 540.0f, 0.0f));
			roadTransforms.add(copyTransform(transform));
		}

		transform.setRotation(0.0f);
		transform.setPosition(new Vector3f(100.0f, 400.0f, 0.0f));
		roadTransforms.add(copyTransform(transform));
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

	private void createRoad(Transform transform, TexturedMesh wallTexturedMesh,
			TexturedMesh checkpointTexturedMesh) {
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

		Transform currentCheckpointTransform = new Transform();
		currentCheckpointTransform.setParent(roadTransform);
		currentCheckpointTransform.setWidth(1.0f);
		currentCheckpointTransform.setHeight(0.02f);

		Transform otherCheckpointTransform = new Transform();
		otherCheckpointTransform.setParent(roadTransform);
		otherCheckpointTransform.setWidth(1.0f);
		otherCheckpointTransform.setHeight(0.02f);
		otherCheckpointTransform.setPosition(new Vector3f(0.0f, -0.49f, 0.0f));

		roadTransform.getChildren().add(leftWallTransform);
		roadTransform.getChildren().add(rightWallTransform);
		roadTransform.getChildren().add(currentCheckpointTransform);
		roadTransform.getChildren().add(otherCheckpointTransform);

		CheckpointComponent otherCheckpoint = new CheckpointComponent(firstCheckpoint);
		CheckpointComponent currentCheckpoint = new CheckpointComponent(otherCheckpoint);
		if (previousCheckpoint != null) {
			previousCheckpoint.setNextCheckpoint(currentCheckpoint);
		}
		previousCheckpoint = otherCheckpoint;
		if (firstCheckpoint == null) {
			firstCheckpoint = currentCheckpoint;
		}

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

		scene.createEntity()
			.with(new TexturedMeshComponent(checkpointTexturedMesh))
			.with(currentCheckpointTransform)
			.with(currentCheckpoint)
			.build();

		scene.createEntity()
			.with(new TexturedMeshComponent(checkpointTexturedMesh))
			.with(otherCheckpointTransform)
			.with(otherCheckpoint)
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
