package org.vkp.engine.mesh;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;

import java.nio.ByteBuffer;

import org.vkp.engine.vulkan.buffer.BufferCreator;
import org.vkp.engine.vulkan.buffer.VulkanBuffer;
import org.vkp.engine.vulkan.command.StagingBufferCommand;

import lombok.Getter;

public class Mesh {

	@Getter
	private VulkanBuffer vertexBuffer;

	@Getter
	private VulkanBuffer indexBuffer;

	@Getter
	private int indexCount;

	private BufferCreator bufferCreator;

	private StagingBufferCommand stagingBufferCommand;

	public Mesh(BufferCreator bufferCreator, StagingBufferCommand stagingBufferCommand) {
		this.bufferCreator = bufferCreator;
		this.stagingBufferCommand = stagingBufferCommand;
	}

	public void createBuffers(ByteBuffer vertexData, ByteBuffer indices) {
		vertexBuffer = bufferCreator.createBuffer(vertexData.capacity(),
				VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
				VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT);
		stagingBufferCommand.copyToBuffer(vertexData, vertexBuffer.getHandle());

		indexBuffer = bufferCreator.createBuffer(indices.capacity(),
				VK_BUFFER_USAGE_INDEX_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
				VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT);
		stagingBufferCommand.copyToBuffer(indices, indexBuffer.getHandle());

		indexCount = indices.asIntBuffer().capacity();
	}

	public void cleanup() {
		vertexBuffer.cleanup();
		indexBuffer.cleanup();
	}

}
