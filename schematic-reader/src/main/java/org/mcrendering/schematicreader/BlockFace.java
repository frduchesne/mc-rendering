package org.mcrendering.schematicreader;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class BlockFace {

	private Vector3f from;
	private Vector3f to;
	private Vector2f uvFrom;
	private Vector2f uvTo;
	private int textureOffset;
	private FaceRotation rotation;
	
	public int getTextureOffset() {
		return textureOffset;
	}
	public void setTextureOffset(int textureOffset) {
		this.textureOffset = textureOffset;
	}

	public Vector3f getFrom() {
		return from;
	}
	public void setFrom(Vector3f from) {
		this.from = from;
	}
	public Vector3f getTo() {
		return to;
	}
	public void setTo(Vector3f to) {
		this.to = to;
	}
	public Vector2f getUvFrom() {
		return uvFrom;
	}
	public void setUvFrom(Vector2f uvFrom) {
		this.uvFrom = uvFrom;
	}
	public Vector2f getUvTo() {
		return uvTo;
	}
	public void setUvTo(Vector2f uvTo) {
		this.uvTo = uvTo;
	}
	public FaceRotation getRotation() {
		return rotation;
	}
	public void setRotation(FaceRotation rotation) {
		this.rotation = rotation;
	}

	@Override
	public String toString() {
		return "BlockFace [from=" + from + ", to=" + to + ", uvFrom=" + uvFrom + ", uvTo=" + uvTo + ", textureOffset="
				+ textureOffset + ", rotation=" + rotation + "]";
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
