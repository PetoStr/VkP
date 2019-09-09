package org.vkp.engine;

import static org.lwjgl.system.MemoryUtil.memUTF8;

import java.nio.ByteBuffer;

public final class Config {

	public static ByteBuffer[] requiredLayers = {
		memUTF8("VK_LAYER_LUNARG_standard_validation")
	};

	/* TODO add to some config file or something similar */
	public static final boolean ENABLE_LAYERS = true;

	public static final int FRAMES = 3;

	public static final boolean VSYNC = true;

	public static final int WIDTH = 1280;
	public static final int HEIGHT = 720;
	public static final boolean RESIZABLE = false;


	private Config() { }

}
