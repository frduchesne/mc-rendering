package org.mcrendering.schematicreader;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector2i;
import org.joml.Vector3i;

import com.flowpowered.nbt.CompoundMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class BlockReader {

	private CompoundMap tagCollection;
	
	private Map<String, BlockType> blocks = new HashMap<>();
	
	public BlockReader(CompoundMap tagCollection) {
		this.tagCollection = tagCollection;
	}
	
	public BlockType read(int index) {
		
		byte block = ((byte[]) tagCollection.get("Blocks").getValue())[index];
		byte data = ((byte[]) tagCollection.get("Data").getValue())[index];
		
		String key = String.format("%d:%d", block, data);
		if (blocks.containsKey(key)) {
			return blocks.get(key);
		}
		
		blocks.put(key, null);
		
        String model = findModel(block, data);
        if (model == null) {
        	return null;
        }
    	
        return readModel(model);
	}
	
	private BlockType readModel(String model) {
    	File modelFile = getResource(String.format("minecraft/models/block/%s.json", model));
    	if (modelFile == null) {
    		System.err.println("No model found for " + model);
    		return null;
    	}
    	
    	JsonObject jsonObject = parse(modelFile);
    	List<JsonObject> jsonObjects = new ArrayList<>();
    	jsonObjects.add(jsonObject);
    	JsonElement parent = jsonObject.get("parent");
    	while (parent != null) {
    		File parentFile = getResource(String.format("minecraft/models/%s.json", parent.getAsString()));
        	if (parentFile == null) {
        		System.err.println("No parent found : " + parent.getAsString());
        		break;
        	}
        	JsonObject parentObject = parse(parentFile);
        	if (parentObject == null) {
        		break;
        	}
        	jsonObjects.add(0, parentObject);
        	parent = parentObject.get("parent");
    	}
    	
    	// find textures
    	Map<String, String> textures = new HashMap<>();
    	for (JsonObject jsonModel : jsonObjects) {
    		JsonElement texture = jsonModel.get("textures");
    		if (texture != null) {
    			for (Map.Entry<String, JsonElement> entry : texture.getAsJsonObject().entrySet()) {
    				textures.put(entry.getKey(), entry.getValue().getAsString());
    			}
    		}
    	}
    	
    	BlockType blockType = new BlockType();
    	blockType.setUp(new BlockFace());
    	blockType.setDown(new BlockFace());
    	blockType.setNorth(new BlockFace());
    	blockType.setSouth(new BlockFace());
    	blockType.setEast(new BlockFace());
    	blockType.setWest(new BlockFace());
    	
    	for (JsonObject jsonModel : jsonObjects) {
    		JsonElement elements = jsonModel.get("elements");
    		if (elements == null) {
    			continue;
    		}
    		
    		for (JsonElement element : elements.getAsJsonArray()) {
    			Vector3i from = getVector3i(element.getAsJsonObject(), "from");
    			Vector3i to = getVector3i(element.getAsJsonObject(), "to");
    			JsonElement faces = element.getAsJsonObject().get("faces");
    			if (faces == null) {
    				System.err.println("no faces for element " + element);
    				continue;
    			}
    			for (Map.Entry<String, JsonElement> face : faces.getAsJsonObject().entrySet()) {
    				BlockFace blockFace = null;
    				switch(face.getKey()) {
    				case "down": blockFace = blockType.getDown(); break;
    				case "up": blockFace = blockType.getUp(); break;
    				case "north": blockFace = blockType.getNorth(); break;
    				case "south": blockFace = blockType.getSouth(); break;
    				case "west": blockFace = blockType.getWest(); break;
    				case "east": blockFace = blockType.getEast(); break;
    				default:
    					System.err.println("face unknown : " + face.getKey());
    					continue;
    				}
    				
    				if (from != null) {
    					blockFace.setFrom(from);
    				}
    				if (to != null) {
    					blockFace.setTo(to);
    				}
    				JsonElement jsonTexture = face.getValue().getAsJsonObject().get("texture");
    				if (jsonTexture == null) {
    					System.err.println("no texture for face " + face);
    					continue;
    				} 
    				String texture = jsonTexture.getAsString();
    				while (texture != null && texture.startsWith("#")) {
    					texture = textures.get(texture.substring(1));
    				}
    				if (texture == null) {
    					System.err.println("no texture found for " + jsonTexture);
    					continue;
    				}
    				blockFace.setTexture(texture);
    				
    				JsonElement jsonUv = face.getValue().getAsJsonObject().get("uv");
    				if (jsonUv == null) {
    					continue;
    				}
    				JsonArray uv = jsonUv.getAsJsonArray();
    				blockFace.setU(new Vector2i(uv.get(0).getAsInt(), uv.get(1).getAsInt()));
    				blockFace.setV(new Vector2i(uv.get(2).getAsInt(), uv.get(3).getAsInt()));
    			}
    		}
    	}
    	
    	if (!blockType.isValid()) {
    		System.err.println("block type not valid : " + blockType);
    		return null;
    	}
    	
    	System.out.println(blockType);
    	
    	return blockType;
	}
	
	private Vector3i getVector3i(JsonObject jsonObject, String property) {
		JsonElement element = jsonObject.get(property);
		if (element == null || !element.isJsonArray()) {
			return null;
		}
		JsonArray array = element.getAsJsonArray();
		return new Vector3i(array.get(0).getAsInt(), array.get(1).getAsInt(), array.get(2).getAsInt());
	}
	
	private String findModel(byte block, byte data) {
		String blockId = (String) ((CompoundMap) tagCollection.get("BlockIDs").getValue()).get(Byte.toString(block)).getValue();
		
		File blockStateFile = getResource(String.format("minecraft/blockstates/%s.json", blockId.split(":")[1]));
		
		if (blockStateFile == null) {
			return null;
		}
		
		JsonObject jsonObject = parse(blockStateFile);
    	
    	// first property
    	JsonElement models = jsonObject.get("variants").getAsJsonObject().entrySet().iterator().next().getValue();
    	
    	// first model
    	JsonObject jsonModel;
    	if (models.isJsonArray()) {
    		jsonModel = models.getAsJsonArray().get(0).getAsJsonObject();
    	} else {
    		jsonModel = models.getAsJsonObject();
    	}
    	
    	return jsonModel.get("model").getAsString();
	}
	
	private JsonObject parse(File file) {
    	try (Reader reader = new FileReader(file);) {
        	return new JsonParser().parse(reader).getAsJsonObject();        	
    	} catch (Exception e) {
    		System.err.println("error for " + file);
            e.printStackTrace();
            return null;
        }
	}
	
	private File getResource(String path) {
		
		URL res = BlockReader.class.getClassLoader().getResource(path);
		
		if (res == null) {
			return null;
		}
		
		File file = new File(res.getFile());
		
		if (!file.exists()) {
			return null;
		}
		return file;
	}
}
