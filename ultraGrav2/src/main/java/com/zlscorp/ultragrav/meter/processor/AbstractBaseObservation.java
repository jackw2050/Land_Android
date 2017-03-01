package com.zlscorp.ultragrav.meter.processor;

import android.location.Location;
//import android.text.format.Time;
import android.util.Log;

import java.util.List;

import com.zlscorp.ultragrav.communication.transfer.CurrentReadingResponse;
import com.zlscorp.ultragrav.debug.MyDebug;
import com.zlscorp.ultragrav.model.CalibratedDialValue;

public abstract class AbstractBaseObservation {

    protected Meter meter;
//    protected Time readingTime;
    protected long readingTime;
    protected Location readingLocation;
	protected Earthtide earthtide;
    protected int dutyCycle;
//	protected long startTimeMs;
	protected long elapsedTime;
	protected int dialReading;
	protected boolean useNoncalibratedPoints;
	protected boolean enableStationSelect;
	protected double observedGravity;
	protected double aGain;
	protected double deltaFrequency;
	protected double sum;
//	protected int error;
    protected boolean isGoodObservation;
    protected boolean isDisplayGravityData;
    protected List<CalibratedDialValue> calibratedDialValues;
    protected double calculatedEarthtide;

    public final String TAG = "ObservationActivity";
    
    public abstract void process(CurrentReadingResponse newDataSet);
	
    public abstract void calculatePwmDutyCycle(Meter meter);

    public abstract void requestReset();                   // Needed in SingleObservation only

    public abstract void setSampleSize(int sampleSize);    // Needed in SingleObservation only

    public abstract int getBeamFrequency();
    
    public abstract int getCrossLevelFrequency();
    
    public abstract int getLongLevelFrequency();
    
    public abstract int getTemperatureFrequency();
    
    public abstract double getObservedGravity();
    
    public abstract int getDutyCycleAs16Bits();

    public abstract double getDutyCycleAsPercentage();

    public abstract int getDialReading();
    
    public abstract double getFeedbackCorrection();
    
    public abstract double getEarthtide();
    
    public abstract double getLevelCorrection();
    
    public abstract double getTemperatureCorrection();
    
    public abstract long getElapsedTimeMs();
    
    public abstract long getReadingTime();
//    public abstract Time getReadingTime();
    
    public abstract double getStandardDeviation();

    public abstract double getBeamError();
    
    public abstract int getDataOutputRate();

    public abstract int getFilterTimeConstant();

    public abstract boolean isGoodObservation();

    public abstract boolean isDisplayGravityData();
    
    public abstract boolean isDisplayStatisticsData();
    
    public abstract boolean isLevelCorrectionGood();
    
    public abstract boolean isTemperatureGood();
    
    public double getCalibratedDialValue(int dialReading) {
        // Return 0 if there is no calibration table or dialReading is not a valid value
        double value = 0.;    

        int tableSize = calibratedDialValues.size();
        if (MyDebug.LOG) {
            Log.i(TAG, "Calibration table size is " + tableSize);
        }
        // Validate dialReading value
        if ((tableSize == 133) && ((dialReading % 50) == 0) && (dialReading >= 250) && 
		        (dialReading <= 6850)) {
            // Over the range 250 - 6850, this returns 0 - 132
            int index = (dialReading / 50) - 5;                        
            // Use index to retrieve value from calibration table database
            value = calibratedDialValues.get(index).getDialValue();
		}
		return value;
	}
}
