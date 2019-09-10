package org.vkp.engine.renderer;

import org.lwjgl.vulkan.VkCommandBuffer;
import org.vkp.engine.VkBase;
import org.vkp.engine.vulkan.descriptor.DescriptorPool;

import lombok.Getter;

public abstract class Renderer {

	@Getter
	public DescriptorPool descriptorPool;

	protected VkBase vkBase;

	protected VkCommandBuffer currentCommandBuffer;

	public Renderer(VkBase vkBase) {
		this.vkBase = vkBase;
	}

	public void init() {
		createDescriptorSetLayouts();
		createDescriptorPool();
	}

	public void cleanup() {
		descriptorPool.cleanup();
	}

	public void begin() {
		currentCommandBuffer = vkBase.getSwapChain().getCurrentFrameCommandBuffer();
	}

	public void end() {
	}

	protected abstract void createDescriptorSetLayouts();
	protected abstract void createDescriptorPool();

}
