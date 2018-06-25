package org.mcrendering.pointshadows;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class PointLight {

    private Vector3f color;

    private Vector3f mPosition;
    
    private Vector3f mvPosition;

	private float intensity;

    private Attenuation attenuation;
    
    private Tile tile;
    
	private static final float fov0 = (float)Math.toRadians(143.98570868f+1.99273682f);
	private static final float fov1 = (float)Math.toRadians(125.26438968f+2.78596497f);
	
	private static Matrix4f tiledShadowProjMatrices[] = new Matrix4f[2];
	private static Matrix4f tiledShadowRotMatrices[] = new Matrix4f[4];
	
	static {
		float radius = 16;
	    tiledShadowProjMatrices[0] = perspectiveFovs(fov0, fov1, 0.2f, radius);
	    tiledShadowProjMatrices[1] = perspectiveFovs(fov1, fov0, 0.2f, radius);
	    
	    tiledShadowRotMatrices[0] = setRotationX(27.36780516f).mul(setRotationY(180.0f));
	    tiledShadowRotMatrices[1] = setRotationZ(90.0f).mul(setRotationX(27.36780516f)).mul(setRotationY(0f));
	    tiledShadowRotMatrices[2] = setRotationX(-27.36780516f).mul(setRotationY(270.0f));
	    tiledShadowRotMatrices[3] = setRotationZ(90.0f).mul(setRotationX(-27.36780516f)).mul(setRotationY(90.0f));
	}
	
	private static Matrix4f setRotationX(float deg) {
		return new Matrix4f().rotateX((float)Math.toRadians(deg));
	}
	
	private static Matrix4f setRotationY(float deg) {
		return new Matrix4f().rotateY((float)Math.toRadians(deg));
	}
	
	private static Matrix4f setRotationZ(float deg) {
		return new Matrix4f().rotateZ((float)Math.toRadians(deg));
	}
	
	private static Matrix4f perspectiveFovs(float fovx, float fovy, float near, float far) {
	    float left, right, top, bottom;
	    top = near * (float) Math.tan(fovy*0.5f);
	    bottom = -top;
	    right = near * (float) Math.tan(fovx*0.5f);
	    left = -right;
	    return new Matrix4f().frustum(left, right, bottom, top, near, far);
	}
	
    public PointLight(Vector3f color, Vector3f position, float intensity, Attenuation attenuation) {
        this.color = color;
        this.mPosition = position;
        this.intensity = intensity;
        this.attenuation = attenuation;
    }

    public PointLight(PointLight pointLight) {
        this(new Vector3f(pointLight.getColor()), new Vector3f(pointLight.getMPosition()),
                pointLight.getIntensity(), pointLight.getAttenuation());
    }

    public Vector3f getColor() {
        return color;
    }

    public void setColor(Vector3f color) {
        this.color = color;
    }

    public Vector3f getMPosition() {
        return mPosition;
    }

    public void setMPosition(Vector3f position) {
        this.mPosition = position;
    }

    public float getIntensity() {
        return intensity;
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }

    public Attenuation getAttenuation() {
        return attenuation;
    }

    public void setAttenuation(Attenuation attenuation) {
        this.attenuation = attenuation;
    }


    public Vector3f getMvPosition() {
		return mvPosition;
	}

	public void updateViewMatrix(Matrix4f viewMatrix) {
        Vector4f aux = new Vector4f(getMPosition(), 1);
        aux.mul(viewMatrix);
        this.mvPosition = new Vector3f(aux.x, aux.y, aux.z);
	}
	
	public void updateTile(TileMap tileMap) {
        this.tile = tileMap.nextTile();
	}
	
	public Matrix4f[] getShadowMatrices() {
    	
    	Matrix4f shadowTexMatrices[] = new Matrix4f[4];
    	shadowTexMatrices[0] = new Matrix4f(
    			tile.size, 0.0f, 0.0f, 0.0f, 
    			0.0f, tile.size*0.5f, 0.0f, 0.0f, 
    			0.0f, 0.0f, 1.0f, 0.0f, 
    			tile.position.x, tile.position.y-(tile.size*0.5f), 0.0f, 1.0f);
    	shadowTexMatrices[1] = new Matrix4f(
    			tile.size*0.5f, 0.0f, 0.0f, 0.0f, 
    			0.0f, tile.size, 0.0f, 0.0f, 
    			0.0f, 0.0f, 1.0f, 0.0f, 
    			tile.position.x+(tile.size*0.5f), tile.position.y, 0.0f, 1.0f);
    	shadowTexMatrices[2] = new Matrix4f(
    			tile.size, 0.0f, 0.0f, 0.0f, 
    			0.0f, tile.size*0.5f, 0.0f, 0.0f, 
    			0.0f, 0.0f, 1.0f, 0.0f, 
    			tile.position.x, tile.position.y+(tile.size*0.5f), 0.0f, 1.0f);
    	shadowTexMatrices[3] = new Matrix4f(
    			tile.size*0.5f, 0.0f, 0.0f, 0.0f, 
    			0.0f, tile.size, 0.0f, 0.0f, 
    			0.0f, 0.0f, 1.0f, 0.0f, 
    			tile.position.x-(tile.size*0.5f), tile.position.y, 0.0f, 1.0f);

    	Matrix4f[] shadowViewProjTexMatrices = new Matrix4f[4];
    	Matrix4f shadowTransMatrix = new Matrix4f().translate(new Vector3f(mPosition).negate());
    	for(int i=0; i<4; i++)
    	{
    		Matrix4f shadowViewMatrix = new Matrix4f(tiledShadowRotMatrices[i]).mul(shadowTransMatrix);
    		int index = i & 1;
    		shadowViewProjTexMatrices[i] = shadowTexMatrices[i].mul(new Matrix4f(tiledShadowProjMatrices[index])).mul(shadowViewMatrix);
    	}

    	return shadowViewProjTexMatrices;
	}
	
    public static class Attenuation {

        private float constant;

        private float linear;

        private float exponent;

        public Attenuation(float constant, float linear, float exponent) {
            this.constant = constant;
            this.linear = linear;
            this.exponent = exponent;
        }

        public float getConstant() {
            return constant;
        }

        public void setConstant(float constant) {
            this.constant = constant;
        }

        public float getLinear() {
            return linear;
        }

        public void setLinear(float linear) {
            this.linear = linear;
        }

        public float getExponent() {
            return exponent;
        }

        public void setExponent(float exponent) {
            this.exponent = exponent;
        }
    }
}