 	package com.zlscorp.ultragrav.communication.transfer;

public class SystemStatusResponse extends AbstractResponse {
	private static final long serialVersionUID = 1L;
	
	public static final Byte RESPONSE_ID = (byte)0x00;
	public static final int MIN_RESPONSE_LENGTH = 6;

	@Override
	public Byte getResponseId() {
		return RESPONSE_ID;
	}

	@Override
	public void fromResponseBytes(byte[] responseBytes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getMinimumResponseLength() {
		return MIN_RESPONSE_LENGTH;
	}
}
