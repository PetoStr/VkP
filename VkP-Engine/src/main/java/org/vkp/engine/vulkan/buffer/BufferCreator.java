package org.vkp.engine.vulkan.buffer;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;

public class BufferCreator {

	private VkDevice device;

	private VkPhysicalDeviceMemoryProperties memoryProperties;

	public BufferCreator(VkDevice device, VkPhysicalDeviceMemoryProperties memoryProperties) {
		this.device = device;
		this.memoryProperties = memoryProperties;
	}

	public VulkanBuffer createBuffer(long size, int usage, int memoryPropertyFlags) {
		VulkanBuffer vulkanBuffer = new VulkanBuffer(device, size, usage);
		vulkanBuffer.allocate(memoryProperties, memoryPropertyFlags);
		vulkanBuffer.bindBufferMemory();

		return vulkanBuffer;
	}

}
