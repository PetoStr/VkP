package org.vkp.racing;

import org.joml.Vector3f;
import org.vkp.engine.VkBase;
import org.vkp.engine.loader.ShapeLoader;
import org.vkp.engine.loader.ShapeType;
import org.vkp.engine.mesh.TexturedMesh;
import org.vkp.engine.texture.Color;
import org.vkp.engine.texture.TextureInfo;
import org.vkp.engine.window.Window;
import org.vkp.racing.ecs.component.CarPhysicsComponent;
import org.vkp.racing.ecs.component.Component;
import org.vkp.racing.ecs.component.CarAiComponent;
import org.vkp.racing.ecs.component.CarKeyboardComponent;
import org.vkp.racing.ecs.component.TexturedMeshComponent;
import org.vkp.racing.ecs.component.Transform;
import org.vkp.racing.ecs.system.CarAiInput;
import org.vkp.racing.ecs.system.CarKeyboardInput;
import org.vkp.racing.ecs.system.CarMovementSystem;
import org.vkp.racing.ecs.system.Dispatcher;
import org.vkp.racing.ecs.system.RenderSystem;
import org.vkp.racing.scene.Scene;

public class Racing {

	private static final int UPS = 60;
	private static final float MS_PER_UPDATE = 1000.0f / UPS;

	private Window window;

	private VkBase vkBase;

	private Scene scene;

	private Dispatcher dispatcher;

	private GameRenderer gameRenderer;

	private Assets assets;

	private int fps;

	public void start() {
		window = new Window();
		window.createWindow();

		vkBase = new VkBase();
		vkBase.init(window);

		scene = new Scene();

		gameRenderer = new GameRenderer(vkBase);

		assets = new Assets(vkBase.getShapeLoader());

		createEntities();
		createGameSystems();

		window.show();

		loop();

		vkBase.waitDevice();

		gameRenderer.cleanup();
		vkBase.cleanup();
		window.destroyWindow();
	}

	private void createEntities() {
		TexturedMeshComponent carTexturedMesh = new TexturedMeshComponent(assets.getRedCar());
		TextureInfo textureInfo = carTexturedMesh.getTexturedMesh().getTexture().getTextureInfo();

		Transform carTransform = new Transform();
		carTransform.setPosition(new Vector3f(400.0f, 200.0f, 0.0f));
		carTransform.setWidth(textureInfo.getWidth() / 20.0f);
		carTransform.setHeight(textureInfo.getHeight() / 20.0f);

		ShapeLoader shapeLoader = assets.getShapeLoader();
		Color white = new Color(255, 255, 255);
		TexturedMesh wheelTexturedMesh = shapeLoader.load(ShapeType.QUAD, white);

		Transform randomAiCarTransform = new Transform();
		randomAiCarTransform.setPosition(new Vector3f(600.0f, 300.0f, 0.0f));
		randomAiCarTransform.setWidth(textureInfo.getWidth() / 20.0f);
		randomAiCarTransform.setHeight(textureInfo.getHeight() / 20.0f);

		createCar(carTransform, wheelTexturedMesh, new CarKeyboardComponent());
		createCar(randomAiCarTransform, wheelTexturedMesh, new CarAiComponent());
	}

	private void createCar(Transform carTransform, TexturedMesh wheelTexturedMesh,
			Component inputComponent) {
		float wheelWidth = carTransform.getWidth() / 150;
		float wheelHeight = carTransform.getHeight() / 100;
		Transform leftWheelTransform = new Transform();
		leftWheelTransform.setPosition(new Vector3f(0.3f, -0.36f, 0.0f));
		leftWheelTransform.setWidth(wheelWidth);
		leftWheelTransform.setHeight(wheelHeight);
		leftWheelTransform.setParent(carTransform);
		carTransform.getChildren().add(leftWheelTransform);

		Transform rightWheelTransform = new Transform();
		rightWheelTransform.setPosition(new Vector3f(0.3f, 0.36f, 0.0f));
		rightWheelTransform.setWidth(wheelWidth);
		rightWheelTransform.setHeight(wheelHeight);
		rightWheelTransform.setParent(carTransform);
		carTransform.getChildren().add(rightWheelTransform);

		scene.createEntity()
			.with(new TexturedMeshComponent(wheelTexturedMesh))
			.with(leftWheelTransform)
			.build();

		scene.createEntity()
			.with(new TexturedMeshComponent(wheelTexturedMesh))
			.with(rightWheelTransform)
			.build();

		scene.createEntity()
			.with(new TexturedMeshComponent(assets.getRedCar()))
			.with(carTransform)
			.with(new CarPhysicsComponent())
			.with(inputComponent)
			.build();
	}

	private void createGameSystems() {
		dispatcher = Dispatcher.builder()
				.gameSystem(new CarMovementSystem())
				.gameSystem(new CarKeyboardInput(window.getKeyCallback()))
				.gameSystem(new CarAiInput())
				.renderSystem(new RenderSystem(gameRenderer))
				.build();
	}

	private void loop() {
		int frames = 0;
		long frameStartTime = System.nanoTime();
		long startTime = System.nanoTime();
		float lag = 0.0f;
		while (!window.shouldClose()) {
			long now = System.nanoTime();
			float frameTime = (float) ((now - frameStartTime) / 1e6);
			frameTime = Math.min(frameTime, 500.0f);
			lag += frameTime;
			frameStartTime = now;

			window.update();
			scene.getCamera().update();

			dispatcher.prepare();
			while (lag >= MS_PER_UPDATE) {
				dispatcher.dispatchGameSystems(scene);
				lag -= MS_PER_UPDATE;
			}
			dispatcher.dispatchRenderSystems(scene);
			dispatcher.finish();

			gameRenderer.drawText("FPS: " + fps, -1.0f, -1.0f, 0.5f);
			gameRenderer.draw(scene.getCamera());
			frames++;
			long diffTime = now - startTime;
			if (diffTime >= 1e9) {
				fps = (int) ((float) frames * (1e9 / diffTime));
				frames = 0;
				startTime = System.nanoTime();
			}
		}
	}

	public static void main(String[] args) {
		new Racing().start();
	}

}
