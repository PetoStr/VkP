package engine;

public class VkPLoop {

	public VkPLoop() {
		WindowManager.createWindow();
	}
	
	public void start() {
		loop();
	}
	
	private void loop() {
		while (!WindowManager.windowShouldClose()) {
			WindowManager.update();
		}
		
		WindowManager.destroyWindow();
	}
	
}
