package com.zlscorp.ultragrav.communication.transfer;

public class EndIntervalReadingCommand extends AbstractCommand<EmptyResponse> {
	private static final long serialVersionUID = 1L;
	
	public static final Byte COMMAND_ID = (byte)0xFF;
	
	@Override
	public byte[] toCommandBytes() {
		byte[] bytes = new byte[3];
		
		bytes[0] = 2;
		bytes[1] = COMMAND_ID;
		bytes[2] = computeChecksum(bytes);
		
		return bytes;
	}

	@Override
	public Byte getCommandId() {
		return COMMAND_ID;
	}

	@Override
	public Class<EmptyResponse> getResponseClass() {
		return EmptyResponse.class;
	}
	
	@Override
	public Byte getResponseId() {
		return EmptyResponse.RESPONSE_ID;
	}
}
