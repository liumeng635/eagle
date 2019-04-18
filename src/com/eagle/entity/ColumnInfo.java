package com.eagle.entity;

import java.io.Serializable;

public class ColumnInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4654825694222909466L;
	
	private String columnName;
	private String columnType;
	public String getColunmName() {
		return columnName;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getColumnType() {
		return columnType;
	}
	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}
	
	

}
