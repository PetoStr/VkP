package org.vkp.engine.window;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFWWindowSizeCallbackI;

public class WindowSizeCallback implements GLFWWindowSizeCallbackI {

	private List<WindowResizeListener> windowResizeListeners = new ArrayList<>();

	public void registerListener(WindowResizeListener windowResizeListener) {
		windowResizeListeners.add(windowResizeListener);
	}

	public void removeListener(WindowResizeListener windowResizeListener) {
		windowResizeListeners.remove(windowResizeListener);
	}

	@Override
	public void invoke(long window, int width, int height) {
		if (width <= 0 || height <= 0) {
			return;
		}
		notifyAllListeners(width, height);
	}

	private void notifyAllListeners(int newWidth, int newHeight) {
		windowResizeListeners.forEach(listener -> listener.onWindowResize(newWidth, newHeight));
	}

}