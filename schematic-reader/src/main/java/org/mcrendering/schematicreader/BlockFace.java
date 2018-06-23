package org.mcrendering.schematicreader;

import org.joml.Vector2i;
import org.joml.Vector3i;

public class BlockFace {

	private Vector3i from;
	private Vector3i to;
	private Vector2i u;
	private Vector2i v;
	private String texture;
	public Vector3i getFrom() {
		return from;
	}
	public void setFrom(Vector3i from) {
		this.from = from;
	}
	public Vector3i getTo() {
		return to;
	}
	public void setTo(Vector3i to) {
		this.to = to;
	}
	public Vector2i getU() {
		return u;
	}
	public void setU(Vector2i u) {
		this.u = u;
	}
	public Vector2i getV() {
		return v;
	}
	public void setV(Vector2i v) {
		this.v = v;
	}
	public String getTexture() {
		return texture;
	}
	public void setTexture(String texture) {
		this.texture = texture;
	}
	@Override
	public String toString() {
		return "BlockFace [from=" + from + ", to=" + to + ", u=" + u + ", v=" + v + ", texture=" + texture + "]";
	}
	
	public boolean isValid() {
		boolean valid = true;
		if (to == null) {
			System.err.println("to null");
			valid = false;
		}
		if (from == null) {
			System.err.println("from null");
			valid = false;
		}
		if (texture == null) {
			System.err.println("texture null");
			valid = false;
		}
		
		return valid;
	}
	
}
