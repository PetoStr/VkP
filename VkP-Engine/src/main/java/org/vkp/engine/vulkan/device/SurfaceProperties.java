package org.vkp.engine.vulkan.device;

import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.vkp.engine.vulkan.VkUtil.vkCheck;

import java.nio.IntBuffer;

import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

import lombok.Getter;

public class SurfaceProperties {

	@Getter
	private IntBuffer presentModes;
	
	@Getter
	private VkSurfaceCapabilitiesKHR surfaceCapabilities;
	
	@Getter
	private VkSurfaceFormatKHR.Buffer surfaceFormats;
	
	public SurfaceProperties(VkPhysicalDevice physicalDevice, long surface) {
		IntBuffer pPresentModeCount = memAllocInt(1);
        vkCheck(vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surface, pPresentModeCount, null));
        int presentModeCount = pPresentModeCount.get(0);
        presentModes = memAllocInt(presentModeCount);
        vkCheck(vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surface, pPresentModeCount, presentModes));        
        memFree(pPresentModeCount);

        surfaceCapabilities = VkSurfaceCapabilitiesKHR.calloc();
        vkCheck(vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice, surface, surfaceCapabilities));
        
        IntBuffer pSurfaceFormatCount = memAllocInt(1);
		vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surface, pSurfaceFormatCount, null);
		int surfaceFormatCount = pSurfaceFormatCount.get(0);
		surfaceFormats = VkSurfaceFormatKHR.calloc(surfaceFormatCount);
		vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surface, pSurfaceFormatCount, surfaceFormats);
		memFree(pSurfaceFormatCount);
	}
	
	public void cleanup() {
		memFree(presentModes);
		surfaceCapabilities.free();
		surfaceFormats.free();
	}
	
}
