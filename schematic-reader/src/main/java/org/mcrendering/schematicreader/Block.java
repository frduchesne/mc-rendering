package org.mcrendering.schematicreader;

import org.joml.Matrix3f;
import org.joml.Vector3f;

public class Block {

	private Vector3f position;
	private BlockType type;
	private float[] positions;
	private float[] textCoords;
	private float[] normals;
	private int[] indices;
	private int indiceCount;

	public int getIndiceCount() {
		return this.indiceCount;
	}
	public float[] getPositions() {
		return positions;
	}
	public float[] getTextCoords() {
		return textCoords;
	}
	public float[] getNormals() {
		return normals;
	}
	public int[] getIndices() {
		return indices;
	}
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
	
    public void init(TextureMap textureMap) {

    	int faces = 
    			(type.getSouth() == null ? 0 : 1) + 
    			(type.getNorth() == null ? 0 : 1) +
    			(type.getWest() == null ? 0 : 1) +
    			(type.getEast() == null ? 0 : 1) +
    			(type.getDown() == null ? 0 : 1) +
    			(type.getUp() == null ? 0 : 1);
    	
    	positions = new float[12 * faces];
    	textCoords = new float[8 * faces];
    	normals = new float[12 * faces];
    	indices = new int[6 * faces];
    	
    	int index = 0;
    	
    	if (type.getSouth() != null) {
        	System.arraycopy(getSouthPositions(), 0, positions, index * 12, 12);
        	System.arraycopy(getTexCoords(type.getSouth(), textureMap), 0, textCoords, index * 8, 8);
        	System.arraycopy(getSouthNormal(), 0, normals, index * 12, 12);
        	System.arraycopy(getSouthIndices(index * 4), 0, indices, index * 6, 6);
        	index++;
    	}
    	
    	if (type.getNorth() != null) {
        	System.arraycopy(getNorthPositions(), 0, positions, index * 12, 12);
        	System.arraycopy(getTexCoords(type.getNorth(), textureMap), 0, textCoords, index * 8, 8);
        	System.arraycopy(getNorthNormal(), 0, normals, index * 12, 12);
        	System.arraycopy(getNorthIndices(index * 4), 0, indices, index * 6, 6);
        	index++;
    	}

    	if (type.getWest() != null) {
        	System.arraycopy(getWestPositions(), 0, positions, index * 12, 12);
        	System.arraycopy(getTexCoords(type.getWest(), textureMap), 0, textCoords, index * 8, 8);
        	System.arraycopy(getWestNormal(), 0, normals, index * 12, 12);
        	System.arraycopy(getWestIndices(index * 4), 0, indices, index * 6, 6);
        	index++;
    	}

    	if (type.getEast() != null) {
        	System.arraycopy(getEastPositions(), 0, positions, index * 12, 12);
        	System.arraycopy(getTexCoords(type.getEast(), textureMap), 0, textCoords, index * 8, 8);
        	System.arraycopy(getEastNormal(), 0, normals, index * 12, 12);
        	System.arraycopy(getEastIndices(index * 4), 0, indices, index * 6, 6);
        	index++;
    	}

    	if (type.getDown() != null) {
        	System.arraycopy(getDownPositions(), 0, positions, index * 12, 12);
        	System.arraycopy(getTexCoords(type.getDown(), textureMap), 0, textCoords, index * 8, 8);
        	System.arraycopy(getDownNormal(), 0, normals, index * 12, 12);
        	System.arraycopy(getDownIndices(index * 4), 0, indices, index * 6, 6);
        	index++;
    	}

    	if (type.getUp() != null) {
        	System.arraycopy(getUpPositions(), 0, positions, index * 12, 12);
        	System.arraycopy(getTexCoords(type.getUp(), textureMap), 0, textCoords, index * 8, 8);
        	System.arraycopy(getUpNormal(), 0, normals, index * 12, 12);
        	System.arraycopy(getUpIndices(index * 4), 0, indices, index * 6, 6);
        	index++;
    	}
    	indiceCount = index * 4;
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
    
    private float[] getTexCoords(BlockFace face, TextureMap textureMap) {
    	return new float[] {
      			getU(face.getUvFrom().x, textureMap), getV(face.getUvFrom().y, face, textureMap),
      			getU(face.getUvTo().x, textureMap), getV(face.getUvFrom().y, face, textureMap),
      			getU(face.getUvTo().x, textureMap), getV(face.getUvTo().y, face, textureMap),
      			getU(face.getUvFrom().x, textureMap), getV(face.getUvTo().y, face, textureMap)
    	};
    }
    
    private float getU(float u, TextureMap textureMap) {
    	return u / (float) textureMap.getWidth();
    }
    
    private float getV(float v, BlockFace face, TextureMap textureMap) {
    	return (v + (float) face.getTextureOffset()) / (float) textureMap.getHeight();
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
