package com.zlscorp.ultragrav.meter.processor;

import com.zlscorp.ultragrav.communication.transfer.CurrentReadingResponse;
import com.zlscorp.ultragrav.debug.MyDebug;
import com.zlscorp.ultragrav.type.ObservationType;

import android.util.Log;

public class ReadingProcessor {
	
	private static final String TAG = "ReadingProcessor";

	private ProcessorConfig config;
	
	private AbstractBaseObservation observation;

	/**
	 * Constructor
	 */
	public ReadingProcessor(ProcessorConfig config) throws Exception {
	    this.config = config;

	    if (config.getObservationType() == ObservationType.SINGLE) {
	        observation = new SingleModeObservation(config);
	    } else if (config.getObservationType() == ObservationType.CONTINUOUS) {
	        observation = new ContinuousModeObservation(config);
	    } else if (config.getObservationType() == ObservationType.READ_METER) {
	        observation = new ReadMeterObservation(config);
	    }
	}
	
	public AbstractBaseObservation getObservation() {
        return observation;
    }

    /**
	 * Processes raw reading inputs from the meter, calculates a new feedback PWM duty cycle for the meter,
	 * accumulates the beam frequency information and calculates statistics on it. If the statistics are 
	 * acceptable, returns an observation data set that is ready to be delivered to the user.
	 */
	public void process(CurrentReadingResponse newDataSet) {
		
        if (MyDebug.LOG) {
            Log.d(TAG, "process = " + newDataSet);
        }

        observation.process(newDataSet);
	}
	
	public ProcessorState getState() {
		ProcessorState answer = new ProcessorState();
		
		answer.setStandardDeviation(observation.getStandardDeviation());
		answer.setElapsedTime(observation.getElapsedTimeMs());
		answer.setBeamFrequency(observation.getBeamFrequency());
		answer.setCrossLevelFreq(observation.getCrossLevelFrequency());
		answer.setLongLevelFreq(observation.getLongLevelFrequency());
		answer.setTemperatureFrequency(observation.getTemperatureFrequency());
		answer.setObservedGravity(observation.getObservedGravity());
		answer.setDutyCycleAs16Bits(observation.getDutyCycleAs16Bits());
		answer.setDutyCycleAsPercentage(observation.getDutyCycleAsPercentage());
		answer.setDialReading(observation.getDialReading());
		answer.setEarthtide(observation.getEarthtide());
		answer.setFeedbackCorrection(observation.getFeedbackCorrection());
        answer.setLevelCorrection(observation.getLevelCorrection());
        answer.setTempCorrection(observation.getTemperatureCorrection());
        answer.setIsLevelCorrectionGood(observation.isLevelCorrectionGood());
        answer.setIsTemperatureGood(observation.isTemperatureGood());
		answer.setGoodObservation(observation.isGoodObservation());
		answer.setDisplayGravityData(observation.isDisplayGravityData());
		answer.setDataOutputRate(observation.getDataOutputRate());
		answer.setFilterTimeConstant(observation.getFilterTimeConstant());
		answer.setReadingTime(observation.getReadingTime());
		answer.setBeamError(observation.getBeamError());
		answer.setProcessorConfig(config);
		answer.setDisplayStatisticsData(observation.isDisplayStatisticsData());
		
		return answer;
	}
}
