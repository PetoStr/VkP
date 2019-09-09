package org.vkp.engine.vulkan.command;

import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_MEMORY_WRITE_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_SHADER_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_TRANSFER_WRITE_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_UNIFORM_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_VERTEX_ATTRIBUTE_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_TRANSFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_VERTEX_INPUT_BIT;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_VERTEX_SHADER_BIT;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_FAMILY_IGNORED;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_BUFFER_MEMORY_BARRIER;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO;
import static org.lwjgl.vulkan.VK10.VK_WHOLE_SIZE;
import static org.lwjgl.vulkan.VK10.vkBeginCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdCopyBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdCopyBufferToImage;
import static org.lwjgl.vulkan.VK10.vkCmdPipelineBarrier;
import static org.lwjgl.vulkan.VK10.vkEndCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkQueueSubmit;
import static org.lwjgl.vulkan.VK10.vkQueueWaitIdle;
import static org.vkp.engine.vulkan.VkUtil.vkCheck;

import java.nio.ByteBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferImageCopy;
import org.lwjgl.vulkan.VkBufferMemoryBarrier;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent3D;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkImageSubresourceLayers;
import org.lwjgl.vulkan.VkImageSubresourceRange;
import org.lwjgl.vulkan.VkOffset3D;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.vkp.engine.texture.TextureInfo;
import org.vkp.engine.vulkan.buffer.VulkanBuffer;

public class StagingBufferCommand {

	private VkDevice device;
	private VkQueue queue;
	private VkCommandBuffer commandBuffer;
	private VkPhysicalDeviceMemoryProperties memoryProperties;
	
	private VulkanBuffer stagingBuffer;
	
	public StagingBufferCommand(VkDevice device, VkQueue queue, VkCommandBuffer commandBuffer,
						 VkPhysicalDeviceMemoryProperties memoryProperties) {
		this.device = device;
		this.queue = queue;
		this.commandBuffer = commandBuffer;
		this.memoryProperties = memoryProperties;
	}
	
	public void copyToBuffer(ByteBuffer data, long dstBuffer) {
		createStagingBuffer(data.capacity());
		stagingBuffer.copy(data);
		
		VkCommandBufferBeginInfo commandBufferBeginInfo = VkCommandBufferBeginInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
				.flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);

		vkBeginCommandBuffer(commandBuffer, commandBufferBeginInfo);
		
		VkBufferCopy.Buffer regions = VkBufferCopy.calloc(1)
				.srcOffset(0)
				.dstOffset(0)
				.size(data.capacity());
		vkCmdCopyBuffer(commandBuffer, stagingBuffer.getHandle(), dstBuffer, regions);
		
		VkBufferMemoryBarrier.Buffer bufferMemoryBarriers = VkBufferMemoryBarrier.calloc(1)
				.sType(VK_STRUCTURE_TYPE_BUFFER_MEMORY_BARRIER)
				.srcAccessMask(VK_ACCESS_MEMORY_WRITE_BIT)
				.dstAccessMask(VK_ACCESS_VERTEX_ATTRIBUTE_READ_BIT)
				.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
				.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
				.buffer(dstBuffer)
				.offset(0)
				.size(data.capacity());
		vkCmdPipelineBarrier(commandBuffer, VK_PIPELINE_STAGE_TRANSFER_BIT,
				VK_PIPELINE_STAGE_VERTEX_INPUT_BIT, 0, null, bufferMemoryBarriers, null);
		
		vkEndCommandBuffer(commandBuffer);
		
		PointerBuffer pCommandBuffers = memAllocPointer(1);
		pCommandBuffers.put(0, commandBuffer.address());
		VkSubmitInfo submitInfo = VkSubmitInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
				.pCommandBuffers(pCommandBuffers);		
		vkCheck(vkQueueSubmit(queue, submitInfo, VK_NULL_HANDLE));
		
		submitInfo.free();
		bufferMemoryBarriers.free();
		regions.free();
		commandBufferBeginInfo.free();
		
		vkQueueWaitIdle(queue); // TODO synchronization
		
		stagingBuffer.cleanup();
	}

	public void copyToUniformBuffer(ByteBuffer data, long uniformBuffer) {
		createStagingBuffer(data.capacity());
		stagingBuffer.copy(data);
		
		VkCommandBufferBeginInfo commandBufferBeginInfo = VkCommandBufferBeginInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
				.flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);

		vkBeginCommandBuffer(commandBuffer, commandBufferBeginInfo);

		VkBufferCopy.Buffer regions = VkBufferCopy.calloc(1)
				.srcOffset(0)
				.dstOffset(0)
				.size(data.capacity());
		vkCmdCopyBuffer(commandBuffer, stagingBuffer.getHandle(), uniformBuffer, regions);

		VkBufferMemoryBarrier.Buffer bufferMemoryBarriers = VkBufferMemoryBarrier.calloc(1)
				.sType(VK_STRUCTURE_TYPE_BUFFER_MEMORY_BARRIER)
				.srcAccessMask(VK_ACCESS_MEMORY_WRITE_BIT)
				.dstAccessMask(VK_ACCESS_UNIFORM_READ_BIT)
				.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
				.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
				.buffer(uniformBuffer)
				.offset(0)
				.size(VK_WHOLE_SIZE);
		vkCmdPipelineBarrier(commandBuffer, VK_PIPELINE_STAGE_TRANSFER_BIT,
				VK_PIPELINE_STAGE_VERTEX_SHADER_BIT, 0, null, bufferMemoryBarriers, null);

		vkEndCommandBuffer(commandBuffer);

		PointerBuffer pCommandBuffers = memAllocPointer(1);
		pCommandBuffers.put(0, commandBuffer.address());
		VkSubmitInfo submitInfo = VkSubmitInfo.calloc().sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
				.pCommandBuffers(pCommandBuffers);
		vkCheck(vkQueueSubmit(queue, submitInfo, VK_NULL_HANDLE));

		submitInfo.free();
		bufferMemoryBarriers.free();
		regions.free();
		commandBufferBeginInfo.free();

		vkQueueWaitIdle(queue); // TODO synchronization
		
		stagingBuffer.cleanup();
	}

	public void copyToImage(ByteBuffer data, long image, TextureInfo metaData) {
		createStagingBuffer(data.capacity());
		stagingBuffer.copy(data);
		
		VkCommandBufferBeginInfo commandBufferBeginInfo = VkCommandBufferBeginInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
				.flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);

		vkBeginCommandBuffer(commandBuffer, commandBufferBeginInfo);

		VkImageSubresourceRange subresourceRange = VkImageSubresourceRange.calloc()
				.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
				.baseMipLevel(0)
				.levelCount(1)
				.baseArrayLayer(0)
				.layerCount(1);
		VkImageMemoryBarrier.Buffer fromUndefinedToTransferDst = VkImageMemoryBarrier.calloc(1)
				.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
				.srcAccessMask(0)
				.dstAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT)
				.oldLayout(VK_IMAGE_LAYOUT_UNDEFINED)
				.newLayout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
				.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
				.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
				.image(image)
				.subresourceRange(subresourceRange);
		vkCmdPipelineBarrier(commandBuffer, VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT,
				VK_PIPELINE_STAGE_TRANSFER_BIT, 0, null, null, fromUndefinedToTransferDst);

		VkImageSubresourceLayers subresourceLayers = VkImageSubresourceLayers.calloc()
				.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
				.mipLevel(0)
				.baseArrayLayer(0)
				.layerCount(1);
		VkOffset3D offset = VkOffset3D.calloc()
				.x(0)
				.y(0)
				.z(0);
		VkExtent3D extent = VkExtent3D.calloc()
				.width(metaData.getWidth())
				.height(metaData.getHeight())
				.depth(1);
		VkBufferImageCopy.Buffer bufferImageCopy = VkBufferImageCopy.calloc(1)
				.bufferOffset(0)
				.bufferRowLength(0)
				.imageSubresource(subresourceLayers)
				.imageOffset(offset)
				.imageExtent(extent);
		vkCmdCopyBufferToImage(commandBuffer, stagingBuffer.getHandle(), image,
				VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, bufferImageCopy);

		VkImageMemoryBarrier.Buffer fromTransferDstToShaderRead = VkImageMemoryBarrier.calloc(1)
				.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
				.srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT)
				.dstAccessMask(VK_ACCESS_SHADER_READ_BIT)
				.oldLayout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
				.newLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
				.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
				.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
				.image(image)
				.subresourceRange(subresourceRange);
		vkCmdPipelineBarrier(commandBuffer, VK_PIPELINE_STAGE_TRANSFER_BIT,
				VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, 0, null, null, fromTransferDstToShaderRead);

		vkEndCommandBuffer(commandBuffer);

		PointerBuffer pCommandBuffers = memAllocPointer(1);
		pCommandBuffers.put(0, commandBuffer.address());

		VkSubmitInfo submitInfo = VkSubmitInfo.calloc().sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
				.pCommandBuffers(pCommandBuffers);
		vkCheck(vkQueueSubmit(queue, submitInfo, VK_NULL_HANDLE));

		submitInfo.free();
		memFree(pCommandBuffers);
		fromTransferDstToShaderRead.free();
		bufferImageCopy.free();
		extent.free();
		offset.free();
		subresourceLayers.free();
		fromUndefinedToTransferDst.free();
		subresourceRange.free();
		commandBufferBeginInfo.free();

		vkQueueWaitIdle(queue); // TODO synchronization
		
		stagingBuffer.cleanup();
	}
	
	private void createStagingBuffer(long size) {
		stagingBuffer = new VulkanBuffer(device, size,
				VK_BUFFER_USAGE_TRANSFER_SRC_BIT);
		stagingBuffer.allocate(memoryProperties,
				VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
		stagingBuffer.bindBufferMemory();
	}

}
