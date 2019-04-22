package com.eagle.entity;

import java.io.Serializable;

public class ParamInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3846443891980554973L;
	
	private String paramName;
	private int index;
	public String getParamName() {
		return paramName;
	}
	public void setParamName(String paramName) {
		this.paramName = paramName;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	
	

}
