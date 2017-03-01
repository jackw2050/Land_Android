package com.zlscorp.ultragrav.meter.processor;

import java.io.Serializable;

public class ProcessorState implements Serializable {
   private static final long serialVersionUID = 1L;

   private ProcessorConfig processorConfig;
	
   private boolean isDisplayGravityData;

   private boolean isDisplayStatisticsData;
	
   private boolean isGoodObservation;
	
   private boolean isLevelCorrectionGood;
    
   private boolean isTemperatureGood;
    
	private long elapsedTime;
	
	private int beamFreq;
	
	private int longLevelFreq;
	
	private int crossLevelFreq;
	
	private int temperatureFreq;
	
	private double standardDeviation;
	
	private double observedGravity;
	
   private int dutyCycleAs16Bits;
    
   private double dutyCycleAsPercentage;
    
	private int dialReading;
	
	private double beamError;
	
	private double feedbackCorrection;
	
	private double earthtide;
	
	private double levelCorrection;
	
	private double tempCorrection;
	
	private int dataOutputRate;
	
	private int filterTimeConstant;

   private long readingTime;

   public boolean isDisplayGravityData() {
        return isDisplayGravityData;
    }

    public void setDisplayGravityData(boolean isDisplayGravityData) {
        this.isDisplayGravityData = isDisplayGravityData;
    }

    public boolean isDisplayStatisticsData() {
        return isDisplayStatisticsData;
    }

    public void setDisplayStatisticsData(boolean isDisplayStatisticsData) {
        this.isDisplayStatisticsData = isDisplayStatisticsData;
    }

    public boolean isGoodObservation() {
		return isGoodObservation;
	}

	public void setGoodObservation(boolean goodObservation) {
		this.isGoodObservation = goodObservation;
	}

	public boolean isLevelCorrectionGood() {
        return isLevelCorrectionGood;
    }

    public void setIsLevelCorrectionGood(boolean isLevelCorrectionGood) {
        this.isLevelCorrectionGood = isLevelCorrectionGood;
    }

    public boolean isTemperatureGood() {
        return isTemperatureGood;
    }

    public void setIsTemperatureGood(boolean isTemperatureGood) {
        this.isTemperatureGood = isTemperatureGood;    }

    public long getElapsedTime() {
		return elapsedTime;
	}

	public void setElapsedTime(long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	public double getStandardDeviation() {
		return standardDeviation;
	}

	public void setStandardDeviation(double standardDeviation) {
		this.standardDeviation = standardDeviation;
	}

	public double getObservedGravity() {
		return observedGravity;
	}

	public void setObservedGravity(double observedGravity) {
		this.observedGravity = observedGravity;
	}

	public int getDutyCycleAs16Bits() {
        return dutyCycleAs16Bits;
    }

    public void setDutyCycleAs16Bits(int dutyCycleAs16Bits) {
        this.dutyCycleAs16Bits = dutyCycleAs16Bits;
    }

    public double getDutyCycleAsPercentage() {
        return dutyCycleAsPercentage;
    }

    public void setDutyCycleAsPercentage(double dutyCycleAsPercentage) {
        this.dutyCycleAsPercentage = dutyCycleAsPercentage;
    }

    public int getDialReading() {
		return dialReading;
	}

	public void setDialReading(int dialReading) {
		this.dialReading = dialReading;
	}

	public double getFeedbackCorrection() {
		return feedbackCorrection;
	}

	public void setFeedbackCorrection(double feedbackCorrection) {
		this.feedbackCorrection = feedbackCorrection;
	}

	public double getEarthtide() {
		return earthtide;
	}

	public void setEarthtide(double earthtide) {
		this.earthtide = earthtide;
	}

	public double getLevelCorrection() {
		return levelCorrection;
	}

	public void setLevelCorrection(double levelCorrection) {
		this.levelCorrection = levelCorrection;
	}

	public double getTempCorrection() {
		return tempCorrection;
	}

	public void setTempCorrection(double tempCorrection) {
		this.tempCorrection = tempCorrection;
	}

	public double getBeamError() {
		return beamError;
	}

	public void setBeamError(double beamError) {
		this.beamError = beamError;
	}

	public int getBeamFreq() {
		return beamFreq;
	}

	public void setBeamFrequency(int beamFreq) {
		this.beamFreq = beamFreq;
	}

	public int getLongLevelFreq() {
		return longLevelFreq;
	}

	public void setLongLevelFreq(int longLevelFreq) {
		this.longLevelFreq = longLevelFreq;
	}

	public int getCrossLevelFreq() {
		return crossLevelFreq;
	}

	public void setCrossLevelFreq(int crossLevelFreq) {
		this.crossLevelFreq = crossLevelFreq;
	}

	public int getTemperatureFrequency() {
		return temperatureFreq;
	}

	public void setTemperatureFrequency(int temperature) {
		this.temperatureFreq = temperature;
	}

    public long getReadingTime() {
        return readingTime;
    }

    public void setReadingTime(long readingTime) {
        this.readingTime = readingTime;
    }

//    public Time getReadingTime() {
//        return readingTime;
//    }
//
//    public void setReadingTime(Time readingTime) {
//        this.readingTime = readingTime;
//    }

    public int getDataOutputRate() {
        return dataOutputRate;
    }

    public void setDataOutputRate(int dataOutputRate) {
        this.dataOutputRate = dataOutputRate;
    }

    public int getFilterTimeConstant() {
        return filterTimeConstant;
    }

    public void setFilterTimeConstant(int filterTimeConstant) {
        this.filterTimeConstant = filterTimeConstant;
    }

    public ProcessorConfig getProcessorConfig() {
		return processorConfig;
	}

	public void setProcessorConfig(ProcessorConfig processorConfig) {
		this.processorConfig = processorConfig;
	}

    @Override
    public String toString() {
        return "ProcessorState [processorConfig=" + processorConfig + ", isDisplayGravityData="
                + isDisplayGravityData + ", isDisplayStatisticsData=" + isDisplayStatisticsData
                + ", isGoodObservation=" + isGoodObservation + ", isLevelCorrectionGood="
                + isLevelCorrectionGood + ", isTemperatureGood=" + isTemperatureGood + ", elapsedTimeMs="
                + elapsedTime + ", beamFreq=" + beamFreq + ", longLevelFreq=" + longLevelFreq
                + ", crossLevelFreq=" + crossLevelFreq + ", temperature=" + temperatureFreq
                + ", standardDeviation=" + standardDeviation + ", observedGravity=" + observedGravity
                + ", dutyCycleAs16Bits=" + dutyCycleAs16Bits + ", dutyCycleAsPercentage="
                + dutyCycleAsPercentage + ", dialReading=" + dialReading + ", beamError=" + beamError
                + ", feedbackCorrection=" + feedbackCorrection + ", earthtide="
                + earthtide + ", levelCorrection=" + levelCorrection + ", tempCorrection="
                + tempCorrection + ", dataOutputRate=" + dataOutputRate + ", filterTimeConstant="
                + filterTimeConstant + ", readingTime=" + readingTime + "]";
    }
}
