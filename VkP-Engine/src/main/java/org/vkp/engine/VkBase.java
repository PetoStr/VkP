package org.vkp.engine;

import static org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.vkDeviceWaitIdle;
import static org.vkp.engine.vulkan.VkUtil.vkCheck;

import java.nio.LongBuffer;

import org.lwjgl.vulkan.VkCommandBuffer;
import org.vkp.engine.model.ShapeLoader;
import org.vkp.engine.vulkan.RenderPass;
import org.vkp.engine.vulkan.VulkanInstance;
import org.vkp.engine.vulkan.buffer.BufferCreator;
import org.vkp.engine.vulkan.command.StagingBufferCommand;
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

	private ShapeLoader modelLoader;

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

		bufferCreator = new BufferCreator(device.getHandle(), physicalDevice.getMemoryProperties());
		imageCreator = new ImageCreator(device.getHandle(), physicalDevice.getMemoryProperties());

		modelLoader = new ShapeLoader(device.getHandle(), stagingBufferCommand,
				bufferCreator, imageCreator);
	}

	public void waitDevice() {
		vkDeviceWaitIdle(device.getHandle());
	}

	public void cleanup() {
		modelLoader.cleanup();
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
