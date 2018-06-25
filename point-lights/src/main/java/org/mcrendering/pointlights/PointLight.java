package org.mcrendering.pointlights;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class PointLight {

    private Vector3f color;

    private Vector3f mPosition;
    
    private Vector3f mvPosition;

	private float intensity;

    private Attenuation attenuation;
	
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