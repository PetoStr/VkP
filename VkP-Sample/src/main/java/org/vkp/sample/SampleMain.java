package org.vkp.sample;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.vkp.engine.VkBase;
import org.vkp.engine.model.Model;
import org.vkp.engine.model.ShapeLoader;
import org.vkp.engine.model.ShapeType;
import org.vkp.engine.renderer.ShapeRenderer;
import org.vkp.engine.renderer.TextRenderer;
import org.vkp.engine.texture.Color;
import org.vkp.engine.window.Window;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class SampleMain {

	private Window window;

	private ShapeRenderer shapeRenderer;
	private TextRenderer textRenderer;

	private VkBase vkBase;

	private List<Block> blocks = new ArrayList<>();

	private Block blockA;
	private Block blockB;
	private Block wall;

	private Block listenCollisionBlock;

	private int fps;
	private int collisions;

	public void start() {
		window = new Window();
		window.createWindow();

		vkBase = new VkBase();
		vkBase.init(window);

		shapeRenderer = new ShapeRenderer(vkBase);
		shapeRenderer.init();

		textRenderer = new TextRenderer(vkBase);
		textRenderer.init();

		createBlocks();

		window.show();

		loop();

		vkBase.waitDevice();

		textRenderer.cleanup();
		shapeRenderer.cleanup();
		vkBase.cleanup();
		window.destroyWindow();
	}

	private void createBlocks() {
		ShapeLoader modelLoader = vkBase.getModelLoader();

		float ratio = (float) window.getWidth() / window.getHeight();

		Model modelA = modelLoader.load(ShapeType.RECTANGLE, "images/grass.png", shapeRenderer);
		modelA.setPosition(new Vector3f(0.0f, 0.1f, 0.0f));
		modelA.setWidth(0.8f);
		modelA.setHeight(0.8f);
		blockA = new Block(modelA, 1.0f, 0.0f);
		blockA.setX(modelA.getPosition().x);
		blocks.add(blockA);

		Model modelB = modelLoader.load(ShapeType.RECTANGLE, "images/texture.png", shapeRenderer);
		modelB.setPosition(new Vector3f(1.1f, 0.0f, 0.0f));
		blockB = new Block(modelB, 100e4, -0.5f);
		blockB.setX(modelB.getPosition().x);
		blocks.add(blockB);

		Color color = new Color((byte) 255, (byte) 255, (byte) 255, (byte) 255);
		Model modelWall = modelLoader.load(ShapeType.RECTANGLE, color, shapeRenderer);
		modelWall.setPosition(new Vector3f(-ratio / 1.2f, 0.0f, 0.0f));
		modelWall.setWidth(0.1f);
		modelWall.setHeight(1.0f);
		wall = new Block(modelWall, Float.POSITIVE_INFINITY, 0.0f);
		wall.setX(modelWall.getPosition().x);
		blocks.add(wall);

		listenCollisionBlock = blockB;
	}

	private void loop() {
		int frames = 0;
		long frameStartTime = System.nanoTime();
		long startTime = System.nanoTime();
		while (!window.shouldClose()) {
			long now = System.nanoTime();
			double frameTime = (now - frameStartTime) / 1e9;
			frameStartTime = now;

			window.update();
			final int parts = 10000;
			double partFrameTime = frameTime / parts;
			for (int i = 0; i < parts; i++) {
				if (handleCollision()) {
					collisions++;
					log.info("Collision count: " + collisions);
				}
				for (Block block : blocks) {
					block.update(partFrameTime);
				}
			}

			if (!vkBase.beginFrame()) {
				continue;
			}

			shapeRenderer.begin();
			for (Block block : blocks) {
				shapeRenderer.recordCommands(block.getModel());
			}
			shapeRenderer.end();

			textRenderer.begin();
			textRenderer.addText("FPS: " + fps, -1.0f, -1.0f, 0.5f);
			textRenderer.addText("Collisions: " + collisions, -0.2f, -1.0f, 0.5f);
			textRenderer.addText(blockA.getMass() + " kg", -1.0f, -0.9f, 0.5f);
			textRenderer.addText(String.valueOf(blockA.getSpeed()), -1.0f, -0.8f, 0.5f);
			textRenderer.addText(blockB.getMass() + " kg", 0.4f, -0.9f, 0.5f);
			textRenderer.addText(String.valueOf(blockB.getSpeed()), 0.4f, -0.8f, 0.5f);
			textRenderer.addText("TEST", 0.0f, 0.0f, 1.0f);
			textRenderer.end();

			vkBase.submitFrame();
			frames++;
			long diffTime = now - startTime;
			if (diffTime >= 1e9) {
				fps = (int) ((float) frames * (1e9 / diffTime));
				frames = 0;
				startTime = System.nanoTime();
			}
		}
	}

	private boolean handleCollision() {
		if (listenCollisionBlock.equals(wall) && blockA.intersects(wall)) {
			blockA.setSpeed(Math.abs(blockA.getSpeed()));
			double dist = Math.abs(blockA.getX() - wall.getX() - wall.getModel().getWidth());
			blockA.setX(wall.getX() + wall.getModel().getWidth() + dist);

			listenCollisionBlock = blockB;

			return true;
		}

		if (!listenCollisionBlock.equals(blockB) || !blockA.intersects(blockB)) {
			return false;
		}

		double v1 = blockA.getSpeed();
		double v2 = blockB.getSpeed();
		double m1 = blockA.getMass();
		double m2 = blockB.getMass();

		double nv1 = ((m1 - m2) / (m1 + m2)) * v1 + (2 * m2 / (m1 + m2)) * v2;
		double nv2 = ((m2 - m1) / (m1 + m2)) * v2 + (2 * m1 / (m1 + m2)) * v1;

		blockA.setSpeed(nv1);
		blockB.setSpeed(nv2);

		listenCollisionBlock = wall;

		return true;
	}

	public static void main(String[] args) {
		try {
			new SampleMain().start();
		} catch (Exception e) {
			log.fatal("", e);
		}
	}

}
