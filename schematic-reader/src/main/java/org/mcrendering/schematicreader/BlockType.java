package org.mcrendering.schematicreader;

public class BlockType {

	private BlockFace up;
	private BlockFace down;
	private BlockFace west;
	private BlockFace east;
	private BlockFace north;
	private BlockFace south;
	private String model;
	private boolean backFaceCulled;
	private int blockId;
	private boolean semiTransparent;
	
	public BlockFace getUp() {
		return up;
	}
	public void setUp(BlockFace up) {
		this.up = up;
	}
	public BlockFace getDown() {
		return down;
	}
	public void setDown(BlockFace down) {
		this.down = down;
	}
	public BlockFace getWest() {
		return west;
	}
	public void setWest(BlockFace west) {
		this.west = west;
	}
	public BlockFace getEast() {
		return east;
	}
	public void setEast(BlockFace east) {
		this.east = east;
	}
	public BlockFace getNorth() {
		return north;
	}
	public void setNorth(BlockFace north) {
		this.north = north;
	}
	public BlockFace getSouth() {
		return south;
	}
	public void setSouth(BlockFace south) {
		this.south = south;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public boolean isBackFaceCulled() {
		return backFaceCulled;
	}
	public void setBackFaceCulled(boolean backFaceCulled) {
		this.backFaceCulled = backFaceCulled;
	}
	public int getBlockId() {
		return blockId;
	}
	public void setBlockId(int blockId) {
		this.blockId = blockId;
	}
	public boolean isSemiTransparent() {
		return semiTransparent;
	}
	public void setSemiTransparent(boolean semiTransparent) {
		this.semiTransparent = semiTransparent;
	}
	
	@Override
	public String toString() {
		return "BlockType [up=" + up + ", down=" + down + ", west=" + west + ", east=" + east + ", north=" + north
				+ ", south=" + south + ", model=" + model + ", backFaceCulled=" + backFaceCulled + ", blockId="
				+ blockId + "]";
	}
	
	public boolean isValid() {
		boolean valid = true;
		if (up != null && !up.isValid()) {
			valid = false;
			System.err.println("up not valid");
		}
		if (down != null && !down.isValid()) {
			valid = false;
			System.err.println("down not valid");
		}
		if (west != null && !west.isValid()) {
			valid = false;
			System.err.println("west not valid");
		}
		if (east == null || !east.isValid()) {
			valid = false;
			System.err.println("east not valid");
		}
		if (north == null || !north.isValid()) {
			valid = false;
			System.err.println("north not valid");
		}
		if (south == null || !south.isValid()) {
			valid = false;
			System.err.println("south not valid");
		}
		
		return valid;
	}
}

