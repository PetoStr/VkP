package org.vkp.engine.vulkan.device;

import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAllocFloat;
import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.system.MemoryUtil.memUTF8;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.vkCreateDevice;
import static org.lwjgl.vulkan.VK10.vkDestroyDevice;
import static org.lwjgl.vulkan.VK10.vkGetDeviceQueue;
import static org.vkp.engine.vulkan.VkUtil.vkCheck;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkQueue;
import org.vkp.engine.Config;
import org.vkp.engine.vulkan.descriptor.DescriptorPool;
import org.vkp.engine.vulkan.pool.CommandPool;

import lombok.Getter;
import lombok.Setter;

@Getter
public class LogicalDevice {

	private VkDevice handle;

	private int graphicsQueueIndex;
	private int presentQueueIndex;

	private VkQueue graphicsQueue;
	private VkQueue presentQueue;

	private CommandPool commandPool;

	@Getter
	@Setter
	private DescriptorPool descriptorPool;

	public LogicalDevice(PhysicalDevice physicalDevice) {
		graphicsQueueIndex = physicalDevice.getQueueFamilyProperties().getGraphicsQueueIndex();
		presentQueueIndex = graphicsQueueIndex;

		if (graphicsQueueIndex == -1) {
			throw new AssertionError("failed to get graphicsQueueIndex");
		}

		FloatBuffer pQueuePriorities = memAllocFloat(1).put(1.0f);
		pQueuePriorities.flip();
		VkDeviceQueueCreateInfo.Buffer queueCreateInfo = VkDeviceQueueCreateInfo.calloc(1)
				.sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
				.queueFamilyIndex(graphicsQueueIndex)
				.pQueuePriorities(pQueuePriorities);

		PointerBuffer ppExtensionNames = memAllocPointer(1);
		ByteBuffer swapchainExtension = memUTF8(VK_KHR_SWAPCHAIN_EXTENSION_NAME);
		ppExtensionNames.put(swapchainExtension);
		ppExtensionNames.flip();

		PointerBuffer ppEnabledLayerNames = memAllocPointer(Config.requiredLayers.length);
		if (Config.ENABLE_LAYERS) {
			for (int i = 0; i < Config.requiredLayers.length; i++) {
				ppEnabledLayerNames.put(Config.requiredLayers[i]);
			}
		}
		ppEnabledLayerNames.flip();

		VkPhysicalDeviceFeatures features = VkPhysicalDeviceFeatures.calloc()
				.samplerAnisotropy(true);

		VkDeviceCreateInfo deviceCreateInfo = VkDeviceCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
				.pNext(NULL)
				.pQueueCreateInfos(queueCreateInfo)
				.ppEnabledExtensionNames(ppExtensionNames)
				.ppEnabledLayerNames(ppEnabledLayerNames)
				.pEnabledFeatures(features);
		PointerBuffer pDevice = memAllocPointer(1);
		vkCheck(vkCreateDevice(physicalDevice.getHandle(), deviceCreateInfo, null, pDevice));
		long device = pDevice.get(0);
		memFree(pDevice);

		this.handle = new VkDevice(device, physicalDevice.getHandle(), deviceCreateInfo);

		deviceCreateInfo.free();
		features.free();
		memFree(ppEnabledLayerNames);
		memFree(swapchainExtension);
		memFree(ppExtensionNames);
		memFree(pQueuePriorities);
		queueCreateInfo.free();

		PointerBuffer pGraphicsQueue = memAllocPointer(1);
		PointerBuffer pPresentQueue = memAllocPointer(1);
		vkGetDeviceQueue(handle, graphicsQueueIndex, 0, pGraphicsQueue);
		vkGetDeviceQueue(handle, presentQueueIndex, 0, pPresentQueue);
		graphicsQueue = new VkQueue(pGraphicsQueue.get(0), handle);
		presentQueue = new VkQueue(pPresentQueue.get(0), handle);
		memFree(pGraphicsQueue);
		memFree(pPresentQueue);

		this.commandPool = new CommandPool(handle, graphicsQueueIndex);
	}

	public void cleanup() {
		descriptorPool.cleanup();
		commandPool.cleanup();
		vkDestroyDevice(handle, null);
	}

}
