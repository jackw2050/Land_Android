package com.zlscorp.ultragrav.model;

import com.j256.ormlite.field.DatabaseField;

public class CalibratedDialValue extends AbstractDomainObject {
    private static final long serialVersionUID = 1L;
    
    public static final String COLUMN_INDEX = "index";
	public static final String COLUMN_DIAL_VALUE = "dialValue";
	

	@DatabaseField(columnName=COLUMN_INDEX)
	private Integer index;
	
	@DatabaseField(columnName=COLUMN_DIAL_VALUE)
	private Double dialValue;

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public Double getDialValue() {
		return dialValue;
	}

	public void setDialValue(Double dialValue) {
		this.dialValue = dialValue;
	}

	@Override
	public String toString() {
		return "CalibratedDialValue [index=" + index + ", dialValue=" + dialValue + "]";
	}
}
