 	package com.zlscorp.ultragrav.communication.transfer;

public class AlternateBreakResponse extends AbstractResponse {
	private static final long serialVersionUID = 1L;
	
	public static final Byte RESPONSE_ID = (byte)0x07;
	public static final int MIN_RESPONSE_BYTES = 3;

	@Override
	public Byte getResponseId() {
		return RESPONSE_ID;
	}
	
	@Override
	public int getMinimumResponseLength() {
		return MIN_RESPONSE_BYTES;
	}
	
	@Override
	public void fromResponseBytes(byte[] responseBytes) {
		
	}
}
