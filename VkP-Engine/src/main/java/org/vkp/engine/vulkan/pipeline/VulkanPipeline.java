package org.vkp.engine.vulkan.pipeline;

import static org.lwjgl.system.MemoryUtil.memAllocFloat;
import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_BLEND_FACTOR_ONE;
import static org.lwjgl.vulkan.VK10.VK_BLEND_FACTOR_ZERO;
import static org.lwjgl.vulkan.VK10.VK_BLEND_OP_ADD;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_A_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_B_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_G_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_R_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMPARE_OP_LESS;
import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_BACK_BIT;
import static org.lwjgl.vulkan.VK10.VK_DYNAMIC_STATE_SCISSOR;
import static org.lwjgl.vulkan.VK10.VK_DYNAMIC_STATE_VIEWPORT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32A32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FRONT_FACE_COUNTER_CLOCKWISE;
import static org.lwjgl.vulkan.VK10.VK_LOGIC_OP_COPY;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_POLYGON_MODE_FILL;
import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX;
import static org.lwjgl.vulkan.VK10.vkCreateGraphicsPipelines;
import static org.lwjgl.vulkan.VK10.vkCreatePipelineLayout;
import static org.lwjgl.vulkan.VK10.vkDestroyPipeline;
import static org.lwjgl.vulkan.VK10.vkDestroyPipelineLayout;
import static org.vkp.engine.vulkan.VkUtil.vkCheck;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDynamicStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkPushConstantRange;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import org.vkp.engine.vulkan.RenderPass;

import lombok.Getter;

public class VulkanPipeline {

	@Getter
	private long handle;

	@Getter
	private long pipelineLayout;

	private VkDevice device;

	private VkPipelineShaderStageCreateInfo.Buffer shaderStageCreateInfos;

	public VulkanPipeline(VkDevice device, int shaderModuleCount) {
		this.device = device;

		shaderStageCreateInfos = VkPipelineShaderStageCreateInfo.calloc(shaderModuleCount);
	}

	public void createPipeline(RenderPass renderPass) {
		shaderStageCreateInfos.flip();

		VkVertexInputBindingDescription.Buffer bindingDescriptions = VkVertexInputBindingDescription.calloc(1);
		bindingDescriptions.get(0)
				.binding(0)
				.stride(Float.BYTES * 6)
				.inputRate(VK_VERTEX_INPUT_RATE_VERTEX);
		VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.calloc(2);
		attributeDescriptions.get(0)
				.location(0)
				.binding(bindingDescriptions.get(0).binding())
				.format(VK_FORMAT_R32G32B32A32_SFLOAT)
				.offset(0);
		attributeDescriptions.get(1)
				.location(1)
				.binding(bindingDescriptions.get(0).binding())
				.format(VK_FORMAT_R32G32_SFLOAT)
				.offset(Float.BYTES * 4);
		VkPipelineVertexInputStateCreateInfo vertexInputStateCreateInfo =
				VkPipelineVertexInputStateCreateInfo.calloc()
					.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
					.pVertexBindingDescriptions(bindingDescriptions)
					.pVertexAttributeDescriptions(attributeDescriptions);

		VkPipelineInputAssemblyStateCreateInfo assemblyStateCreateInfo =
				VkPipelineInputAssemblyStateCreateInfo.calloc()
					.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO)
					.topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST)
					.primitiveRestartEnable(false);



		VkPipelineViewportStateCreateInfo viewportStateCreateInfo =
				VkPipelineViewportStateCreateInfo.calloc()
					.sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO)
					.viewportCount(1)
					.pViewports(null)
					.scissorCount(1)
					.pScissors(null);

		IntBuffer pDynamicStates = memAllocInt(2);
		pDynamicStates.put(VK_DYNAMIC_STATE_VIEWPORT).put(VK_DYNAMIC_STATE_SCISSOR).flip();
		VkPipelineDynamicStateCreateInfo dynamicStateCreateInfo = VkPipelineDynamicStateCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO)
				.pDynamicStates(pDynamicStates);

		VkPipelineRasterizationStateCreateInfo rasterizationStateCreateInfo =
				VkPipelineRasterizationStateCreateInfo.calloc()
					.sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO)
					.depthClampEnable(false)
					.rasterizerDiscardEnable(false)
					.polygonMode(VK_POLYGON_MODE_FILL)
					.lineWidth(1.0f)
					.cullMode(VK_CULL_MODE_BACK_BIT)
					.frontFace(VK_FRONT_FACE_COUNTER_CLOCKWISE)
					.depthBiasEnable(false)
					.depthBiasConstantFactor(0.0f)
					.depthBiasClamp(0.0f)
					.depthBiasSlopeFactor(1.0f);

		VkPipelineMultisampleStateCreateInfo multisampleStateCreateInfo =
				VkPipelineMultisampleStateCreateInfo.calloc()
					.sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO)
					.sampleShadingEnable(false)
					.rasterizationSamples(VK_SAMPLE_COUNT_1_BIT)
					.minSampleShading(1.0f)
					.pSampleMask(null)
					.alphaToCoverageEnable(false)
					.alphaToOneEnable(false);

		VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachmentState =
				VkPipelineColorBlendAttachmentState.calloc(1)
					.blendEnable(false)
					.srcColorBlendFactor(VK_BLEND_FACTOR_ONE)
					.dstColorBlendFactor(VK_BLEND_FACTOR_ZERO)
					.colorBlendOp(VK_BLEND_OP_ADD)
					.srcAlphaBlendFactor(VK_BLEND_FACTOR_ONE)
					.dstAlphaBlendFactor(VK_BLEND_FACTOR_ZERO)
					.alphaBlendOp(VK_BLEND_OP_ADD)
					.colorWriteMask(
							VK_COLOR_COMPONENT_R_BIT
							| VK_COLOR_COMPONENT_G_BIT
							| VK_COLOR_COMPONENT_B_BIT
							| VK_COLOR_COMPONENT_A_BIT
					);
		FloatBuffer pBlendConstants = memAllocFloat(4);
		pBlendConstants.put(new float[] { 0.0f, 0.0f, 0.0f, 0.0f });
		pBlendConstants.flip();
		VkPipelineColorBlendStateCreateInfo colorBlendStateCreateInfo =
				VkPipelineColorBlendStateCreateInfo.calloc()
					.sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO)
					.logicOpEnable(false)
					.logicOp(VK_LOGIC_OP_COPY)
					.pAttachments(colorBlendAttachmentState)
					.blendConstants(pBlendConstants);

		VkPipelineDepthStencilStateCreateInfo depthStencilStateCreateInfo =
				VkPipelineDepthStencilStateCreateInfo.calloc()
					.sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO)
					.depthTestEnable(true)
					.depthWriteEnable(true)
					.depthCompareOp(VK_COMPARE_OP_LESS)
					.depthBoundsTestEnable(false)
					.stencilTestEnable(false);

		VkGraphicsPipelineCreateInfo.Buffer pipelineCreateInfo = VkGraphicsPipelineCreateInfo.calloc(1)
				.sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO)
				.pStages(shaderStageCreateInfos)
				.pVertexInputState(vertexInputStateCreateInfo)
				.pInputAssemblyState(assemblyStateCreateInfo)
				.pTessellationState(null)
				.pViewportState(viewportStateCreateInfo)
				.pRasterizationState(rasterizationStateCreateInfo)
				.pMultisampleState(multisampleStateCreateInfo)
				.pDepthStencilState(depthStencilStateCreateInfo)
				.pColorBlendState(colorBlendStateCreateInfo)
				.pDynamicState(dynamicStateCreateInfo)
				.layout(pipelineLayout)
				.renderPass(renderPass.getHandle())
				.subpass(0)
				.basePipelineHandle(VK_NULL_HANDLE)
				.basePipelineIndex(-1);
		LongBuffer pPipeline = memAllocLong(1);
		vkCheck(vkCreateGraphicsPipelines(device, VK_NULL_HANDLE, pipelineCreateInfo,
										  null, pPipeline));
		handle = pPipeline.get(0);
		memFree(pPipeline);

		pipelineCreateInfo.free();
		depthStencilStateCreateInfo.free();
		colorBlendStateCreateInfo.free();
		memFree(pBlendConstants);
		multisampleStateCreateInfo.free();
		rasterizationStateCreateInfo.free();
		dynamicStateCreateInfo.free();
		memFree(pDynamicStates);
		viewportStateCreateInfo.free();
		assemblyStateCreateInfo.free();
		vertexInputStateCreateInfo.free();
		attributeDescriptions.free();
		bindingDescriptions.free();
		shaderStageCreateInfos.free();
	}

	public void createPipelineLayout(LongBuffer descriptorLayouts,
									 VkPushConstantRange.Buffer pushConstantRanges) {
		VkPipelineLayoutCreateInfo layoutCreateInfo = VkPipelineLayoutCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
				.pPushConstantRanges(pushConstantRanges)
				.pSetLayouts(descriptorLayouts);
		LongBuffer pPipelineLayout = memAllocLong(1);
		vkCheck(vkCreatePipelineLayout(device, layoutCreateInfo, null, pPipelineLayout));
		pipelineLayout = pPipelineLayout.get(0);
		memFree(pPipelineLayout);
	}

	public void addShaderModule(ShaderModule shaderModule) {
		VkPipelineShaderStageCreateInfo createInfo = VkPipelineShaderStageCreateInfo.calloc()
			.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
			.stage(shaderModule.getStage())
			.module(shaderModule.getHandle())
			.pName(ShaderModule.ENTRY_POINT_NAME);
		shaderStageCreateInfos.put(createInfo);
	}

	public void cleanup() {
		vkDestroyPipelineLayout(device, pipelineLayout, null);
		vkDestroyPipeline(device, handle, null);
	}

}
