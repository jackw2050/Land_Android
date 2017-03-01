package com.zlscorp.ultragrav.meter.processor;

public class LevelParameters {
	private int frequency;
	private double zero; 
	private double coefficientA;
	private double coefficientB;
	private double coefficientC;
 
	public int getFrequency() {
        return frequency;
    }
    
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
    
    public double getZero() {
        return zero;
    }
    
    public void setZero(double zero) {
        this.zero = zero;
    }
    
    public double getCoefficientA() {
        return coefficientA;
    }
    
    public void setCoefficientA(double coefficientA) {
        this.coefficientA = coefficientA;
    }
    
    public double getCoefficientB() {
        return coefficientB;
    }
    
    public void setCoefficientB(double coefficientB) {
        this.coefficientB = coefficientB;
    }
    
    public double getCoefficientC() {
        return coefficientC;
    }
    
    public void setCoefficientC(double coefficientC) {
        this.coefficientC = coefficientC;
    }
}
