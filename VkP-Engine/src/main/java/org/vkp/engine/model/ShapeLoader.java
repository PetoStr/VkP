package org.vkp.engine.model;

import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.vulkan.VkDevice;
import org.vkp.engine.mesh.Mesh;
import org.vkp.engine.mesh.RectangleMesh;
import org.vkp.engine.renderer.Renderer;
import org.vkp.engine.texture.Color;
import org.vkp.engine.texture.Texture;
import org.vkp.engine.vulkan.buffer.BufferCreator;
import org.vkp.engine.vulkan.command.StagingBufferCommand;
import org.vkp.engine.vulkan.descriptor.DescriptorSet;
import org.vkp.engine.vulkan.descriptor.DescriptorSetLayout;
import org.vkp.engine.vulkan.image.ImageCreator;

public class ShapeLoader {

	private VkDevice device;

	private StagingBufferCommand stagingBufferCommand;
	private BufferCreator bufferCreator;
	private ImageCreator imageCreator;

	private Map<ShapeType, Mesh> shapeMeshes = new EnumMap<>(ShapeType.class);
	private Map<String, Texture> textures = new HashMap<>();
	private List<Texture> colorTextures = new ArrayList<>();

	public ShapeLoader(VkDevice device, StagingBufferCommand stagingBufferCommand,
			BufferCreator bufferCreator, ImageCreator imageCreator) {
		this.device = device;
		this.stagingBufferCommand = stagingBufferCommand;
		this.bufferCreator = bufferCreator;
		this.imageCreator = imageCreator;
	}

	public Model load(ShapeType shapeType, String texturePath, Renderer renderer) {
		Mesh mesh = loadShapeMesh(shapeType);
		Texture texture = loadTexture(texturePath, renderer);

		return new Model(mesh, texture);
	}

	public Model load(ShapeType shapeType, Color color, Renderer renderer) {
		Mesh mesh = loadShapeMesh(shapeType);
		Texture colorTexture = loadColorTexture(color, renderer);

		return new Model(mesh, colorTexture);
	}

	public void cleanup() {
		shapeMeshes.forEach((shapeType, mesh) -> mesh.cleanup());
		textures.forEach((texturePath, texture) -> texture.cleanup());
		colorTextures.forEach(Texture::cleanup);
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

	private Texture loadTexture(String texturePath, Renderer renderer) {
		if (textures.containsKey(texturePath)) {
			return textures.get(texturePath);
		}

		LongBuffer layouts = memAllocLong(1);
		DescriptorSetLayout layout = renderer.getCombinedImageSamplerSetLayout();
		layouts.put(layout.getHandle()).flip();

		DescriptorSet descriptorSet = new DescriptorSet(device,
				renderer.getDescriptorPool().getHandle(),
				layouts);

		Texture texture = new Texture(device, imageCreator, descriptorSet);
		texture.createTexture(stagingBufferCommand, texturePath);
		descriptorSet.updateCombinedImageSampler(layout.getBinding(),
				texture.getSampler().getHandle(),
				texture.getImageView().getHandle());
		memFree(layouts);

		textures.put(texturePath, texture);

		return texture;
	}


	private Texture loadColorTexture(Color color, Renderer renderer) {
		LongBuffer layouts = memAllocLong(1);
		DescriptorSetLayout layout = renderer.getCombinedImageSamplerSetLayout();
		layouts.put(layout.getHandle()).flip();

		DescriptorSet descriptorSet = new DescriptorSet(device,
				renderer.getDescriptorPool().getHandle(),
				layouts);

		Texture texture = new Texture(device, imageCreator, descriptorSet);
		texture.createTexture(stagingBufferCommand, color);
		descriptorSet.updateCombinedImageSampler(layout.getBinding(),
				texture.getSampler().getHandle(),
				texture.getImageView().getHandle());
		memFree(layouts);

		colorTextures.add(texture);

		return texture;
	}

}
