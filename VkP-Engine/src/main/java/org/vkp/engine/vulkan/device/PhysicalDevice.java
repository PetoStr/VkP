package org.vkp.engine.vulkan.device;

import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.vkEnumeratePhysicalDevices;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceMemoryProperties;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceQueueFamilyProperties;
import static org.vkp.engine.vulkan.VkUtil.vkCheck;

import java.nio.IntBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

import lombok.Getter;

public class PhysicalDevice {

	@Getter
	private VkPhysicalDevice handle;

	@Getter
	private QueueFamilyProperties queueFamilyProperties;

	@Getter
	private SurfaceProperties surfaceProperties;

	@Getter
	private VkPhysicalDeviceMemoryProperties memoryProperties;

	private long surface;

	public PhysicalDevice(VkInstance instance, long surface) {
		this.surface = surface;

		long physicalDevice = selectPhysicalDevice(instance);
		handle = new VkPhysicalDevice(physicalDevice, instance);

		IntBuffer pQueueFamilyPropertyCount = memAllocInt(1);
		vkGetPhysicalDeviceQueueFamilyProperties(handle, pQueueFamilyPropertyCount, null);
		int queueFamilyPropertyCount = pQueueFamilyPropertyCount.get(0);
		VkQueueFamilyProperties.Buffer queueProperties = VkQueueFamilyProperties.calloc(queueFamilyPropertyCount);
		vkGetPhysicalDeviceQueueFamilyProperties(handle, pQueueFamilyPropertyCount, queueProperties);
		memFree(pQueueFamilyPropertyCount);

		memoryProperties = VkPhysicalDeviceMemoryProperties.calloc();
		vkGetPhysicalDeviceMemoryProperties(handle, memoryProperties);

		surfaceProperties = new SurfaceProperties(handle, surface);
		queueFamilyProperties = new QueueFamilyProperties(handle, surface);
	}

	public void updateSurfaceProperties() {
		surfaceProperties.cleanup();
		surfaceProperties = new SurfaceProperties(handle, surface);
	}

	public void cleanup() {
		memoryProperties.free();
		surfaceProperties.cleanup();
	}

	/* TODO select the most suitable physical device */
	private long selectPhysicalDevice(VkInstance instance) {
		IntBuffer pPhysicalDeviceCount = memAllocInt(1);
		vkCheck(vkEnumeratePhysicalDevices(instance, pPhysicalDeviceCount, null));
		if (pPhysicalDeviceCount.get(0) == 0) {
			throw new AssertionError("vkEnumeratePhysicalDevices returned 0 physical devices");
		}

		PointerBuffer pPhysicalDevices = memAllocPointer(pPhysicalDeviceCount.get(0));
		vkCheck(vkEnumeratePhysicalDevices(instance, pPhysicalDeviceCount, pPhysicalDevices));
		if (pPhysicalDeviceCount.get(0) == 0) {
			throw new AssertionError("vkEnumeratePhysicalDevices returned 0 physical devices");
		}
		long physicalDevice = pPhysicalDevices.get(0);
		memFree(pPhysicalDeviceCount);
		memFree(pPhysicalDevices);

		return physicalDevice;
	}

}
