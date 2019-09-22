package org.vkp.engine.texture;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8G8B8A8_UNORM;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_SAMPLED_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.lwjgl.vulkan.VkDevice;
import org.vkp.engine.util.FileUtil;
import org.vkp.engine.vulkan.VulkanSampler;
import org.vkp.engine.vulkan.command.StagingBufferCommand;
import org.vkp.engine.vulkan.descriptor.DescriptorSet;
import org.vkp.engine.vulkan.image.ImageCreator;
import org.vkp.engine.vulkan.image.VulkanImage;
import org.vkp.engine.vulkan.image.VulkanImageView;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Texture {

	@Getter
	private DescriptorSet descriptorSet;

	@Getter
	private VulkanSampler sampler;

	@Getter
	private VulkanImage image;

	@Getter
	private VulkanImageView imageView;

	@Getter
	private TextureInfo textureInfo;

	private ImageCreator imageCreator;

	public Texture(VkDevice device, ImageCreator imageCreator, DescriptorSet descriptorSet) {
		this.imageCreator = imageCreator;
		this.descriptorSet = descriptorSet;
		sampler = new VulkanSampler(device);
	}

	public void createTexture(StagingBufferCommand bufferCommand, String texturePath) {
		ByteBuffer fileData;
		try {
			fileData = FileUtil.readResourceFile(texturePath);
		} catch (IOException e) {
			log.error("", e);
			throw new AssertionError();
		}
		TextureInfo metaData = FileUtil.readImageInfo(fileData);
		ByteBuffer imageData = FileUtil.readImage(fileData);
		log.info("Texture {}: {}x{}", texturePath,
				metaData.getWidth(), metaData.getHeight());

		image = imageCreator.createImage(metaData, VK_FORMAT_R8G8B8A8_UNORM,
				VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT);
		imageView = imageCreator.createImageView(image, VK_IMAGE_ASPECT_COLOR_BIT);

		bufferCommand.copyToImage(imageData, image.getHandle(), metaData);

		descriptorSet.updateCombinedImageSampler(0, sampler.getHandle(), imageView.getHandle());

		textureInfo = metaData;

		memFree(imageData);
	}

	public void createTexture(StagingBufferCommand bufferCommand, Color color) {
		TextureInfo metaData = new TextureInfo(1, 1, 1);

		image = imageCreator.createImage(metaData, VK_FORMAT_R8G8B8A8_UNORM,
				VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT);
		imageView = imageCreator.createImageView(image, VK_IMAGE_ASPECT_COLOR_BIT);

		ByteBuffer emptyData = memAlloc(4);
		emptyData.put(color.getRed());
		emptyData.put(color.getGreen());
		emptyData.put(color.getBlue());
		emptyData.put(color.getAlpha());
		emptyData.flip();
		bufferCommand.copyToImage(emptyData, image.getHandle(), metaData);

		descriptorSet.updateCombinedImageSampler(0, sampler.getHandle(), imageView.getHandle());

		memFree(emptyData);
	}

	public void cleanup() {
		sampler.cleanup();
		imageView.cleanup();
		image.cleanup();
	}

}
