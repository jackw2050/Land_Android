package com.zlscorp.ultragrav.meter.processor;

public class ThreeStageRcFilter {

	// Replace this with the filter in the Fortran code?

	private double f1;
	private double f2;
	private double f3;

	private int filterTimeConstant;

	public ThreeStageRcFilter(ProcessorConfig config) {
		f1 = f2 = f3 = 0.0;
		filterTimeConstant = config.getSystemParams().getFilterTimeConstant();
	}

	public void setFilterTimeConstant(int filterTimeConstant) {
		this.filterTimeConstant = filterTimeConstant;
	}

	public int getFilterTimeConstant() {
		return filterTimeConstant;
	}

	public double calculate(double signal) {

		f1 = f1 + (signal - f1) / (double)filterTimeConstant;
		f2 = f2 + (f1 - f2) / (double)filterTimeConstant;
		f3 = f3 + (f2 - f3) / (double)filterTimeConstant;

		return f3;
	}
}
