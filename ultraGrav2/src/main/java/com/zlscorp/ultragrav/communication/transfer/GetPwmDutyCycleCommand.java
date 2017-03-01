package com.zlscorp.ultragrav.communication.transfer;

public class GetPwmDutyCycleCommand extends AbstractCommand<GetPwmDutyCycleResponse> {
	private static final long serialVersionUID = 1L;
	
	public static final Byte COMMAND_ID = (byte)0xFE;
	
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
	public Class<GetPwmDutyCycleResponse> getResponseClass() {
		return GetPwmDutyCycleResponse.class;
	}
	
	@Override
	public Byte getResponseId() {
		return GetPwmDutyCycleResponse.RESPONSE_ID;
	}
}
