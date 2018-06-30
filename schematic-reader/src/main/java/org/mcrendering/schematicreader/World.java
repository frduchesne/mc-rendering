package org.mcrendering.schematicreader;

import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.glCullFace;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glDisable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class World {
	
	private Collection<Block> blocks;
	private TextureMap texture;
	private List<Block> backFaceCulledBlocks = new ArrayList<>();
	private List<Block> otherBlocks = new ArrayList<>();
	
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
			if (block.getType().isBackFaceCulled()) {
				backFaceCulledBlocks.add(block);
			} else {
				otherBlocks.add(block);
			}
		}
		this.texture.init();
		
	}
	
	public void render() {
		
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

		for (Block block : backFaceCulledBlocks) {
			block.render();
		}

        glDisable(GL_CULL_FACE);

        for (Block block : otherBlocks) {
			block.render();
		}
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
