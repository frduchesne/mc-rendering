package org.mcrendering.schematicreader;

import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.glCullFace;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glDisable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joml.Vector3f;

public class World {
	
	private Collection<Block> blocks;
	private TextureMap texture;
	private BlockTypeFinder blockTypeFinder;
	private BlockListRenderer backFaceCulledBlocksRenderer;
	private BlockListRenderer otherBlocksRenderer;
	private BlockListRenderer semiTransparentBlocksRenderer;
	

	public World(Collection<Block> blocks, TextureMap texture, BlockTypeFinder blockTypeFinder) {
		this.blocks = blocks;
		this.texture = texture;
		this.blockTypeFinder = blockTypeFinder;
	}
	
	public Collection<Block> getBlocks() {
		return this.blocks;
	}
	
	public void init() {
				
		// remove invisible blocks
		List<Block> newBlocks = new ArrayList<>();
		for (Block block : blocks) {
			float x = block.getPosition().x;
			float y = block.getPosition().y;
			float z = block.getPosition().z;
			if (!isOpaque(x + 1, y, z) || !isOpaque(x - 1, y, z) || !isOpaque(x, y + 1, z) || !isOpaque(x, y - 1, z) || !isOpaque(x, y, z + 1) || !isOpaque(x, y, z - 1)) {
				newBlocks.add(block);
			}
		}
		blocks = newBlocks;
		
		List<Block> backFaceCulledBlocks = new ArrayList<>();
		List<Block> semiTransparentBlocks = new ArrayList<>();
		List<Block> otherBlocks = new ArrayList<>();
		for (Block block : blocks) {
			block.init(texture);
			if (block.getType().isSemiTransparent()){
				semiTransparentBlocks.add(block);
			} else if (block.getType().isBackFaceCulled()) {
				backFaceCulledBlocks.add(block);
			} else {
				otherBlocks.add(block);
			}
		}
		
		this.backFaceCulledBlocksRenderer = new BlockListRenderer(backFaceCulledBlocks, texture);
		this.otherBlocksRenderer = new BlockListRenderer(otherBlocks, texture);
		this.semiTransparentBlocksRenderer = new BlockListRenderer(semiTransparentBlocks, texture);
		
		this.texture.init();
		
	}
	
	public void render(Vector3f cameraPosition) {
		renderBackFaceCulledBlocks();
		renderOtherBlocks();
		renderSemiTransparentBlocks();
	}
	
	public void renderBackFaceCulledBlocks() {
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

		backFaceCulledBlocksRenderer.render();
	}
	
	public void renderOtherBlocks() {
        glDisable(GL_CULL_FACE);

        otherBlocksRenderer.render();
	}
	
	public void renderSemiTransparentBlocks() {
//      semiTransparentBlocks.sort(new Comparator<Block>() {
//
//			@Override
//			public int compare(Block o1, Block o2) {
//				Vector3f pos1 = new Vector3f(o1.getPosition()).sub(cameraPosition);
//				Vector3f pos2 = new Vector3f(o2.getPosition()).sub(cameraPosition);
//				float diff = (pos1.x * pos1.x + pos1.y * pos1.y + pos1.z * pos1.z) - (pos2.x * pos2.x + pos2.y * pos2.y + pos2.z * pos2.z);
//				if (diff < 0) {
//					return -1;
//				}
//				if (diff == 0) {
//					return 0;
//				}
//				return 1;
//			}
//		});

      glEnable(GL_CULL_FACE);
      glCullFace(GL_BACK);
      
      semiTransparentBlocksRenderer.render();
	}
	
	public void cleanup() {
		backFaceCulledBlocksRenderer.cleanup();
		otherBlocksRenderer.cleanup();
		semiTransparentBlocksRenderer.cleanup();
        texture.cleanup();
	}
	
	public int getTextureId() {
		return this.texture.getTextureId();
	}

	private boolean isOpaque(float x, float y, float z) {
		BlockType type = blockTypeFinder.get((int) x, (int) y, (int) z);
		if (type == null) {
			return false;
		}
		return !type.isSemiTransparent() && type.isBackFaceCulled();
	}

}
