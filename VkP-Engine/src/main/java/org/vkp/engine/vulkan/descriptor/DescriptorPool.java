package org.vkp.engine.vulkan.descriptor;

import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.vkCreateDescriptorPool;
import static org.lwjgl.vulkan.VK10.vkDestroyDescriptorPool;
import static org.vkp.engine.vulkan.VkUtil.vkCheck;

import java.nio.LongBuffer;

import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDevice;

import lombok.Getter;

public class DescriptorPool {

	@Getter
	private long handle;
	
	private VkDevice device;
	
	private VkDescriptorPoolSize.Buffer poolSizes;
	
	public DescriptorPool(VkDevice device, int poolSizeCount) {
		this.device = device;
		poolSizes = VkDescriptorPoolSize.calloc(poolSizeCount);
	}
	
	public void createDescriptorPool(int sets) {
		poolSizes.flip();
		
		VkDescriptorPoolCreateInfo poolCreateInfo = VkDescriptorPoolCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
				.maxSets(sets)
				.pPoolSizes(poolSizes);
		
		LongBuffer pHandle = memAllocLong(1);
		vkCheck(vkCreateDescriptorPool(device, poolCreateInfo, null, pHandle));
		this.handle = pHandle.get(0);
		memFree(pHandle);
		poolCreateInfo.free();
		poolSizes.free();
	}
	
	public void addPoolSize(int type, int descriptorCount) {
		VkDescriptorPoolSize poolSize = VkDescriptorPoolSize.calloc()
				.type(type)
				.descriptorCount(descriptorCount);
		poolSizes.put(poolSize);
	}
	
	public void cleanup() {
		vkDestroyDescriptorPool(device, handle, null);
	}
	
}
