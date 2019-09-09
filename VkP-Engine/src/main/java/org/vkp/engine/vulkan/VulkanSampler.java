package org.vkp.engine.vulkan;

import java.nio.LongBuffer;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkSamplerCreateInfo;

import static org.lwjgl.vulkan.VK10.VK_FILTER_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_MIPMAP_MODE_NEAREST;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.vkCreateSampler;
import static org.lwjgl.vulkan.VK10.vkDestroySampler;
import static org.vkp.engine.vulkan.VkUtil.vkCheck;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;

import lombok.Getter;

public class VulkanSampler {

	@Getter
	private long handle;

	private VkDevice device;

	public VulkanSampler(VkDevice device) {
		this.device = device;

		LongBuffer pHandle = memAllocLong(1);
		VkSamplerCreateInfo samplerCreateInfo = VkSamplerCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO)
				.magFilter(VK_FILTER_LINEAR)
				.minFilter(VK_FILTER_LINEAR)
				.mipmapMode(VK_SAMPLER_MIPMAP_MODE_NEAREST)
				.addressModeU(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE)
				.addressModeV(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE)
				.addressModeW(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE)
				.anisotropyEnable(true)
				.maxAnisotropy(16)
				.compareEnable(false)
				.unnormalizedCoordinates(false);
		vkCheck(vkCreateSampler(device, samplerCreateInfo, null, pHandle));
		this.handle = pHandle.get(0);
		memFree(pHandle);

		samplerCreateInfo.free();
	}

	public void cleanup() {
		vkDestroySampler(device, handle, null);
	}

}
