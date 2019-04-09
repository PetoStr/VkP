package engine;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.EXTDebugReport.*;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.ByteBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;

public class VkBase {

	private static ByteBuffer[] requiredLayers = {
		memUTF8("VK_LAYER_LUNARG_standard_validation")
	};
	
	public static VkInstance createInstance(PointerBuffer requiredExtensions) {
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
		
		PointerBuffer layers = memAllocPointer(requiredLayers.length);
		for (int i = 0; i < requiredLayers.length; i++) {
			layers.put(requiredLayers[i]);
		}
		layers.flip();
		
		VkInstanceCreateInfo createInfo = VkInstanceCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
				.pApplicationInfo(appInfo)
				.ppEnabledExtensionNames(extensions)
				.ppEnabledLayerNames(layers);
		
		PointerBuffer pInstance = memAllocPointer(1);
		int err = vkCreateInstance(createInfo, null, pInstance);
		if (err != VK_SUCCESS) {
			throw new AssertionError("Failed to create VkInstance: " + err);
		}		
		long instance = pInstance.get(0);
		memFree(pInstance);
		
		VkInstance ret = new VkInstance(instance, createInfo);
		createInfo.free();
		
		memFree(layers);
		memFree(extensions);		
		memFree(debugReportExtension);
		memFree(appInfo.pApplicationName());
		memFree(appInfo.pEngineName());
		appInfo.free();
		
		return ret;
	}
	
}
