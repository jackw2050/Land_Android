package com.zlscorp.ultragrav.communication.transfer;

public class SystemStatusCommand extends AbstractCommand<SystemStatusResponse> {
	private static final long serialVersionUID = 1L;
	
	public static final Byte COMMAND_ID = (byte)0x00;
	
	@Override
	public byte[] toCommandBytes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Byte getCommandId() {
		return COMMAND_ID;
	}

	@Override
	public Class<SystemStatusResponse> getResponseClass() {
		return SystemStatusResponse.class;
	}
	
	@Override
	public Byte getResponseId() {
		return SystemStatusResponse.RESPONSE_ID;
	}
}
