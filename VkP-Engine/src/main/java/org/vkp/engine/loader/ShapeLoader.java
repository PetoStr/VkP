package org.vkp.engine.loader;

import java.util.EnumMap;
import java.util.Map;

import org.vkp.engine.mesh.Arrow;
import org.vkp.engine.mesh.Circle;
import org.vkp.engine.mesh.Mesh;
import org.vkp.engine.mesh.Quad;
import org.vkp.engine.mesh.TexturedMesh;
import org.vkp.engine.mesh.Triangle;
import org.vkp.engine.texture.Color;
import org.vkp.engine.texture.Texture;
import org.vkp.engine.vulkan.buffer.BufferCreator;
import org.vkp.engine.vulkan.command.StagingBufferCommand;
import org.vkp.engine.vulkan.descriptor.DescriptorSetLayout;

public class ShapeLoader {

	private StagingBufferCommand stagingBufferCommand;
	private long commandPool;
	private DescriptorSetLayout combinedImageSampler;
	private BufferCreator bufferCreator;
	private TextureLoader textureLoader;

	private Map<ShapeType, Mesh> shapeMeshes = new EnumMap<>(ShapeType.class);

	public ShapeLoader(StagingBufferCommand stagingBufferCommand, long commandPool,
			DescriptorSetLayout combinedImageSampler, BufferCreator bufferCreator,
			TextureLoader textureLoader) {
		this.stagingBufferCommand = stagingBufferCommand;
		this.commandPool = commandPool;
		this.combinedImageSampler = combinedImageSampler;
		this.bufferCreator = bufferCreator;
		this.textureLoader = textureLoader;
	}

	public TexturedMesh load(ShapeType shapeType, String texturePath) {
		Mesh mesh = loadShapeMesh(shapeType);
		Texture texture = textureLoader.loadTexture(texturePath, commandPool, combinedImageSampler);

		return new TexturedMesh(mesh, texture);
	}

	public TexturedMesh load(ShapeType shapeType, Color color) {
		Mesh mesh = loadShapeMesh(shapeType);
		Texture colorTexture =
				textureLoader.loadColorTexture(color, commandPool, combinedImageSampler);

		return new TexturedMesh(mesh, colorTexture);
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
			case QUAD:
				shapeMesh = new Quad(bufferCreator, stagingBufferCommand);
				break;
			case TRIANGLE:
				shapeMesh = new Triangle(bufferCreator, stagingBufferCommand);
				break;
			case CIRCLE:
				shapeMesh = new Circle(bufferCreator, stagingBufferCommand);
				break;
			case ARROW:
				shapeMesh = new Arrow(bufferCreator, stagingBufferCommand);
				break;

		}

		shapeMeshes.put(shapeType, shapeMesh);

		return shapeMesh;
	}

}
