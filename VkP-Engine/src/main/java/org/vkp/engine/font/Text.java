package org.vkp.engine.font;

import org.joml.Vector2f;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Text {

	private String value;
	private Vector2f position = new Vector2f();
	private float scale;

}
