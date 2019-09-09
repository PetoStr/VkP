package org.vkp.engine.vulkan;

import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.system.MemoryUtil.memUTF8;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_ERROR_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_WARNING_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_EXT_DEBUG_REPORT_EXTENSION_NAME;
import static org.lwjgl.vulkan.EXTDebugReport.VK_STRUCTURE_TYPE_DEBUG_REPORT_CALLBACK_CREATE_INFO_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.vkCreateDebugReportCallbackEXT;
import static org.lwjgl.vulkan.EXTDebugReport.vkDestroyDebugReportCallbackEXT;
import static org.lwjgl.vulkan.VK10.VK_MAKE_VERSION;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_APPLICATION_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.vkCreateInstance;
import static org.lwjgl.vulkan.VK10.vkDestroyInstance;
import static org.vkp.engine.vulkan.VkUtil.vkCheck;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkDebugReportCallbackCreateInfoEXT;
import org.lwjgl.vulkan.VkDebugReportCallbackEXT;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.vkp.engine.Config;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class VulkanInstance {

	@Getter
	private VkInstance handle;

	private long debugCallbackHandle;

	public VulkanInstance(PointerBuffer requiredExtensions) {
		VkApplicationInfo appInfo = VkApplicationInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
				.pApplicationName(memUTF8("VkP"))
				.pEngineName(memUTF8(""))
				.apiVersion(VK_MAKE_VERSION(1, 0, 2));

		ByteBuffer debugReportExtension = memUTF8(VK_EXT_DEBUG_REPORT_EXTENSION_NAME);
		PointerBuffer extensions = memAllocPointer(requiredExtensions.remaining() + 1);
		extensions.put(requiredExtensions);
		extensions.put(debugReportExtension);
		extensions.flip();

		PointerBuffer layers = memAllocPointer(Config.requiredLayers.length);
		if (Config.ENABLE_LAYERS) {
			for (int i = 0; i < Config.requiredLayers.length; i++) {
				layers.put(Config.requiredLayers[i]);
			}
		}
		layers.flip();

		VkInstanceCreateInfo createInfo = VkInstanceCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
				.pApplicationInfo(appInfo)
				.ppEnabledExtensionNames(extensions)
				.ppEnabledLayerNames(layers);

		PointerBuffer pInstance = memAllocPointer(1);
		vkCheck(vkCreateInstance(createInfo, null, pInstance));
		long instance = pInstance.get(0);
		memFree(pInstance);

		handle = new VkInstance(instance, createInfo);
		createInfo.free();

		memFree(layers);
		memFree(extensions);
		memFree(debugReportExtension);
		memFree(appInfo.pApplicationName());
		memFree(appInfo.pEngineName());
		appInfo.free();

		setupDebug();
	}

	public void cleanup() {
		vkDestroyDebugReportCallbackEXT(handle, debugCallbackHandle, null);
		vkDestroyInstance(handle, null);
	}

	private void setupDebug() {
		VkDebugReportCallbackEXT debugCallback = new VkDebugReportCallbackEXT() {
			public int invoke(int flags, int objectType, long object, long location, int messageCode, long pLayerPrefix,
					long pMessage, long pUserData) {
				switch (flags) {
					case VK_DEBUG_REPORT_ERROR_BIT_EXT:
						log.error(VkDebugReportCallbackEXT.getString(pMessage));
						break;
					case VK_DEBUG_REPORT_WARNING_BIT_EXT:
						log.warn(VkDebugReportCallbackEXT.getString(pMessage));
						break;
				}
				return 0;
			}
		};

		VkDebugReportCallbackCreateInfoEXT debugCreateInfo = VkDebugReportCallbackCreateInfoEXT.calloc()
				.sType(VK_STRUCTURE_TYPE_DEBUG_REPORT_CALLBACK_CREATE_INFO_EXT)
				.pfnCallback(debugCallback)
				.flags(VK_DEBUG_REPORT_ERROR_BIT_EXT | VK_DEBUG_REPORT_WARNING_BIT_EXT);
		LongBuffer pCallback = memAllocLong(1);
		vkCheck(vkCreateDebugReportCallbackEXT(handle, debugCreateInfo, null, pCallback));
		debugCallbackHandle = pCallback.get(0);
		memFree(pCallback);
		debugCreateInfo.free();
	}

}
