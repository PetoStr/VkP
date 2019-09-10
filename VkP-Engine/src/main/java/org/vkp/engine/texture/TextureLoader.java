package org.vkp.engine.texture;

import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.vulkan.VkDevice;
import org.vkp.engine.vulkan.command.StagingBufferCommand;
import org.vkp.engine.vulkan.descriptor.DescriptorSet;
import org.vkp.engine.vulkan.descriptor.DescriptorSetLayout;
import org.vkp.engine.vulkan.image.ImageCreator;

public class TextureLoader {

	private VkDevice device;

	private StagingBufferCommand stagingBufferCommand;
	private ImageCreator imageCreator;

	private Map<String, Texture> textures = new HashMap<>();
	private List<Texture> colorTextures = new ArrayList<>();

	public TextureLoader(VkDevice device, StagingBufferCommand stagingBufferCommand,
			ImageCreator imageCreator) {
		this.device = device;
		this.stagingBufferCommand = stagingBufferCommand;
		this.imageCreator = imageCreator;
	}

	public Texture loadTexture(String texturePath, long descriptorPool,
			DescriptorSetLayout combinedImageSamplerLayout) {
		if (textures.containsKey(texturePath)) {
			return textures.get(texturePath);
		}

		LongBuffer layouts = memAllocLong(1);
		layouts.put(combinedImageSamplerLayout.getHandle()).flip();

		DescriptorSet descriptorSet = new DescriptorSet(device, descriptorPool, layouts);

		Texture texture = new Texture(device, imageCreator, descriptorSet);
		texture.createTexture(stagingBufferCommand, texturePath);
		descriptorSet.updateCombinedImageSampler(combinedImageSamplerLayout.getBinding(),
				texture.getSampler().getHandle(),
				texture.getImageView().getHandle());
		memFree(layouts);

		textures.put(texturePath, texture);

		return texture;
	}

	public Texture loadColorTexture(Color color, long descriptorPool,
			DescriptorSetLayout combinedImageSamplerLayout) {
		LongBuffer layouts = memAllocLong(1);
		layouts.put(combinedImageSamplerLayout.getHandle()).flip();

		DescriptorSet descriptorSet = new DescriptorSet(device, descriptorPool, layouts);

		Texture texture = new Texture(device, imageCreator, descriptorSet);
		texture.createTexture(stagingBufferCommand, color);
		descriptorSet.updateCombinedImageSampler(combinedImageSamplerLayout.getBinding(),
				texture.getSampler().getHandle(),
				texture.getImageView().getHandle());
		memFree(layouts);

		colorTextures.add(texture);

		return texture;
	}

	public void cleanup() {
		textures.forEach((texturePath, texture) -> texture.cleanup());
		colorTextures.forEach(Texture::cleanup);
	}

}
