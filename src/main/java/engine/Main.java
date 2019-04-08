package engine;

import static org.lwjgl.system.MemoryUtil.*;

import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWVulkan.*;

public class Main {
	
	public static void main(String[] args) {
		if (!glfwInit()) {
			throw new RuntimeException("Failed to init GLFW");
		}
		if (!glfwVulkanSupported()) {
			throw new AssertionError("Vulkan not supported");
		}
		
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
		glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
		
		long window = glfwCreateWindow(1280, 720, "VkP", NULL, NULL);
		
		GLFWKeyCallback keyCallback = new GLFWKeyCallback() {			
			public void invoke(long window, int key, int scancode, int action, int mods) {
				if (action == GLFW_PRESS && key == GLFW_KEY_ESCAPE) {
					glfwSetWindowShouldClose(window, true);
				}
			}
		};
		glfwSetKeyCallback(window, keyCallback);
		
		GLFWWindowSizeCallback sizeCallback = new GLFWWindowSizeCallback() {			
			public void invoke(long window, int width, int height) {
				if (width <= 0 || height <= 0) {
					return;
				}
			}
		};
		
		glfwShowWindow(window);
		
		while (!glfwWindowShouldClose(window)) {
			glfwPollEvents();
		}
		
		keyCallback.free();
		sizeCallback.free();
		
		glfwDestroyWindow(window);
		glfwTerminate();
	}
	
}
