package org.vkp.racing.math;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2f;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class Polygon {

	@Getter
	private List<Vector2f> points = new ArrayList<>();

}
