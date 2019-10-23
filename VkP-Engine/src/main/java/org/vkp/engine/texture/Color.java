package org.vkp.engine.texture;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Color {

	private byte red;
	private byte green;
	private byte blue;
	private byte alpha;

	public Color(int r, int g, int b, int a) {
		this((byte) r, (byte) g, (byte) b, (byte) a);
	}

	public Color(int r, int g, int b) {
		this((byte) r, (byte) g, (byte) b, (byte) 255);
	}

}
