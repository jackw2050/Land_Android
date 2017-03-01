package com.zlscorp.ultragrav.communication.transfer;

public class CurrentReadingResponse extends AbstractResponse {
	private static final long serialVersionUID = 1L;

	public static final Byte RESPONSE_ID = (byte) 0x03;
	public static final int MIN_RESPONSE_LENGTH = 7 + 3;

	private int beamFreq;
	private int longLevelFreq;
	private int crossLevelFreq;
	private int temperatureFreq;

	@Override
	public Byte getResponseId() {
		return RESPONSE_ID;
	}

	@Override
	public int getMinimumResponseLength() {
		return MIN_RESPONSE_LENGTH;
	}

	@Override
	public void fromResponseBytes(byte[] responseBytes) {

		beamFreq = asInt(responseBytes[2], responseBytes[3]);
		longLevelFreq = asInt(responseBytes[4], responseBytes[5]);
		crossLevelFreq = asInt(responseBytes[6], responseBytes[7]);
		temperatureFreq = asInt(responseBytes[8]);
	}

	public int getBeamFreq() {
		return beamFreq;
	}

	public void setBeamFreq(int beamFreq) {
		this.beamFreq = beamFreq;
	}

	public int getLongLevelFreq() {
		return longLevelFreq;
	}

	public void setLongLevelFreq(int longLevelFreq) {
		this.longLevelFreq = longLevelFreq;
	}

	public int getCrossLevelFreq() {
		return crossLevelFreq;
	}

	public void setCrossLevelFreq(int crossLevelFreq) {
		this.crossLevelFreq = crossLevelFreq;
	}

	public int getTemperatureFreq() {
		return temperatureFreq;
	}

	public void setTemperatureFreq(int temperatureFreq) {
		this.temperatureFreq = temperatureFreq;
	}

	@Override
	public String toString() {
		return "CurrentReadingResponse [beamFreq=" + beamFreq + ", longLevelFreq=" + longLevelFreq
				+ ", crossLevelFreq=" + crossLevelFreq + ", temperatureFreq=" + temperatureFreq + "]";
	}
}
