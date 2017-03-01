package com.zlscorp.ultragrav.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "levelCorrectionParams")
public class LevelCorrectionParams extends AbstractParamsObject {
	private static final long serialVersionUID = 1L;

	@DatabaseField
	private Integer longLevel0;
	
	@DatabaseField
	private Double longLevelA;
	
	@DatabaseField
	private Double longLevelB;
	
	@DatabaseField
	private Double longLevelC;
	
	@DatabaseField
	private Integer crossLevel0;
	
	@DatabaseField
	private Double crossLevelA;
	
	@DatabaseField
	private Double crossLevelB;
	
    @DatabaseField
    private Double crossLevelC;

    @DatabaseField
    private boolean justRestored;

	public Integer getLongLevel0() {
		return longLevel0;
	}

	public void setLongLevel0(Integer longLevel0) {
		this.longLevel0 = longLevel0;
	}

	public Double getLongLevelA() {
		return longLevelA;
	}

	public void setLongLevelA(Double longLevelA) {
		this.longLevelA = longLevelA;
	}

	public Double getLongLevelB() {
		return longLevelB;
	}

	public void setLongLevelB(Double longLevelB) {
		this.longLevelB = longLevelB;
	}

	public Double getLongLevelC() {
		return longLevelC;
	}

	public void setLongLevelC(Double longLevelC) {
		this.longLevelC = longLevelC;
	}

	public Integer getCrossLevel0() {
		return crossLevel0;
	}

	public void setCrossLevel0(Integer crossLevel0) {
		this.crossLevel0 = crossLevel0;
	}

	public Double getCrossLevelA() {
		return crossLevelA;
	}

	public void setCrossLevelA(Double crossLevelA) {
		this.crossLevelA = crossLevelA;
	}

	public Double getCrossLevelB() {
		return crossLevelB;
	}

	public void setCrossLevelB(Double crossLevelB) {
		this.crossLevelB = crossLevelB;
	}

	public Double getCrossLevelC() {
		return crossLevelC;
	}

	public void setCrossLevelC(Double crossLevelC) {
		this.crossLevelC = crossLevelC;
	}

    public boolean justRestored() {
        return justRestored;
    }

    public void setJustRestored(boolean justRestored) {
        this.justRestored = justRestored;
    }

    @Override
    public String toString() {
        return "LevelCorrectionParams [longLevel0=" + longLevel0 + ", longLevelA=" + longLevelA + ", longLevelB="
                + longLevelB + ", longLevelC=" + longLevelC + ", crossLevel0=" + crossLevel0 + ", crossLevelA="
                + crossLevelA + ", crossLevelB=" + crossLevelB + ", crossLevelC=" + crossLevelC + ", allowPersist="
                + justRestored + "]";
    }
}
