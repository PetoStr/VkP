package org.vkp.engine.vulkan;

import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_MEMORY_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_ATTACHMENT_LOAD_OP_CLEAR;
import static org.lwjgl.vulkan.VK10.VK_ATTACHMENT_LOAD_OP_DONT_CARE;
import static org.lwjgl.vulkan.VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE;
import static org.lwjgl.vulkan.VK10.VK_ATTACHMENT_STORE_OP_STORE;
import static org.lwjgl.vulkan.VK10.VK_DEPENDENCY_BY_REGION_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUBPASS_EXTERNAL;
import static org.lwjgl.vulkan.VK10.vkCreateRenderPass;
import static org.lwjgl.vulkan.VK10.vkDestroyRenderPass;
import static org.vkp.engine.vulkan.VkUtil.vkCheck;

import java.nio.LongBuffer;

import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSubpassDependency;
import org.lwjgl.vulkan.VkSubpassDescription;
import org.vkp.engine.vulkan.device.LogicalDevice;

import lombok.Getter;

public class RenderPass {

	@Getter
	private long handle;

	private LogicalDevice device;

	public RenderPass(LogicalDevice device, int format, int depthFormat) {
		this.device = device;
		VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.calloc(2);
		VkAttachmentDescription colorAttachment = VkAttachmentDescription.calloc()
				.format(format)
				.samples(VK_SAMPLE_COUNT_1_BIT)
				.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
				.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE)
				.storeOp(VK_ATTACHMENT_STORE_OP_STORE)
				.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
				.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
				.finalLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);
		attachments.put(colorAttachment);
		VkAttachmentDescription depthAttachment = VkAttachmentDescription.calloc()
				.format(depthFormat)
				.samples(VK_SAMPLE_COUNT_1_BIT)
				.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
				.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE)
				.storeOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
				.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
				.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
				.finalLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
		attachments.put(depthAttachment);
		attachments.flip();

		VkAttachmentReference.Buffer colorReference = VkAttachmentReference.calloc(1)
				.attachment(0)
				.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
		VkAttachmentReference depthReference = VkAttachmentReference.calloc()
				.attachment(1)
				.layout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

		VkSubpassDescription.Buffer subpassDescription = VkSubpassDescription.calloc(1)
				.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS)
				.colorAttachmentCount(1)
				.pColorAttachments(colorReference)
				.pDepthStencilAttachment(depthReference);

		VkSubpassDependency.Buffer dependencies = VkSubpassDependency.calloc(2);
		dependencies.get(0)
			.srcSubpass(VK_SUBPASS_EXTERNAL)
			.dstSubpass(0)
			.srcStageMask(VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT)
			.dstStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
			.srcAccessMask(VK_ACCESS_MEMORY_READ_BIT)
			.dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT)
			.dependencyFlags(VK_DEPENDENCY_BY_REGION_BIT);
		dependencies.get(1)
			.srcSubpass(0)
			.dstSubpass(VK_SUBPASS_EXTERNAL)
			.srcStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
			.dstStageMask(VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT)
			.srcAccessMask(VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT)
			.dstAccessMask(VK_ACCESS_MEMORY_READ_BIT)
			.dependencyFlags(VK_DEPENDENCY_BY_REGION_BIT);

		VkRenderPassCreateInfo renderPassCreateInfo = VkRenderPassCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO)
				.pAttachments(attachments)
				.pSubpasses(subpassDescription)
				.pDependencies(dependencies);

		LongBuffer pRenderPass = memAllocLong(1);
		vkCheck(vkCreateRenderPass(device.getHandle(), renderPassCreateInfo, null, pRenderPass));
		this.handle = pRenderPass.get(0);

		memFree(pRenderPass);
		dependencies.free();
		renderPassCreateInfo.free();
		subpassDescription.free();
		depthReference.free();
		colorReference.free();
		depthAttachment.free();
		colorAttachment.free();
		attachments.free();
	}

	public void cleanup() {
		vkDestroyRenderPass(device.getHandle(), handle, null);
	}

}
