package com.zlscorp.ultragrav.meter.processor;

import com.zlscorp.ultragrav.communication.transfer.CurrentReadingResponse;
import com.zlscorp.ultragrav.debug.MyDebug;
import com.zlscorp.ultragrav.model.MeterParams;

import android.util.Log;

import java.util.Locale;

public class Meter {
	private static final String TAG = "Meter_Class";

	public double readingLine;
	public double beamScale;
	public double feedbackScale;
	public double minStop;
	public double maxStop;
	public double gain;
	public double stopBoost;
	public double boostFactor;
	public String serialNumber;
	public boolean calibrated;
	public boolean platesReversed;
	public MeterLevel levels;
	public Temperature temperature;
	private CurrentReadingResponse currentDataSet, previousDataSet, olderDataSet;
	public int beamFrequency;

	public Meter(ProcessorConfig config) throws Exception {
	    
        // Test
        boolean test = false;
        if (test) {
            throw new Exception("config is null.");
        }

	    if (config != null) {
	        levels = new MeterLevel(config.getLevelCorrectionParams());
	        temperature = new Temperature(config.getMeterParams());
	        beamFrequency = 0;

	        // Get meter parameter values from config object
	        MeterParams meterParams = config.getMeterParams();

	        // These are displayed on the Settings/Meter Parameters tab 
	        readingLine =   meterParams.getReadingLine();
	        beamScale =     meterParams.getBeamScale();
	        feedbackScale = meterParams.getFeedbackScale();
	        minStop =       meterParams.getMinStop();
	        maxStop =       meterParams.getMaxStop();

	        // These are displayed on the Private/Private Parameters tab 
	        gain =          meterParams.getGain();
	        stopBoost =     meterParams.getStopBoost();
	        boostFactor =   meterParams.getBoostFactor();
	        serialNumber =  meterParams.getSerialNumber();
	    } else {
	        if (MyDebug.LOG) {
	            Log.e(TAG, "config is null");
	        }
	        throw new Exception("config is null.");
	    }
	}

	public void processMeterDataSet(CurrentReadingResponse newDataSet) {
		
		currentDataSet = newDataSet;
		
		if (olderDataSet != null) {
			int value;

			// calc rollover for beam and scale it
			if (olderDataSet.getBeamFreq() >= currentDataSet.getBeamFreq()) {
				value = olderDataSet.getBeamFreq() - currentDataSet.getBeamFreq();
			} else {
				value = 0x10000 + olderDataSet.getBeamFreq() - currentDataSet.getBeamFreq();
			}
			beamFrequency = value / 5; // Scale the value

			if (MyDebug.LOG) {
			    String newBeamFreqValues = String.format(Locale.US, "New Beam Frequency value = %d, %X, " +
			            "%X, %X", beamFrequency, currentDataSet.getBeamFreq(), previousDataSet.getBeamFreq(), 
			            olderDataSet.getBeamFreq()); 
			    Log.d(TAG, newBeamFreqValues);
			}

			// calc rollover for long level and scale it
			if (olderDataSet.getLongLevelFreq() >= currentDataSet.getLongLevelFreq()) {
				value = olderDataSet.getLongLevelFreq() - currentDataSet.getLongLevelFreq();
			} else {
				value = 0x10000 + olderDataSet.getLongLevelFreq() - currentDataSet.getLongLevelFreq();
			}
			levels.longLevel.setFrequency(value / 5);

			// calc rollover for cross level and scale it
			if (olderDataSet.getCrossLevelFreq() >= currentDataSet.getCrossLevelFreq()) {
				value = olderDataSet.getCrossLevelFreq() - currentDataSet.getCrossLevelFreq();
			} else {
				value = 0x10000 + olderDataSet.getCrossLevelFreq() - currentDataSet.getCrossLevelFreq();
			}
			levels.crossLevel.setFrequency(value / 5);
		}
		
		olderDataSet = previousDataSet;
		previousDataSet = currentDataSet;
		
		// Filters the incoming temperature frequency to create the new temperature value
		temperature.filter(currentDataSet.getTemperatureFreq());     
	}
}
