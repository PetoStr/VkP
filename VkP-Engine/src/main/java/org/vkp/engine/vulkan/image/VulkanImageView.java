package org.vkp.engine.vulkan.image;

import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_COMPONENT_SWIZZLE_A;
import static org.lwjgl.vulkan.VK10.VK_COMPONENT_SWIZZLE_B;
import static org.lwjgl.vulkan.VK10.VK_COMPONENT_SWIZZLE_G;
import static org.lwjgl.vulkan.VK10.VK_COMPONENT_SWIZZLE_R;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_VIEW_TYPE_2D;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.vkCreateImageView;
import static org.lwjgl.vulkan.VK10.vkDestroyImageView;
import static org.vkp.engine.vulkan.VkUtil.vkCheck;

import java.nio.LongBuffer;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkImageViewCreateInfo;

import lombok.Getter;

public class VulkanImageView {

	@Getter
	private long handle;
	
	private VkDevice device;

	public VulkanImageView(VkDevice device, long image, int format, int aspectMask) {
		this.device = device;
		LongBuffer imageView = memAllocLong(1);
		VkImageViewCreateInfo imageViewCreateInfo = VkImageViewCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
				.image(image)
				.viewType(VK_IMAGE_VIEW_TYPE_2D)
				.format(format);
		imageViewCreateInfo.subresourceRange()
				.aspectMask(aspectMask)
				.baseMipLevel(0)
				.levelCount(1)
				.baseArrayLayer(0)
				.layerCount(1);
		imageViewCreateInfo.components()
			.r(VK_COMPONENT_SWIZZLE_R)
			.g(VK_COMPONENT_SWIZZLE_G)
			.b(VK_COMPONENT_SWIZZLE_B)
			.a(VK_COMPONENT_SWIZZLE_A);
		vkCheck(vkCreateImageView(device, imageViewCreateInfo, null, imageView));
		imageViewCreateInfo.free();
		this.handle = imageView.get(0);
		memFree(imageView);
	}
	
	public void cleanup() {
		vkDestroyImageView(device, handle, null);
	}

}
