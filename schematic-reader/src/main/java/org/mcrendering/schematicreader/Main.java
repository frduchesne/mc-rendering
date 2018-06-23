package org.mcrendering.schematicreader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3i;

import com.flowpowered.nbt.CompoundMap;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.stream.NBTInputStream;

public class Main {

	public static void main(String[] args) {

        try (   InputStream fis = Class.forName(Main.class.getName()).getResourceAsStream("/models/caverne.schematic");
                NBTInputStream nbt = new NBTInputStream(fis);
        		) {
            CompoundTag backuptag = (CompoundTag) nbt.readTag();
            CompoundMap tagCollection = backuptag.getValue();

            short width = (Short) tagCollection.get("Width").getValue();
            short height = (Short) tagCollection.get("Height").getValue();
            short length = (Short) tagCollection.get( "Length").getValue();


            BlockReader blockReader = new BlockReader(tagCollection);
            
            byte[] blocks = (byte[]) tagCollection.get("Blocks").getValue();
            System.out.println("width : " + width);
            System.out.println("height : " + height);
            System.out.println("length : " + length);
            System.out.println("nb blocks :" + blocks.length);
            
            System.out.println("Blocks");
            List<Block> blockList = new ArrayList<>();
            for (int h = 0; h < height; h++) {
            	for (int l = 0; l < length; l++) {
            		for (int w = 0; w < width; w++) {
            			int index = (h * length + l) * width + w;

            			BlockType blockType = blockReader.read(index);
            			if (blockType != null) {
            				Block block = new Block();
            				block.setType(blockType);
            				block.setPosition(new Vector3i(w, h, l));
            				blockList.add(block);
            			}
            		}
            	}
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}
