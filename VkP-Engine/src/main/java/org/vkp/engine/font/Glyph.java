package org.vkp.engine.font;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Glyph {

	private int x;
	private int y;
	private int width;
	private int height;
	private int xOffset;
	private int yOffset;
	private int xAdvance;

}
