package org.mcrendering.schematicreader;

import org.joml.Vector3i;

public class Block {

	private Vector3i position;
	private BlockType type;
	
	public Vector3i getPosition() {
		return position;
	}
	public void setPosition(Vector3i position) {
		this.position = position;
	}
	public BlockType getType() {
		return type;
	}
	public void setType(BlockType type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return "Block [position=" + position + ", type=" + type + "]";
	}
	
	
}
