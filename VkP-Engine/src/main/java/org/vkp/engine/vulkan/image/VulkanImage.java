package org.vkp.engine.vulkan.image;

import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_TILING_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_TYPE_2D;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHARING_MODE_EXCLUSIVE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.vkAllocateMemory;
import static org.lwjgl.vulkan.VK10.vkBindImageMemory;
import static org.lwjgl.vulkan.VK10.vkCreateImage;
import static org.lwjgl.vulkan.VK10.vkDestroyImage;
import static org.lwjgl.vulkan.VK10.vkFreeMemory;
import static org.lwjgl.vulkan.VK10.vkGetImageMemoryRequirements;
import static org.vkp.engine.vulkan.VkUtil.vkCheck;

import java.nio.LongBuffer;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent3D;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;

import lombok.Getter;

public class VulkanImage {

	@Getter
	private long handle;

	@Getter
	private long memory;

	@Getter
	private int format;

	@Getter
	private VkExtent3D extent;

	private VkDevice device;

	public VulkanImage(VkDevice device, int format, int width, int height, int depth, int usage) {
		this.device = device;
		this.format = format;
		LongBuffer pImage = memAllocLong(1);
		extent = VkExtent3D.calloc()
				.width(width)
				.height(height)
				.depth(depth);
		VkImageCreateInfo imageCreateInfo = VkImageCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
				.imageType(VK_IMAGE_TYPE_2D)
				.mipLevels(1)
				.arrayLayers(1)
				.format(format)
				.tiling(VK_IMAGE_TILING_OPTIMAL)
				.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
				.usage(usage)
				.sharingMode(VK_SHARING_MODE_EXCLUSIVE)
				.samples(VK_SAMPLE_COUNT_1_BIT)
				.extent(extent);
		vkCheck(vkCreateImage(device, imageCreateInfo, null, pImage));
		this.handle = pImage.get(0);
		memFree(pImage);
		imageCreateInfo.free();
	}

	public VulkanImage(long image) {
		this.handle = image;
	}

	public void allocateMemory(VkPhysicalDeviceMemoryProperties memoryProperties) {
		VkMemoryRequirements memoryRequirements = VkMemoryRequirements.calloc();
		vkGetImageMemoryRequirements(device, handle, memoryRequirements);
		int memoryTypeIndex = findMemoryTypeIndex(memoryRequirements.memoryTypeBits(),
				VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, memoryProperties);
		VkMemoryAllocateInfo memoryAllocateInfo = VkMemoryAllocateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
				.allocationSize(memoryRequirements.size())
				.memoryTypeIndex(memoryTypeIndex);
		LongBuffer pMemory = memAllocLong(1);
		vkCheck(vkAllocateMemory(device, memoryAllocateInfo, null, pMemory));
		this.memory = pMemory.get(0);
		memFree(pMemory);
		memoryAllocateInfo.free();
		memoryRequirements.free();
	}

	public void bindMemory() {
		vkCheck(vkBindImageMemory(device, handle, memory, 0));
	}

	public void cleanup() {
		extent.free();
		vkFreeMemory(device, memory, null);
		vkDestroyImage(device, handle, null);
	}

	private int findMemoryTypeIndex(int memoryTypeBits, int memoryProperty,
			VkPhysicalDeviceMemoryProperties physicalDeviceMemoryProperties) {
		for (int i = 0; i < physicalDeviceMemoryProperties.memoryTypeCount(); i++) {
			if ((memoryTypeBits & (1 << i)) != 0
					&& (physicalDeviceMemoryProperties.memoryTypes(i).propertyFlags() & memoryProperty) == memoryProperty) {
				return i;
			}
		}

		throw new AssertionError("Failed to find suitable memory type");
	}

}
