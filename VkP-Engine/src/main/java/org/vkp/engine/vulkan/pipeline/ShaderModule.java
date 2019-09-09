package org.vkp.engine.vulkan.pipeline;

import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.system.MemoryUtil.memUTF8;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.vkCreateShaderModule;
import static org.lwjgl.vulkan.VK10.vkDestroyShaderModule;
import static org.vkp.engine.vulkan.VkUtil.vkCheck;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import org.vkp.engine.util.FileUtil;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ShaderModule {

	public static final ByteBuffer ENTRY_POINT_NAME = memUTF8("main");

	@Getter
	private long handle;

	@Getter
	private int stage;

	private VkDevice device;

	public ShaderModule(VkDevice device, String shaderPath, int stage) {
		this.device = device;
		this.stage = stage;

		ByteBuffer pCode;
		try {
			pCode = FileUtil.readResourceFile(shaderPath);
		} catch (IOException e) {
			log.error("", e);
			throw new AssertionError("failed to read file");
		}
		VkShaderModuleCreateInfo shaderModuleCreateInfo = VkShaderModuleCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
				.pCode(pCode);

		LongBuffer pShaderModule = memAllocLong(1);
		vkCheck(vkCreateShaderModule(device, shaderModuleCreateInfo, null, pShaderModule));
		this.handle = pShaderModule.get(0);
		memFree(pShaderModule);
	}

	public void cleanup() {
		vkDestroyShaderModule(device, handle, null);
	}

}
