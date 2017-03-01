package com.zlscorp.ultragrav.communication.transfer;

public class AlternateBreakCommand extends AbstractCommand<AlternateBreakResponse> {
	private static final long serialVersionUID = 1L;
	
	public static final Byte COMMAND_ID = (byte)0xF9;
	
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
	public Class<AlternateBreakResponse> getResponseClass() {
		return AlternateBreakResponse.class;
	}
	
	@Override
	public Byte getResponseId() {
		return AlternateBreakResponse.RESPONSE_ID;
	}
}
