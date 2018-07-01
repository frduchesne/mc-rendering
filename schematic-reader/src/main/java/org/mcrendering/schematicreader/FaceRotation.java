package org.mcrendering.schematicreader;

public class FaceRotation {

	private String axis;
	private float angle;
	
	public String getAxis() {
		return axis;
	}
	public void setAxis(String axis) {
		this.axis = axis;
	}
	public float getAngle() {
		return angle;
	}
	public void setAngle(float angle) {
		this.angle = angle;
	}
	@Override
	public String toString() {
		return "FaceRotation [axis=" + axis + ", angle=" + angle + "]";
	}
	
	
}
