package com.zlscorp.ultragrav.communication;

import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.debug.MyDebug;
import com.zlscorp.ultragrav.file.ErrorHandler;
import com.zlscorp.ultragrav.meter.MeterService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Level;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

public class BluetoothCommAdapter implements ICommAdapter {

	private static final String TAG = "BluetoothCommAdapter";
	
	//If you are connecting to a Bluetooth serial board then try using the well-known SPP UUID 00001101-0000-1000-8000-00805F9B34FB.
	private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	private CommAdapterListener listener;
	
	private BluetoothAdapter bluetoothAdapter;
	private ConnectThread connectThread;
	private ConnectedThread connectedThread;
	private ConnectionState connectionState;
    private MyBluetoothBroadcastReceiver bluetoothReceiver;
    private MyBluetoothBroadcastReceiver resetReceiver;
    private MyBluetoothBroadcastReceiver enableReceiver;
    private Context meterService;
    private String currentDeviceAddress;
    private boolean isActivating;
    private boolean isReConnecting;
    private boolean isResetting;
    private boolean isEnabling;
    private Boolean waitingForBTState;
    private Handler backgroundHandler;

    // a general handler for running code on the ui thread
//    private Handler uiHandler;


	public BluetoothCommAdapter(CommAdapterListener listener, MeterService meterService) {
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		connectionState = ConnectionState.NONE;
		this.listener = listener;
        this.meterService = meterService;
        isReConnecting = false;
        isResetting = false;

        HandlerThread backgroundThread = new HandlerThread("meter-service");
        backgroundThread.start();
        this.backgroundHandler = new Handler(backgroundThread.getLooper());

//        this.uiHandler = new Handler(Looper.getMainLooper());
	}

	@Override
	public synchronized void activate(String address) {
        if (MyDebug.LOG) {
            Log.d(TAG, "bluetooth comm adapter activate");
        }
		
		currentDeviceAddress = address;

		// Cancel any thread attempting to make a connection
		if (connectThread != null) {
			connectThread.cancel();
			connectThread = null;
		}

		// Cancel any thread currently running a connection
		if (connectedThread != null) {
			connectedThread.cancel();
			connectedThread = null;
		}

		setState(ConnectionState.LISTENING);

		BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
		
        // Make sure the BT adapter is enabled and attempt to enable it if it is not.
		if ((device != null) && isAdapterEnabled()) {
            connect(device, false);
		} else {
			setState(ConnectionState.NONE);
		}
	}
	
	/**
	 * This method can have one of four possible results:
     *   1 - Succeed immediately
     *   2 - Fail immediately
     *   3 - Succeed after some time
     *   4 - Fail after some time
     *   
	 * @Returns False = Not enabled, error while attempting to enable it (Fail immediately) or
     *                  Not enabled/timeout (Fail after some time);
     *          True =  Enabled (Succeed immediately) or
	 *                  Enabled in this method (Succeed after some time);
	 */
	private boolean isAdapterEnabled() {
	    boolean result;

	    if (bluetoothAdapter.isEnabled()) {
	        result = true;
	    } else {
	        if (MyDebug.LOG) {
	            Log.d(TAG, "Attempting to enable BT adapter.");
	        }
	        isEnabling = true;
            waitingForBTState = true;

	        // Register for broadcasts of BT adapter state changes
	        enableReceiver = new MyBluetoothBroadcastReceiver();
	        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
	        meterService.registerReceiver(enableReceiver, filter, null, backgroundHandler);
	        
	        if (bluetoothAdapter.enable()) {
	            // Create timeout for broadcast receiver
                result = true;
                long currentTime;
                long startTime;
                startTime = currentTime = System.currentTimeMillis();
                
                while (waitingForBTState) {
                    currentTime = System.currentTimeMillis();
                    if (currentTime - startTime > 2000) {
                        // We didn't hear from the broadcast receiver in time
                        synchronized (waitingForBTState) {
                            waitingForBTState = false;
                            try {
                                meterService.unregisterReceiver(enableReceiver);
                            } catch (Exception e) {
                                if (MyDebug.LOG) {
                                    Log.e(TAG, enableReceiver + " already unregistered." + e);
                                }
                                ErrorHandler errorHandler = ErrorHandler.getInstance();
                                errorHandler.logError(Level.INFO, "BluetoothCommAdapter." +
                                        "isAdapterEnabled(): enableReceiver is already unregistered - " + e);
                            }
                        }
                        result = false;
                    }
                }
                if (MyDebug.LOG) {
                    Log.d(TAG, "BT enable timer = " + (currentTime - startTime));
                }
	        } else {
                result = false;
	            // unregister the receiver here, since the enable failed
                if (MyDebug.LOG) {
                    Log.d(TAG, "Can't enable BT adapter.");
                }
	            try {
	                meterService.unregisterReceiver(enableReceiver);
	            } catch (Exception e) {
	                if (MyDebug.LOG) {
	                    Log.e(TAG, enableReceiver + " already unregistered. " + e);
	                }
                    ErrorHandler errorHandler = ErrorHandler.getInstance();
                    errorHandler.logError(Level.INFO, "BluetoothCommAdapter." +
                            "isAdapterEnabled(): enableReceiver is already unregistered - " + e);
	            }
	        }
            isEnabling = false;
        }
	    return result;
	}
	
//	private BluetoothDevice getDeviceByAddress(String address) {
////		Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
//		
//		BluetoothDevice answer = bluetoothAdapter.getRemoteDevice(address);
////		// If there are paired devices, add each one to the ArrayAdapter
////		if (pairedDevices.size() > 0) {
////			for (BluetoothDevice device : pairedDevices) {
////				if (device.getAddress().equals(address)) {
////					answer = device;
////					break;
////				}
////			}
////			if (answer == null) {
////				Log.e(TAG, "host has no paired devices by address " + address);
////			}
////		} else {
////			Log.e(TAG, "host has no paired devices.");
////			answer = null;
////		}
//		
//		return answer;
//	}

	@Override
	public synchronized void deactivate() {
        if (MyDebug.LOG) {
            Log.d(TAG, "bluetooth comm adapter deactivate");
        }

		if (connectThread != null) {
			connectThread.cancel();
			connectThread = null;
		}

		if (connectedThread != null) {
			connectedThread.cancel();
			connectedThread = null;
		}
		
		setState(ConnectionState.NONE);
		
		this.listener = null;
	}

	@Override
	public void sendBytes(byte[] command) {
		// Create temporary object
		ConnectedThread r;
		// Synchronize a copy of the ConnectedThread
		synchronized (this) {
			if (connectionState != ConnectionState.ESTABLISHED) {
		        if (MyDebug.LOG) {
		            Log.e(TAG, "cannot write to a connection that is not established");
		        }
				return;
			}
			r = connectedThread;
		}
		// Perform the write unsynchronized
		
		//String msg = "ULTRAGRAV_TEST";
		//r.write(msg.getBytes());
		
		r.write(command);
		
	}
	
	@Override
	public ConnectionState getConnectionState() {
		return connectionState;
	}

	/**
	 * Set the current state of the chat connection
	 * 
	 * @param state
	 *            An integer defining the current connection state
	 */
	private synchronized void setState(ConnectionState newState) {
		ConnectionState oldState = connectionState;
		connectionState = newState;
		
        if (MyDebug.LOG) {
            Log.d(TAG, "setState() " + oldState + " -> " + newState);
        }
		
		if (newState != oldState && listener != null) {
			listener.onConnectionState(newState);
		}
	}

	/**
	 * Start the ConnectThread to initiate a connection to a remote device.
	 * 
	 * @param device
	 *            The BluetoothDevice to connect
	 * @param secure
	 *            Socket Security type - Secure (true) , Insecure (false)
	 */
	public synchronized void connect(BluetoothDevice device, boolean secure) {
        if (MyDebug.LOG) {
            Log.d(TAG, "connecting to: " + device);
        }

		// Cancel any thread attempting to make a connection
		if (connectionState == ConnectionState.CONNECTING) {
			if (connectThread != null) {
				connectThread.cancel();
				connectThread = null;
			}
		}

		// Cancel any thread currently running a connection
		if (connectedThread != null) {
			connectedThread.cancel();
			connectedThread = null;
		}

		// Start the thread to connect with the given device
		connectThread = new ConnectThread(device, secure);
		connectThread.start();
		setState(ConnectionState.CONNECTING);
	}

	/**
	 * Start the ConnectedThread to begin managing a Bluetooth connection
	 * 
	 * @param socket
	 *            The BluetoothSocket on which the connection was made
	 * @param device
	 *            The BluetoothDevice that has been connected
	 */
	public synchronized void connected(BluetoothSocket socket, BluetoothDevice device, final String socketType) {
        if (MyDebug.LOG) {
            Log.d(TAG, "connected, Socket Type:" + socketType);
        }

		// Cancel the thread that completed the connection
		if (connectThread != null) {
			connectThread.cancel();
			connectThread = null;
		}

		// Cancel any thread currently running a connection
		if (connectedThread != null) {
			connectedThread.cancel();
			connectedThread = null;
		}

		// Start the thread to manage the connection and perform transmissions
		connectedThread = new ConnectedThread(socket, socketType);
		connectedThread.start();

		// Send the name of the connected device back to the UI Activity
        if (MyDebug.LOG) {
            Log.d(TAG, "connection established");
        }

		setState(ConnectionState.ESTABLISHED);
	}

	/**
	 * Indicate that the connection attempt failed and notify the UI Activity.
	 */
	private void connectionFailed() {
		// Send a failure message back to the Activity
        if (MyDebug.LOG) {
            Log.w(TAG, "connection failed");
        }
		setState(ConnectionState.FAILED);
	}

	/**
	 * Indicate that the connection was lost and notify the UI Activity.
	 */
	private void connectionLost() {
		// Send a failure message back to the Activity
        if (MyDebug.LOG) {
            Log.w(TAG, "connection lost");
        }
		setState(ConnectionState.NONE);
		
		// Attempt to reconnect with the same BT device
		reConnect();
	}

    /**
     * Auto-reconnect
     * If the BT connection is lost, try to reconnect
     * This procedure is cancelled in the following cases:
     *    1 - A connection is reestablished
     *    2 - The user initiates a connection with a different BT device
     *    3 - A USB connection is established
     */
	private void reConnect() {

        bluetoothReceiver = new MyBluetoothBroadcastReceiver();

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        meterService.registerReceiver(bluetoothReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        meterService.registerReceiver(bluetoothReceiver, filter);
        
        connectedThread = null;
        isActivating = false;
        isReConnecting = true;
        
        doDiscovery();
	}
	
	private synchronized boolean resetBluetoothAdapter() {
	    boolean result = true;

	    isResetting = true;
        waitingForBTState = true;

        // Register for broadcasts of BT adapter state changes
        resetReceiver = new MyBluetoothBroadcastReceiver();
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        meterService.registerReceiver(resetReceiver, filter);
        
	    if (bluetoothAdapter.disable()) {
            long currentTime;
            long startTime;
            startTime = currentTime = System.currentTimeMillis();
            
            while (waitingForBTState) {
                currentTime = System.currentTimeMillis();
                if (currentTime - startTime > 2000) {
                    // We didn't hear from the broadcast receiver in time
                    waitingForBTState = false;
                    try {
                        meterService.unregisterReceiver(resetReceiver);
                    } catch (Exception e) {
                        if (MyDebug.LOG) {
                            Log.e(TAG, resetReceiver + " already unregistered." + e);
                        }
                        ErrorHandler errorHandler = ErrorHandler.getInstance();
                        errorHandler.logError(Level.INFO, "BluetoothCommAdapter." +
                                "resetBluetoothAdapter(): resetReceiver is already unregistered - " + e);
                    }
                    result = false;
                }
            }
            if (MyDebug.LOG) {
                Log.d(TAG, "BT reset 1 timer = " + (currentTime - startTime));
            }
	        // Now that the adapter is disabled, enable it
	        if (bluetoothAdapter.enable()) {
	            waitingForBTState = true;
	            startTime = currentTime = System.currentTimeMillis();
	            
	            while (waitingForBTState) {
	                currentTime = System.currentTimeMillis();
	                if (currentTime - startTime > 2000) {
	                    // We didn't hear from the broadcast receiver in time
	                    waitingForBTState = false;
	                    try {
	                        meterService.unregisterReceiver(resetReceiver);
	                    } catch (Exception e) {
	                        if (MyDebug.LOG) {
	                            Log.e(TAG, resetReceiver + " already unregistered." + e);
	                        }
	                        ErrorHandler errorHandler = ErrorHandler.getInstance();
	                        errorHandler.logError(Level.INFO, "BluetoothCommAdapter." +
	                                "resetBluetoothAdapter(): resetReceiver is already unregistered - " + e);
	                    }
	                    result = false;
	                }
	            }
	            if (MyDebug.LOG) {
	                Log.d(TAG, "BT reset 2 timer = " + (currentTime - startTime));
	            }
	        } else {
	            result = false;
	        }
	    } else {
            result = false;
	    }

        isResetting = false;
	    return result;
    }
	
    @Override
    public boolean isReConnectingToDevice(String address) {
        return isReConnecting && address.equals(currentDeviceAddress);
    }

	private void doDiscovery() {
        // Request discover from BluetoothAdapter
        if (MyDebug.LOG) {
            Log.i(TAG, "Discovery Started");
        }
        bluetoothAdapter.startDiscovery();
	}

    // The BroadcastReceiver that listens for discovered devices and
    // tries to connect if the most recent BT device is found
    private class MyBluetoothBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's the device we were most recently connected to, reconnect with it
                if (device.getAddress().equals(currentDeviceAddress)) {
                    if (MyDebug.LOG) {
                        Log.i(TAG, "Found previous BT device; trying to reactivate");
                    }
                    isActivating = true;
                    activate(currentDeviceAddress);
                }
            // When discovery is finished, if we have not yet reconnected, continue trying
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (MyDebug.LOG) {
                    Log.i(TAG, "Discovery Finished.");
                }
                if (isActivating) {
                    // Unregister broadcast listener
                    meterService.unregisterReceiver(bluetoothReceiver);
                    isReConnecting = false;
                } else if (isReConnecting) {
                    doDiscovery();
                }
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                synchronized (waitingForBTState) {
                    if (MyDebug.LOG) {
                        Log.i(TAG, "Bluetooth Adapter State has changed. isEnabling: " + isEnabling + ", " +
                    		"isResetting: " + isResetting + ", BT State: " + bluetoothAdapter.getState());
                    }
                    if (isEnabling) {
                        if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                            if (MyDebug.LOG) {
                                Log.i(TAG, "BroadcastReceiver - Enabling BT Adapter - State is ON.");
                            }
                            waitingForBTState = false;
                            // Unregister broadcast listener
                            meterService.unregisterReceiver(enableReceiver);
                        }
                    } else if (isResetting) {
                        if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                            if (MyDebug.LOG) {
                                Log.i(TAG, "BroadcastReceiver - Resetting BT Adapter - State is OFF.");
                            }
                            waitingForBTState = false;
                        } else if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                            if (MyDebug.LOG) {
                                Log.i(TAG, "BroadcastReceiver - Resetting BT Adapter - State is ON.");
                            }
                            waitingForBTState = false;
                            // Unregister broadcast listener
                            meterService.unregisterReceiver(resetReceiver);
                        }
                    }
                }
            }
        }
    };

    /**
	 * This thread runs while attempting to make an outgoing connection with a device. 
	 * It runs straight through; the connection either succeeds or fails.
	 */
	private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;
		private String mSocketType;
		private boolean cancelled = false;

		public ConnectThread(BluetoothDevice device, boolean secure) {
			mmDevice = device;
			BluetoothSocket tmp = null;
			mSocketType = secure ? "Secure" : "Insecure";

			// Get a BluetoothSocket for a connection with the
			// given BluetoothDevice
			if (secure) {
			    try {
			        tmp = device.createRfcommSocketToServiceRecord(SPP_UUID);
			        // Test
//                  throw new Exception("test");
			    } catch (Exception ex) {
			        if (MyDebug.LOG) {
			            Log.w(TAG, "trying alt create");
			        }
			        try {
			            Method m = device.getClass().getMethod("createRfcommSocket", 
			                    new Class[] { int.class });
			            tmp = (BluetoothSocket) m.invoke(device, 1);
			            // Test
//                      throw new Exception("test");
			        } catch (Exception e) {
			            ErrorHandler errorHandler = ErrorHandler.getInstance();
			            errorHandler.logError(Level.WARNING, "BluetoothCommAdapter$ConnectThread." +
			                    "ConnectThread(): Bluetooth Adapter Error(secure) - " + e,
			                    R.string.bluetooth_adapter_error_title,
			                    R.string.bluetooth_adapter_error_message);
			        }
			    }
			} else {
			    try {
			        Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", 
			                new Class[] { UUID.class });
			        tmp = (BluetoothSocket) m.invoke(device, SPP_UUID);
			        // Test
//                 throw new Exception("test");
			    } catch (Exception ex) {
			        if (MyDebug.LOG) {
			            Log.w(TAG, "trying alternate socket create");
			        }
			        try {
			            Method m = device.getClass().getMethod("createRfcommSocket", 
			                    new Class[] { int.class });
			            tmp = (BluetoothSocket) m.invoke(device, 1);
			            // Test
//                      throw new Exception("test");
			        } catch (Exception e) {
			            ErrorHandler errorHandler = ErrorHandler.getInstance();
			            errorHandler.logError(Level.WARNING, "BluetoothCommAdapter$ConnectThread." +
			                    "ConnectThread(): Bluetooth Adapter Error (unsecure) - " + e,
			                    R.string.bluetooth_adapter_error_title, 
			                    R.string.bluetooth_adapter_error_message);
			        }
			    }
			}
			mmSocket = tmp;
		}

		public void run() {
	        if (MyDebug.LOG) {
	            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
	        }
			setName("ConnectThread" + mSocketType);

			// Always cancel discovery because it will slow down a connection
			bluetoothAdapter.cancelDiscovery();

			// Make a connection to the BluetoothSocket
			boolean attemptConnection = true;
			boolean attemptReset = true;
            boolean isConnected = false;

            while (attemptConnection) {
			    try {
			        // This is a blocking call and will only return on a
			        // successful connection or an exception
			        mmSocket.connect();
			        attemptConnection = false;
			        isConnected = true;
			    } catch (IOException e) {
			        if (MyDebug.LOG) {
			            Log.e(TAG, "unable to connect() " + mSocketType + " socket on first attempt, " +
			            		"resetting BT adapter", e);
			        }
			        if (!cancelled) {
			            // To accommodate an Android bug, try resetting the adapter and attempt to 
			            // connect again. Do this only one time.
			            if (attemptReset) {
			                if (MyDebug.LOG) {
			                    Log.d(TAG, "Attempting to reset BT adapter.");
			                }
			                if (resetBluetoothAdapter()) {
			                    if (MyDebug.LOG) {
			                        Log.d(TAG, "resetBluetoothAdapter Success!.");
			                    }
			                    attemptReset = false;
			                } else {
			                    if (MyDebug.LOG) {
			                        Log.d(TAG, "resetBluetoothAdapter failed.");
			                    }
                                attemptConnection = false;
			                }
			            } else {
			                if (MyDebug.LOG) {
			                    Log.d(TAG, "Reset BT adapter, but could still not connect to meter.");
			                }
                            attemptConnection = false;
			            }
			        } else {
	                    attemptConnection = false;
			        }
			    }
            }
            
            if (isConnected) {
                // Reset the ConnectThread because we're done
                synchronized (BluetoothCommAdapter.this) {
                    connectThread = null;
                }

                // Start the connected thread
                connected(mmSocket, mmDevice, mSocketType);
                
            } else if (!cancelled) {
                try {
                    mmSocket.close();
                } catch (IOException e) {
                    if (MyDebug.LOG) {
                        Log.e(TAG, "unable to close() " + mSocketType + " socket during connection failure", e);
                    }
                    ErrorHandler errorHandler = ErrorHandler.getInstance();
                    errorHandler.logError(Level.WARNING, "BluetoothCommAdapter$ConnectThread." +
                            "run(): Could not close the socket - " + e,
                            R.string.bluetooth_adapter_error_title,
                            R.string.bluetooth_adapter_error_message);
                }
                connectionFailed();
            }

            
//			try {
//				// This is a blocking call and will only return on a
//				// successful connection or an exception
//				mmSocket.connect();
//			} catch (IOException e) {
//                Log.e(TAG, "unable to connect() " + mSocketType + " socket on first attempt, resetting BT adapter", e);
//                if (!cancelled) {
//                    // To accommodate an Android bug, try resetting the adapter and attempt to connect again.
//                    if (resetBluetoothAdapter()) {
//                        try {
//                            mmSocket.connect();
//                        } catch (IOException e1) {
//                            Log.e(TAG, "unable to connect() " + mSocketType + " socket during 2nd connection attempt", e1);
//                            // Close the socket
//                            try {
//                                mmSocket.close();
//                            } catch (IOException e2) {
//                                Log.e(TAG, "unable to close() " + mSocketType + " socket during connection failure", e2);
//                            }
//                            connectionFailed();
//                        }
//                    } else {
//                        Log.e(TAG, "unable to connect() " + mSocketType + " socket during 2nd connection attempt", e);
//                        // Close the socket
//                        try {
//                            mmSocket.close();
//                        } catch (IOException e2) {
//                            Log.e(TAG, "unable to close() " + mSocketType + " socket during connection failure", e2);
//                        }
//                        connectionFailed();
//                    }
//                }
//				return;
//			}
//
//			// Reset the ConnectThread because we're done
//			synchronized (BluetoothCommAdapter.this) {
//				connectThread = null;
//			}
//
//			// Start the connected thread
//			connected(mmSocket, mmDevice, mSocketType);
		}

		public void cancel() {
			cancelled = true;
			try {
				mmSocket.close();
			} catch (IOException e) {
		        if (MyDebug.LOG) {
		            Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
		        }
                ErrorHandler errorHandler = ErrorHandler.getInstance();
                errorHandler.logError(Level.WARNING, "BluetoothCommAdapter$ConnectThread." +
                        "cancel(): Could not close the socket - " + e,
                        R.string.bluetooth_adapter_error_title,
                        R.string.bluetooth_adapter_error_message);
			}
		}
	}

	/**
	 * This thread runs during a connection with a remote device. It handles all incoming and outgoing transmissions.
	 */
	private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;
		private boolean cancelled = false;

        public ConnectedThread(BluetoothSocket socket, String socketType) {
            if (MyDebug.LOG) {
                Log.d(TAG, "create ConnectedThread: " + socketType);
            }
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the BluetoothSocket input and output streams
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
		        if (MyDebug.LOG) {
		            Log.e(TAG, "temp sockets not created", e);
		        }
                ErrorHandler errorHandler = ErrorHandler.getInstance();
                errorHandler.logError(Level.WARNING, "BluetoothCommAdapter$ConnectedThread." +
                        "ConnectedThread(): Could not get socket stream - " + e,
                        R.string.bluetooth_adapter_error_title,
                        R.string.bluetooth_adapter_error_message);
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}
		
		public void run() {
	        if (MyDebug.LOG) {
	            Log.i(TAG, "BEGIN mConnectedThread");
	        }
			byte[] buffer = new byte[1024];
			int bytes;

			// Keep listening to the InputStream while connected
			while (!cancelled) {
				try {
					// Read from the InputStream
					bytes = mmInStream.read(buffer);
					
					byte[] response = new byte[bytes];
					System.arraycopy(buffer, 0, response, 0, bytes);
					
					if (listener != null) {
						listener.onReceiveBytes(response);
					}
				} catch (IOException e) {
					if (!cancelled) {
				        if (MyDebug.LOG) {
				            Log.e(TAG, "connected thread read error", e);
				        }
						connectionLost();
						cancel();
					}
					break;
				}
			}
		}

		/**
		 * Write to the connected OutStream.
		 * 
		 * @param buffer
		 *            The bytes to write
		 */
		public void write(byte[] buffer) {
			try {
				mmOutStream.write(buffer);
			} catch (IOException e) {
				if (!cancelled) {
			        if (MyDebug.LOG) {
			            Log.e(TAG, "connected thread write error", e);
			        }
					connectionLost();
				}
			}
		}

		public void cancel() {
			cancelled = true;
			try {
				mmSocket.close();
			} catch (IOException e) {
		        if (MyDebug.LOG) {
		            Log.e(TAG, "close() of connect socket failed", e);
		        }
	            ErrorHandler errorHandler = ErrorHandler.getInstance();
	            errorHandler.logError(Level.WARNING, "BluetoothCommAdapter$ConnectedThread." +
	                    "cancel(): Could not close the socket - " + e,
	                    R.string.bluetooth_adapter_error_title,
	                    R.string.bluetooth_adapter_error_message);
			}
		}
	}
}
