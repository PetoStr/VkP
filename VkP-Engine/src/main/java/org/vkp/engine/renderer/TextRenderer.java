package org.vkp.engine.renderer;

import static org.lwjgl.system.MemoryUtil.memAddress;
import static org.lwjgl.system.MemoryUtil.memAllocFloat;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memCopy;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_STRIP;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX;
import static org.lwjgl.vulkan.VK10.vkCmdBindDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkCmdBindPipeline;
import static org.lwjgl.vulkan.VK10.vkCmdBindVertexBuffers;
import static org.lwjgl.vulkan.VK10.vkCmdDraw;
import static org.lwjgl.vulkan.VK10.vkCmdSetScissor;
import static org.lwjgl.vulkan.VK10.vkCmdSetViewport;

import java.nio.FloatBuffer;
import java.nio.LongBuffer;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import org.lwjgl.vulkan.VkViewport;
import org.vkp.engine.Config;
import org.vkp.engine.VkBase;
import org.vkp.engine.font.FntParser;
import org.vkp.engine.font.Glyph;
import org.vkp.engine.texture.Texture;
import org.vkp.engine.vulkan.buffer.VulkanBuffer;
import org.vkp.engine.vulkan.descriptor.DescriptorPool;
import org.vkp.engine.vulkan.descriptor.DescriptorSetLayout;
import org.vkp.engine.vulkan.pipeline.ShaderModule;
import org.vkp.engine.vulkan.pipeline.VulkanPipeline;
import org.vkp.engine.vulkan.swapchain.SwapChain;

public class TextRenderer extends Renderer {

	public static final int MAX_CHARS = 2048;

	private VulkanPipeline graphicsPipeline;
	private ShaderModule vertexShaderModule;
	private ShaderModule fragmentShaderModule;

	private DescriptorSetLayout combinedImageSamplerLayout;

	private Texture fontTexture;

	private VertexData[] vertexData;

	private Map<Character, Glyph> glyphs = new HashMap<>();

	private int currentFrame;

	private int characterCount;

	public TextRenderer(VkBase vkBase) {
		super(vkBase);
		vertexData = new VertexData[Config.FRAMES];
		for (int i = 0; i < Config.FRAMES; i++) {
			vertexData[i] = new VertexData();
			vertexData[i].data = memAllocPointer(1);
		}
	}

	@Override
	public void init() {
		super.init();

		String vertexShaderPath = "shaders/text.vert.spv";
		String fragmentShaderPath = "shaders/text.frag.spv";
		createPipeline(vertexShaderPath, fragmentShaderPath);
		createVertexBuffers();
		fontTexture = vkBase.getTextureLoader().loadTexture("fonts/dejavusans.png",
				descriptorPool.getHandle(), combinedImageSamplerLayout);
		glyphs = FntParser.parseFntFile("fonts/dejavusans.fnt");
	}

	@Override
	public void begin() {
		currentFrame = vkBase.getSwapChain().getCurrentFrame();

		long size = vertexData[currentFrame].vertexBuffer.getSize();
		vertexData[currentFrame].vertexBuffer.mapMemory(size, vertexData[currentFrame].data);
	}

	public void addText(String text, float x, float y, float scale) {
		FloatBuffer data = memAllocFloat(16);

		VkExtent2D extent = vkBase.getSwapChain().getExtent();
		float ratio = (float) extent.width() / extent.height();

		for (Character character : text.toCharArray()) {
			Glyph glyph = glyphs.get(character);

			float w = (float) glyph.getWidth() / 512 * scale;
			float h = (float) glyph.getHeight() / 512 * scale;

			float x0 = x + (float) glyph.getXOffset() / 512 * scale / ratio;
			float y0 = y + (float) glyph.getYOffset() / 512 * scale;
			float x1 = x0 + w / ratio;
			float y1 = y0 + h;
			float s0 = (float) glyph.getX() / 512;
			float t0 = (float) glyph.getY() / 512;
			float s1 = s0 + (float) glyph.getWidth() / 512;
			float t1 = t0 + (float) glyph.getHeight() / 512;

			data.put(0, x0);
			data.put(1, y0);
			data.put(2, s0);
			data.put(3, t0);

			data.put(4, x0);
			data.put(5, y1);
			data.put(6, s0);
			data.put(7, t1);

			data.put(8, x1);
			data.put(9, y0);
			data.put(10, s1);
			data.put(11, t0);

			data.put(12, x1);
			data.put(13, y1);
			data.put(14, s1);
			data.put(15, t1);

			memCopy(memAddress(data),
					vertexData[currentFrame].data.get(0) + characterCount * 64, 64);

			x += (float) glyph.getXAdvance() / 512 / ratio * scale;

			characterCount++;
		}

		memFree(data);
	}

	@Override
	public void end() {
		super.begin();

		vertexData[currentFrame].vertexBuffer.unmapMemory();

		SwapChain swapChain = vkBase.getSwapChain();
		VkExtent2D extent = swapChain.getExtent();
		VkOffset2D offset = VkOffset2D.calloc()
				.x(0)
				.y(0);

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

		LongBuffer descriptorSets = memAllocLong(1);
		descriptorSets.put(0, fontTexture.getDescriptorSet().getHandle());
		vkCmdBindDescriptorSets(currentCommandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS,
				graphicsPipeline.getPipelineLayout(), 0, descriptorSets, null);

		LongBuffer offsets = memAllocLong(1);
		offsets.put(0, 0L);
		LongBuffer pBuffers = memAllocLong(1);
		pBuffers.put(0, vertexData[currentFrame].vertexBuffer.getHandle());
		vkCmdBindVertexBuffers(currentCommandBuffer, 0, pBuffers, offsets);

		for (int i = 0; i < characterCount; i++) {
			vkCmdDraw(currentCommandBuffer, 4, 1, i * 4, 0);
		}

		super.end();

		characterCount = 0;

		memFree(pBuffers);
		memFree(offsets);
		memFree(descriptorSets);
		scissor.free();
		viewport.free();
		offset.free();
	}

	@Override
	public void cleanup() {
		super.cleanup();
		for (VertexData vertData : vertexData) {
			vertData.vertexBuffer.cleanup();
			memFree(vertData.data);
		}
		combinedImageSamplerLayout.cleanup();
		fragmentShaderModule.cleanup();
		vertexShaderModule.cleanup();
		graphicsPipeline.cleanup();
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
		descriptorPool = new DescriptorPool(device, 1);
		descriptorPool.addPoolSize(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 1);
		descriptorPool.createDescriptorPool(1);
	}

	private void createPipeline(String vertexShaderPath, String fragmentShaderPath) {
		LongBuffer layouts = memAllocLong(1);
		layouts.put(combinedImageSamplerLayout.getHandle()).flip();

		VkVertexInputBindingDescription.Buffer bindingDescriptions = VkVertexInputBindingDescription.calloc(1)
				.binding(0)
				.stride(Float.BYTES * 4)
				.inputRate(VK_VERTEX_INPUT_RATE_VERTEX);
		VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.calloc(2);
		attributeDescriptions.get(0)
				.location(0)
				.binding(bindingDescriptions.get(0).binding())
				.format(VK_FORMAT_R32G32_SFLOAT)
				.offset(0);
		attributeDescriptions.get(1)
				.location(1)
				.binding(bindingDescriptions.get(0).binding())
				.format(VK_FORMAT_R32G32_SFLOAT)
				.offset(Float.BYTES * 2);

		VkDevice device = vkBase.getDevice().getHandle();
		vertexShaderModule = new ShaderModule(device, vertexShaderPath,
				VK_SHADER_STAGE_VERTEX_BIT);
		fragmentShaderModule = new ShaderModule(device, fragmentShaderPath,
				VK_SHADER_STAGE_FRAGMENT_BIT);
		graphicsPipeline = new VulkanPipeline(device, 2);
		graphicsPipeline.addShaderModule(vertexShaderModule);
		graphicsPipeline.addShaderModule(fragmentShaderModule);
		graphicsPipeline.setBindingDescriptions(bindingDescriptions);
		graphicsPipeline.setAttributeDescriptions(attributeDescriptions);
		graphicsPipeline.createPipelineLayout(layouts, null);
		graphicsPipeline.createPipeline(vkBase.getRenderPass(),
				VK_PRIMITIVE_TOPOLOGY_TRIANGLE_STRIP);

		attributeDescriptions.free();
		bindingDescriptions.free();
		memFree(layouts);
	}

	private void createVertexBuffers() {
		long size = (long) MAX_CHARS * VertexData.BYTES;
		for (VertexData vertData : vertexData) {
			vertData.vertexBuffer = vkBase.getBufferCreator().createBuffer(size,
					VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
					VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
		}
	}

	private static class VertexData {

		public static final int BYTES = 4 * Float.BYTES; // x y s t

		public VulkanBuffer vertexBuffer;

		public PointerBuffer data;

	}

}
