package com.zlscorp.ultragrav.meter.processor;

import android.location.Location;
//import android.text.format.Time;
import android.util.Log;

import com.zlscorp.ultragrav.communication.transfer.CurrentReadingResponse;
//import com.zlscorp.ultragrav.model.CalibratedDialValue;
import com.zlscorp.ultragrav.debug.MyDebug;

public class SingleModeObservation extends AbstractBaseObservation {

    private static final String TAG = "SingleModeObservation";

    private Statistics statistics;
    private double offset;
    private int boostFl;
    private double bGain;
    private boolean isDisplayStatisticsData;
    private boolean doResetStatistics;
    private boolean doResetStatisticsAgain;
    private boolean useEarthtide;
//	private double overBoost;
    
    // TODO - Test
//    private int dCTest;
//    private boolean isIncDcTest;
    
    public SingleModeObservation(ProcessorConfig config) throws Exception {

        meter = new Meter(config);
        statistics = new Statistics(meter, config);
        readingLocation = new Location("ultragrav");
        readingLocation.setLatitude(config.getStation().getLatitude());
        readingLocation.setLongitude(config.getStation().getLongitude());
        readingLocation.setAltitude(config.getStation().getElevation());
        earthtide = new Earthtide();
        useEarthtide = config.getStation().useEarthTide();

        elapsedTime = 0;
        isGoodObservation = false;
        doResetStatisticsAgain = false;
        boostFl = 2;
//		overBoost = 1.;
        bGain = meter.gain * config.getSystemParams().getFeedbackGain();
//        aGain = bGain * meter.boostFactor * overBoost;
        aGain = bGain * meter.boostFactor;
        sum = 0;
        dialReading = config.getSystemParams().getDialReading();
        calibratedDialValues = config.getCalibratedDialValues();

        // TODO Test for DC Range Alert
//	    dCTest = 15;      // In %
//	    isIncDcTest = true;
	}

    public void process(CurrentReadingResponse newDataSet) {

        isDisplayGravityData = false;
        isDisplayStatisticsData = false;

        // Take new raw frequency data from receive buffer, do
        // initial processing and put it into it's respective members
        meter.processMeterDataSet(newDataSet);   
        elapsedTime++;
		
        // elapsedTime won't overflow for 292 billion years - seriously
//		if (elapsedTime >= 65000) {     
//			// TODO - report error?
//		}

		if (doResetStatistics) {
			statistics.resetStatistics();
			doResetStatistics = false;
		}

		readingTime = System.currentTimeMillis();

		calculatePwmDutyCycle(meter);

		meter.levels.calculateCorrection();
		meter.temperature.calculateCorrection();
		
		// Accumulate and calculate the statistics
		statistics.process(this);

		if (statistics.isNewSetReady()) {

            if (MyDebug.LOG) {
                Log.d(TAG, "Calc:  Elapsed time = " + elapsedTime + ", CycleCounter = " +
                      statistics.getCycleCounter());
            }
            isDisplayStatisticsData = true;

			observedGravity = getCalibratedDialValue(dialReading) + statistics.getFeedbackCorrection() 
			        + meter.temperature.getCorrection() + meter.levels.getCorrection();

            calculatedEarthtide = earthtide.calculate(readingTime, readingLocation);
			if (useEarthtide) {
			    observedGravity += calculatedEarthtide;
			}

            if (elapsedTime < 182) {
                if (statistics.isGoodPrecision()) {
                    isGoodObservation = true;
                    isDisplayGravityData = true;
                } else {

                  // process collects X meter data sets, then calculates their
                  // standard deviation and the beam error.
                  // if these are both < precision, we have a good observation.
                  // If not, we reset the statistics registers and take another X meter data sets.
                  doResetStatistics = true;
                }
            } else {

              // after the reading has been going for more than 3 minutes,
              // we don't bother checking precision (for some reason)
              // we just let the user keep pressing the More button to his or her heart's content.
//              if (doResetStatisticsAgain) {
//                  doResetStatistics = true;
//                  doResetStatisticsAgain = false;
//              } else {
                  isDisplayGravityData = true;
//              }
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
	            
		offset = ((double) meter.beamFrequency) - meter.readingLine;
		if (boostFl > 0) {
			aGain = bGain * meter.boostFactor;             // removed "*overBoost" 
			if (Math.abs(offset) > ((meter.maxStop - meter.minStop) / 2 - 100.)) {
				aGain = bGain * meter.boostFactor;
				boostFl = 1;
			}
			if (Math.abs(offset) < meter.stopBoost) {
				aGain = bGain;
				boostFl++;
				if (boostFl > 3)
					boostFl = 0;            // three strikes and out (Herb's original comment)
			}
		}
		deltaFrequency = offset * aGain;
		sum = sum + deltaFrequency / 4.;
		dutyCycle = (int) (32767. + sum + deltaFrequency);
		
        // Test for DC Range Alerts
		// Make DC go from 85% - 95, then back to 85.
		// and from 15% to 5, then back to 15.
//		if (isIncDcTest) {
//            if (dCTest > 5) {
//                dCTest--;
//            } else {
//                isIncDcTest = false;
//            }
//		} else {
//		    if (dCTest < 15) {
//	            dCTest++;
//		    }
//		}
//        // Turn 85 - 95% into  
		// dCTest = 0 >> DC = 65535, dCTest = 100 >> DC = 0
//		dutyCycle = (int) ((100 - dCTest) * 655.34);  
		
		if (dutyCycle > 0xFFFE) {                // keep the new pwm duty cycle in range
		    dutyCycle = 0xFFFE;
		}
		if (dutyCycle < 1) {
		    dutyCycle = 1;
		}
	}
    
    public double getOffset() {
        return offset;
    }

    @Override
    public void requestReset() {
        doResetStatisticsAgain = true;
        doResetStatistics = true;
    }

    @Override
    public double getStandardDeviation() {
        return statistics.getStandardDeviation();
    }

    @Override
    public boolean isGoodObservation() {
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
        return statistics.getFeedbackCorrection();
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
    public long getElapsedTimeMs() {
        return elapsedTime;
    }

    @Override
    public double getBeamError() {
        return statistics.getBeamError();
    }

    @Override
    public boolean isDisplayStatisticsData() {
        return isDisplayStatisticsData;
    }

    @Override
    public boolean isDisplayGravityData() {
        return isDisplayGravityData;
    }

    @Override
    public long getReadingTime() {
        return readingTime;
    }

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
        return 0;
    }

    @Override
    public int getFilterTimeConstant() {
        return 0;
    }

    @Override
    public void setSampleSize(int sampleSize) {
        statistics.setSampleSize(sampleSize);
    }
}
