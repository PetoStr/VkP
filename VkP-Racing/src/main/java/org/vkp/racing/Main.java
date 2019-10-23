package org.vkp.racing;

import org.joml.Vector3f;
import org.vkp.engine.VkBase;
import org.vkp.engine.texture.TextureInfo;
import org.vkp.engine.window.Window;
import org.vkp.racing.component.NullInputComponent;
import org.vkp.racing.component.NullPhysicsComponent;
import org.vkp.racing.component.car.CarGraphicsComponent;
import org.vkp.racing.component.car.CarPhysicsComponent;
import org.vkp.racing.component.car.KeyboardInputComponent;
import org.vkp.racing.component.car.WheelGraphicsComponent;
import org.vkp.racing.entity.Entity;
import org.vkp.racing.entity.Transform;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class Main {

	private static final int UPS = 60;
	private static final float MS_PER_UPDATE = 1000.0f / UPS;

	private Window window;

	private VkBase vkBase;

	private Scene scene;

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

		window.show();

		loop();

		vkBase.waitDevice();

		gameRenderer.cleanup();
		vkBase.cleanup();
		window.destroyWindow();
	}

	private void createEntities() {
		CarGraphicsComponent carGraphicsComponent = new CarGraphicsComponent(assets);
		CarPhysicsComponent carPhysicsComponent = new CarPhysicsComponent();
		KeyboardInputComponent keyboardInputComponent =
				new KeyboardInputComponent(carPhysicsComponent, window.getKeyCallback());
		Transform carTransform = new Transform();
		TextureInfo textureInfo =
				carGraphicsComponent.getTexturedMesh().getTexture().getTextureInfo();
		carTransform.setPosition(new Vector3f(400.0f, 200.0f, 0.0f));
		carTransform.setWidth(textureInfo.getWidth() / 20.0f);
		carTransform.setHeight(textureInfo.getHeight() / 20.0f);
		Entity car = new Entity(carTransform, carGraphicsComponent, carPhysicsComponent,
				keyboardInputComponent);

		float wheelWidth = carTransform.getWidth() / 150;
		float wheelHeight = carTransform.getHeight() / 100;
		Transform leftWheelTransform = new Transform();
		leftWheelTransform.setPosition(new Vector3f(0.3f, -0.36f, 0.0f));
		leftWheelTransform.setWidth(wheelWidth);
		leftWheelTransform.setHeight(wheelHeight);
		leftWheelTransform.setParent(carTransform);
		carTransform.getChildren().add(leftWheelTransform);
		Entity leftWheel = new Entity(leftWheelTransform,
				new WheelGraphicsComponent(assets),
				new NullPhysicsComponent(),
				new NullInputComponent());
		scene.getEntities().add(leftWheel);

		Transform rightWheelTransform = new Transform();
		rightWheelTransform.setPosition(new Vector3f(0.3f, 0.36f, 0.0f));
		rightWheelTransform.setWidth(wheelWidth);
		rightWheelTransform.setHeight(wheelHeight);
		rightWheelTransform.setParent(carTransform);
		carTransform.getChildren().add(rightWheelTransform);
		Entity rightWheel = new Entity(rightWheelTransform, new WheelGraphicsComponent(assets),
				new NullPhysicsComponent(),
				new NullInputComponent());
		scene.getEntities().add(rightWheel);

		scene.getEntities().add(car);
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

			while (lag >= MS_PER_UPDATE) {
				for (Entity entity : scene.getEntities()) {
					entity.update(scene);
				}
				lag -= MS_PER_UPDATE;
			}
			for (Entity entity : scene.getEntities()) {
				entity.draw(gameRenderer);
			}

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
		new Main().start();
	}

}
