package org.vkp.engine.vulkan.image;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.vkp.engine.texture.TextureInfo;

public class ImageCreator {

	private VkDevice device;

	private VkPhysicalDeviceMemoryProperties memoryProperties;

	public ImageCreator(VkDevice device, VkPhysicalDeviceMemoryProperties memoryProperties) {
		this.device = device;
		this.memoryProperties = memoryProperties;
	}

	public VulkanImage createImage(TextureInfo metaData, int format, int usage) {
		VulkanImage vulkanImage = new VulkanImage(device, format,
				metaData.getWidth(), metaData.getHeight(), 1, usage);
		vulkanImage.allocateMemory(memoryProperties);
		vulkanImage.bindMemory();

		return vulkanImage;
	}

	public VulkanImageView createImageView(VulkanImage vulkanImage, int aspectMask) {
		return new VulkanImageView(device, vulkanImage.getHandle(),
				vulkanImage.getFormat(), aspectMask);
	}

}
