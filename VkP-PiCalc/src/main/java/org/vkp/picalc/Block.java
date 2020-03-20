package org.vkp.picalc;

import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.vkp.engine.mesh.TexturedMesh;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Block {

	private Vector2d position;

	private double mass;

	private double speed;

	private double width;

	private double height;

	private TexturedMesh texturedMesh;

	public Block(TexturedMesh texturedMesh, Vector2d position, double width, double height,
			double mass, double speed) {
		this.texturedMesh = texturedMesh;
		this.position = position;
		this.width = width;
		this.height = height;
		this.mass = mass;
		this.speed = speed;
	}

	public void update() {
		position.x += speed;
	}

	public boolean intersects(Block block) {
		double thisW = getWidth();
		double otherW = block.getWidth();
		double thisX = position.x - thisW / 2;
		double otherX = block.getPosition().x - otherW / 2;
		return thisX + thisW > otherX && otherX + otherW > thisX;
	}

	public Matrix4f calculateModelMatrix() {
		Matrix4f mMatrix = new Matrix4f();
		mMatrix.translation((float) position.x, (float) position.y, 0.0f);
		mMatrix.scaleXY((float) width, (float) height);
		return mMatrix;
	}

}
