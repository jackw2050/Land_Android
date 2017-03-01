package com.zlscorp.ultragrav.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "meterParams")
public class MeterParams extends AbstractParamsObject {
    private static final long serialVersionUID = 1L;
    
    public static final double DC_LOW_LIMIT = 5.0;
    public static final double DC_NEAR_LOW_LIMIT = 10.0;
    public static final double DC_NEAR_HIGH_LIMIT = 93.0;
    public static final double DC_HIGH_LIMIT = 98.0;

    @DatabaseField
    private String serialNumber;
    
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
    private Double gain;

    @DatabaseField
    private Integer stopBoost;

    @DatabaseField
    private Double boostFactor;

    @DatabaseField
    private Integer temperature0;

    @DatabaseField
    private Double temperatureA;

    @DatabaseField
    private Double temperatureB;

    @DatabaseField
    private Double temperatureC;

    @DatabaseField
    private Boolean isCalibrated;

    @DatabaseField
    private Boolean isPlatesReversed;

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

    public Double getGain() {
        return gain;
    }

    public void setGain(Double gain) {
        this.gain = gain;
    }

    public Integer getStopBoost() {
        return stopBoost;
    }

    public void setStopBoost(Integer stopBoost) {
        this.stopBoost = stopBoost;
    }

    public Double getBoostFactor() {
        return boostFactor;
    }

    public void setBoostFactor(Double boostFactor) {
        this.boostFactor = boostFactor;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Integer getTemperature0() {
        return temperature0;
    }

    public void setTemperature0(Integer temperature0) {
        this.temperature0 = temperature0;
    }

    public Double getTemperatureA() {
        return temperatureA;
    }

    public void setTemperatureA(Double temperatureA) {
        this.temperatureA = temperatureA;
    }

    public Double getTemperatureB() {
        return temperatureB;
    }

    public void setTemperatureB(Double temperatureB) {
        this.temperatureB = temperatureB;
    }

    public Double getTemperatureC() {
        return temperatureC;
    }

    public void setTemperatureC(Double temperatureC) {
        this.temperatureC = temperatureC;
    }

    public Boolean isCalibrated() {
        return isCalibrated;
    }

    public void setCalibrated(Boolean calibrated) {
        this.isCalibrated = calibrated;
    }

    public Boolean getPlatesReversed() {
        return isPlatesReversed;
    }

    public void setPlatesReversed(Boolean platesReversed) {
        this.isPlatesReversed = platesReversed;
    }

    @Override
    public String toString() {
        return "MeterParams [serialNumber=" + serialNumber + ", readingLine=" + readingLine + ", beamScale="
                + beamScale + ", feedbackScale=" + feedbackScale + ", minStop=" + minStop + ", maxStop="
                + maxStop + ", gain=" + gain + ", stopBoost=" + stopBoost + ", boostFactor=" + boostFactor
                + ", temperature0=" + temperature0 + ", temperatureA=" + temperatureA + ", temperatureB="
                + temperatureB + ", temperatureC=" + temperatureC + ", isCalibrated=" + isCalibrated
                + ", isPlatesReversed=" + isPlatesReversed + "]";
    }

}
