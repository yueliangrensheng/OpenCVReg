package com.diwen.android.bean;

public class LineData {
	public float startX;
	public float startY;
	public float endX;
	public float endY;
	public boolean isEditor;
	public int editroCount;
	public int count = 0;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(startX);
		result = prime * result + Float.floatToIntBits(startY);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LineData other = (LineData) obj;
		if (Float.floatToIntBits(startX) != Float.floatToIntBits(other.startX))
			return false;
		if (Float.floatToIntBits(startY) != Float.floatToIntBits(other.startY))
			return false;
		return true;
	}
	
	
}
