package com.zlscorp.ultragrav.meter.processor;

public class Statistics {
    private Meter meter;
    private int sampleSize;
    private int sampleSizeBuffer;
	private int cycleCounter;
	private double beam, beamSum, beamSumSquared;
	private double feedbackOut;
	private double feedbackSum, feedbackSumSquared;
	private double beamError;
	private double feedbackCorrection;
	private double beamStandardDeviation, feedbackStandardDeviation, standardDeviation;
	private double precision;
	private boolean isNewSetReady;

	public Statistics(Meter meter, ProcessorConfig config) {
	    this.meter = meter;
        sampleSize = 5;
        sampleSizeBuffer = 5;
		resetStatistics();
		precision = (config.getSystemParams()).getObservationPrecision();
	}
   
    public void resetStatistics() {
        beamSum = 0.;
        feedbackSum = 0.;
        beamSumSquared = 0.;
        feedbackSumSquared = 0.;
        cycleCounter = 0;
        isNewSetReady = false;
        sampleSize = sampleSizeBuffer;
    }
    
    /**
     * cycleCounter is incremented from 1 to sampleSize + 1. From 1 to sampleSize,
     * the accumulate method accumulates the samples of the meter observation data
     * sets. For cycleCounter == 5, the calculate method calculates the statistics.
     * After that and until the statistics registers are reset, cycleCounter ==
     * sampleSize + 1 and no observation samples are accumulated or statistics calculated.
     * 
     * @param control
     */
    public void process(SingleModeObservation control) {
        if (cycleCounter < sampleSize + 1) {
            cycleCounter++;
        }

        if (cycleCounter <= sampleSize) {
//            Log.d(TAG, "Acc: Elapsed time = " + elapsedTime + ", CycleCounter = " + statistics.getCycleCounter());         // DEBUG
            accumulate(control);
        }
        
        if (cycleCounter == sampleSize) {
            calculate();
        }
    }
    
    public boolean isNewSetReady() {
        if (isNewSetReady) {
            isNewSetReady = false;
            return true;
        } else {
            return false;
        }
    }
    
	public void accumulate(SingleModeObservation control) {
		beam = control.getOffset() * meter.beamScale;
		feedbackOut = (control.getDutyCycleAs16Bits() - 32767) * meter.feedbackScale;
		beamSum = beamSum + beam;
		beamSumSquared = beamSumSquared + beam * beam;
		feedbackSum = feedbackSum + feedbackOut;
		feedbackSumSquared = feedbackSumSquared + feedbackOut * feedbackOut;
	}

	public void calculate() {
		beamError = beamSum / (double)sampleSize;
		beamStandardDeviation = beamSumSquared - (beamSum * beamSum) / (double)sampleSize;
		if (beamStandardDeviation < 0.) {
			beamStandardDeviation = 4.;          // avoid neg sq root. why 4? ask Herb.
		}
		beamStandardDeviation = Math.sqrt(beamStandardDeviation / ((double)(sampleSize - 1)));
		feedbackCorrection = -feedbackSum / (double)sampleSize;
		feedbackStandardDeviation = feedbackSumSquared - (feedbackSum * feedbackSum) / (double)sampleSize;
		if (feedbackStandardDeviation < 0.) {
			feedbackStandardDeviation = 4.;
		}
		feedbackStandardDeviation = Math.sqrt(feedbackStandardDeviation / ((double)(sampleSize - 1)));
		standardDeviation = Math.sqrt(feedbackStandardDeviation * feedbackStandardDeviation + 
		                              beamStandardDeviation * beamStandardDeviation);
		isNewSetReady = true;
	}
	
    public boolean isGoodPrecision() {
        return (Math.abs(beamError) < precision && standardDeviation < precision);
    }

    public double getPrecision() {
        return precision;
    }
    
    public double getStandardDeviation() {
        return standardDeviation;
    }
    
    public void setBeamError(double beamError) {
        this.beamError = beamError;
    }

    public double getBeamError() {
        return beamError;
    }
    
    public double getFeedbackCorrection() {
        return feedbackCorrection;
    }
    
    public int getCycleCounter() {
        return cycleCounter;
    }
    
    public double getBSum() {
        return beamSum;
    }
    
    public double getFSum() {
        return feedbackSum;
    }
    
    public double getBSumSq() {
        return beamSumSquared;
    }
    
    public double getFSumSq() {
        return feedbackSumSquared;
    }

    public int getSampleSize() {
        return sampleSize;
    }

    public void setSampleSize(int sampleSize) {
        this.sampleSizeBuffer = sampleSize;
    }
}
