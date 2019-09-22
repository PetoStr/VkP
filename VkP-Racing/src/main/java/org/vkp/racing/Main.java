package org.vkp.racing;

import org.joml.Vector3f;
import org.vkp.engine.VkBase;
import org.vkp.engine.window.Window;
import org.vkp.racing.entity.Car;
import org.vkp.racing.entity.Entity;

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
		Car car = new Car(assets);
		car.setPosition(new Vector3f(400.0f, 200.0f, 0.0f));
		scene.getEntities().add(car);
		window.getKeyCallback().registerListener(car);
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

			scene.getCamera().update();
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
