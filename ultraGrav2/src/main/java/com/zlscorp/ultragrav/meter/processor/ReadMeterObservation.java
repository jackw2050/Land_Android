package com.zlscorp.ultragrav.meter.processor;

//import android.os.SystemClock;
import android.util.Log;

import com.zlscorp.ultragrav.communication.transfer.CurrentReadingResponse;
import com.zlscorp.ultragrav.debug.MyDebug;

public class ReadMeterObservation extends AbstractBaseObservation {

    private static final String TAG = "ReadMeterObservation";

    public ReadMeterObservation(ProcessorConfig config) throws Exception {
		meter = new Meter(config); 
        elapsedTime = 0;
//		startTimeMs = SystemClock.elapsedRealtime();
    }

    public void process(CurrentReadingResponse newDataSet) {

        if (MyDebug.LOG) {
            Log.d(TAG, "process = " + newDataSet);
        }

        // Take new raw frequency data from receive buffer, do initial processing and put it into 
        // it's respective members
		meter.processMeterDataSet(newDataSet);

		elapsedTime++;
	}
	
    @Override
	public void calculatePwmDutyCycle(Meter meter) {
	}
    
    @Override
    public void requestReset() {
    }

	@Override
	public double getStandardDeviation() {
		return 0;
	}

    @Override
    public boolean isGoodObservation() {
        return false;
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
        return 0;
    }

    @Override
    public int getDialReading() {
        return 0;
    }

    @Override
    public double getFeedbackCorrection() {
        return meter.feedbackScale;
    }

    @Override
    public double getEarthtide() {
        return 0;
    }

    @Override
    public double getLevelCorrection() {
        return 0;
    }

    @Override
    public double getTemperatureCorrection() {
        return 0;
    }

    @Override
    public long getElapsedTimeMs() {
        return elapsedTime;
    }

    @Override
    public long getReadingTime() {
        return 0;
    }

    @Override
    public double getBeamError() {
        return 0;
    }

    @Override
    public boolean isDisplayGravityData() {
        return false;
    }

    @Override
    public boolean isDisplayStatisticsData() {
        return false;
    }

    @Override
    public boolean isLevelCorrectionGood() {
        return false;
    }

    @Override
    public boolean isTemperatureGood() {
        return false;
    }

    @Override
    public int getDataOutputRate() {
        return 0;
    }

    @Override
    public int getFilterTimeConstant() {
        return 0;
    }

    @Override
    public int getDutyCycleAs16Bits() {
        return 0;
    }

    @Override
    public double getDutyCycleAsPercentage() {
        return 0;
    }

    @Override
    public void setSampleSize(int sampleSize) {
    }

}
