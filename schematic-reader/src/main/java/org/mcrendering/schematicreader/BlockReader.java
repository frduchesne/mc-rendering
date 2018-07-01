package org.mcrendering.schematicreader;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector2f;
import org.joml.Vector3f;

import com.flowpowered.nbt.CompoundMap;
import com.flowpowered.nbt.Tag;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class BlockReader {

	private CompoundMap tagCollection;
	private TextureMap textureMap;
	
	private Map<String, BlockType> blocks = new HashMap<>();
	
	public BlockReader(CompoundMap tagCollection, TextureMap textureMap) {
		this.tagCollection = tagCollection;
		this.textureMap = textureMap;
	}
	
	public BlockType read(int index) {
		
		int block = ((byte[]) tagCollection.get("Blocks").getValue())[index] & 0xFF;
		int data = ((byte[]) tagCollection.get("Data").getValue())[index] & 0xFF;
		
		String key = String.format("%d:%d", block, data);
		if (blocks.containsKey(key)) {
			return blocks.get(key);
		}
		
		blocks.put(key, null);
		
        String model = findModel(block, data);
        if (model == null) {
        	return null;
        }
    	
        BlockType blockType = readModel(model);
        if (blockType == null) {
        	return null;
        }
        
        blocks.put(key, blockType);
        
        return blockType;
	}
	
	private BlockType readModel(String model) {
    	File modelFile = Utils.getResource(String.format("minecraft/models/block/%s.json", model));
    	if (modelFile == null) {
    		System.err.println("No model found for " + model);
    		return null;
    	}
    	
    	JsonObject jsonObject = parse(modelFile);
    	List<JsonObject> jsonObjects = new ArrayList<>();
    	jsonObjects.add(jsonObject);
    	JsonElement parent = jsonObject.get("parent");
    	while (parent != null) {
    		File parentFile = Utils.getResource(String.format("minecraft/models/%s.json", parent.getAsString()));
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
    	
    	BlockType blockType = new BlockType(textureMap, model);
    	
    	for (JsonObject jsonModel : jsonObjects) {
    		JsonElement elements = jsonModel.get("elements");
    		if (elements == null) {
    			continue;
    		}
    		
    		for (JsonElement element : elements.getAsJsonArray()) {
    			JsonObject elementObj = element.getAsJsonObject();
    			Vector3f from = getVector3f(elementObj, "from");
    			Vector3f to = getVector3f(elementObj, "to");
    			JsonElement faces = elementObj.get("faces");
    			if (faces == null) {
    				System.err.println("no faces for element " + element);
    				continue;
    			}
				
    			JsonObject rotation = null;
    			if (elementObj.get("rotation") != null) {
    				rotation = elementObj.get("rotation").getAsJsonObject();
    			}

    			for (Map.Entry<String, JsonElement> face : faces.getAsJsonObject().entrySet()) {
    				BlockFace blockFace = null;
    				switch(face.getKey()) {
    				case "down": 
    					blockFace = blockType.getDown();
    					if (blockFace == null) {
    						blockFace = new BlockFace();
    						blockType.setDown(blockFace);
    					}
    				break;
    				case "up": 
    					blockFace = blockType.getUp(); 
    					if (blockFace == null) {
    						blockFace = new BlockFace();
    						blockType.setUp(blockFace);
    					}
    					break;
    				case "north": 
    					blockFace = blockType.getNorth(); 
    					if (blockFace == null) {
    						blockFace = new BlockFace();
    						blockType.setNorth(blockFace);
    					}
    					break;
    				case "south": 
    					blockFace = blockType.getSouth(); 
    					if (blockFace == null) {
    						blockFace = new BlockFace();
    						blockType.setSouth(blockFace);
    					}
    					break;
    				case "west": 
    					blockFace = blockType.getWest(); 
    					if (blockFace == null) {
    						blockFace = new BlockFace();
    						blockType.setWest(blockFace);
    					}
    					break;
    				case "east": 
    					blockFace = blockType.getEast(); 
    					if (blockFace == null) {
    						blockFace = new BlockFace();
    						blockType.setEast(blockFace);
    					}
    					break;
    				default:
    					System.err.println("face unknown : " + face.getKey());
    					continue;
    				}
    				
    				JsonObject jsonFace = face.getValue().getAsJsonObject();
    				
    				JsonElement jsonCull = jsonFace.get("cullface");
    				if (jsonCull != null) {
    					blockType.setBackFaceCulled(true);
    				}
    				
    				JsonElement jsonTexture = jsonFace.get("texture");
    				if (jsonTexture == null) {
    					System.err.println("no texture for face " + face);
    					continue;
    				} 
    				String texture = jsonTexture.getAsString();
    				if ("#overlay".equals(texture)) {
    					continue;
    				}
    				if (from != null) {
    					blockFace.setFrom(from);
    				}
    				if (to != null) {
    					blockFace.setTo(to);
    				}
    				while (texture != null && texture.startsWith("#")) {
    					texture = textures.get(texture.substring(1));
    				}
    				if (texture == null) {
    					System.err.println("no texture found for " + jsonTexture);
    					continue;
    				}
    				boolean tinted = jsonFace.get("tintindex") == null ? false : true;
    				blockFace.setTextureOffset(textureMap.getTextureOffset(texture, tinted));
    				
    				JsonElement jsonUv = jsonFace.get("uv");
    				if (jsonUv == null) {
    					blockFace.setUvFrom(new Vector2f(0, 0));
    					blockFace.setUvTo(new Vector2f(16, 16));
    				} else {
        				JsonArray uv = jsonUv.getAsJsonArray();
        				blockFace.setUvFrom(new Vector2f(uv.get(0).getAsFloat(), uv.get(1).getAsFloat()));
        				blockFace.setUvTo(new Vector2f(uv.get(2).getAsFloat(), uv.get(3).getAsFloat()));
    				}
    				
    				if (rotation != null) {
    					blockFace.setRotation(new FaceRotation());
    					blockFace.getRotation().setAngle(rotation.get("angle").getAsFloat());
    					blockFace.getRotation().setAxis(rotation.get("axis").getAsString());
    				}    				
    			}
    		}
    	}
    	
    	if (!blockType.isValid()) {
    		System.err.println("block type not valid : " + blockType);
    		return null;
    	}
    	
    	return blockType;
	}
	
	private Vector3f getVector3f(JsonObject jsonObject, String property) {
		JsonElement element = jsonObject.get(property);
		if (element == null || !element.isJsonArray()) {
			return null;
		}
		JsonArray array = element.getAsJsonArray();
		return new Vector3f(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat());
	}
	
	private String findModel(int block, int data) {
		
		// air
		if (block == 0) {
			return null;
		}
		
		// wood
		if (block == 17) {
			switch(data & 0x02) {
			case (0) : return "oak_log";
			case (1) : return "spruce_log";
			case (2) : return "birch_log";
			case (3) : return "jungle_log";
			default : System.err.println(String.format("no wood found for block %d data %d", block, data));
			return "oak_log";
			}
		}
		
		if (block == 162) {
			switch(data & 0x02) {
			case (0) : return "acacia_log";
			case (1) : return "dark_oak_log";
			default : System.err.println(String.format("no wood found for block %d data %d", block, data));
			return "acacia_log";
			}
		}
		
		// leaves
		if (block == 18) {
			switch(data & 0x02) {
			case (0) : return "oak_leaves";
			case (1) : return "spruce_leaves";
			case (2) : return "birch_leaves";
			case (3) : return "jungle_leaves";
			default : System.err.println(String.format("no leaf found for block %d data %d", block, data));
			return "oak_leaves";
			}
		}
		
		if (block == 161) {
			switch(data & 0x02) {
			case (0) : return "acacia_leaves";
			case (1) : return "dark_oak_leaves";
			default : System.err.println(String.format("no leaf found for block %d data %d", block, data));
			return "acacia_leaves";
			}
		}

		// grass
		if (block == 31) {
			return "tall_grass";
		}
		
		// flowers
		if (block == 37) {
			return "dandelion";
		}
		
		if (block == 38) {
			switch(data) {
			case (0) : return "poppy";
			case (1) : return "orchid";
			case (2) : return "allium";
			case (3) : return "azure_bluet";
			case (4) : return "red_tulip";
			case (5) : return "orange_tulip";
			case (6) : return "white_tulip";
			case (7) : return "pink_tulip";
			case (8) : return "daisy";
			default : System.err.println(String.format("no flower found for block %d data %d", block, data));
			return "poppy";
			}
		}
		
		Tag<?> tag = ((CompoundMap) tagCollection.get("BlockIDs").getValue()).get(Integer.toString(block));
		if (tag == null) {
			System.err.println("tag not found for block " + block);
			return null;
		}
		String blockId = (String) tag.getValue();		
		
		System.out.println(String.format("block: %s, data:%d", blockId, data));
		
		File blockStateFile = Utils.getResource(String.format("minecraft/blockstates/%s.json", blockId.split(":")[1]));
		
		if (blockStateFile == null) {
			System.err.println("no block state file found for block " + blockId);
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
	

}
