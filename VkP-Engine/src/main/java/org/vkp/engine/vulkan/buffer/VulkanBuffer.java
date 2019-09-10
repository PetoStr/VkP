package org.vkp.engine.vulkan.buffer;

import static org.lwjgl.system.MemoryUtil.memAddress;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memCopy;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_SHARING_MODE_EXCLUSIVE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.vkAllocateMemory;
import static org.lwjgl.vulkan.VK10.vkBindBufferMemory;
import static org.lwjgl.vulkan.VK10.vkCreateBuffer;
import static org.lwjgl.vulkan.VK10.vkDestroyBuffer;
import static org.lwjgl.vulkan.VK10.vkFreeMemory;
import static org.lwjgl.vulkan.VK10.vkGetBufferMemoryRequirements;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;
import static org.vkp.engine.vulkan.VkUtil.vkCheck;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;

import lombok.Getter;

public class VulkanBuffer {

	@Getter
	private long handle;

	@Getter
	private long memory;

	@Getter
	private long size;

	private VkDevice device;

	public VulkanBuffer(VkDevice device, long size, int usage) {
		this.device = device;
		this.size = size;

		VkBufferCreateInfo bufferCreateInfo = VkBufferCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
				.size(size)
				.usage(usage)
				.sharingMode(VK_SHARING_MODE_EXCLUSIVE);
		LongBuffer pBuffer = memAllocLong(1);
		vkCheck(vkCreateBuffer(device, bufferCreateInfo, null, pBuffer));
		handle = pBuffer.get(0);

		memFree(pBuffer);
		bufferCreateInfo.free();
	}

	public void allocate(VkPhysicalDeviceMemoryProperties memoryProperties, int memoryPropertyFlags) {
		VkMemoryRequirements memoryRequirements = VkMemoryRequirements.calloc();
		vkGetBufferMemoryRequirements(device, handle, memoryRequirements);

		boolean found = false;
		for (int i = 0; i < memoryProperties.memoryTypeCount(); i++) {
			if ((memoryRequirements.memoryTypeBits() & (1 << i)) != 0
				&& (memoryProperties.memoryTypes(i).propertyFlags() & memoryPropertyFlags) == memoryPropertyFlags) {
				VkMemoryAllocateInfo memoryAllocateInfo = VkMemoryAllocateInfo.calloc()
						.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
						.allocationSize(memoryRequirements.size())
						.memoryTypeIndex(i);
				LongBuffer pMemory = memAllocLong(1);
				vkCheck(vkAllocateMemory(device, memoryAllocateInfo, null, pMemory));
				memory = pMemory.get(0);
				memFree(pMemory);
				memoryAllocateInfo.free();
				found = true;
				break;
			}
		}
		if (!found) {
			throw new AssertionError("Memory type not found");
		}

		memoryRequirements.free();
	}

	public void bindBufferMemory() {
		vkCheck(vkBindBufferMemory(device, handle, memory, 0));
	}

	public void mapMemory(long size, PointerBuffer ppData) {
		vkCheck(vkMapMemory(device, memory, 0, size, 0, ppData));
	}

	public void unmapMemory() {
		vkUnmapMemory(device, memory);
	}

	public void copy(ByteBuffer data) {
		PointerBuffer ppData = memAllocPointer(1);

		mapMemory(data.capacity(), ppData);

		memCopy(memAddress(data), ppData.get(0), data.capacity());

		unmapMemory();

		memFree(ppData);
	}

	public void cleanup() {
		vkFreeMemory(device, memory, null);
		vkDestroyBuffer(device, handle, null);
	}
}
