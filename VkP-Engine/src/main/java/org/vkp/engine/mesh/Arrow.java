package org.vkp.engine.mesh;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.vkp.engine.vulkan.buffer.BufferCreator;
import org.vkp.engine.vulkan.command.StagingBufferCommand;

public class Arrow extends Mesh {

	public Arrow(BufferCreator bufferCreator, StagingBufferCommand stagingBufferCommand) {
		super(bufferCreator, stagingBufferCommand);

		ByteBuffer vertexData = memAlloc(7 * 6 * 4);
		FloatBuffer fb = vertexData.asFloatBuffer();
		fb.put(0.0f).put(-0.866f).put(0.0f).put(1.0f).put(0.5f).put(0.0f);
		fb.put(-0.5f).put(0.0f).put(0.0f).put(1.0f).put(0.0f).put(0.5f);
		fb.put(0.5f).put(0.0f).put(0.0f).put(1.0f).put(1.0f).put(0.5f);

		fb.put(-0.3f).put(0.0f).put(0.0f).put(1.0f).put(0.2f).put(0.5f);
		fb.put(-0.3f).put(1.0f).put(0.0f).put(1.0f).put(0.2f).put(1.0f);
		fb.put(0.3f).put(0.0f).put(0.0f).put(1.0f).put(0.8f).put(0.5f);
		fb.put(0.3f).put(1.0f).put(0.0f).put(1.0f).put(0.8f).put(1.0f);
		fb.flip();

		ByteBuffer indices = memAlloc(3 * 3 * 4);
		IntBuffer ib = indices.asIntBuffer();
		ib.put(0).put(1).put(2);
		ib.put(3).put(4).put(5);
		ib.put(4).put(6).put(5);
		ib.flip();

		createBuffers(vertexData, indices);

		memFree(indices);
		memFree(vertexData);
	}

}
