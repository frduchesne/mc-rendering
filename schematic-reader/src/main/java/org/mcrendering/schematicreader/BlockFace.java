package org.mcrendering.schematicreader;

import org.joml.Vector2i;
import org.joml.Vector3i;

public class BlockFace {

	private Vector3i from;
	private Vector3i to;
	private Vector2i uvFrom;
	private Vector2i uvTo;
	private int textureOffset;
	
	public int getTextureOffset() {
		return textureOffset;
	}
	public void setTextureOffset(int textureOffset) {
		this.textureOffset = textureOffset;
	}

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
	public Vector2i getUvFrom() {
		return uvFrom;
	}
	public void setUvFrom(Vector2i uvFrom) {
		this.uvFrom = uvFrom;
	}
	public Vector2i getUvTo() {
		return uvTo;
	}
	public void setUvTo(Vector2i uvTo) {
		this.uvTo = uvTo;
	}

	@Override
	public String toString() {
		return "BlockFace [from=" + from + ", to=" + to + ", uvFrom=" + uvFrom + ", uvTo=" + uvTo + ", textureOffset="
				+ textureOffset + "]";
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
		
		return valid;
	}
	
}
