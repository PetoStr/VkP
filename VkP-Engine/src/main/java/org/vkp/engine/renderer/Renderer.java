package org.vkp.engine.renderer;

import org.lwjgl.vulkan.VkCommandBuffer;
import org.vkp.engine.VkBase;

public abstract class Renderer {

	protected VkBase vkBase;

	protected VkCommandBuffer currentCommandBuffer;

	public Renderer(VkBase vkBase) {
		this.vkBase = vkBase;
	}

	public void init() {
		createDescriptorSetLayouts();
	}

	public void cleanup() {
	}

	public void begin() {
		currentCommandBuffer = vkBase.getSwapChain().getCurrentFrameCommandBuffer();
	}

	public void end() {
	}

	protected abstract void createDescriptorSetLayouts();

}
