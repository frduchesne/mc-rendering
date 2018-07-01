package org.mcrendering.schematicreader;

import org.joml.Matrix3f;
import org.joml.Vector3f;
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

	private Vector3f position;
	private BlockType type;
    private int vaoId;
    private List<Integer> vboIdList;
    private int vertexCount;
	
	public Vector3f getPosition() {
		return position;
	}
	public void setPosition(Vector3f position) {
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
    	
    	int faces = 
    			(type.getSouth() == null ? 0 : 1) + 
    			(type.getNorth() == null ? 0 : 1) +
    			(type.getWest() == null ? 0 : 1) +
    			(type.getEast() == null ? 0 : 1) +
    			(type.getDown() == null ? 0 : 1) +
    			(type.getUp() == null ? 0 : 1);
    	
    	float[] positions = new float[12 * faces];
    	float[] textCoords = new float[8 * faces];
    	float[] normals = new float[12 * faces];
    	int[] indices = new int[6 * faces];
    	
    	int index = 0;
    	
    	if (type.getSouth() != null) {
        	System.arraycopy(getSouthPositions(), 0, positions, index * 12, 12);
        	System.arraycopy(getTexCoords(type.getSouth()), 0, textCoords, index * 8, 8);
        	System.arraycopy(getSouthNormal(), 0, normals, index * 12, 12);
        	System.arraycopy(getSouthIndices(index * 4), 0, indices, index * 6, 6);
        	index++;
    	}
    	
    	if (type.getNorth() != null) {
        	System.arraycopy(getNorthPositions(), 0, positions, index * 12, 12);
        	System.arraycopy(getTexCoords(type.getNorth()), 0, textCoords, index * 8, 8);
        	System.arraycopy(getNorthNormal(), 0, normals, index * 12, 12);
        	System.arraycopy(getNorthIndices(index * 4), 0, indices, index * 6, 6);
        	index++;
    	}

    	if (type.getWest() != null) {
        	System.arraycopy(getWestPositions(), 0, positions, index * 12, 12);
        	System.arraycopy(getTexCoords(type.getWest()), 0, textCoords, index * 8, 8);
        	System.arraycopy(getWestNormal(), 0, normals, index * 12, 12);
        	System.arraycopy(getWestIndices(index * 4), 0, indices, index * 6, 6);
        	index++;
    	}

    	if (type.getEast() != null) {
        	System.arraycopy(getEastPositions(), 0, positions, index * 12, 12);
        	System.arraycopy(getTexCoords(type.getEast()), 0, textCoords, index * 8, 8);
        	System.arraycopy(getEastNormal(), 0, normals, index * 12, 12);
        	System.arraycopy(getEastIndices(index * 4), 0, indices, index * 6, 6);
        	index++;
    	}

    	if (type.getDown() != null) {
        	System.arraycopy(getDownPositions(), 0, positions, index * 12, 12);
        	System.arraycopy(getTexCoords(type.getDown()), 0, textCoords, index * 8, 8);
        	System.arraycopy(getDownNormal(), 0, normals, index * 12, 12);
        	System.arraycopy(getDownIndices(index * 4), 0, indices, index * 6, 6);
        	index++;
    	}

    	if (type.getUp() != null) {
        	System.arraycopy(getUpPositions(), 0, positions, index * 12, 12);
        	System.arraycopy(getTexCoords(type.getUp()), 0, textCoords, index * 8, 8);
        	System.arraycopy(getUpNormal(), 0, normals, index * 12, 12);
        	System.arraycopy(getUpIndices(index * 4), 0, indices, index * 6, 6);
        	index++;
    	}

    	FloatBuffer posBuffer = null;
        FloatBuffer textCoordsBuffer = null;
        FloatBuffer normalBuffer = null;
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

            // Normal VBO
            vboId = glGenBuffers();
            vboIdList.add(vboId);
            normalBuffer = MemoryUtil.memAllocFloat(normals.length);
            normalBuffer.put(normals).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, normalBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);

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
            if (normalBuffer != null) {
            	MemoryUtil.memFree(normalBuffer);
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
        glEnableVertexAttribArray(2);

        glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);

        // Restore state
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
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
    	Vector3f from = face.getFrom();
    	Vector3f to = face.getTo();
    	
    	return transform(new Vector3f[] {
	        new Vector3f(from.x, from.y, to.z),
	        new Vector3f(to.x, from.y, to.z),
	        new Vector3f(to.x, to.y, to.z),
	        new Vector3f(from.x, to.y, to.z)}, 
    			face.getRotation());
    }
    
    private float[] getSouthNormal() {
    	return getNormalCoordinates(0f, 0f, 1f);
    }
    
    private int[] getSouthIndices(int offset) {
    	return addValue(new int[] {
    		0, 1, 2,
    		0, 2, 3
    	}, offset);
    }
    
    private float[] getNorthPositions() {
    	BlockFace face = type.getNorth();
    	Vector3f from = face.getFrom();
    	Vector3f to = face.getTo();
    	
    	return transform(new Vector3f[] {
    	        new Vector3f(from.x, from.y, from.z),
    	        new Vector3f(to.x, from.y, from.z),
    	        new Vector3f(to.x, to.y, from.z),
    	        new Vector3f(from.x, to.y, from.z)}, 
    			face.getRotation());
    }
    
    private float[] getNorthNormal() {
    	return getNormalCoordinates(0f, 0f, -1f);
    }

    private int[] getNorthIndices(int offset) {
    	return addValue(new int[] {
        		0, 2, 1,
        		0, 3, 2
        	}, offset);
    }
    
    private float[] getWestPositions() {
    	BlockFace face = type.getWest();
    	Vector3f from = face.getFrom();
    	Vector3f to = face.getTo();
    	
    	return transform(new Vector3f[] {
    	        new Vector3f(from.x, from.y, from.z),
    	        new Vector3f(from.x, from.y, to.z),
    	        new Vector3f(from.x, to.y, to.z),
    	        new Vector3f(from.x, to.y, from.z)}, 
    			face.getRotation());
    }
    
    private float[] getWestNormal() {
    	return getNormalCoordinates(-1f, 0f, 0f);
    }

    private int[] getWestIndices(int offset) {
    	return addValue(new int[] {
        		0, 1, 2,
        		0, 2, 3
        	}, offset);
    }
    
    private float[] getEastPositions() {
    	BlockFace face = type.getEast();
    	Vector3f from = face.getFrom();
    	Vector3f to = face.getTo();
    	
    	return transform(new Vector3f[] {
    	        new Vector3f(to.x, from.y, from.z),
    	        new Vector3f(to.x, from.y, to.z),
    	        new Vector3f(to.x, to.y, to.z),
    	        new Vector3f(to.x, to.y, from.z)}, 
    			face.getRotation());
    }
    
    private float[] getEastNormal() {
    	return getNormalCoordinates(1f, 0f, 0f);
    }

    private int[] getEastIndices(int offset) {
    	return addValue(new int[] {
        		0, 2, 1,
        		0, 3, 2
        	}, offset);
    }
    
    private float[] getDownPositions() {
    	BlockFace face = type.getDown();
    	Vector3f from = face.getFrom();
    	Vector3f to = face.getTo();
    	
    	return transform(new Vector3f[] {
    	        new Vector3f(from.x, from.y, from.z),
    	        new Vector3f(to.x, from.y, from.z),
    	        new Vector3f(to.x, from.y, to.z),
    	        new Vector3f(from.x, from.y, to.z)}, 
    			face.getRotation());
    }
    
    private float[] getDownNormal() {
    	return getNormalCoordinates(0f, -1f, 0f);
    }
    
    private int[] getDownIndices(int offset) {
    	return addValue(new int[] {
        		0, 1, 2,
        		0, 2, 3
        	}, offset);
    }
    
    private float[] getUpPositions() {
    	BlockFace face = type.getUp();
    	Vector3f from = face.getFrom();
    	Vector3f to = face.getTo();
    	
    	return transform(new Vector3f[] {
    	        new Vector3f(from.x, to.y, from.z),
    	        new Vector3f(to.x, to.y, from.z),
    	        new Vector3f(to.x, to.y, to.z),
    	        new Vector3f(from.x, to.y, to.z)}, 
    			face.getRotation());
    }
    
    private float[] getUpNormal() {
    	return getNormalCoordinates(0f, 1f, 0f);
    }

    private int[] getUpIndices(int offset) {
    	return addValue(new int[] {
        		0, 2, 1,
        		0, 3, 2
        	}, offset);
    }

    private float[] getNormalCoordinates(float x, float y, float z) {
    	return new float[] {
        		x, y, z,
        		x, y, z,
        		x, y, z,
        		x, y, z
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
    
    private float getU(float u) {
    	return u / (float) type.getTextureMap().getWidth();
    }
    
    private float getV(float v, BlockFace face) {
    	return (v + (float) face.getTextureOffset()) / (float) type.getTextureMap().getHeight();
    }
    
    private int[] addValue(int[] array, int value) {
    	for (int i=0; i < array.length; i++) {
    		array[i] += value;
    	}
    	return array;
    }

    private float[] transform(Vector3f[] positions, FaceRotation rotation) {
    	Matrix3f matrix = new Matrix3f();
    	if (rotation != null) {
    		if ("x".equals(rotation.getAxis())) {
    			matrix.rotateX((float)Math.toRadians(rotation.getAngle()));
    		} else if ("y".equals(rotation.getAxis())) {
    			matrix.rotateY((float)Math.toRadians(rotation.getAngle()));
    		} else if ("z".equals(rotation.getAxis())) {
    			matrix.rotateZ((float)Math.toRadians(rotation.getAngle()));
    		}
    	}
    	float[] coordinates = new float[positions.length * 3];
    	for (int i=0; i < positions.length; i++) {
    		
    		Vector3f pos = new Vector3f(
        			positions[i].x / 16f - 0.5f, 
        			positions[i].y / 16f - 0.5f, 
        			positions[i].z / 16f - 0.5f);
    		
    		pos = matrix.transform(pos);
    		pos.x += position.x;
    		pos.y += position.y;
    		pos.z += position.z;
    		
    		int j = i * 3;
    		coordinates[j] = pos.x;
    		coordinates[j + 1] = pos.y;
    		coordinates[j + 2] = pos.z;
    	}
    	return coordinates;
    }

}
