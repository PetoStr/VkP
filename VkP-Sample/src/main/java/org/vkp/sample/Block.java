package org.vkp.sample;

import org.joml.Vector3f;
import org.vkp.engine.model.Model;

import lombok.Data;

@Data
public class Block {

	private Model model;

	private double mass;

	private double speed;

	private double x;

	public Block(Model model, double mass, double speed) {
		this.model = model;
		this.mass = mass;
		this.speed = speed;
	}

	public void update(double frameTime) {
		x += speed * frameTime;
		Vector3f pos = new Vector3f(model.getPosition());
		pos.x = (float) x;
		model.setPosition(pos);
	}

	public boolean intersects(Block block) {
		Model otherModel = block.getModel();
		double thisW = model.getWidth();
		double otherW = otherModel.getWidth();
		double thisX = x - thisW / 2;
		double otherX = otherModel.getPosition().x - otherW / 2;
		return thisX + thisW > otherX && otherX + otherW > thisX;
	}

}
