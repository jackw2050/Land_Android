package com.zlscorp.ultragrav.meter.processor;

import android.location.Location;
import android.util.Log;

import com.zlscorp.ultragrav.communication.transfer.CurrentReadingResponse;
import com.zlscorp.ultragrav.debug.MyDebug;

public class ContinuousModeObservation extends AbstractBaseObservation {

    private static final String TAG = "ContinuousModeObservation";

	private ThreeStageRcFilter readingFilter;
	private int dataOutputRate;
    private double totalCorrection; 

    // TODO Test for DC Range Alert
//    private int dCTest;
//    private boolean isIncDcTest;

	public ContinuousModeObservation(ProcessorConfig config) throws Exception {
		meter = new Meter(config);
        readingFilter = new ThreeStageRcFilter(config);
        readingLocation = new Location("ultragrav");
        readingLocation.setLatitude(config.getStation().getLatitude());
        readingLocation.setLongitude(config.getStation().getLongitude());
        readingLocation.setAltitude(config.getStation().getElevation());
        earthtide = new Earthtide();

        elapsedTime = 0;
        dataOutputRate = config.getSystemParams().getDataOutputRate();
        dialReading = config.getSystemParams().getDialReading();
		aGain = meter.gain * config.getSystemParams().getFeedbackGain();
		sum = 0;
        calibratedDialValues = config.getCalibratedDialValues();
        isDisplayGravityData = false;

        // TODO Test for DC Range Alert
//        dCTest = 85;      // In %
//        isIncDcTest = true;
	}

	@Override
	public void process(CurrentReadingResponse newDataSet) {
	    double filter;

        meter.processMeterDataSet(newDataSet);   // Take new raw frequency data from receive buffer, do
                                                 // initial processing and put it into it's respective members

        // If true, we displayed gravity data the last pass. Now clear flags and reset timer.
        if (isDisplayGravityData) { 
            isDisplayGravityData = false;
            isGoodObservation = false;
            elapsedTime = 0;
        }
        
        elapsedTime++;

		calculatePwmDutyCycle(meter);
		totalCorrection = (32767. - ((double) dutyCycle)) * meter.feedbackScale; // sign switched
		filter = readingFilter.calculate(totalCorrection);
		observedGravity = getCalibratedDialValue(dialReading) + filter + 
		        meter.temperature.getCorrection() + meter.levels.getCorrection();

        if (MyDebug.LOG) {
            Log.d(TAG, "Continuous Observation.process: elapsedTimeMs = " + elapsedTime + ", " +
            		"TC = " + totalCorrection + ", Filter = " + filter);
        }

        readingTime = System.currentTimeMillis();

        if (((readingTime/1000) % dataOutputRate) == 0) {

	        meter.levels.calculateCorrection();
			meter.temperature.calculateCorrection();
			calculatedEarthtide = -earthtide.calculate(readingTime, readingLocation);
			isDisplayGravityData = true;
			isGoodObservation = true;

	        if (MyDebug.LOG) {
	            Log.d(TAG, "Observation Ready: isDisplayGravityData = " + isDisplayGravityData + 
	                    " isGoodObservation = " + isGoodObservation);
	        }
		}
	}
	
    /**
     * The new duty cycle is sent to the meter in MeterService.
     * dutyCycle is sent to the meter in the range 65534 - 1,
     * and converted to 0.0 - 100.0% for the display.
     */
    @Override
	public void calculatePwmDutyCycle(Meter meter) {

		deltaFrequency = (((double) meter.beamFrequency) - meter.readingLine) * aGain;
		sum = sum + deltaFrequency / 4.;
		dutyCycle = 32767 + (int)(sum + deltaFrequency);

		// TODO - Test for DC Range Alerts
		// Make DC go from 85% - 95, then back to 85.
		// and from 15% to 5, then back to 15.
//		if (isIncDcTest) {
//		    if (dCTest < 96) {
//		        dCTest++;
//		    } else {
//		        isIncDcTest = false;
//		    }
//		} else {
//		    if (dCTest > 85) {
//		        dCTest--;
//		    }
//		}
//		// Turn 85 - 95% into  
//		dutyCycle = (int) ((100 - dCTest) * 655.34);  // dCTest = 0 >> DC = 65535, dCTest = 100 >> DC = 0

		if (dutyCycle > 0xFFFE) { // keep the new pwm duty cycle in range
		    dutyCycle = 0xFFFE;
		}
		if (dutyCycle < 1) {
		    dutyCycle = 1;
		}
	}
    
    @Override
    public void requestReset() {                 // Only needed for SingleModeObservation
    }

	@Override
	public double getStandardDeviation() {       // Only needed for SingleModeObservation
		return 0;
	}

    @Override
    public boolean isGoodObservation() {         // Only needed for SingleModeObservation
        return isGoodObservation;
    }

    @Override
    public int getBeamFrequency() {
        return meter.beamFrequency;
    }

    @Override
    public int getCrossLevelFrequency() {
        return meter.levels.crossLevel.getFrequency();
    }

    @Override
    public int getLongLevelFrequency() {
        return meter.levels.longLevel.getFrequency();
    }

    @Override
    public int getTemperatureFrequency() {
        return meter.temperature.getFrequency();
    }

    @Override
    public double getObservedGravity() {
        return observedGravity;
    }

    @Override
    public int getDutyCycleAs16Bits() {
        return dutyCycle;
    }

    @Override
    public double getDutyCycleAsPercentage() {
        return 100.0 - (dutyCycle / 655.34);
    }

    @Override
    public int getDialReading() {
        return dialReading;
    }

    @Override
    public double getEarthtide() {
        return calculatedEarthtide;
    }

    @Override
    public double getFeedbackCorrection() {
        return totalCorrection;
    }

    @Override
    public double getLevelCorrection() {
        return meter.levels.getCorrection();
    }

    @Override
    public double getTemperatureCorrection() {
        return meter.temperature.getCorrection();
    }

    @Override
    public boolean isDisplayGravityData() {
        return isDisplayGravityData;
    }

    @Override
    public long getElapsedTimeMs() {
        return elapsedTime;
    }

    @Override
    public double getBeamError() {               // Only needed for SingleModeObservation
        return 0;
    }

    @Override
    public boolean isDisplayStatisticsData() {   // Only needed for SingleModeObservation
        return false;
    }

    @Override
    public long getReadingTime() {
        return readingTime;
    }

//    @Override
//    public Time getReadingTime() {
//        return readingTime;
//    }

    @Override
    public boolean isLevelCorrectionGood() {
        return meter.levels.isWithinLimit();
    }

    @Override
    public boolean isTemperatureGood() {
        return meter.temperature.isWithinLimits();
    }

    @Override
    public int getDataOutputRate() {
        return dataOutputRate;
    }

    @Override
    public int getFilterTimeConstant() {
        return readingFilter.getFilterTimeConstant();
    }

    @Override
    public void setSampleSize(int sampleSize) {
    }
}
