package engine;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWVulkan.*;
import static org.lwjgl.system.MemoryUtil.*;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;

import lombok.extern.log4j.Log4j2;

@Log4j2
public final class WindowManager {
	
	private static long window;

	private static GLFWWindowSizeCallback sizeCallback;
	
	private static GLFWKeyCallback keyCallback;
	
	private static boolean created = false;
	
	public static void createWindow() {
		if (created) {
			log.warn("window has already been created");
			return;
		}
		created = true;
		
		if (!glfwInit()) {
			throw new RuntimeException("Failed to init GLFW");
		}
		if (!glfwVulkanSupported()) {
			throw new AssertionError("Vulkan not supported");
		}

		GLFWErrorCallback.createPrint(System.err).set();
		
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
		glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
		
		window = glfwCreateWindow(1280, 720, "VkP", NULL, NULL);
		
		keyCallback = new GLFWKeyCallback() {			
			public void invoke(long window, int key, int scancode, int action, int mods) {
				if (action == GLFW_PRESS && key == GLFW_KEY_ESCAPE) {
					glfwSetWindowShouldClose(window, true);
				}
			}
		};
		glfwSetKeyCallback(window, keyCallback);
		
		sizeCallback = new GLFWWindowSizeCallback() {			
			public void invoke(long window, int width, int height) {
				if (width <= 0 || height <= 0) {
					return;
				}
			}
		};
		
		glfwShowWindow(window);
		log.info("window successfully created");
	}
	
	public static void update() {
		glfwPollEvents();
	}
	
	public static void destroyWindow() {
		if (!created) {
			log.warn("cannot destroy window because it has not been created");
			return;
		}
		created = false;
		
		keyCallback.free();
		sizeCallback.free();
		
		glfwDestroyWindow(window);
		glfwTerminate();
		glfwSetErrorCallback(null).free();
		log.info("window destroyed");
	}
	
	public static boolean windowShouldClose() {
		return glfwWindowShouldClose(window);
	}
	
}
