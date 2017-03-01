package com.zlscorp.ultragrav.meter.processor;

import com.zlscorp.ultragrav.model.LevelCorrectionParams;

public class MeterLevel {
    private final double MAX_ERROR = .05;
    public LevelParameters crossLevel;
    public LevelParameters longLevel;
    private double correction;
    private boolean isWithinLimit;

    public MeterLevel(LevelCorrectionParams params) {
        crossLevel = new LevelParameters();
        crossLevel.setZero(params.getCrossLevel0());
        crossLevel.setCoefficientA(params.getCrossLevelA());
        crossLevel.setCoefficientB(params.getCrossLevelB());
        crossLevel.setCoefficientC(params.getCrossLevelC());
        
        longLevel = new LevelParameters();
        longLevel.setZero(params.getLongLevel0());
        longLevel.setCoefficientA(params.getLongLevelA());
        longLevel.setCoefficientB(params.getLongLevelB());
        longLevel.setCoefficientC(params.getLongLevelC());
        
        isWithinLimit = true;
    }

    public void calculateCorrection() {
        double offset, corr;

        offset = (double)(crossLevel.getFrequency()) - crossLevel.getZero();
        corr = crossLevel.getCoefficientA() + offset * crossLevel.getCoefficientB() + offset * offset * crossLevel.getCoefficientC();
        offset = (double)(longLevel.getFrequency()) - longLevel.getZero();
        corr += longLevel.getCoefficientA() + offset * longLevel.getCoefficientB() + offset * offset * longLevel.getCoefficientC();
        correction = -corr; // negative for correction

        isWithinLimit = true;
        if (Math.abs(correction) > MAX_ERROR) {
            isWithinLimit = false;
        }
    }

    public double getCorrection() {
        return correction;
    }

    public boolean isWithinLimit() {
        return isWithinLimit;
    }
}