package com.zlscorp.ultragrav.communication.transfer;

public class BeginIntervalReadingCommand extends AbstractCommand<CurrentReadingResponse> {
	private static final long serialVersionUID = 1L;
	
	public static final Byte COMMAND_ID = (byte)0x01;
	
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
	public Class<CurrentReadingResponse> getResponseClass() {
		return CurrentReadingResponse.class;
	}
	
	@Override
	public Byte getResponseId() {
		return CurrentReadingResponse.RESPONSE_ID;
	}
	
	@Override
	public Byte getIntervalEndCommandId() {
		return EndIntervalReadingCommand.COMMAND_ID;
	}
}
