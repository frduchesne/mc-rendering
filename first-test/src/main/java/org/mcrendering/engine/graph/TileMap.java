package org.mcrendering.engine.graph;

import org.joml.Vector2f;

public class TileMap {

	
	private int index = 0;
	
	public Tile nextTile() {
    	float size = 512f/ShadowMap.SHADOW_LENGTH;
    	Tile tile = new Tile();
    	tile.size = size;
    	
    	Vector2f position = null;
    	switch(index) {
    	case 0 : position = new Vector2f(-0.5f, -0.5f); break;
    	case 1 : position = new Vector2f(0.5f, -0.5f); break;
    	case 2 : position = new Vector2f(-0.5f, 0.5f); break;
    	case 3 : position = new Vector2f(-0.5f, 0.5f); break;
    	}
    	if (position == null) {
    		return null;
    	}
    	tile.position = position;

    	index++;
    	
    	return tile;
	}
}
