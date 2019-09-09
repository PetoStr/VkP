package org.vkp.engine.window;

import static org.lwjgl.glfw.GLFW.GLFW_CLIENT_API;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_NO_API;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwHideWindow;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions;
import static org.lwjgl.glfw.GLFWVulkan.glfwVulkanSupported;
import static org.lwjgl.system.MemoryUtil.NULL;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.vkp.engine.Config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class Window implements WindowResizeListener {

	@Getter
	private long id;

	@Getter
	@Setter
	private int width = Config.WIDTH;

	@Getter
	@Setter
	private int height = Config.HEIGHT;

	@Getter
	private boolean vsync = Config.VSYNC;

	@Getter
	private WindowSizeCallback windowSizeCallback;

	private GLFWKeyCallback keyCallback;

	public void createWindow() {
		if (!glfwInit()) {
			throw new AssertionError("Failed to init GLFW");
		}
		if (!glfwVulkanSupported()) {
			throw new AssertionError("Vulkan not supported");
		}

		GLFWErrorCallback.createPrint(System.err).set();

		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, Config.RESIZABLE ? GLFW_TRUE : GLFW_FALSE);
		glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);

		id = glfwCreateWindow(width, height, "VkP", NULL, NULL);

		keyCallback = new GLFWKeyCallback() {
			public void invoke(long window, int key, int scancode, int action, int mods) {
				if (action == GLFW_PRESS && key == GLFW_KEY_ESCAPE) {
					glfwSetWindowShouldClose(window, true);
				}
			}
		};
		glfwSetKeyCallback(id, keyCallback);

		windowSizeCallback = new WindowSizeCallback();
		glfwSetWindowSizeCallback(id, windowSizeCallback);
		windowSizeCallback.registerListener(this);

		log.info("window successfully created");
	}

	public void show() {
		glfwShowWindow(id);
	}

	public void hide() {
		glfwHideWindow(id);
	}

	public void update() {
		glfwPollEvents();
	}

	public void destroyWindow() {
		keyCallback.free();

		glfwDestroyWindow(id);
		glfwTerminate();
		glfwSetErrorCallback(null).free();
		log.info("window destroyed");
	}

	public boolean shouldClose() {
		return glfwWindowShouldClose(id);
	}

	public PointerBuffer getRequiredExtensions() {
		PointerBuffer requiredExtensions = glfwGetRequiredInstanceExtensions();
		if (requiredExtensions == null) {
			throw new AssertionError("Failed to get required vulkan extensions");
		}
		return requiredExtensions;
	}

	@Override
	public void onWindowResize(int width, int height) {
		this.width = width;
		this.height = height;
	}

}
