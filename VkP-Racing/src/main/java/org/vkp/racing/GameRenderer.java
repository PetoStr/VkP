package org.vkp.racing;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.vulkan.VkExtent2D;
import org.vkp.engine.Camera;
import org.vkp.engine.VkBase;
import org.vkp.engine.font.Text;
import org.vkp.engine.renderer.ShapeRenderer;
import org.vkp.engine.renderer.TextRenderer;
import org.vkp.racing.entity.Entity;

import lombok.Getter;

public class GameRenderer {

	@Getter
	private ShapeRenderer shapeRenderer;

	@Getter
	private TextRenderer textRenderer;

	private VkBase vkBase;

	private List<Entity> entitiesToDraw = new ArrayList<>();
	private List<Text> textsToDraw = new ArrayList<>();

	public GameRenderer(VkBase vkBase) {
		this.vkBase = vkBase;

		shapeRenderer = vkBase.getShapeRenderer();
		textRenderer = vkBase.getTextRenderer();
	}

	public void drawEntity(Entity entity) {
		entitiesToDraw.add(entity);
	}

	public void drawText(Text text) {
		textsToDraw.add(text);
	}

	public void drawText(String text, float x, float y, float scale) {
		drawText(new Text(text, new Vector2f(x, y), scale));
	}

	public void draw(Camera camera) {
		if (!vkBase.beginFrame()) {
			return;
		}

		VkExtent2D extent = vkBase.getSwapChain().getExtent();
		ShapeRenderer.PushConstants constants = new ShapeRenderer.PushConstants();
		constants.pMatrix = new Matrix4f().ortho2D(0.0f, extent.width(), 0.0f, extent.height());
		constants.vMatrix = camera.getViewMatrix();
		shapeRenderer.begin();
		for (Entity entity : entitiesToDraw) {
			constants.mMatrix = entity.getTransform().getModelMatrix();
			shapeRenderer.recordCommands(entity.getTexturedMesh(), constants);
		}
		shapeRenderer.end();

		textRenderer.begin();
		for (Text text : textsToDraw) {
			textRenderer.addText(text);
		}
		textRenderer.end();

		vkBase.submitFrame();

		entitiesToDraw.clear();
		textsToDraw.clear();
	}

	public void cleanup() {
		shapeRenderer.cleanup();
		textRenderer.cleanup();
	}

}