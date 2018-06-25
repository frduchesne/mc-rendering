package org.mcrendering.schematicreader;

import java.util.Collection;

public class World {
	
	private Collection<Block> blocks;
	private TextureMap texture;
	
	public World(Collection<Block> blocks, TextureMap texture) {
		this.blocks = blocks;
		this.texture = texture;
	}
	
	public Collection<Block> getBlocks() {
		return this.blocks;
	}

	public void init() {
		for (Block block : blocks) {
			block.init();
		}
		this.texture.init();
	}
	
	public void render() {
		for (Block block : blocks) {
			block.render();
		}
		//blocks.iterator().next().render();
	}
	
	public void cleanup() {
		for (Block block : blocks) {
			block.cleanUp();
		}
        texture.cleanup();
	}
	
	public int getTextureId() {
		return this.texture.getTextureId();
	}
}
