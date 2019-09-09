package org.vkp.engine.mesh;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.vkp.engine.vulkan.buffer.BufferCreator;
import org.vkp.engine.vulkan.command.StagingBufferCommand;

public class RectangleMesh extends Mesh {

	public RectangleMesh(BufferCreator bufferCreator, StagingBufferCommand stagingBufferCommand) {
		super(bufferCreator, stagingBufferCommand);

		ByteBuffer vertexData = memAlloc(6 * 4 * 4);
		FloatBuffer fb = vertexData.asFloatBuffer();
		fb.put(-0.5f).put(-0.5f).put(0.0f).put(1.0f).put(0.0f).put(0.0f);
		fb.put(-0.5f).put(0.5f).put(0.0f).put(1.0f).put(0.0f).put(1.0f);
		fb.put(0.5f).put(-0.5f).put(0.0f).put(1.0f).put(1.0f).put(0.0f);
		fb.put(0.5f).put(0.5f).put(0.0f).put(1.0f).put(1.0f).put(1.0f);
		fb.flip();

		ByteBuffer indices = memAlloc(6 * 4);
		IntBuffer ib = indices.asIntBuffer();
		ib.put(0).put(1).put(3);
		ib.put(0).put(3).put(2);
		ib.flip();

		createBuffers(vertexData, indices);

		memFree(indices);
		memFree(vertexData);
	}



}
