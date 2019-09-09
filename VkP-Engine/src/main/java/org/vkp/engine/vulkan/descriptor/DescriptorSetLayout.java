package org.vkp.engine.vulkan.descriptor;

import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.vkCreateDescriptorSetLayout;
import static org.lwjgl.vulkan.VK10.vkDestroyDescriptorSetLayout;
import static org.vkp.engine.vulkan.VkUtil.vkCheck;

import java.nio.LongBuffer;

import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkDevice;

import lombok.Getter;

public class DescriptorSetLayout {

	@Getter
	private long handle;

	@Getter
	private int binding;

	private VkDevice device;

	private VkDescriptorSetLayoutBinding.Buffer layoutBindings;

	public DescriptorSetLayout(VkDevice device, int layoutBindingCount) {
		this.device = device;
		layoutBindings = VkDescriptorSetLayoutBinding.calloc(layoutBindingCount);
	}

	public void createDescriptorSetLayout() {
		layoutBindings.flip();
		VkDescriptorSetLayoutCreateInfo layoutCreateInfo = VkDescriptorSetLayoutCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
				.pBindings(layoutBindings);
		LongBuffer pHandle = memAllocLong(1);
		vkCheck(vkCreateDescriptorSetLayout(device, layoutCreateInfo, null, pHandle));
		handle = pHandle.get(0);
		memFree(pHandle);
		layoutBindings.free();
	}

	public void addLayoutBinding(int binding, int descriptorType, int stage) {
		this.binding = binding;

		VkDescriptorSetLayoutBinding layoutBinding = VkDescriptorSetLayoutBinding.calloc()
				.binding(binding)
				.descriptorType(descriptorType)
				.descriptorCount(1)
				.stageFlags(stage);
		layoutBindings.put(layoutBinding);
	}

	public void cleanup() {
		vkDestroyDescriptorSetLayout(device, handle, null);
	}

}
