package com.zlscorp.ultragrav.activity.fragment;

import java.sql.SQLException;
import java.util.logging.Level;
//import java.text.DecimalFormat;

import roboguice.inject.InjectView;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.google.inject.Inject;
import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.activity.AbstractBaseActivity;
import com.zlscorp.ultragrav.activity.fragment.BluetoothScanFragment.OnBluetoothDeviceSelectedListener;
import com.zlscorp.ultragrav.activity.validate.Validator;
import com.zlscorp.ultragrav.activity.widget.ListFragmentPagerAdapter.OnFragmentSelectedListener;
import com.zlscorp.ultragrav.communication.CommunicationErrorType;
import com.zlscorp.ultragrav.communication.CommunicationManager.CommunicationManagerListener;
import com.zlscorp.ultragrav.debug.MyDebug;
import com.zlscorp.ultragrav.file.ErrorHandler;
import com.zlscorp.ultragrav.meter.MeterService;
import com.zlscorp.ultragrav.model.CommunicationParams;
import com.zlscorp.ultragrav.model.SystemParams;
import com.zlscorp.ultragrav.persist.CommunicationParamsDao;
import com.zlscorp.ultragrav.persist.SystemParamsDao;
import com.zlscorp.ultragrav.persist.AbstractDao.TransactionCallback;
import com.zlscorp.ultragrav.type.CommunicationType;

public class SystemParamsFragment extends AbstractBaseFragment implements OnFragmentSelectedListener {

	private static final String TAG = "SystemParamsFragment";

	@Inject
	private SystemParamsDao systemParamsDao;

	@Inject
	private Validator validator;
	
	@InjectView(R.id.useNonCalibratedPointsCheckBox)
	private CheckBox useNonCalibratedPointsCheckBox;

    @InjectView(R.id.dummyLinearLayout)
    private LinearLayout dummyLinearLayout;

    @InjectView(R.id.newDialReading)
    private EditText newDialReading;

	@InjectView(R.id.feedbackGain)
	private EditText feedbackGain;

	@InjectView(R.id.observationPrecision)
	private EditText observationPrecision;

	@InjectView(R.id.enableStationSelect)
	private CheckBox enableStationSelect;

    @InjectView(R.id.tempUnitsPrompt)
    private TextView tempUnitsPrompt;

    @InjectView(R.id.celsiusRadioButton)
    private RadioButton celsiusRadioButton;

	@InjectView(R.id.fahrenheitRadioButton)
	private RadioButton fahrenheitRadioButton;
	
	@Inject
	private CommunicationParamsDao communicationParamsDao;
	
	@InjectView(R.id.communicationType)
	private TextView communicationTypeText;
	
    @InjectView(R.id.deviceName)
    private TextView deviceName;
    
    @InjectView(R.id.connectionStatus)
    private TextView connectionStatus;
    
	@InjectView(R.id.bluetoothScanButton)
	private Button bluetoothScanButton;
	
	@InjectView(R.id.usbScanButton)
	private Button usbScanButton;
	
	private CommunicationParams communicationParams;
	
	private SystemParams systemParams;
	
	private IntentFilter usbFilter;
	private boolean isRegistered;
	
    private MyCommunicationManagerListener myCommunicationManagerListener = new MyCommunicationManagerListener();

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_system_params, container, false);
		return v;
	}

	@Override
	public void setupView(View view, Bundle savedInstanceState) {

	    AbstractBaseActivity.fragmentName = this.getClass().getSimpleName();
	    
	    if (useNonCalibratedPointsCheckBox.isChecked()) {
	        validator.validateAsInteger(newDialReading, SystemParams.DIAL_READING_MIN, 
	                SystemParams.DIAL_READING_MAX);
	    } else {
	        validator.validateAsInteger(newDialReading, SystemParams.DIAL_READING_MIN, 
	                SystemParams.DIAL_READING_MAX, 
	                SystemParams.DIAL_READING_INC);
	    }

	    validator.validateAsDouble(feedbackGain, SystemParams.FEEDBACK_GAIN_MIN, 
	            SystemParams.FEEDBACK_GAIN_MAX);

	    validator.validateAsDouble(observationPrecision, SystemParams.OBS_PRECISION_MIN, 
	            SystemParams.OBS_PRECISION_MAX);

        feedbackGain.setOnFocusChangeListener(new OnFocusChangeListener() {          
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && feedbackGain.getText().toString() != null) {
                    feedbackGain.setSelection(feedbackGain.getText().toString().length());   // position cursor at right end of line
                }
            }
        });
        
        observationPrecision.setOnFocusChangeListener(new OnFocusChangeListener() {          
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && observationPrecision.getText().toString() != null) {
                    observationPrecision.setSelection(observationPrecision.getText().toString().length());   // position cursor at right end of line
                }
            }
        });
        
        try {
      	  communicationParams = communicationParamsDao.queryForDefault();
      	  
      	  // Test
//      	  if (false) {
//      	      throw new SQLException("test");
//      	  }
        } catch (SQLException e) {
            if (MyDebug.LOG) {
                Log.e(TAG, "Failed to get default CommunicationParams", e);
            }
            
            ErrorHandler errorHandler = ErrorHandler.getInstance();
            errorHandler.logError(Level.WARNING, "SystemParamsFragment.setupView(): " +
                    "Failed to read communicationParams - " + e,
                    R.string.communication_params_file_open_error_title,
                    R.string.communication_params_file_open_error_message);
        }

        MeterService meter = MeterService.getInstance();
        if (meter != null) {
            updateCommunicationDisplay();   
            if (meter.isConnected()) {
                connectionStatus.setText(getString(R.string.connected));
            } else {
//                Toast.makeText(getActivity(), getString(R.string.meter_not_connected) , Toast.LENGTH_LONG).show();
                if (meter.isReConnectingToBtDevice(communicationParams.getAddress())) {
                    connectionStatus.setText(getString(R.string.attempting_to_reconnect));
                } else {
                    connectionStatus.setText(getString(R.string.disconnected));
                }
            }
        } else {
            communicationTypeText.setText(R.string.none);
            deviceName.setText(getString(R.string.none));
            connectionStatus.setText(getString(R.string.disconnected));
        }

        bluetoothScanButton.setOnClickListener(new BluetoothScanClickListener());
        usbScanButton.setOnClickListener(new UsbScanClickListener());
        if (meter != null && !meter.isUsbDeviceAttached()) {
            usbScanButton.setEnabled(false);
        }

        tempUnitsPrompt.setVisibility(View.GONE);            // For future use. The current meter does not report it's temperature
        celsiusRadioButton.setVisibility(View.GONE);
        fahrenheitRadioButton.setVisibility(View.GONE);

        usbFilter = new IntentFilter();
        usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        usbFilter.setPriority(500);
        
        isRegistered = false;
	}

    @Override
    public void onResume() {
      MeterService meter = MeterService.getInstance();
      if (meter != null) {
          meter.addCommStatusListener(myCommunicationManagerListener);
      }
      LocalBroadcastManager.getInstance(getActivity()).
           registerReceiver(meterEventReceiver, new IntentFilter(MeterService.METER_SERVICE_EVENT));

      myRegisterReceiver(mUsbReceiver, usbFilter);   

      super.onResume();

      if (AbstractBaseActivity.fragmentName != null && 
              AbstractBaseActivity.fragmentName.equals(this.getClass().getSimpleName())) {
          // This keeps any real element from getting focus.
          dummyLinearLayout.requestFocus();

          // set focus on this field and position cursor at right end of line
//        newDialReading.requestFocus();
//        newDialReading.setSelection(newDialReading.getText().toString().length());
      }
    }
    
    private void myRegisterReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        if (!isRegistered) {
            getActivity().registerReceiver(receiver, filter);   
            isRegistered = true;
        }
    }
    
    private void myUnregisterReceiver(BroadcastReceiver receiver) {
        if (isRegistered) {
            try {
                getActivity().unregisterReceiver(receiver);   
                isRegistered = false;
            } catch (Exception e) {
                isRegistered = false;
                if (MyDebug.LOG) {
                    Log.e(TAG, mUsbReceiver + " already unregistered." + e);
                }
            }
        }
    }

    @Override
    public void onPause() {
        MeterService meter = MeterService.getInstance();
        if (meter != null) {
            meter.removeCommStatusListener(myCommunicationManagerListener);
        }
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(meterEventReceiver);
        
        myUnregisterReceiver(mUsbReceiver);

        super.onPause();
    }
    
    @Override
    public void onFragmentSelected() {
        myRegisterReceiver(mUsbReceiver, usbFilter);   

        // This keeps any real element from getting focus.
        dummyLinearLayout.requestFocus();

        // set focus on this field and position cursor at right end of line
//        newDialReading.requestFocus();
//        newDialReading.setSelection(newDialReading.getText().toString().length());   

        AbstractBaseActivity.fragmentName = this.getClass().getSimpleName();
    }

    @Override
    public void onFragmentUnselected() {
        myUnregisterReceiver(mUsbReceiver);
        
        // Hide the soft keyboard before changing tabs. 
        InputMethodManager imm = (InputMethodManager) getActivity().
                getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    public void notifyUSBDeviceAttach() {
//        Log.d(TAG,"USB Device attached...");
        usbScanButton.setEnabled(true);
    }

    public void notifyUSBDeviceDetach() {
//        Log.d(TAG,"USB Device detached...");
        usbScanButton.setEnabled(false);
    }

    /**
     * USB broadcast receiver
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) 
        {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                notifyUSBDeviceDetach();

            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                notifyUSBDeviceAttach();
            }
        }   
    };

    private void updateNewDialReadingValidator() {
        
        if (systemParams.getUseNoncalibratedPoints()==null || !systemParams.getUseNoncalibratedPoints()) {
            validator.validateAsInteger(newDialReading, 250, 6850, 50);
        } else {
            validator.validateAsInteger(newDialReading, 250, 6850);
        }
        
        validator.validateAll();
    }

	@Override
	public void populateData() {
		try {
			systemParams = systemParamsDao.queryForDefault();
		} catch (SQLException e) {
            ErrorHandler errorHandler = ErrorHandler.getInstance();
            errorHandler.logError(Level.SEVERE, "SystemParamsFragment.populateData(): " +
                    "Can't open systemParams - " + e,
                    R.string.system_params_file_open_error_title,
                    R.string.system_params_file_open_error_message);
		}

		if (systemParams == null) {
			systemParams = new SystemParams();
			systemParams.setDefaultUse(true);
		}

		if (systemParams.getUseNoncalibratedPoints() == null || !systemParams.getUseNoncalibratedPoints()) {
			useNonCalibratedPointsCheckBox.setChecked(false);
		} else {
			useNonCalibratedPointsCheckBox.setChecked(true);
		}
		if (systemParams.getDialReading() != null) {
		    newDialReading.setText(systemParams.getDialReading().toString());
		    
	        // set focus on this field and position cursor at right end of line
//	        newDialReading.requestFocus();
//	        newDialReading.setSelection(newDialReading.getText().toString().length());   
		}
		if (systemParams.getFeedbackGain() != null) {
			feedbackGain.setText(systemParams.getFeedbackGain().toString());
		}
		if (systemParams.getObservationPrecision() != null) {
			observationPrecision.setText(systemParams.getObservationPrecision().toString());
		}
		if (systemParams.getEnableStationSelect() != null) {
			enableStationSelect.setChecked(systemParams.getEnableStationSelect());
		}
		if (systemParams.getUseCelsius() == null || systemParams.getUseCelsius()) {
			celsiusRadioButton.setChecked(true);
		} else {
			fahrenheitRadioButton.setChecked(true);
		}

		updateNewDialReadingValidator();

//        if (systemParams.getDialReading() != null) {
//    	    newDialReading.setSelection(systemParams.getDialReading().toString().length());   // position cursor at right end of line
//        }

		useNonCalibratedPointsCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		        systemParams.setUseNoncalibratedPoints(isChecked);
		        updateNewDialReadingValidator();
		    }
		});

		validator.validateAll();
        dummyLinearLayout.requestFocus();
	}

	@Override
	public void persistData() {
		
		systemParams.setUseNoncalibratedPoints(useNonCalibratedPointsCheckBox.isChecked());

		if (validator.isValid(newDialReading)) {
		    systemParams.setDialReading(Integer.parseInt(newDialReading.getText().toString()));
		}
		if (validator.isValid(feedbackGain)) {
		    systemParams.setFeedbackGain(Double.parseDouble(feedbackGain.getText().toString()));
		}
		if (validator.isValid(observationPrecision)) {
		    systemParams.setObservationPrecision(Double.parseDouble(observationPrecision.getText().toString()));
		}

		systemParams.setEnableStationSelect(enableStationSelect.isChecked());
		systemParams.setUseCelsius(celsiusRadioButton.isChecked());

		try {
			systemParamsDao.createOrUpdate(systemParams);
		} catch (SQLException e) {
            ErrorHandler errorHandler = ErrorHandler.getInstance();
            errorHandler.logError(Level.WARNING, "SystemParamsFragment.persistData(): " +
                    "Can't update systemParams - " + e,
                    R.string.system_params_file_update_error_title,
                    R.string.system_params_file_update_error_message);
		}
	}
	
	// Communications
   
	private class MyCommunicationManagerListener implements CommunicationManagerListener {

        @Override
        public void onDataRecieved() {}

        @Override
        public void onDataSent() {}

        @Override
        public void onConnecting() {}

        @Override
        public void onConnected() {
            updateCommunicationDisplay();
            connectionStatus.setText(getString(R.string.connected));
        }

        @Override
        public void onDisconnected() {
            MeterService meterService = MeterService.getInstance();
            if (meterService != null && meterService.isReConnectingToBtDevice(communicationParams.getAddress())) {
                connectionStatus.setText(getString(R.string.attempting_to_reconnect));
            } else {
                updateCommunicationDisplay();
                connectionStatus.setText(getString(R.string.disconnected));
            }
        }

        @Override
        public void onError(CommunicationErrorType type) {
            if (MyDebug.LOG) {
                Log.d(TAG, "Connection Manager - Error");
            }
        }
	}

	private BroadcastReceiver meterEventReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	        String message = intent.getStringExtra("message");
            if (MyDebug.LOG) {
                Log.d("receiver", "Got message: " + message);
            }

	        if (message.equals(MeterService.METER_SERVICE_CREATED)) {
              MeterService meter = MeterService.getInstance();
              if (meter != null) {
                  meter.addCommStatusListener(myCommunicationManagerListener);
              }
              
	        } else if  (message.equals(MeterService.METER_SERVICE_DESTROYED)) {
	            final Intent startIntent = MeterService.createIntent(getActivity());
	            communicationParamsDao.updateDefault(communicationParams, new TransactionCallback() {
	
	                @Override
	                public void onSuccess() {
	                    getActivity().startService(startIntent);
	                }
	
	                @Override
	                public void onFailed(Exception e) {
	                    // Start the service back up even though this transaction failed.
                        getActivity().startService(startIntent);

                        if (MyDebug.LOG) {
                            Log.e(TAG, "failed to save communication params", e);
                        }
	                    ErrorHandler errorHandler = ErrorHandler.getInstance();
	                    errorHandler.logError(Level.WARNING, "SystemParamsFragment$meterEventReceiver.onReceive(): " +
	                            "Can't save communication params - " + e,
	                            R.string.communication_params_file_update_error_title,
	                            R.string.communication_params_file_update_error_message);
	                }
	            });
	        }
	    }
	};
	    
	private void updateCommunicationDisplay() {
	    communicationTypeText.setText(communicationParams.getCommunicationType().name());
        deviceName.setText(communicationParams.getName());
	}
   
   private class UsbScanClickListener implements OnClickListener {

       @Override
       public void onClick(View v) {

           MeterService meter = MeterService.getInstance();
           if (meter == null) {
               // Display an appropriate message
               return;
           }
           if (communicationParams.getCommunicationType() != CommunicationType.USB) { 

               final Intent intent = MeterService.createIntent(getActivity());
               getActivity().stopService(intent);

               communicationParams.setCommunicationType(CommunicationType.USB);
               communicationParams.setName(getString(R.string.none));
               communicationParams.setAddress(getString(R.string.none));

               // This code was moved to meterEventReceiver 
               // so it gets executed after the service has shut down
//               communicationParamsDao.updateDefault(communicationParams, new TransactionCallback() {
//
//                   @Override
//                   public void onSuccess() {
//                       getActivity().startService(intent);
//                       MeterService meter = MeterService.getInstance();
//                       if (meter != null) {
//                           meter.addCommStatusListener(myCommunicationManagerListener);
//                       }
//                   }
//
//                   @Override
//                   public void onFailed(Exception e) {
//                       Log.e(TAG, "failed to save communication params", e);
//                   }
//               });

           } else if (communicationParams.getCommunicationType() == CommunicationType.USB) {
               if (meter.isConnected()) {
                   Toast.makeText(getActivity(), getString(R.string.meter_already_connected) , Toast.LENGTH_LONG).show();
               } else {
                   meter.usbDeviceConnect();
               }
           }
       }
   }
   
   private class BluetoothScanClickListener implements OnClickListener {
       public void onClick(View v) {

           BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
           if (bluetoothAdapter == null) {
               AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
               builder.setTitle(R.string.no_bluetooth_support_title);
               builder.setMessage(R.string.no_bluetooth_support_message);
               builder.setCancelable(false);
               builder.setPositiveButton(R.string.ok, null);
               builder.show();
               return;
           } else {
               if (!bluetoothAdapter.isEnabled()) {
                   AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                   builder.setTitle(R.string.enable_bluetooth_title);
                   builder.setMessage(R.string.enable_bluetooth_message);
                   builder.setCancelable(false);
                   builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int which) {
                           getActivity().startActivity(new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS));
                       }
                   });
                   builder.show();
                   return;
               }
           }

           BluetoothScanFragment dialog = new BluetoothScanFragment();
           dialog.setOnBluetoothDeviceSelectedListener(new OnBluetoothDeviceSelectedListener() {
               @Override
               public void onBluetoothDeviceSelectedListener(BluetoothDevice device) {

                   // Check to see if we are trying to reconnect to a BT device
                   MeterService meter = MeterService.getInstance();
                   if (meter != null && meter.isReConnectingToBtDevice(device.getAddress())) {
                       // Display toast letting user know we are trying to reconnect to this device
                       Toast.makeText(getActivity(), getString(R.string.reconnecting_to_device) , Toast.LENGTH_LONG).show();
                   } else {
                       final Intent intent = MeterService.createIntent(getActivity());
                       getActivity().stopService(intent);

                       communicationParams.setCommunicationType(CommunicationType.BLUETOOTH);
                       communicationParams.setName(device.getName());
                       communicationParams.setAddress(device.getAddress());
                   }

                   // This code was moved to meterEventReceiver 
                   // so it gets executed after the meter service has shut down
//                   communicationParamsDao.updateDefault(communicationParams, new TransactionCallback() {
//
//                       @Override
//                       public void onSuccess() {
//                           getActivity().startService(intent);
//                           MeterService meter = MeterService.getInstance();
//                           if (meter != null) {
//                               meter.addCommStatusListener(myCommunicationManagerListener);
//                           }
//                       }
//
//                       @Override
//                       public void onFailed(Exception e) {
//                           Log.e(TAG, "failed to save communication params", e);
//                       }
//                   });
               }
           });
           dialog.show(getSherlockActivity().getSupportFragmentManager().beginTransaction(), "dialog");
       }
   }
}
