package org.mcrendering.schematicreader;

import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glTexSubImage2D;
import static org.lwjgl.opengl.GL30.GL_RGBA32F;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.opengl.GL45.glCreateTextures;
import static org.lwjgl.opengl.GL45.glTextureStorage2D;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;


/*
 * 
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL21.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.opengl.GL40.*;
import static org.lwjgl.opengl.GL41.*;
import static org.lwjgl.opengl.GL42.*;
import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.opengl.GL44.*;
import static org.lwjgl.opengl.GL45.*;

 */

public class TextureMap {

	private int id;

    private int currentOffset = 0;
    
    private Map<String, Integer> textureOffset = new HashMap<>();
    private Map<String, ByteBuffer> textureByteBuffer = new HashMap<>();
    private Map<String, Integer> textureHeight = new HashMap<>();
    
    private List<String> textures = new ArrayList<>();
    
    public int getTextureOffset(String texture) {
    	if (textureOffset.containsKey(texture)) {
    		return textureOffset.get(texture);
    	}

    	File textureFile;
    	if ("fallback".equals(texture)) {
    		textureFile = Utils.getResource("fallback.png");
    	} else {
    		textureFile = Utils.getResource(String.format("minecraft/textures/%s.png", texture));
        	if (textureFile == null) {
        		System.err.println("No texture found for " + texture);
        		return getTextureOffset("fallback");
        	}	
    	}
    	
    	try (InputStream is = new FileInputStream(textureFile);) {
            // Load Texture file
            PNGDecoder decoder = new PNGDecoder(is);

            if (decoder.getWidth() != 16) {
            	System.err.println("Texture width not 16 : " + texture);
            	return getTextureOffset("fallback");
            }
            
            // Load texture contents into a byte buffer
            ByteBuffer buf = ByteBuffer.allocateDirect(
                    4 * decoder.getWidth() * decoder.getHeight());
            decoder.decodeFlipped(buf, decoder.getWidth() * 4, Format.RGBA);
            buf.flip();
            
            textureOffset.put(texture, currentOffset);
        	textureByteBuffer.put(texture, buf);
        	textureHeight.put(texture, decoder.getHeight());
        	textures.add(texture);
        	currentOffset += decoder.getHeight();
            

    	} catch (Exception e) {
    		e.printStackTrace();
    		return getTextureOffset("fallback");
    	}
    	
		return textureOffset.get(texture);
    }

    public void init() {

    	// Create a new OpenGL texture 
        this.id = glCreateTextures(GL_TEXTURE_2D);
        glTextureStorage2D(this.id, 3, GL_RGBA32F, getWidth(), getHeight());
        
        // Bind the texture
        glBindTexture(GL_TEXTURE_2D, this.id);

        // Tell OpenGL how to unpack the RGBA bytes. Each component is 1 byte size
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        for (String texture : textures) {
        	glTexSubImage2D(GL_TEXTURE_2D, 0, 0, textureOffset.get(texture), getWidth(), textureHeight.get(texture), GL_RGBA, GL_UNSIGNED_BYTE, textureByteBuffer.get(texture));
        }
        
        // Generate Mip Map 
        glGenerateMipmap(GL_TEXTURE_2D);
    }

    public int getTextureId() {
        return id;
    }

    public void cleanup() {
        glDeleteTextures(id);
    }
    
    public int getWidth() {
    	return 16;
    }
    
    public int getHeight() {
    	return currentOffset;
    }
}
