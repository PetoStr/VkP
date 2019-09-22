package org.vkp.sample;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.lwjgl.vulkan.VkExtent2D;
import org.vkp.engine.Camera;
import org.vkp.engine.VkBase;
import org.vkp.engine.loader.ShapeLoader;
import org.vkp.engine.loader.ShapeType;
import org.vkp.engine.mesh.TexturedMesh;
import org.vkp.engine.renderer.ShapeRenderer;
import org.vkp.engine.renderer.TextRenderer;
import org.vkp.engine.texture.Color;
import org.vkp.engine.window.Window;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class SampleMain {

	private static final double MS_PER_UPDATE = 0.001d;

	private Window window;

	private ShapeRenderer shapeRenderer;
	private TextRenderer textRenderer;

	private VkBase vkBase;

	private List<Block> blocks = new ArrayList<>();

	private Block blockA;
	private Block blockB;
	private Block wall;

	private Block listenCollisionBlock;

	private Camera camera;

	private int fps;
	private long collisions;

	public void start() {
		window = new Window();
		window.createWindow();

		vkBase = new VkBase();
		vkBase.init(window);

		shapeRenderer = vkBase.getShapeRenderer();

		textRenderer = vkBase.getTextRenderer();

		camera = new Camera();

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
		ShapeLoader shapeLoader = vkBase.getShapeLoader();

		TexturedMesh shapeA = shapeLoader.load(ShapeType.QUAD, "images/grass.png");
		Vector2d blockAPos = new Vector2d(window.getWidth() / 2.0f, window.getHeight() / 2.0f);
		blockA = new Block(shapeA, blockAPos, 320.0f, 320.0f, 1.0f, 0.0f);
		blocks.add(blockA);

		TexturedMesh shapeB = shapeLoader.load(ShapeType.QUAD, "images/texture.png");
		Vector2d blockBPos = new Vector2d(window.getWidth() * (5.0f / 6.0f),
				window.getHeight() / 2.0f - 20.0f);
		blockB = new Block(shapeB, blockBPos, 360.0f, 360.0f, 100e8, -0.0001f);
		blocks.add(blockB);

		Color color = new Color((byte) 255, (byte) 255, (byte) 255, (byte) 255);
		TexturedMesh shapeWall = shapeLoader.load(ShapeType.QUAD, color);
		Vector2d wallPos = new Vector2d(window.getWidth() * (1.0f / 10.0f),
				window.getHeight() / 2.0f - 40.0f);
		wall = new Block(shapeWall, wallPos, 40.0f, 400.0f, Float.POSITIVE_INFINITY, 0.0f);
		blocks.add(wall);

		listenCollisionBlock = blockB;
	}

	private void loop() {
		int frames = 0;
		long frameStartTime = System.nanoTime();
		long startTime = System.nanoTime();
		double lag = 0.0d;
		while (!window.shouldClose()) {
			long now = System.nanoTime();
			double frameTime = (now - frameStartTime) / 1e6;
			lag += frameTime;
			frameStartTime = now;

			window.update();
			while (lag >= MS_PER_UPDATE) {
				if (handleCollision()) {
					collisions++;
				}
				for (Block block : blocks) {
					block.update();
				}

				lag -= MS_PER_UPDATE;
			}
			camera.update();

			if (!vkBase.beginFrame()) {
				continue;
			}


			VkExtent2D extent = vkBase.getSwapChain().getExtent();
			ShapeRenderer.PushConstants constants = new ShapeRenderer.PushConstants();
			constants.pMatrix = new Matrix4f().ortho2D(0.0f, extent.width(), 0.0f, extent.height());
			constants.vMatrix = camera.getViewMatrix();
			shapeRenderer.begin();
			for (Block block : blocks) {
				constants.mMatrix = block.calculateModelMatrix();
				shapeRenderer.recordCommands(block.getTexturedMesh(), constants);
			}
			shapeRenderer.end();

			textRenderer.begin();
			textRenderer.addText("FPS: " + fps, -1.0f, -1.0f, 0.5f);
			textRenderer.addText("Collisions: " + collisions, -0.2f, -1.0f, 0.5f);
			textRenderer.addText(blockA.getMass() + " kg", -1.0f, -0.9f, 0.5f);
			textRenderer.addText(String.valueOf(blockA.getSpeed()), -1.0f, -0.8f, 0.5f);
			textRenderer.addText(blockB.getMass() + " kg", 0.4f, -0.9f, 0.5f);
			textRenderer.addText(String.valueOf(blockB.getSpeed()), 0.4f, -0.8f, 0.5f);
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
			double dist = Math.abs(blockA.getPosition().x - wall.getPosition().x - wall.getWidth());
			blockA.setPosition(new Vector2d(wall.getPosition().x + wall.getWidth() + dist,
					blockA.getPosition().y));

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
