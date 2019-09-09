package org.vkp.engine.vulkan.descriptor;

import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET;
import static org.lwjgl.vulkan.VK10.vkAllocateDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkUpdateDescriptorSets;
import static org.vkp.engine.vulkan.VkUtil.vkCheck;

import java.nio.LongBuffer;

import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import lombok.Getter;

public class DescriptorSet {

	@Getter
	private long handle;
	
	private VkDevice device;
	
	public DescriptorSet(VkDevice device, long descriptorPool, LongBuffer descriptorSetLayouts) {
		this.device = device;
		
		VkDescriptorSetAllocateInfo allocateInfo = VkDescriptorSetAllocateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
				.descriptorPool(descriptorPool)
				.pSetLayouts(descriptorSetLayouts);
		
		LongBuffer pHandle = memAllocLong(1);
		vkCheck(vkAllocateDescriptorSets(device, allocateInfo, pHandle));
		this.handle = pHandle.get(0);
		memFree(pHandle);
		
		allocateInfo.free();
	}
	
	public void updateUniformBuffer(int binding, long buffer, long range) {		
		VkDescriptorBufferInfo.Buffer bufferInfo = VkDescriptorBufferInfo.calloc(1)
				.buffer(buffer)
				.offset(0)
				.range(range);

		VkWriteDescriptorSet.Buffer descriptorWrites = VkWriteDescriptorSet.calloc(1)
				.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
				.dstSet(handle)
				.dstBinding(binding)
				.dstArrayElement(0)
				.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
				.pBufferInfo(bufferInfo);
		
		vkUpdateDescriptorSets(device, descriptorWrites, null);

		descriptorWrites.free();
		bufferInfo.free();
	}
	
	public void updateCombinedImageSampler(int binding, long sampler, long imageView) {
		VkDescriptorImageInfo.Buffer imageInfo = VkDescriptorImageInfo.calloc(1)
				.sampler(sampler)
				.imageView(imageView)
				.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
		
		VkWriteDescriptorSet.Buffer descriptorWrites = VkWriteDescriptorSet.calloc(1)
				.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
				.dstSet(handle)
				.dstBinding(binding)
				.dstArrayElement(0)
				.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
				.pImageInfo(imageInfo);
		
		vkUpdateDescriptorSets(device, descriptorWrites, null);
		
		descriptorWrites.free();
		imageInfo.free();
	}
	
}
