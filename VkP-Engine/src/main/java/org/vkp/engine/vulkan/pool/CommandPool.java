package org.vkp.engine.vulkan.pool;

import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_POOL_CREATE_TRANSIENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.vkCreateCommandPool;
import static org.lwjgl.vulkan.VK10.vkDestroyCommandPool;
import static org.vkp.engine.vulkan.VkUtil.vkCheck;

import java.nio.LongBuffer;

import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDevice;

import lombok.Getter;

public class CommandPool {

	@Getter
	private long handle;
	
	private VkDevice device;
	
	public CommandPool(VkDevice device, int graphicsQueueIndex) {
		LongBuffer pHandle = memAllocLong(1);
		VkCommandPoolCreateInfo commandPoolCreateInfo = VkCommandPoolCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO)
				.flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT
						| VK_COMMAND_POOL_CREATE_TRANSIENT_BIT)
				.queueFamilyIndex(graphicsQueueIndex);
		vkCheck(vkCreateCommandPool(device, commandPoolCreateInfo, null, pHandle));
		this.handle = pHandle.get(0);
		memFree(pHandle);		
		commandPoolCreateInfo.free();
		this.device = device;
	}

	public void cleanup() {
		vkDestroyCommandPool(device, handle, null);
	}
	
}
