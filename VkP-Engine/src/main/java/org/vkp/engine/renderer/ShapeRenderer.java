package org.vkp.engine.renderer;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_INDEX_TYPE_UINT32;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUBPASS_CONTENTS_INLINE;
import static org.lwjgl.vulkan.VK10.vkCmdBeginRenderPass;
import static org.lwjgl.vulkan.VK10.vkCmdBindDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkCmdBindIndexBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdBindPipeline;
import static org.lwjgl.vulkan.VK10.vkCmdBindVertexBuffers;
import static org.lwjgl.vulkan.VK10.vkCmdDrawIndexed;
import static org.lwjgl.vulkan.VK10.vkCmdPushConstants;
import static org.lwjgl.vulkan.VK10.vkCmdSetScissor;
import static org.lwjgl.vulkan.VK10.vkCmdSetViewport;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import org.joml.Matrix4f;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkPushConstantRange;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkViewport;
import org.vkp.engine.VkBase;
import org.vkp.engine.mesh.Mesh;
import org.vkp.engine.model.Model;
import org.vkp.engine.vulkan.RenderPass;
import org.vkp.engine.vulkan.descriptor.DescriptorPool;
import org.vkp.engine.vulkan.descriptor.DescriptorSetLayout;
import org.vkp.engine.vulkan.pipeline.ShaderModule;
import org.vkp.engine.vulkan.pipeline.VulkanPipeline;
import org.vkp.engine.vulkan.swapchain.SwapChain;

import lombok.Data;

public class ShapeRenderer extends Renderer {

	private VulkanPipeline graphicsPipeline;
	private ShaderModule vertexShaderModule;
	private ShaderModule fragmentShaderModule;

	private long[] descriptorSets;

	public ShapeRenderer(VkBase vkBase) {
		super(vkBase);
	}

	@Override
	public void init() {
		super.init();

		String vertexShaderPath = "shaders/shape.vert.spv";
		String fragmentShaderPath = "shaders/shape.frag.spv";
		LongBuffer layouts = memAllocLong(1);
		layouts.put(combinedImageSamplerSetLayout.getHandle()).flip();
		VkPushConstantRange.Buffer pushConstantRanges = VkPushConstantRange.calloc(1)
				.stageFlags(VK_SHADER_STAGE_VERTEX_BIT)
				.size(PushConstants.BYTES)
				.offset(0);
		createPipeline(layouts, pushConstantRanges, vertexShaderPath, fragmentShaderPath);
		memFree(layouts);
		pushConstantRanges.free();
	}

	@Override
	public void recordCommands(VkCommandBuffer commandBuffer, Model model) {
		recordPushConstants(commandBuffer, model);

		descriptorSets[0] = model.getTexture().getDescriptorSet().getHandle();

		vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS,
				graphicsPipeline.getPipelineLayout(), 0, descriptorSets, null);

		recordDrawMesh(commandBuffer, model.getMesh());
	}

	@Override
	public void cleanup() {
		super.cleanup();
		descriptorPool.cleanup();
		graphicsPipeline.cleanup();
		fragmentShaderModule.cleanup();
		vertexShaderModule.cleanup();
	}

	protected void beginRecord(VkCommandBuffer commandBuffer) {
		SwapChain swapChain = vkBase.getSwapChain();
		VkExtent2D extent = swapChain.getExtent();
		RenderPass renderPass = vkBase.getRenderPass();

		VkOffset2D offset = VkOffset2D.calloc()
				.x(0)
				.y(0);
		VkRect2D renderArea = VkRect2D.calloc()
				.offset(offset)
				.extent(extent);
		VkClearValue.Buffer clearValues = VkClearValue.calloc(2);
		clearValues.get(0).color()
				.float32(0, 0.0f)
				.float32(1, 0.1f)
				.float32(2, 0.0f)
				.float32(3, 1.0f);
		clearValues.get(1).depthStencil()
				.set(1.0f, 0);

		VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
				.renderPass(renderPass.getHandle())
				.framebuffer(swapChain.getCurrentImageFrameBuffer())
				.renderArea(renderArea)
				.pClearValues(clearValues);
		vkCmdBeginRenderPass(commandBuffer, renderPassBeginInfo, VK_SUBPASS_CONTENTS_INLINE);

		vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS,
				graphicsPipeline.getHandle());

		VkViewport.Buffer viewport = VkViewport.calloc(1)
				.width(extent.width())
				.height(extent.height())
				.minDepth(0.0f)
				.maxDepth(1.0f);
		vkCmdSetViewport(commandBuffer, 0, viewport);

		VkRect2D.Buffer scissor = VkRect2D.calloc(1)
				.offset(offset)
				.extent(extent);
		vkCmdSetScissor(commandBuffer, 0, scissor);

		vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS,
				graphicsPipeline.getHandle());

		descriptorSets = new long[1];

		scissor.free();
		viewport.free();
		renderPassBeginInfo.free();
		clearValues.free();
		renderArea.free();
		offset.free();
	}

	protected void createDescriptorPool() {
		VkDevice device = vkBase.getDevice().getHandle();
		descriptorPool = new DescriptorPool(device, 5); // XXX: hard coded
		descriptorPool.addPoolSize(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 5);
		descriptorPool.createDescriptorPool(5);
	}

	protected void createDescriptorSetLayouts() {
		VkDevice device = vkBase.getDevice().getHandle();

		combinedImageSamplerSetLayout = new DescriptorSetLayout(device, 1);
		combinedImageSamplerSetLayout.addLayoutBinding(0, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
				VK_SHADER_STAGE_FRAGMENT_BIT);
		combinedImageSamplerSetLayout.createDescriptorSetLayout();
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

	private void recordPushConstants(VkCommandBuffer commandBuffer, Model model) {
		ByteBuffer pPushConstants = memAlloc(PushConstants.BYTES);
		SwapChain swapChain = vkBase.getSwapChain();
		VkExtent2D extent = swapChain.getExtent();
		float ratio = (float) extent.width() / extent.height();

		PushConstants constants = new PushConstants();
		constants.mpMatrix = new Matrix4f();
		constants.mpMatrix.ortho(-ratio, ratio, -1.0f, 1.0f, -1.0f, 1.0f);
		constants.mpMatrix.mul(model.getModelMatrix());
		constants.mpMatrix.get(pPushConstants);

		vkCmdPushConstants(commandBuffer,
				   graphicsPipeline.getPipelineLayout(),
				   VK_SHADER_STAGE_VERTEX_BIT, 0, pPushConstants);
	}

	private void createPipeline(LongBuffer descriptorSetLayouts,
								VkPushConstantRange.Buffer pushConstatnRanges,
								String vertexShaderPath,
								String fragmentShaderPath) {
		VkDevice device = vkBase.getDevice().getHandle();
		vertexShaderModule = new ShaderModule(device, vertexShaderPath,
				VK_SHADER_STAGE_VERTEX_BIT);
		fragmentShaderModule = new ShaderModule(device, fragmentShaderPath,
				VK_SHADER_STAGE_FRAGMENT_BIT);
		graphicsPipeline = new VulkanPipeline(device, 2);
		graphicsPipeline.addShaderModule(vertexShaderModule);
		graphicsPipeline.addShaderModule(fragmentShaderModule);
		graphicsPipeline.createPipelineLayout(descriptorSetLayouts, pushConstatnRanges);
		graphicsPipeline.createPipeline(vkBase.getRenderPass());
	}

	@Data
	private static class PushConstants {

		public static final int BYTES = 16 * 4;

		private Matrix4f mpMatrix;

	}

}
