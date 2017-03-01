package com.zlscorp.ultragrav.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "systemParams")
public class SystemParams extends AbstractParamsObject {
    private static final long serialVersionUID = 1L;

    @DatabaseField
    private Integer dialReading;

    @DatabaseField
    private Boolean useNoncalibratedPoints;

    @DatabaseField
    private Double feedbackGain;

    @DatabaseField
    private Double observationPrecision;

    @DatabaseField
    private Boolean enableStationSelect;

    @DatabaseField
    private Boolean useCelsius;

    @DatabaseField
    private Integer dataOutputRate;         // Only used for continuous observations

    @DatabaseField
    private Integer filterTimeConstant;     // Only used for continuous observations

    @DatabaseField
    private Boolean isSaveFrequencies;      // If this is true, the beam and level frequencies are output with the Continuous obs data. 

    @DatabaseField
    private Boolean isTestMode;             // If this is true, additional test data is displayed in Single and Continuous activities 

    @DatabaseField
    private Boolean justRestored;

    public static int DIAL_READING_MIN = 250;
    public static int DIAL_READING_MAX = 6850;
    public static int DIAL_READING_INC = 50;

    public static double FEEDBACK_GAIN_MIN = 0.2;
    public static double FEEDBACK_GAIN_MAX = 2.0;

    public static double OBS_PRECISION_MIN = 0.003;
    public static double OBS_PRECISION_MAX = 0.010;

    public static int DATA_OUTPUT_RATE_MIN = 1;
    public static int FILTER_TIME_CONST_MIN = 1;

    public Integer getDialReading() {
        return dialReading;
    }

    public void setDialReading(Integer dialReading) {
        this.dialReading = dialReading;
    }

    public Boolean getUseNoncalibratedPoints() {
        return useNoncalibratedPoints;
    }

    public void setUseNoncalibratedPoints(Boolean useNoncalibratedPoints) {
        this.useNoncalibratedPoints = useNoncalibratedPoints;
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

    public Boolean getEnableStationSelect() {
        return enableStationSelect;
    }

    public void setEnableStationSelect(Boolean enableStationSelect) {
        this.enableStationSelect = enableStationSelect;
    }

    public Boolean getUseCelsius() {
        return useCelsius;
    }

    public void setUseCelsius(Boolean useCelsius) {
        this.useCelsius = useCelsius;
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

    public Boolean isSaveFrequencies() {
        return isSaveFrequencies;
    }

    public void setSaveFrequencies(Boolean isSaveFrequencies) {
        this.isSaveFrequencies = isSaveFrequencies;
    }

    public Boolean isTestMode() {
        return isTestMode;
    }

    public void setTestMode(Boolean isTestMode) {
        this.isTestMode = isTestMode;
    }

    public Boolean justRestored() {
        return justRestored;
    }

    public void setJustRestored(Boolean justRestored) {
        this.justRestored = justRestored;
    }

    @Override
    public String toString() {
        return "SystemParams [dialReading=" + dialReading + ", useNoncalibratedPoints=" + useNoncalibratedPoints
                + ", feedbackGain=" + feedbackGain + ", observationPrecision=" + observationPrecision
                + ", enableStationSelect=" + enableStationSelect + ", useCelsius=" + useCelsius + ", dataOutputRate="
                + dataOutputRate + ", filterTimeConstant=" + filterTimeConstant + ", isSaveFrequencies="
                + isSaveFrequencies + ", isTestMode=" + isTestMode + ", allowPersist=" + justRestored + "]";
    }
}
