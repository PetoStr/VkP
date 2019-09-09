package org.vkp.engine.util;

import static org.lwjgl.stb.STBImage.STBI_rgb_alpha;
import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_info_from_memory;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memAllocInt;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.vkp.engine.texture.TextureInfo;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class FileUtil {

	private FileUtil() {

	}

	public static ByteBuffer readResourceFile(String filePath) throws IOException {
		ByteBuffer buffer = null;

		try (InputStream inputStream = getResourceURL(filePath).openStream()) {
			byte[] bytes = inputStream.readAllBytes();
			buffer = memAlloc(bytes.length);
			buffer.put(bytes);
			buffer.flip();
		}

		return buffer;
	}

	public static ByteBuffer readImage(ByteBuffer fileData) {
		IntBuffer width = memAllocInt(1);
		IntBuffer height = memAllocInt(1);
		IntBuffer channel = memAllocInt(1);
		ByteBuffer res = stbi_load_from_memory(fileData, width, height, channel, STBI_rgb_alpha);
		if (res == null) {
			log.error("Failed to load image: {}", stbi_failure_reason());
		}
		return res;
	}

	public static TextureInfo readImageInfo(ByteBuffer fileData) {
		IntBuffer width = memAllocInt(1);
		IntBuffer height = memAllocInt(1);
		IntBuffer channel = memAllocInt(1);
		if (!stbi_info_from_memory(fileData, width, height, channel)) {
			log.error("Failed to load image info: {}", stbi_failure_reason());
		}
		return new TextureInfo(width.get(0), height.get(0), channel.get(0));
	}

	public static URL getResourceURL(String path) throws FileNotFoundException {
		URL url = Thread.currentThread().getContextClassLoader().getResource(path);
		if (url == null) {
			throw new FileNotFoundException(path);
		}
		return url;
	}
}
