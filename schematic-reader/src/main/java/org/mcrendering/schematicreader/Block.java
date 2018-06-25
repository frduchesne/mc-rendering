package org.mcrendering.schematicreader;

import org.joml.Vector3f;
import org.joml.Vector3i;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.system.MemoryUtil;

public class Block {

	private Vector3i position;
	private BlockType type;
    private int vaoId;
    private List<Integer> vboIdList;
    private int vertexCount;
	
	public Vector3i getPosition() {
		return position;
	}
	public void setPosition(Vector3i position) {
		this.position = position;
	}
	public BlockType getType() {
		return type;
	}
	public void setType(BlockType type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return "Block [position=" + position + ", type=" + type + "]";
	}
	
    public void init() {
    	float[] positions = new float[6 * 4 * 3];
    	System.arraycopy(getSouthPositions(), 0, positions, 0, 12);
    	System.arraycopy(getNorthPositions(), 0, positions, 12, 12);
    	System.arraycopy(getWestPositions(), 0, positions, 24, 12);
    	System.arraycopy(getEastPositions(), 0, positions, 36, 12);
    	System.arraycopy(getDownPositions(), 0, positions, 48, 12);
    	System.arraycopy(getUpPositions(), 0, positions, 60, 12);
    	
    	int[] indices = new int[36];
    	System.arraycopy(getSouthIndices(), 0, indices, 0, 6);
    	System.arraycopy(getNorthIndices(), 0, indices, 6, 6);
    	System.arraycopy(getWestIndices(), 0, indices, 12, 6);
    	System.arraycopy(getEastIndices(), 0, indices, 18, 6);
    	System.arraycopy(getDownIndices(), 0, indices, 24, 6);
    	System.arraycopy(getUpIndices(), 0, indices, 30, 6);
    	
    	float[] textCoords = new float[48];
    	System.arraycopy(getTexCoords(type.getSouth()), 0, textCoords, 0, 8);
    	System.arraycopy(getTexCoords(type.getNorth()), 0, textCoords, 8, 8);
    	System.arraycopy(getTexCoords(type.getWest()), 0, textCoords, 16, 8);
    	System.arraycopy(getTexCoords(type.getEast()), 0, textCoords, 24, 8);
    	System.arraycopy(getTexCoords(type.getDown()), 0, textCoords, 32, 8);
    	System.arraycopy(getTexCoords(type.getUp()), 0, textCoords, 40, 8);

    	FloatBuffer posBuffer = null;
        FloatBuffer textCoordsBuffer = null;
        IntBuffer indicesBuffer = null;
        try {
            vertexCount = indices.length;
            vboIdList = new ArrayList<>();

            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            // Position VBO
            int vboId = glGenBuffers();
            vboIdList.add(vboId);
            posBuffer = MemoryUtil.memAllocFloat(positions.length);
            posBuffer.put(positions).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

            // Texture coordinates VBO
            vboId = glGenBuffers();
            vboIdList.add(vboId);
            textCoordsBuffer = MemoryUtil.memAllocFloat(textCoords.length);
            textCoordsBuffer.put(textCoords).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, textCoordsBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

            // Index VBO
            vboId = glGenBuffers();
            vboIdList.add(vboId);
            indicesBuffer = MemoryUtil.memAllocInt(indices.length);
            indicesBuffer.put(indices).flip();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        } finally {
            if (posBuffer != null) {
                MemoryUtil.memFree(posBuffer);
            }
            if (textCoordsBuffer != null) {
                MemoryUtil.memFree(textCoordsBuffer);
            }
            if (indicesBuffer != null) {
                MemoryUtil.memFree(indicesBuffer);
            }
        }
    }

    public void render() {

        // Draw the mesh
        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);

        // Restore state
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);
    }

    public void cleanUp() {
        glDisableVertexAttribArray(0);

        // Delete the VBOs
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        for (int vboId : vboIdList) {
            glDeleteBuffers(vboId);
        }

        // Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }
	
    private float[] getSouthPositions() {
    	BlockFace face = type.getSouth();
    	Vector3f from = new Vector3f(
    			(float)position.x + (float) face.getFrom().x / 16f - 0.5f, 
    			(float)position.y + (float) face.getFrom().y / 16f - 0.5f, 
    			(float)position.z + (float) face.getTo().z / 16f - 0.5f);
    	Vector3f to = new Vector3f(
    			(float)position.x + (float) face.getTo().x / 16f - 0.5f, 
    			(float)position.y + (float) face.getTo().y / 16f - 0.5f, 
    			(float)position.z + (float) face.getTo().z / 16f - 0.5f);
    	
    	return new float[] {
	        from.x, from.y, from.z,
	        to.x, from.y, from.z,
	        to.x, to.y, from.z,
	        from.x, to.y, from.z};
    }
    
    private int[] getSouthIndices() {
    	return new int[] {
    		0, 1, 2,
    		0, 2, 3
    	};
    }
    
    private float[] getNorthPositions() {
    	BlockFace face = type.getNorth();
    	Vector3f from = new Vector3f(
    			(float)position.x + (float) face.getFrom().x / 16f - 0.5f, 
    			(float)position.y + (float) face.getFrom().y / 16f - 0.5f, 
    			(float)position.z + (float) face.getFrom().z / 16f - 0.5f);
    	Vector3f to = new Vector3f(
    			(float)position.x + (float) face.getTo().x / 16f - 0.5f, 
    			(float)position.y + (float) face.getTo().y / 16f - 0.5f, 
    			(float)position.z + (float) face.getFrom().z / 16f - 0.5f);
    	
    	return new float[] {
	        from.x, from.y, from.z,
	        to.x, from.y, from.z,
	        to.x, to.y, from.z,
	        from.x, to.y, from.z};
    }

    private int[] getNorthIndices() {
    	return new int[] {
    		4, 6, 5,
    		4, 7, 6
    	};
    }
    
    private float[] getWestPositions() {
    	BlockFace face = type.getWest();
    	Vector3f from = new Vector3f(
    			(float)position.x + (float) face.getFrom().x / 16f - 0.5f, 
    			(float)position.y + (float) face.getFrom().y / 16f - 0.5f, 
    			(float)position.z + (float) face.getFrom().z / 16f - 0.5f);
    	Vector3f to = new Vector3f(
    			(float)position.x + (float) face.getFrom().x / 16f - 0.5f, 
    			(float)position.y + (float) face.getTo().y / 16f - 0.5f, 
    			(float)position.z + (float) face.getTo().z / 16f - 0.5f);
    	
    	return new float[] {
	        from.x, from.y, from.z,
	        from.x, from.y, to.z,
	        from.x, to.y, to.z,
	        from.x, to.y, from.z};
    }

    private int[] getWestIndices() {
    	return new int[] {
    		8, 9, 10,
    		8, 10, 11
    	};
    }
    
    private float[] getEastPositions() {
    	BlockFace face = type.getEast();
    	Vector3f from = new Vector3f(
    			(float)position.x + (float) face.getTo().x / 16f - 0.5f, 
    			(float)position.y + (float) face.getFrom().y / 16f - 0.5f, 
    			(float)position.z + (float) face.getFrom().z / 16f - 0.5f);
    	Vector3f to = new Vector3f(
    			(float)position.x + (float) face.getTo().x / 16f - 0.5f, 
    			(float)position.y + (float) face.getTo().y / 16f - 0.5f, 
    			(float)position.z + (float) face.getTo().z / 16f - 0.5f);
    	
    	return new float[] {
	        from.x, from.y, from.z,
	        from.x, from.y, to.z,
	        from.x, to.y, to.z,
	        from.x, to.y, from.z};
    }

    private int[] getEastIndices() {
    	return new int[] {
    		12, 14, 13,
    		12, 15, 14
    	};
    }
    
    private float[] getDownPositions() {
    	BlockFace face = type.getDown();
    	Vector3f from = new Vector3f(
    			(float)position.x + (float) face.getFrom().x / 16f - 0.5f, 
    			(float)position.y + (float) face.getFrom().y / 16f - 0.5f, 
    			(float)position.z + (float) face.getFrom().z / 16f - 0.5f);
    	Vector3f to = new Vector3f(
    			(float)position.x + (float) face.getTo().x / 16f - 0.5f, 
    			(float)position.y + (float) face.getFrom().y / 16f - 0.5f, 
    			(float)position.z + (float) face.getTo().z / 16f - 0.5f);
    	
    	return new float[] {
	        from.x, from.y, from.z,
	        to.x, from.y, from.z,
	        to.x, from.y, to.z,
	        from.x, from.y, to.z};
    }

    private int[] getDownIndices() {
    	return new int[] {
    		16, 17, 18,
    		16, 18, 19
    	};
    }
    
    private float[] getUpPositions() {
    	BlockFace face = type.getUp();
    	Vector3f from = new Vector3f(
    			(float)position.x + (float) face.getFrom().x / 16f - 0.5f, 
    			(float)position.y + (float) face.getTo().y / 16f - 0.5f, 
    			(float)position.z + (float) face.getFrom().z / 16f - 0.5f);
    	Vector3f to = new Vector3f(
    			(float)position.x + (float) face.getTo().x / 16f - 0.5f, 
    			(float)position.y + (float) face.getTo().y / 16f - 0.5f, 
    			(float)position.z + (float) face.getTo().z / 16f - 0.5f);
    	
    	return new float[] {
	        from.x, from.y, from.z,
	        to.x, from.y, from.z,
	        to.x, from.y, to.z,
	        from.x, from.y, to.z};
    }

    private int[] getUpIndices() {
    	return new int[] {
    		20, 22, 21,
    		20, 23, 22
    	};
    }

    private float[] getTexCoords(BlockFace face) {
    	return new float[] {
      			getU(face.getUvFrom().x), getV(face.getUvFrom().y, face),
      			getU(face.getUvTo().x), getV(face.getUvFrom().y, face),
      			getU(face.getUvTo().x), getV(face.getUvTo().y, face),
      			getU(face.getUvFrom().x), getV(face.getUvTo().y, face)
    	};
    }
    
    private float getU(int u) {
    	return (float) u / type.getTextureMap().getWidth();
    }
    
    private float getV(int v, BlockFace face) {
    	return ((float) v + (float) face.getTextureOffset()) / (float) type.getTextureMap().getHeight();
    }
}
