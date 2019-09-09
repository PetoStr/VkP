package org.vkp.engine.vulkan.device;

import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_GRAPHICS_BIT;
import static org.lwjgl.vulkan.VK10.VK_TRUE;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceQueueFamilyProperties;

import java.nio.IntBuffer;

import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

import lombok.Getter;

public class QueueFamilyProperties {

	@Getter
	private int graphicsQueueIndex = -1;

	public QueueFamilyProperties(VkPhysicalDevice physicalDevice, long surface) {
		IntBuffer pQueueFamilyPropertyCount = memAllocInt(1);
		vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, pQueueFamilyPropertyCount, null);
		int queueFamilyPropertyCount = pQueueFamilyPropertyCount.get(0);
		VkQueueFamilyProperties.Buffer properties = VkQueueFamilyProperties.calloc(queueFamilyPropertyCount);
		vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, pQueueFamilyPropertyCount, properties);
		memFree(pQueueFamilyPropertyCount);

		IntBuffer supportsPresent = memAllocInt(1);
		for (int i = 0; i < queueFamilyPropertyCount; i++) {
			vkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice, i, surface, supportsPresent);
			if (supportsPresent.get(0) == VK_TRUE
					&& (properties.queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0) {
				graphicsQueueIndex = i;
				break;
			}
		}
		memFree(supportsPresent);
		properties.free();
	}

}
