package org.mcrendering.schematicreader;

import java.util.HashMap;
import java.util.Map;

public class BlockTypeFinder {
	
	private Map<String, BlockType> blockTypes = new HashMap<>();
	
	public void set(int x, int y, int z, BlockType blockType) {
		blockTypes.put(key(x, y, z), blockType);
	}
	
	public BlockType get(int x, int y, int z) {
		return blockTypes.get(key(x, y , z));
	}
	
	private String key(int x, int y, int z) {
		return String.format("%d:%d:%d", x, y, z);
	}
}
