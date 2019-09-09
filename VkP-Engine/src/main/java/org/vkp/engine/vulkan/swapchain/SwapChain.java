package org.vkp.engine.vulkan.swapchain;

import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_PRESENT_MODE_FIFO_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_PRESENT_MODE_MAILBOX_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_SUBOPTIMAL_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkAcquireNextImageKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkCreateSwapchainKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkDestroySwapchainKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkGetSwapchainImagesKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkQueuePresentKHR;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT;
import static org.lwjgl.vulkan.VK10.VK_FENCE_CREATE_SIGNALED_BIT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_B8G8R8A8_UNORM;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_D24_UNORM_S8_UINT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_D32_SFLOAT_S8_UINT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_DEPTH_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHARING_MODE_EXCLUSIVE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_FENCE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO;
import static org.lwjgl.vulkan.VK10.vkAllocateCommandBuffers;
import static org.lwjgl.vulkan.VK10.vkBeginCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdEndRenderPass;
import static org.lwjgl.vulkan.VK10.vkCreateFence;
import static org.lwjgl.vulkan.VK10.vkCreateFramebuffer;
import static org.lwjgl.vulkan.VK10.vkCreateSemaphore;
import static org.lwjgl.vulkan.VK10.vkDestroyFence;
import static org.lwjgl.vulkan.VK10.vkDestroyFramebuffer;
import static org.lwjgl.vulkan.VK10.vkDestroySemaphore;
import static org.lwjgl.vulkan.VK10.vkDeviceWaitIdle;
import static org.lwjgl.vulkan.VK10.vkEndCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkFreeCommandBuffers;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceFormatProperties;
import static org.lwjgl.vulkan.VK10.vkQueueSubmit;
import static org.lwjgl.vulkan.VK10.vkResetFences;
import static org.lwjgl.vulkan.VK10.vkWaitForFences;
import static org.vkp.engine.vulkan.VkUtil.vkCheck;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkFormatProperties;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;
import org.vkp.engine.Config;
import org.vkp.engine.vulkan.RenderPass;
import org.vkp.engine.vulkan.VkUtil;
import org.vkp.engine.vulkan.device.LogicalDevice;
import org.vkp.engine.vulkan.device.PhysicalDevice;
import org.vkp.engine.vulkan.image.VulkanImage;
import org.vkp.engine.vulkan.image.VulkanImageView;
import org.vkp.engine.window.Window;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class SwapChain {

	@Getter
	private long handle;

	@Getter
	private int depthFormat;

	@Getter
	private VkExtent2D extent;

	@Getter
	private SurfaceFormat surfaceFormat;

	@Getter
	private List<VulkanImage> swapChainImages;

	@Getter
	private List<VulkanImageView> swapChainImageViews;

	@Getter
	private VulkanImage depthImage;

	@Getter
	private VulkanImageView depthImageView;

	@Getter
	private LongBuffer frameBuffers;

	@Getter
	private VkCommandBuffer[] drawCommandBuffers;

	@Getter
	private int currentFrame;

	private LogicalDevice device;
	private PhysicalDevice physicalDevice;
	private long surface;

	private Window window;

	private LongBuffer acquireSemaphores;
	private LongBuffer renderCompleteSemaphores;
	private LongBuffer commandBufferFences;

	private long currentAcquireSemaphore;
	private long currentRenderCompleteSemaphore;

	private int imageIndex;
	private IntBuffer pImageIndex;

	private RenderPass renderPass;

	public SwapChain(PhysicalDevice physicalDevice, LogicalDevice device,
					 long surface, Window window) {
		this.physicalDevice = physicalDevice;
		this.device = device;
		this.surface = surface;
		this.window = window;

		depthFormat = pickDepthFormat(physicalDevice);
		surfaceFormat = pickSurfaceFormat(physicalDevice.getSurfaceProperties().getSurfaceFormats());

		drawCommandBuffers = new VkCommandBuffer[Config.FRAMES];
		commandBufferFences = memAllocLong(Config.FRAMES);

		acquireSemaphores = memAllocLong(Config.FRAMES);
		renderCompleteSemaphores = memAllocLong(Config.FRAMES);
	}

	public void init(RenderPass renderPass) {
		this.renderPass = renderPass;

		createSwapchain();
		createSemaphores();
		createFrameBuffers();
		createCommandBuffers();
	}

	public void recreateSwapchain() {
		vkDeviceWaitIdle(device.getHandle());
		createSwapchain();
	}

	public boolean beginFrame() {
		currentAcquireSemaphore = acquireSemaphores.get(currentFrame);
		currentRenderCompleteSemaphore = renderCompleteSemaphores.get(currentFrame);

		vkCheck(vkWaitForFences(device.getHandle(), commandBufferFences.get(currentFrame),
								true, 1000000000l));

		pImageIndex = memAllocInt(1);
		int res = vkAcquireNextImageKHR(device.getHandle(), handle, -1L,
				currentAcquireSemaphore, VK_NULL_HANDLE, pImageIndex);
		if (res == VK_ERROR_OUT_OF_DATE_KHR || res == VK_SUBOPTIMAL_KHR) {
			recreateSwapchain();
			return false;
		} else {
			vkCheck(res);
		}
		imageIndex = pImageIndex.get(0);

		vkCheck(vkResetFences(device.getHandle(), commandBufferFences.get(currentFrame)));

		VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
				.flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
		vkCheck(vkBeginCommandBuffer(drawCommandBuffers[currentFrame], beginInfo));

		beginInfo.free();

		return true;
	}

	public void submitFrame() {
		vkCmdEndRenderPass(drawCommandBuffers[currentFrame]);

		vkCheck(vkEndCommandBuffer(drawCommandBuffers[currentFrame]));

		PointerBuffer pCommandBuffers = memAllocPointer(1);
		pCommandBuffers.put(0, drawCommandBuffers[currentFrame].address());
		LongBuffer pAcquireSemaphores = memAllocLong(1);
		pAcquireSemaphores.put(0, currentAcquireSemaphore);
		LongBuffer pRenderCompleteSemaphores = memAllocLong(1);
		pRenderCompleteSemaphores.put(0, currentRenderCompleteSemaphore);

		IntBuffer waitStages = memAllocInt(1);
		waitStages.put(0, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
		VkSubmitInfo submitInfo = VkSubmitInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
				.waitSemaphoreCount(1)
				.pWaitSemaphores(pAcquireSemaphores)
				.pSignalSemaphores(pRenderCompleteSemaphores)
				.pCommandBuffers(pCommandBuffers)
				.pWaitDstStageMask(waitStages);

		LongBuffer pSwapchains = memAllocLong(1);
		pSwapchains.put(0, handle);
		VkPresentInfoKHR presentInfo = VkPresentInfoKHR.calloc()
				.sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
				.pWaitSemaphores(pRenderCompleteSemaphores)
				.swapchainCount(1)
				.pSwapchains(pSwapchains)
				.pImageIndices(pImageIndex);

		vkCheck(vkQueueSubmit(device.getGraphicsQueue(), submitInfo,
				commandBufferFences.get(currentFrame)));

		int res = vkQueuePresentKHR(device.getPresentQueue(), presentInfo);
		if (res == VK_ERROR_OUT_OF_DATE_KHR || res == VK_SUBOPTIMAL_KHR) {
			recreateSwapchain();
		} else {
			vkCheck(res);
		}

		currentFrame = (currentFrame + 1) % Config.FRAMES;

		presentInfo.free();
		submitInfo.free();
		memFree(pSwapchains);
		memFree(waitStages);
		memFree(pCommandBuffers);
		memFree(pRenderCompleteSemaphores);
		memFree(pAcquireSemaphores);
		memFree(pImageIndex);
	}

	public void cleanup() {
		for (int i = 0; i < Config.FRAMES; i++) {
			vkDestroyFence(device.getHandle(), commandBufferFences.get(i), null);
			swapChainImageViews.get(i).cleanup();
			vkFreeCommandBuffers(device.getHandle(), device.getCommandPool().getHandle(),
					drawCommandBuffers[i]);
			vkDestroyFramebuffer(device.getHandle(), frameBuffers.get(i), null);
			vkDestroySemaphore(device.getHandle(), acquireSemaphores.get(i), null);
			vkDestroySemaphore(device.getHandle(), renderCompleteSemaphores.get(i), null);
		}
		memFree(frameBuffers);
		memFree(acquireSemaphores);
		memFree(renderCompleteSemaphores);
		memFree(commandBufferFences);
		depthImageView.cleanup();
		depthImage.cleanup();
		vkDestroySwapchainKHR(device.getHandle(), handle, null);
	}

	public VkCommandBuffer getCurrentFrameCommandBuffer() {
		return drawCommandBuffers[currentFrame];
	}

	public long getCurrentImageFrameBuffer() {
		return frameBuffers.get(imageIndex);
	}

	private void createSwapchain() {
		long oldSwapchain = handle;
		if (oldSwapchain != VK_NULL_HANDLE) {
			depthImageView.cleanup();
			depthImage.cleanup();
		}
		IntBuffer presentModes = physicalDevice.getSurfaceProperties().getPresentModes();
		int preferredPresentMode = window.isVsync() ? VK_PRESENT_MODE_FIFO_KHR
				: VK_PRESENT_MODE_MAILBOX_KHR;
		int swapchainPresentMode = pickPresentMode(presentModes, preferredPresentMode);

		physicalDevice.updateSurfaceProperties();
		VkSurfaceCapabilitiesKHR surfaceCapabilities =
				physicalDevice.getSurfaceProperties().getSurfaceCapabilities();
		extent = surfaceCapabilities.currentExtent();
		if (extent.width() == -1) {
			extent.set(window.getWidth(), window.getHeight());
		}
		VkSwapchainCreateInfoKHR swapchainCreateInfoKHR = VkSwapchainCreateInfoKHR.calloc()
				.sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
				.oldSwapchain(oldSwapchain)
				.surface(surface)
				.imageArrayLayers(1)
				.imageExtent(extent)
				.minImageCount(Config.FRAMES)
				.imageFormat(surfaceFormat.getFormat())
				.imageColorSpace(surfaceFormat.getColorSpace())
				.presentMode(swapchainPresentMode)
				.imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
				.preTransform(VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR)
				.compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
				.clipped(true)
				.imageSharingMode(VK_SHARING_MODE_EXCLUSIVE);

		log.info("Swap chain properties:");
		log.info("  Format:       "
				+ VkUtil.imageFormatToString(swapchainCreateInfoKHR.imageFormat()));
		log.info("  Present mode: "
				+ VkUtil.presentModeToString(swapchainCreateInfoKHR.presentMode()));
		log.info("  Buffer size:  " + swapchainCreateInfoKHR.imageExtent().width() + "x"
				+ swapchainCreateInfoKHR.imageExtent().height());
		log.info("  Image count:  " + swapchainCreateInfoKHR.minImageCount());

		LongBuffer pSwapChain = memAllocLong(1);
		vkCheck(vkCreateSwapchainKHR(device.getHandle(), swapchainCreateInfoKHR, null, pSwapChain));
		this.handle = pSwapChain.get(0);
		memFree(pSwapChain);
		swapchainCreateInfoKHR.free();

		if (oldSwapchain != VK_NULL_HANDLE) {
			vkDestroySwapchainKHR(device.getHandle(), oldSwapchain, null);
		}

		IntBuffer pImageCount = memAllocInt(1);
		vkCheck(vkGetSwapchainImagesKHR(device.getHandle(), handle, pImageCount, null));
		int imageCount = pImageCount.get(0);
		LongBuffer pSwapChainImages = memAllocLong(imageCount);
		vkCheck(vkGetSwapchainImagesKHR(device.getHandle(), handle, pImageCount, pSwapChainImages));
		swapChainImages = new ArrayList<>(imageCount);
		for (int i = 0; i < imageCount; i++) {
			swapChainImages.add(i, new VulkanImage(pSwapChainImages.get(i)));
		}
		memFree(pImageCount);
		createImageViews();

		depthImage = new VulkanImage(device.getHandle(), depthFormat, extent.width(),
				extent.height(), 1, VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT);
		depthImage.allocateMemory(physicalDevice.getMemoryProperties());
		depthImage.bindMemory();
		depthImageView = new VulkanImageView(device.getHandle(), depthImage.getHandle(),
				depthImage.getFormat(), VK_IMAGE_ASPECT_DEPTH_BIT);

		if (oldSwapchain != VK_NULL_HANDLE) {
			createFrameBuffers();
		}
	}

	private void createFrameBuffers() {
		cleanUpFrameBuffers();
		memFree(frameBuffers);

		frameBuffers = memAllocLong(Config.FRAMES);
		for (int i = 0; i < Config.FRAMES; i++) {
			LongBuffer attachments = memAllocLong(2);
			attachments.put(swapChainImageViews.get(i).getHandle());
			attachments.put(depthImageView.getHandle());
			attachments.flip();
			VkFramebufferCreateInfo framebufferCreateInfo = VkFramebufferCreateInfo.calloc()
					.sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
					.renderPass(renderPass.getHandle())
					.pAttachments(attachments)
					.width(extent.width())
					.height(extent.height())
					.layers(1);
			vkCheck(vkCreateFramebuffer(device.getHandle(), framebufferCreateInfo, null,
					frameBuffers.position(i)));
			framebufferCreateInfo.free();
			memFree(attachments);
		}
	}

	private void cleanUpFrameBuffers() {
		if (frameBuffers == null) return;

		for (int i = 0; i < frameBuffers.limit(); i++) {
			if (frameBuffers.get(i) != VK_NULL_HANDLE) {
				vkDestroyFramebuffer(device.getHandle(), frameBuffers.get(i), null);
			}
		}
	}

	private void createCommandBuffers() {
		VkCommandBufferAllocateInfo commandBufferAllocateInfo = VkCommandBufferAllocateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
				.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
				.commandPool(device.getCommandPool().getHandle()).commandBufferCount(Config.FRAMES);
		PointerBuffer pCommandBuffers = memAllocPointer(Config.FRAMES);
		vkCheck(vkAllocateCommandBuffers(device.getHandle(), commandBufferAllocateInfo,
				pCommandBuffers));
		VkFenceCreateInfo fenceCreateInfo = VkFenceCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO).flags(VK_FENCE_CREATE_SIGNALED_BIT);

		for (int i = 0; i < Config.FRAMES; i++) {
			this.drawCommandBuffers[i] = new VkCommandBuffer(pCommandBuffers.get(i),
					this.device.getHandle());
			vkCheck(vkCreateFence(device.getHandle(), fenceCreateInfo, null,
					commandBufferFences.position(i)));
		}

		fenceCreateInfo.free();
		pCommandBuffers.free();
		commandBufferAllocateInfo.free();
	}

	private int pickPresentMode(IntBuffer pPresentModes, int preferred) {
		int swapchainPresentMode = VK_PRESENT_MODE_FIFO_KHR;
		for (int i = 0; i < pPresentModes.limit(); i++) {
			if (pPresentModes.get(i) == preferred) {
				swapchainPresentMode = preferred;
				break;
			}
		}

		return swapchainPresentMode;
	}

	private SurfaceFormat pickSurfaceFormat(VkSurfaceFormatKHR.Buffer pSurfaceFormats) {
		if (pSurfaceFormats.limit() == 1
				&& pSurfaceFormats.get(0).format() == VK_FORMAT_UNDEFINED) {
			return new SurfaceFormat(VK_FORMAT_B8G8R8A8_UNORM, VK_COLOR_SPACE_SRGB_NONLINEAR_KHR);
		}

		for (int i = 0; i < pSurfaceFormats.limit(); i++) {
			VkSurfaceFormatKHR vkSurfaceFormat = pSurfaceFormats.get(i);
			if (vkSurfaceFormat.format() == VK_FORMAT_B8G8R8A8_UNORM
					&& vkSurfaceFormat.colorSpace() == VK_COLOR_SPACE_SRGB_NONLINEAR_KHR) {
				return new SurfaceFormat(VK_FORMAT_B8G8R8A8_UNORM,
						VK_COLOR_SPACE_SRGB_NONLINEAR_KHR);
			}
		}

		return new SurfaceFormat(pSurfaceFormats.get(0).format(),
				pSurfaceFormats.get(0).colorSpace());
	}

	private void createImageViews() {
		if (swapChainImageViews != null) {
			for (VulkanImageView imageView : swapChainImageViews) {
				imageView.cleanup();
			}
		}
		swapChainImageViews = new ArrayList<>(swapChainImages.size());
		for (VulkanImage swapChainImage : swapChainImages) {
			swapChainImageViews
					.add(new VulkanImageView(device.getHandle(), swapChainImage.getHandle(),
							surfaceFormat.getFormat(), VK_IMAGE_ASPECT_COLOR_BIT));
		}
	}

	private int pickDepthFormat(PhysicalDevice physicalDevice) {
		int[] formats = { VK_FORMAT_D32_SFLOAT_S8_UINT, VK_FORMAT_D24_UNORM_S8_UINT };

		int pickedFormat = VK_FORMAT_UNDEFINED;
		VkFormatProperties formatProperties = VkFormatProperties.calloc();
		int requiredFeature = VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT;
		for (int format : formats) {
			vkGetPhysicalDeviceFormatProperties(physicalDevice.getHandle(), format,
					formatProperties);
			if ((formatProperties.optimalTilingFeatures() & requiredFeature) == requiredFeature) {
				pickedFormat = format;
			}
		}
		formatProperties.free();

		if (pickedFormat == VK_FORMAT_UNDEFINED) {
			throw new AssertionError("Supported format not found");
		}

		return pickedFormat;
	}

	private void createSemaphores() {
		VkSemaphoreCreateInfo semaphoreCreateInfo = VkSemaphoreCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);
		for (int i = 0; i < Config.FRAMES; i++) {
			vkCheck(vkCreateSemaphore(device.getHandle(), semaphoreCreateInfo, null,
					acquireSemaphores.position(i)));
			vkCheck(vkCreateSemaphore(device.getHandle(), semaphoreCreateInfo, null,
					renderCompleteSemaphores.position(i)));
		}
		semaphoreCreateInfo.free();
	}

}
