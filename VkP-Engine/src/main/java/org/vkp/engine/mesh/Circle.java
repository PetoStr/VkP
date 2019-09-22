package org.vkp.engine.mesh;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.vkp.engine.vulkan.buffer.BufferCreator;
import org.vkp.engine.vulkan.command.StagingBufferCommand;

public class Circle extends Mesh {

	public Circle(BufferCreator bufferCreator, StagingBufferCommand stagingBufferCommand) {
		super(bufferCreator, stagingBufferCommand);

		final int edges = 60;
		final float part = (float) (2.0f * Math.PI / edges);

		ByteBuffer vertexData = memAlloc((edges + 1) * 6 * 4);
		FloatBuffer fb = vertexData.asFloatBuffer();
		fb.put(0.0f).put(0.0f).put(0.0f).put(1.0f).put(0.5f).put(0.5f);
		float angle = 0.0f;
		for (int i = 1; i <= edges; i++, angle += part) {
			float x = (float) Math.sin(angle) * 0.5f;
			float y = (float) Math.cos(angle) * 0.5f;
			float s = (x + 1.0f) / 2.0f;
			float t = (y + 1.0f) / 2.0f;
			fb.put(x).put(y).put(0.0f).put(1.0f).put(s).put(t);
		}
		fb.flip();

		ByteBuffer indices = memAlloc(edges * 3 * 4);
		IntBuffer ib = indices.asIntBuffer();
		for (int i = 1; i <= edges; i++) {
			ib.put(0).put(i).put((i % edges) + 1);
		}
		ib.flip();

		createBuffers(vertexData, indices);

		memFree(indices);
		memFree(vertexData);
	}



}
