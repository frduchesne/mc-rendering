package org.mcrendering.schematicreader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3f;

import com.flowpowered.nbt.CompoundMap;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.stream.NBTInputStream;

public class SchematicReader {

	private CompoundMap tagCollection;
	private short xSize;
	private short ySize;
	private short zSize;
	private List<Block> blockList;
	private BlockTypeFinder blockTypeFinder;
	
	public World read(InputStream fis) {
        TextureMap textureMap = new TextureMap();
        try (NBTInputStream nbt = new NBTInputStream(fis);) {
            tagCollection = ((CompoundTag) nbt.readTag()).getValue();
            
            xSize = (Short) tagCollection.get("Width").getValue();
            ySize = (Short) tagCollection.get("Height").getValue();
            zSize = (Short) tagCollection.get( "Length").getValue();
            System.out.println(((CompoundMap) tagCollection.get("BlockIDs").getValue()).values());
            
            blockList = new ArrayList<>();
            blockTypeFinder = new BlockTypeFinder();
            BlockReader blockReader = new BlockReader(tagCollection, textureMap);
            
            BlockType water = new BlockType();
            water.setBlockId(9);
            
            // from top to bottom for performance (?)
            for (int y = ySize - 1; y >= 0; y--) {
            	for (int z = 0; z < zSize; z++) {
            		for (int x = 0; x < xSize; x++) {
            			
            			int blockId = getBlockId(x, y, z);
            			int data = getData(x, y, z);

            			// air
            			if (blockId == 0) {
            				continue;
            			}
            			
            			// water
            			if (blockId == water.getBlockId()) {
            				continue;
            			} 
            			
            			BlockType blockType = blockReader.read(blockId, data);
            			if (blockType != null) {
            				addBlock(x, y, z, blockType);
            			}
            		}
            	}
            }
            
            BlockType waterSurface = new BlockType();
            waterSurface.setBlockId(water.getBlockId());
            BlockFace up = new BlockFace();
            up.setFrom(new Vector3f(0, 14, 0));
            up.setTo(new Vector3f(16, 14, 16));
            up.setUvFrom(new Vector2f(0, 0));
            up.setUvTo(new Vector2f(16, 16));
            up.setTextureOffset(textureMap.getTextureOffset(-1, "blocks/water_overlay", false));
            waterSurface.setUp(up);
            for (int y = 0; y < ySize; y++) {
            	for (int z = 0; z < zSize; z++) {
            		for (int x = 0; x < xSize; x++) {
            			int blockId = getBlockId(x, y, z);
            			if (blockId == water.getBlockId()) {
            				int blockIdAbove = getBlockId(x, y + 1, z);
            				if (blockIdAbove == 0) {
            					addBlock(x, y, z, waterSurface);
            				}
            			}
            		}
            	}
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
        World world = new World(blockList, textureMap, blockTypeFinder);
        
        return world;
	}
	
	private int getIndex(int x, int y, int z) {
		return (y * zSize + z) * xSize + x;
	}
	
	private int getBlockId(int x, int y, int z) {
		byte[] blocks = (byte[]) tagCollection.get("Blocks").getValue();
		int index = getIndex(x, y, z);
		if (index < 0 || index >= blocks.length) {
			return 0;
		}
		return blocks[index] & 0xFF;
	}
	
	private int getData(int x, int y, int z) {
		return ((byte[]) tagCollection.get("Data").getValue())[getIndex(x, y , z)] & 0xFF;
	}
	
	private void addBlock(int x, int y, int z, BlockType blockType) {
		if (blockTypeFinder.get(x, y, z) == null) {
			blockTypeFinder.set(x, y, z, blockType);
		}
		Block block = new Block();
		block.setType(blockType);
		block.setPosition(new Vector3f((float)x, (float)y, (float)z));
		blockList.add(block);	
	}

}
