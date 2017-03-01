package com.zlscorp.ultragrav.meter.processor;

import java.io.Serializable;
import java.util.List;

import com.zlscorp.ultragrav.model.CalibratedDialValue;
import com.zlscorp.ultragrav.model.LevelCorrectionParams;
import com.zlscorp.ultragrav.model.MeterParams;
import com.zlscorp.ultragrav.model.Station;
import com.zlscorp.ultragrav.model.SystemParams;
import com.zlscorp.ultragrav.type.ObservationType;

/**
 * Contains all the system parameters needed to process meter readings
 */
public class ProcessorConfig implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private ObservationType observationType;
	private String observationNote;
	private Station station;
	private SystemParams systemParams;
	private MeterParams meterParams;
	private LevelCorrectionParams levelCorrectionParams;
	private List<CalibratedDialValue> calibratedDialValues;
	
	public ObservationType getObservationType() {
		return observationType;
	}
	public void setObservationType(ObservationType observationType) {
		this.observationType = observationType;
	}
	public String getObservationNote() {
		return observationNote;
	}
	public void setObservationNote(String observationNote) {
		this.observationNote = observationNote;
	}
	public Station getStation() {
		return station;
	}
	public void setStation(Station station) {
		this.station = station;
	}
	public SystemParams getSystemParams() {
		return systemParams;
	}
	public void setSystemParams(SystemParams systemParams) {
		this.systemParams = systemParams;
	}
	public MeterParams getMeterParams() {
		return meterParams;
	}
	public void setMeterParams(MeterParams meterParams) {
		this.meterParams = meterParams;
	}
	public LevelCorrectionParams getLevelCorrectionParams() {
		return levelCorrectionParams;
	}
	public void setLevelCorrectionParams(LevelCorrectionParams levelCorrectionParams) {
		this.levelCorrectionParams = levelCorrectionParams;
	}
	public List<CalibratedDialValue> getCalibratedDialValues() {
		return calibratedDialValues;
	}
	public void setCalibratedDialValues(List<CalibratedDialValue> calibratedDialValues) {
		this.calibratedDialValues = calibratedDialValues;
	}
	
	@Override
	public String toString() {
		return "ProcessorConfig [observationType=" + observationType + ", observationNote=" + observationNote + ", station=" + station + ", systemParams=" + systemParams
				+ ", meterParams=" + meterParams + ", levelCorrectionParams=" + levelCorrectionParams + ", calibratedDialValues=" + calibratedDialValues.size() + "]";
	}
}
