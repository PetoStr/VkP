package org.vkp.engine.renderer;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import org.joml.Matrix4f;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkPushConstantRange;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import org.lwjgl.vulkan.VkViewport;
import org.vkp.engine.VkBase;
import org.vkp.engine.mesh.Mesh;
import org.vkp.engine.mesh.TexturedMesh;
import org.vkp.engine.vulkan.descriptor.DescriptorPool;
import org.vkp.engine.vulkan.descriptor.DescriptorSetLayout;
import org.vkp.engine.vulkan.pipeline.ShaderModule;
import org.vkp.engine.vulkan.pipeline.VulkanPipeline;
import org.vkp.engine.vulkan.swapchain.SwapChain;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32A32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_INDEX_TYPE_UINT32;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX;
import static org.lwjgl.vulkan.VK10.vkCmdBindDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkCmdBindIndexBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdBindPipeline;
import static org.lwjgl.vulkan.VK10.vkCmdBindVertexBuffers;
import static org.lwjgl.vulkan.VK10.vkCmdDrawIndexed;
import static org.lwjgl.vulkan.VK10.vkCmdPushConstants;
import static org.lwjgl.vulkan.VK10.vkCmdSetScissor;
import static org.lwjgl.vulkan.VK10.vkCmdSetViewport;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;

import lombok.Getter;

public class ShapeRenderer extends Renderer {

	@Getter
	private DescriptorSetLayout combinedImageSamplerLayout;

	private VulkanPipeline graphicsPipeline;
	private ShaderModule vertexShaderModule;
	private ShaderModule fragmentShaderModule;

	private long[] descriptorSets;

	public static class PushConstants {

		public static final int BYTES = 16 * 4;

		public Matrix4f mMatrix;
		public Matrix4f vMatrix;
		public Matrix4f pMatrix;

	}

	public ShapeRenderer(VkBase vkBase) {
		super(vkBase);
	}

	@Override
	public void init() {
		super.init();

		String vertexShaderPath = "shaders/shape.vert.spv";
		String fragmentShaderPath = "shaders/shape.frag.spv";
		createPipeline(vertexShaderPath, fragmentShaderPath);
	}

	@Override
	public void begin() {
		super.begin();

		SwapChain swapChain = vkBase.getSwapChain();
		VkExtent2D extent = swapChain.getExtent();
		VkOffset2D offset = VkOffset2D.calloc()
				.x(0)
				.y(0);

		vkCmdBindPipeline(currentCommandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS,
				graphicsPipeline.getHandle());

		VkViewport.Buffer viewport = VkViewport.calloc(1)
				.width(extent.width())
				.height(extent.height())
				.minDepth(0.0f)
				.maxDepth(1.0f);
		vkCmdSetViewport(currentCommandBuffer, 0, viewport);

		VkRect2D.Buffer scissor = VkRect2D.calloc(1)
				.offset(offset)
				.extent(extent);
		vkCmdSetScissor(currentCommandBuffer, 0, scissor);

		vkCmdBindPipeline(currentCommandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS,
				graphicsPipeline.getHandle());

		descriptorSets = new long[1];

		scissor.free();
		viewport.free();
		offset.free();
	}

	public void recordCommands(TexturedMesh texturedMesh, PushConstants constants) {
		recordPushConstants(currentCommandBuffer, constants);

		descriptorSets[0] = texturedMesh.getTexture().getDescriptorSet().getHandle();

		vkCmdBindDescriptorSets(currentCommandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS,
				graphicsPipeline.getPipelineLayout(), 0, descriptorSets, null);

		recordDrawMesh(currentCommandBuffer, texturedMesh.getMesh());
	}

	@Override
	public void cleanup() {
		super.cleanup();
		combinedImageSamplerLayout.cleanup();
		graphicsPipeline.cleanup();
		fragmentShaderModule.cleanup();
		vertexShaderModule.cleanup();
	}

	@Override
	protected void createDescriptorSetLayouts() {
		VkDevice device = vkBase.getDevice().getHandle();

		combinedImageSamplerLayout = new DescriptorSetLayout(device, 1);
		combinedImageSamplerLayout.addLayoutBinding(0, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
				VK_SHADER_STAGE_FRAGMENT_BIT);
		combinedImageSamplerLayout.createDescriptorSetLayout();
	}

	@Override
	protected void createDescriptorPool() {
		VkDevice device = vkBase.getDevice().getHandle();
		descriptorPool = new DescriptorPool(device, 5); // XXX: hard coded
		descriptorPool.addPoolSize(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 5);
		descriptorPool.createDescriptorPool(5);
	}

	private void recordDrawMesh(VkCommandBuffer commandBuffer, Mesh mesh) {
		LongBuffer offsets = memAllocLong(1);
		offsets.put(0, 0L);
		LongBuffer pBuffers = memAllocLong(1);
		pBuffers.put(0, mesh.getVertexBuffer().getHandle());
		vkCmdBindVertexBuffers(commandBuffer, 0, pBuffers, offsets);

		vkCmdBindIndexBuffer(commandBuffer, mesh.getIndexBuffer().getHandle(),
							 0, VK_INDEX_TYPE_UINT32);

		vkCmdDrawIndexed(commandBuffer, mesh.getIndexCount(), 1, 0, 0, 0);

		memFree(pBuffers);
		memFree(offsets);
	}

	private void recordPushConstants(VkCommandBuffer commandBuffer, PushConstants constants) {
		ByteBuffer pPushConstants = memAlloc(PushConstants.BYTES);
		Matrix4f mvpMatrix = new Matrix4f(constants.pMatrix);
		mvpMatrix.mul(constants.vMatrix);
		mvpMatrix.mul(constants.mMatrix);
		mvpMatrix.get(pPushConstants);

		vkCmdPushConstants(commandBuffer,
				   graphicsPipeline.getPipelineLayout(),
				   VK_SHADER_STAGE_VERTEX_BIT, 0, pPushConstants);
	}

	private void createPipeline(String vertexShaderPath, String fragmentShaderPath) {
		VkDevice device = vkBase.getDevice().getHandle();

		LongBuffer layouts = memAllocLong(1);
		layouts.put(combinedImageSamplerLayout.getHandle()).flip();
		VkPushConstantRange.Buffer pushConstantRanges = VkPushConstantRange.calloc(1)
				.stageFlags(VK_SHADER_STAGE_VERTEX_BIT)
				.size(PushConstants.BYTES)
				.offset(0);

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

		vertexShaderModule = new ShaderModule(device, vertexShaderPath,
				VK_SHADER_STAGE_VERTEX_BIT);
		fragmentShaderModule = new ShaderModule(device, fragmentShaderPath,
				VK_SHADER_STAGE_FRAGMENT_BIT);
		graphicsPipeline = new VulkanPipeline(device, 2);
		graphicsPipeline.addShaderModule(vertexShaderModule);
		graphicsPipeline.addShaderModule(fragmentShaderModule);
		graphicsPipeline.setBindingDescriptions(bindingDescriptions);
		graphicsPipeline.setAttributeDescriptions(attributeDescriptions);
		graphicsPipeline.createPipelineLayout(layouts, pushConstantRanges);
		graphicsPipeline.createPipeline(vkBase.getRenderPass(),
				VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);

		attributeDescriptions.free();
		bindingDescriptions.free();

		memFree(layouts);
		pushConstantRanges.free();
	}

}
