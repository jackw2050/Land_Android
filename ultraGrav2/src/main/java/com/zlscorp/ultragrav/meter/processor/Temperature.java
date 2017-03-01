package com.zlscorp.ultragrav.meter.processor;

import com.zlscorp.ultragrav.model.MeterParams;

public class Temperature {

	private static final int MINIMUM_TEMPERATURE = 15;
	private static final int MAXIMUM_TEMPERATURE = 240;
	private static final double FILTER_CONSTANT1 = 0.2;
	private static final double FILTER_CONSTANT2 = 0.5;
	private double filter;
	private int frequency;
	private double correction;
	private boolean isWithinLimits;
	private int zero;
	private double coefficientA;
	private double coefficientB;
	private double coefficientC;

	public Temperature(MeterParams params) {
		filter = 0.;
		zero = params.getTemperature0();
		coefficientA = params.getTemperatureA();
        coefficientB = params.getTemperatureB();
        coefficientC = params.getTemperatureC();
        isWithinLimits = true;
	}

	/**
	 * Takes the int temperature frequency value from the meter, filters it and returns the filtered value.
	 */
	public void filter(int value) {
		filter = filter + (((double) value) - filter) * FILTER_CONSTANT1;
		frequency = (int) (filter + FILTER_CONSTANT2);
	}

	public void calculateCorrection() {
		double offset;

		offset = (double)(frequency - zero);
		correction = coefficientA + offset * coefficientB + offset * offset * coefficientC;

        isWithinLimits = true;
		if (frequency < MINIMUM_TEMPERATURE || frequency > MAXIMUM_TEMPERATURE) {
			isWithinLimits = false;
		}
	}

    public int getFrequency() {
        return frequency;
    }

    public double getCorrection() {
        return correction;
    }

    public boolean isWithinLimits() {
        return isWithinLimits;
    }

    public double getZero() {
        return zero;
    }

    public double getCoefficientA() {
        return coefficientA;
    }

    public double getCoefficientB() {
        return coefficientB;
    }

    public double getCoefficientC() {
        return coefficientC;
    }
}
