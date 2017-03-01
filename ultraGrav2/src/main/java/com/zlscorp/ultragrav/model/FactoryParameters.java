package com.zlscorp.ultragrav.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "factoryParameters")
public class FactoryParameters extends AbstractParamsObject {
	private static final long serialVersionUID = 1L;

    @DatabaseField
    private boolean isFileCreated;

    @DatabaseField
    private Double feedbackGain;

    @DatabaseField
    private Double observationPrecision;

    @DatabaseField
    private Integer dataOutputRate;         // Only used for continuous observations

    @DatabaseField
    private Integer filterTimeConstant;     // Only used for continuous observations

    @DatabaseField
    private Integer readingLine;

    @DatabaseField
    private Double beamScale;

    @DatabaseField
    private Double feedbackScale;

    @DatabaseField
    private Integer minStop;

    @DatabaseField
    private Integer maxStop;

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

    public boolean isFileCreated() {
        return isFileCreated;
    }

    public void setFileCreated(boolean isFileCreated) {
        this.isFileCreated = isFileCreated;
    }

    public Double getFeedbackGain() {
        return feedbackGain;
    }

    public void setFeedbackGain(Double feedbackGain) {
        this.feedbackGain = feedbackGain;
    }

    public Double getObservationPrecision() {
        return observationPrecision;
    }

    public void setObservationPrecision(Double observationPrecision) {
        this.observationPrecision = observationPrecision;
    }

    public Integer getDataOutputRate() {
        return dataOutputRate;
    }

    public void setDataOutputRate(Integer dataOutputRate) {
        this.dataOutputRate = dataOutputRate;
    }

    public Integer getFilterTimeConstant() {
        return filterTimeConstant;
    }

    public void setFilterTimeConstant(Integer filterTimeConstant) {
        this.filterTimeConstant = filterTimeConstant;
    }

    public Integer getReadingLine() {
        return readingLine;
    }

    public void setReadingLine(Integer readingLine) {
        this.readingLine = readingLine;
    }

    public Double getBeamScale() {
        return beamScale;
    }

    public void setBeamScale(Double beamScale) {
        this.beamScale = beamScale;
    }

    public Double getFeedbackScale() {
        return feedbackScale;
    }

    public void setFeedbackScale(Double feedbackScale) {
        this.feedbackScale = feedbackScale;
    }

    public Integer getMinStop() {
        return minStop;
    }

    public void setMinStop(Integer minStop) {
        this.minStop = minStop;
    }

    public Integer getMaxStop() {
        return maxStop;
    }

    public void setMaxStop(Integer maxStop) {
        this.maxStop = maxStop;
    }

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

    @Override
    public String toString() {
        return "FactoryParameters [feedbackGain=" + feedbackGain + ", observationPrecision=" + observationPrecision
                + ", dataOutputRate=" + dataOutputRate + ", filterTimeConstant=" + filterTimeConstant
                + ", readingLine=" + readingLine + ", beamScale=" + beamScale + ", feedbackScale=" + feedbackScale
                + ", minStop=" + minStop + ", maxStop=" + maxStop + ", longLevel0=" + longLevel0 + ", longLevelA="
                + longLevelA + ", longLevelB=" + longLevelB + ", longLevelC=" + longLevelC + ", crossLevel0="
                + crossLevel0 + ", crossLevelA=" + crossLevelA + ", crossLevelB=" + crossLevelB + ", crossLevelC="
                + crossLevelC + "]";
    }
}
