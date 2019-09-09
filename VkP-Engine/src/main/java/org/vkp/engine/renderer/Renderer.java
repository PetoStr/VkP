package org.vkp.engine.renderer;

import org.lwjgl.vulkan.VkCommandBuffer;
import org.vkp.engine.VkBase;
import org.vkp.engine.model.Model;
import org.vkp.engine.vulkan.descriptor.DescriptorPool;
import org.vkp.engine.vulkan.descriptor.DescriptorSetLayout;
import org.vkp.engine.vulkan.swapchain.SwapChain;

import lombok.Getter;

public abstract class Renderer {

	@Getter
	public DescriptorSetLayout combinedImageSamplerSetLayout;

	@Getter
	public DescriptorPool descriptorPool;

	protected VkBase vkBase;

	public Renderer(VkBase vkBase) {
		this.vkBase = vkBase;
	}

	public boolean beginRecord() {
		SwapChain swapChain = vkBase.getSwapChain();
		if (!swapChain.beginFrame()) {
			return false;
		}

		beginRecord(swapChain.getCurrentFrameCommandBuffer());

		return true;
	}

	public void recordDraw(Model model) {
		SwapChain swapChain = vkBase.getSwapChain();
		recordCommands(swapChain.getCurrentFrameCommandBuffer(), model);
	}

	public void endRecord() {
		vkBase.getSwapChain().submitFrame();
	}

	public void init() {
		createDescriptorSetLayouts();
		createDescriptorPool();
	}

	public void cleanup() {
		combinedImageSamplerSetLayout.cleanup();
	}

	protected abstract void beginRecord(VkCommandBuffer commandBuffer);
	protected abstract void recordCommands(VkCommandBuffer commandBuffer, Model models);

	protected abstract void createDescriptorSetLayouts();
	protected abstract void createDescriptorPool();

}
