package com.zlscorp.ultragrav.communication;


public interface ICommAdapter {
	
	public void activate(String address);
	
	public void deactivate();
	
	public boolean isReConnectingToDevice(String address);
	
    public ConnectionState getConnectionState();
    
	public void sendBytes(byte[] command);
	
	public interface CommAdapterListener {
		
		public void onConnectionState(ConnectionState connectionState);
		public void onReceiveBytes(byte[] response);
	}
}
