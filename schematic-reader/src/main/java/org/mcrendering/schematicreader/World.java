package org.mcrendering.schematicreader;

import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.glCullFace;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glDisable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.joml.Vector3f;

public class World {
	
	private Collection<Block> blocks;
	private TextureMap texture;
	private List<Block> backFaceCulledBlocks = new ArrayList<>();
	private List<Block> semiTransparentBlocks = new ArrayList<>();
	private List<Block> otherBlocks = new ArrayList<>();
	private BlockTypeFinder blockTypeFinder;
	
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
		this.texture.init();
		
	}
	
	public void render(Vector3f cameraPosition) {
		
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

		for (Block block : backFaceCulledBlocks) {
			block.render();
		}

        glDisable(GL_CULL_FACE);

        for (Block block : otherBlocks) {
			block.render();
		}
        
        semiTransparentBlocks.sort(new Comparator<Block>() {

			@Override
			public int compare(Block o1, Block o2) {
				Vector3f pos1 = new Vector3f(o1.getPosition()).sub(cameraPosition);
				Vector3f pos2 = new Vector3f(o2.getPosition()).sub(cameraPosition);
				float diff = (pos1.x * pos1.x + pos1.y * pos1.y + pos1.z * pos1.z) - (pos2.x * pos2.x + pos2.y * pos2.y + pos2.z * pos2.z);
				if (diff < 0) {
					return -1;
				}
				if (diff == 0) {
					return 0;
				}
				return 1;
			}
		});

        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        for (Block block : semiTransparentBlocks) {
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

	private boolean isOpaque(float x, float y, float z) {
		BlockType type = blockTypeFinder.get((int) x, (int) y, (int) z);
		if (type == null) {
			return false;
		}
		return !type.isSemiTransparent() && type.isBackFaceCulled();
	}

}
