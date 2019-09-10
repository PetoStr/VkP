package org.vkp.engine.model;

import java.util.EnumMap;
import java.util.Map;

import org.vkp.engine.mesh.Mesh;
import org.vkp.engine.mesh.RectangleMesh;
import org.vkp.engine.renderer.ShapeRenderer;
import org.vkp.engine.texture.Color;
import org.vkp.engine.texture.Texture;
import org.vkp.engine.texture.TextureLoader;
import org.vkp.engine.vulkan.buffer.BufferCreator;
import org.vkp.engine.vulkan.command.StagingBufferCommand;

public class ShapeLoader {

	private StagingBufferCommand stagingBufferCommand;
	private BufferCreator bufferCreator;
	private TextureLoader textureLoader;

	private Map<ShapeType, Mesh> shapeMeshes = new EnumMap<>(ShapeType.class);

	public ShapeLoader(StagingBufferCommand stagingBufferCommand,
			BufferCreator bufferCreator, TextureLoader textureLoader) {
		this.stagingBufferCommand = stagingBufferCommand;
		this.bufferCreator = bufferCreator;
		this.textureLoader = textureLoader;
	}

	public Model load(ShapeType shapeType, String texturePath, ShapeRenderer renderer) {
		Mesh mesh = loadShapeMesh(shapeType);
		Texture texture = textureLoader.loadTexture(texturePath,
				renderer.getDescriptorPool().getHandle(),
				renderer.getCombinedImageSamplerLayout());

		return new Model(mesh, texture);
	}

	public Model load(ShapeType shapeType, Color color, ShapeRenderer renderer) {
		Mesh mesh = loadShapeMesh(shapeType);
		Texture colorTexture = textureLoader.loadColorTexture(color,
				renderer.getDescriptorPool().getHandle(),
				renderer.getCombinedImageSamplerLayout());

		return new Model(mesh, colorTexture);
	}

	public void cleanup() {
		shapeMeshes.forEach((shapeType, mesh) -> mesh.cleanup());
	}

	private Mesh loadShapeMesh(ShapeType shapeType) {
		if (shapeMeshes.containsKey(shapeType)) {
			return shapeMeshes.get(shapeType);
		}

		Mesh shapeMesh = null;

		switch (shapeType) {
			case RECTANGLE:
				shapeMesh = new RectangleMesh(bufferCreator, stagingBufferCommand);
				break;

		}

		shapeMeshes.put(shapeType, shapeMesh);

		return shapeMesh;
	}

}
