package org.vkp.engine.window;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFWKeyCallbackI;

public class KeyCallback implements GLFWKeyCallbackI {

	private List<KeyListener> keyListeners = new ArrayList<>();

	public void registerListener(KeyListener keyListener) {
		keyListeners.add(keyListener);
	}

	public void removeListener(KeyListener keyListener) {
		keyListeners.remove(keyListener);
	}

	@Override
	public void invoke(long window, int key, int scancode, int action, int mods) {
		keyListeners.forEach(listener -> listener.onKeyAction(action, key, mods));
	}

}
