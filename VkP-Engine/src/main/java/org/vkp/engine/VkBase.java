package org.vkp.engine;

import static org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUBPASS_CONTENTS_INLINE;
import static org.lwjgl.vulkan.VK10.vkCmdBeginRenderPass;
import static org.lwjgl.vulkan.VK10.vkCmdEndRenderPass;
import static org.lwjgl.vulkan.VK10.vkDeviceWaitIdle;
import static org.vkp.engine.vulkan.VkUtil.vkCheck;

import java.nio.LongBuffer;

import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.vkp.engine.loader.ShapeLoader;
import org.vkp.engine.loader.TextureLoader;
import org.vkp.engine.renderer.ShapeRenderer;
import org.vkp.engine.renderer.TextRenderer;
import org.vkp.engine.vulkan.RenderPass;
import org.vkp.engine.vulkan.VulkanInstance;
import org.vkp.engine.vulkan.buffer.BufferCreator;
import org.vkp.engine.vulkan.command.StagingBufferCommand;
import org.vkp.engine.vulkan.descriptor.DescriptorPool;
import org.vkp.engine.vulkan.device.LogicalDevice;
import org.vkp.engine.vulkan.device.PhysicalDevice;
import org.vkp.engine.vulkan.image.ImageCreator;
import org.vkp.engine.vulkan.swapchain.SwapChain;
import org.vkp.engine.window.Window;

import lombok.Getter;

@Getter
public class VkBase {

	private VulkanInstance instance;

	private long surface;

	private PhysicalDevice physicalDevice;

	private LogicalDevice device;

	private SwapChain swapChain;

	private RenderPass renderPass;

	private StagingBufferCommand stagingBufferCommand;

	private BufferCreator bufferCreator;

	private ImageCreator imageCreator;

	private TextureLoader textureLoader;

	private ShapeRenderer shapeRenderer;

	private TextRenderer textRenderer;

	private ShapeLoader shapeLoader;

	public void init(Window window) {
		instance = new VulkanInstance(window.getRequiredExtensions());

		createSurface(window.getId());

		physicalDevice = new PhysicalDevice(instance.getHandle(), surface);
		device = new LogicalDevice(physicalDevice);
		swapChain = new SwapChain(physicalDevice, device, surface, window);

		renderPass = new RenderPass(device,
				swapChain.getSurfaceFormat().getFormat(), swapChain.getDepthFormat());
		swapChain.init(renderPass);

		VkCommandBuffer commandBuffer = swapChain.getDrawCommandBuffers()[0];
		stagingBufferCommand = new StagingBufferCommand(device.getHandle(),
				device.getGraphicsQueue(), commandBuffer, physicalDevice.getMemoryProperties());

		DescriptorPool descriptorPool = new DescriptorPool(device.getHandle(), 1);
		descriptorPool.addPoolSize(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 20); // XXX: hard coded
		descriptorPool.createDescriptorPool();
		device.setDescriptorPool(descriptorPool);

		bufferCreator = new BufferCreator(device.getHandle(), physicalDevice.getMemoryProperties());
		imageCreator = new ImageCreator(device.getHandle(), physicalDevice.getMemoryProperties());

		textureLoader = new TextureLoader(device.getHandle(), stagingBufferCommand, imageCreator);

		shapeRenderer = new ShapeRenderer(this);
		shapeRenderer.init();
		textRenderer = new TextRenderer(this);
		textRenderer.init();

		shapeLoader = new ShapeLoader(stagingBufferCommand, descriptorPool.getHandle(),
				shapeRenderer.getCombinedImageSamplerLayout(), bufferCreator, textureLoader);
	}

	public boolean beginFrame() {
		if (!swapChain.beginFrame()) {
			return false;
		}

		VkCommandBuffer currentCommandBuffer = swapChain.getCurrentFrameCommandBuffer();

		VkExtent2D extent = swapChain.getExtent();

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
		vkCmdBeginRenderPass(currentCommandBuffer, renderPassBeginInfo, VK_SUBPASS_CONTENTS_INLINE);

		renderPassBeginInfo.free();
		clearValues.free();
		renderArea.free();

		return true;
	}

	public void submitFrame() {
		vkCmdEndRenderPass(swapChain.getCurrentFrameCommandBuffer());
		swapChain.submitFrame();
	}

	public void waitDevice() {
		vkDeviceWaitIdle(device.getHandle());
	}

	public void cleanup() {
		shapeLoader.cleanup();
		textureLoader.cleanup();
		renderPass.cleanup();
		swapChain.cleanup();
		device.cleanup();
		physicalDevice.cleanup();
		instance.cleanup();
	}

	private void createSurface(long window) {
		LongBuffer pSurface = memAllocLong(1);
		vkCheck(glfwCreateWindowSurface(instance.getHandle(), window, null, pSurface));
		surface = pSurface.get(0);
		memFree(pSurface);
	}

}
