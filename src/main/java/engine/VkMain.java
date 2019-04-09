package engine;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class VkMain {
	
	public static void main(String[] args) {
		try {
			new VkPLoop().start();
		} catch (Exception e) {
			log.fatal("unexpected error", e);
		}
	}
	
}
