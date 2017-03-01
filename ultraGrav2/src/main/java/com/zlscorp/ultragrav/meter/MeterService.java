package com.zlscorp.ultragrav.meter;

import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import roboguice.service.RoboService;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.activity.DashboardActivity;
import com.zlscorp.ultragrav.communication.CommunicationErrorType;
import com.zlscorp.ultragrav.communication.CommunicationManager;
import com.zlscorp.ultragrav.communication.CommunicationManager.CommunicationManagerListener;
import com.zlscorp.ultragrav.communication.ConnectionState;
import com.zlscorp.ultragrav.communication.ICommAdapter;
import com.zlscorp.ultragrav.communication.transfer.AlternateBreakCommand;
import com.zlscorp.ultragrav.communication.transfer.AlternateBreakResponse;
import com.zlscorp.ultragrav.communication.transfer.BeginIntervalReadingCommand;
import com.zlscorp.ultragrav.communication.transfer.CurrentReadingResponse;
import com.zlscorp.ultragrav.communication.transfer.EmptyResponse;
import com.zlscorp.ultragrav.communication.transfer.EndIntervalReadingCommand;
import com.zlscorp.ultragrav.communication.transfer.GetPwmDutyCycleCommand;
import com.zlscorp.ultragrav.communication.transfer.GetPwmDutyCycleResponse;
import com.zlscorp.ultragrav.communication.transfer.ResponseCallback;
import com.zlscorp.ultragrav.communication.transfer.SetPwmDutyCycleCommand;
import com.zlscorp.ultragrav.debug.MyDebug;
import com.zlscorp.ultragrav.file.ErrorHandler;
import com.zlscorp.ultragrav.meter.processor.ProcessorConfig;
import com.zlscorp.ultragrav.meter.processor.ProcessorState;
import com.zlscorp.ultragrav.meter.processor.ReadingProcessor;
import com.zlscorp.ultragrav.model.CalibratedDialValue;
import com.zlscorp.ultragrav.model.CommunicationParams;
import com.zlscorp.ultragrav.model.LevelCorrectionParams;
import com.zlscorp.ultragrav.model.MeterParams;
import com.zlscorp.ultragrav.model.Observation;
import com.zlscorp.ultragrav.model.Station;
import com.zlscorp.ultragrav.model.SystemParams;
import com.zlscorp.ultragrav.persist.CalibratedDialValueDao;
import com.zlscorp.ultragrav.persist.CommunicationParamsDao;
import com.zlscorp.ultragrav.persist.LevelCorrectionParamsDao;
import com.zlscorp.ultragrav.persist.MeterParamsDao;
import com.zlscorp.ultragrav.persist.ObservationDao;
import com.zlscorp.ultragrav.persist.SystemParamsDao;
import com.zlscorp.ultragrav.type.CommunicationType;
import com.zlscorp.ultragrav.type.ObservationType;

public class MeterService extends RoboService {

	private static final String TAG = "MeterService";

    private static final int NOTIFICATION_ID = 0xAA00001;

    public static final String METER_SERVICE_EVENT = "meter service event";
    public static final String METER_SERVICE_CREATED = "meter service created";
    public static final String METER_SERVICE_DESTROYED = "meter service destroyed";

	private static MeterService instance;

    @Inject
	private NotificationManager notificationManager;

	@Inject
	private ObservationDao observationDao;

	@Inject
	private SystemParamsDao systemParamsDao;

	@Inject
	private MeterParamsDao meterParamsDao;

	@Inject
	private LevelCorrectionParamsDao levelCorrectionParamsDao;
	
	@Inject
	private CalibratedDialValueDao calibratedDialValueDao;
	
	@Inject
	private CommunicationParamsDao communicationParamsDao;

    // registry of listeners that want to be notified for the status of communications *access 
	// synchronized on statusListeners*
    public Set<CommunicationManagerListener> statusListeners = 
            new LinkedHashSet<CommunicationManagerListener>();

	private boolean readingInProgress = false;
	// private IntervalReadingResponseCallback intervalReadingResponseCallback = null;
	// private ProcessorStateListener processorStateListener = null;

    private CommunicationManager communicationManager;

	private ReadingProcessor readingProcessor = null;

	private Handler uiHandler;
	
//	private boolean isUsbDeviceAttached;

	public static Intent createIntent(Context callee) {
		Intent intent = new Intent(callee, MeterService.class);
		return intent;
	}
	
    @Override
	public void onCreate() {
		super.onCreate();

		// save the ref to the singleton managed by android
		instance = this;

		ErrorHandler errorHandler = ErrorHandler.getInstance();
		if (errorHandler == null) {
            if (MyDebug.LOG) {
                Log.d(TAG, "MeterService.onCreate() - errorHandler is null");
            }
		} else {
	        errorHandler.logError(Level.INFO, "MeterService.onCreate().", 0, 0);
		}
        if (MyDebug.LOG) {
            Log.d(TAG, "MeterService.onCreate() - Entered");
        }

		uiHandler = new Handler(getMainLooper());
		
		try {
			CommunicationParams communicationParams = communicationParamsDao.queryForDefault();
			communicationManager = new CommunicationManager(this, communicationParams);
			communicationManager.connect();
        } catch (SQLException e) {
            if (MyDebug.LOG) {
                Log.e(TAG, "failed to retrieve default CommunicationsParams or a valid commAdapter " +
                		"was not created", e);
            }
	        errorHandler.logError(Level.SEVERE, "MeterService.onCreate(): " +
	        		"Failed to retrieve default CommunicationsParams - " + e, 
	        		R.string.communication_params_file_open_error_title,
	        		R.string.communication_params_file_open_error_message);
		}
		
		IntentFilter usbFilter = new IntentFilter();
        usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        usbFilter.setPriority(500);
        this.registerReceiver(mUsbReceiver, usbFilter);   

        Intent intent = new Intent(METER_SERVICE_EVENT);
        intent.putExtra("message", METER_SERVICE_CREATED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if (communicationManager != null) {
	        communicationManager.disconnect();
		}

		instance = null;
        ErrorHandler errorHandler = ErrorHandler.getInstance();
        errorHandler.logError(Level.INFO, "MeterService.onDestroy().", 0, 0);

		Runnable run = new Runnable() {
			public void run() {
				notificationManager.cancel(NOTIFICATION_ID);
			}
		};
		uiHandler.post(run);

        unregisterReceiver(mUsbReceiver);
        
        // Let others know that the service is being destroyed
        Intent intent = new Intent(METER_SERVICE_EVENT);
        intent.putExtra("message", METER_SERVICE_DESTROYED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

	public static MeterService getInstance() {
		return instance;
	}

	@Override
	public IBinder onBind(Intent intent) {

		return null;
	}

    /**
     * Adds a listener to be notified for the status of communications
     */
    public synchronized void addCommStatusListener(CommunicationManagerListener listener) {
        if (MyDebug.LOG) {
            Log.d(TAG, "Adding CommStatusListener");
        }
        synchronized (statusListeners) {
            statusListeners.add(listener);
        }
    }

    /**
     * Removes a listener to be notified for the status of communications
     */
    public synchronized void removeCommStatusListener(CommunicationManagerListener listener) {
        boolean result;
        synchronized (statusListeners) {
            result = statusListeners.remove(listener);
        }
        if (MyDebug.LOG) {
            Log.d(TAG, "Removing CommStatusListener " + result);
        }
    }
    
    public boolean isReConnectingToBtDevice(String address) {
        return communicationManager.isReConnectingToBtDevice(address);
    }
    
    public void startReading(ObservationType observationType, String observationNote, Station station, 
            StartReadingCallback callback, ReadingResponseCallback readingCallback, 
            ProcessorStateListener listener) {

		try {
			readingInProgress = true;

            // Test
            boolean test = false;
            if (test) {
                throw new Exception("test");
            }

            SystemParams systemParams = systemParamsDao.queryForDefault();
			MeterParams meterParams = meterParamsDao.queryForDefault();
			LevelCorrectionParams levelCorrectionParams = levelCorrectionParamsDao.queryForDefault();
			List<CalibratedDialValue> calibratedDialValues = calibratedDialValueDao.
			        queryAllCalibratedDialValues();
			
			// build a config object with all the params the processor will need
			ProcessorConfig config = new ProcessorConfig();
			config.setObservationType(observationType);
			config.setObservationNote(observationNote);
			config.setStation(station);
			config.setSystemParams(systemParams);
			config.setMeterParams(meterParams);
			config.setLevelCorrectionParams(levelCorrectionParams);
			config.setCalibratedDialValues(calibratedDialValues);
			
            if (MyDebug.LOG) {
                Log.d(TAG, "ReadingProcessor config= " + config);
            }

			// create the processor that will be used for the observation
			readingProcessor = new ReadingProcessor(config);
						
			BeginIntervalReadingCommand command = new BeginIntervalReadingCommand();
			IntervalReadingResponseCallback commCallback = 
			        new IntervalReadingResponseCallback(readingCallback, listener);
			command.setResponseCallback(commCallback);

			communicationManager.sendCommand(command);

			notificationManager.notify(NOTIFICATION_ID, 
			        createNotification("Observation In Progress", true));
			
			callback.onSuccess();
		} catch (Exception e) {
            if (MyDebug.LOG) {
                Log.e(TAG, "failed to start reading", e);
            }
			callback.onFailed("Failed to start reading - " + e);
		}
	}

	public void endReading(EndReadingCallback callback) {
		try {
            if (MyDebug.LOG) {
                Log.d("EndObsRaceCond", "MeterService:endReading - Sending End Reading command to meter");
            }
			EndIntervalReadingCommand command = new EndIntervalReadingCommand();
			command.setResponseCallback(new EndIntervalReadingResponseCallback(callback));
			
            // Test
			boolean test = false;
			if (test) {
			    throw new Exception("test");
			}

			communicationManager.sendCommand(command);

			notificationManager.notify(NOTIFICATION_ID, createNotification("Meter Connected", true));
		} catch (Exception e) {
            if (MyDebug.LOG) {
                Log.e(TAG, "failed to stop reading", e);
            }

//			readingProcessor = null;
            readingInProgress = false;

            callback.onFailed(e.toString());
		}
	}

	public SaveResult saveObservation() {  // was type Observation, throws Exception 
	    
	    SaveResult result = new SaveResult();
	    Observation observation;
	    
	    try {

            // Test
            boolean test = false;
            if (test) {
                throw new Exception("test"); 
            }

	        ProcessorState state = readingProcessor.getState();

//          Log.d(TAG, "Saving Observation: isDisplayGravityData = " + state.isDisplayGravityData() + 
//  		               " isGoodObservation = " + state.isGoodObservation());
//          Log.d(TAG, "Saving Observation: state " + state.toString());

	        // Use state to make observation object
	        observation = new Observation();
	        observation.setCreateDate(System.currentTimeMillis());
	        observation.setObservationType(state.getProcessorConfig().getObservationType());

	        // Station Info
	        Station station = state.getProcessorConfig().getStation();
	        observation.setStationId(station.getStationId());
	        observation.setObserverId(station.getObserverId());
	        observation.setLatitude(station.getLatitude());
	        observation.setLongitude(station.getLongitude());
	        observation.setElevation(station.getElevation());
	        observation.setMeterHeight(station.getMeterHeight());
	        observation.setUseEarthTide(station.useEarthTide());

	        observation.setSerialNumber(state.getProcessorConfig().getMeterParams().getSerialNumber());
	        observation.setReadingtime(state.getReadingTime());
	        observation.setObservedGravity(state.getObservedGravity());
	        observation.setDial(state.getDialReading());
	        observation.setFeedbackCorrection(state.getFeedbackCorrection());
	        observation.setEarthtide(state.getEarthtide());
	        observation.setLevelCorrection(state.getLevelCorrection());
	        observation.setTemperatureCorrection(state.getTempCorrection());
	        observation.setBeamError(state.getBeamError());
	        observation.setElapsedTime(state.getElapsedTime());
	        observation.setDataOutputRate(state.getProcessorConfig().getSystemParams().
	                getDataOutputRate());
	        observation.setFilterTimeConstant(state.getProcessorConfig().getSystemParams().
	                getFilterTimeConstant());
	        observation.setStandardDeviation(state.getStandardDeviation());
	        observation.setTemperatureFrequency(state.getTemperatureFrequency());
	        observation.setBeamFrequency(state.getBeamFreq());
	        observation.setCrossLevelFrequency(state.getCrossLevelFreq());
	        observation.setLongLevelFrequency(state.getLongLevelFreq());
	        observation.setNote(state.getProcessorConfig().getObservationNote());

	    } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMessage(getString(R.string.did_not_create_observation_object_message) + e);
            return result;
	    }

		try {
	        // Test
	        boolean test = false;
	        if (test) {
	            throw new SQLException("test"); 
	        }
	        
			CreateOrUpdateStatus status = observationDao.createOrUpdate(observation);
            if (!status.isCreated()) {
	            result.setSuccess(false);
	            result.setErrorMessage(getString(R.string.did_not_create_observation_record_message));
	            return result;
			}
		} catch (SQLException e) {
            result.setSuccess(false);
            result.setErrorMessage(getString(R.string.observation_file_write_error_message) + e);
            return result;
		}

        result.setSuccess(true);
        result.setErrorMessage(null);
		return result;
	}
	
    public static class SaveResult {
        private boolean success;
        private String errorMessage;
        
        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }

    public void resetStatistics() {
		readingProcessor.getObservation().requestReset();
	}
	
	public void setSampleSize(int sampleSize) {
        readingProcessor.getObservation().setSampleSize(sampleSize);
	}
	
    public void setDutyCycle(int dutyCycle, SetDutyCycleCallback callback) {
		try {
			SetPwmDutyCycleCommand command = new SetPwmDutyCycleCommand();
			command.setDutyCycle(dutyCycle);
			command.setResponseCallback(new SetDutyCycleResponseCallback(callback));
			
            // Test
            boolean test = false;
            if (test) {
                throw new Exception("test");
            }
            
			communicationManager.sendCommand(command);
		} catch (Exception ex) {
            if (MyDebug.LOG) {
                Log.e(TAG, "failed to send set duty sysle", ex);
            }
            callback.onFailed(ex.toString());
//            callback.onFailed(getString(R.string.failed_to_set_duty_cycle) + ex);
		}
	}
	
	public void getDutyCycle(GetDutyCycleCallback callback) {
		try {
			GetPwmDutyCycleCommand command = new GetPwmDutyCycleCommand();
			command.setResponseCallback(new GetDutyCycleResponseCallback(callback));
			
			communicationManager.sendCommand(command);
		} catch (Exception ex) {
            if (MyDebug.LOG) {
                Log.e(TAG, "failed to send get duty cycle", ex);
            }
            callback.onFailed(ex.toString());
//			callback.onFailed(getString(R.string.failed_to_get_duty_cycle));
		}
	}

	private String toUserMessage(CommunicationErrorType errorType) {

		switch (errorType) {
			case TIMEOUT_RESPONSE:
				return getString(R.string.meterCommandTimeout);
			case MALFORMED_RESPONSE:
				return getString(R.string.malformed_response);
			case TRANSMIT_FAILED:
				return getString(R.string.transmit_failed);
			case UNEXPECTED_RESPONSE:
				return getString(R.string.unexpected_response);
			case UNKNOWN_RESPONSE:
				return getString(R.string.unknown_response);
			default:
				return getString(R.string.meterCommandUnknownError);
		}
	}

	private Notification createNotification(String message, boolean ongoing) {
        if (MyDebug.LOG) {
            Log.d(TAG, "Create Notification with message: " + message);
        }
		Intent intent = DashboardActivity.createIntent(this);
		intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 
		        PendingIntent.FLAG_UPDATE_CURRENT);

		BitmapDrawable icon = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher);

		Notification notification = new NotificationCompat.Builder(this)
				.setContentTitle("ZLS Burris Gravity Meter")
				.setContentText(message).setContentIntent(contentIntent)
				.setSmallIcon(R.drawable.ic_status)
				.setLargeIcon(icon.getBitmap())
				.setOngoing(ongoing)
				.setTicker(message)
				.build();
		return notification;
	}
	
	public boolean isConnected() {
        ICommAdapter commAdapter = null;

        if (MyDebug.LOG) {
            Log.d(TAG, "isConnected");
        }
        
        if (communicationManager != null) {
            commAdapter = communicationManager.getCurrentCommAdapter();
        }
		if (commAdapter == null ) {
			return false;
		}
		return commAdapter.getConnectionState() == ConnectionState.ESTABLISHED;
	}

	public boolean isReadingInProgress() {
		return readingInProgress;
	}
	
	public void onCommConnecting() {
		dispatchCommConnecting();
	}
	
	private void dispatchCommConnecting() {
		Runnable run = new Runnable() {
			public void run() {
				String message = getString(R.string.meter_connecting);
				notificationManager.notify(NOTIFICATION_ID, createNotification(message, true));
				Toast.makeText(MeterService.this, message, Toast.LENGTH_SHORT).show();

				synchronized (statusListeners) {
					for (CommunicationManagerListener listener : statusListeners) {
						listener.onConnecting();
					}
				}
			}
		};
		uiHandler.post(run);
	}
	
	public void onCommConnected() {
        if (MyDebug.LOG) {
            Log.d(TAG, "onCommConnected");
        }
		
		// send an AlternateBreak right after comm connect then dispatch connected if the command 
        // was responded to
		AlternateBreakCommand breakCommand = new AlternateBreakCommand();
		breakCommand.setResponseCallback(new ResponseCallback<AlternateBreakResponse>() {
			@Override
			public void onResponse(AlternateBreakResponse response) {
				dispatchCommConnected();
				
				// Initialize the meter's duty cycle to 50%
				setDutyCycle(32767, new SetDutyCycleCallback() {
               
				    @Override
				    public void onSuccess() {
			            if (MyDebug.LOG) {
			                Log.d(TAG, "OnCommConnected setDutyCycle success.");
			            }
				    }
               
				    @Override
				    public void onFailed(String reason) {
			            if (MyDebug.LOG) {
			                Log.e(TAG, "OnCommConnected setDutyCycle failed. " + reason);
			            }
				        
		                ErrorHandler errorHandler = ErrorHandler.getInstance();
		                errorHandler.logError(Level.WARNING, "MeterService.onCommConnected()$" +
		                		"onResponse$SetDutyCycleCallback.onFailed(): " +
		                        "setDutyCycle() failed - " + reason,
		                        R.string.set_duty_cycle_failed_title,
		                        R.string.set_duty_cycle_failed_message);
				    }
				});
			}
			
			@Override
			public void onFailed(CommunicationErrorType type) {
				dispatchCommError(type, toUserMessage(type), AlternateBreakCommand.COMMAND_ID);
				
                ErrorHandler errorHandler = ErrorHandler.getInstance();
                errorHandler.logError(Level.WARNING, "MeterService.onCommConnected()$" +
                        "ResponseCallback.onFailed(): breakCommand failed with the error - " + type,
                        R.string.meter_not_responding_title,
                        R.string.meter_not_responding_message);
			}
		});
		communicationManager.sendCommand(breakCommand);
	}

	private void dispatchCommConnected() {
		Runnable run = new Runnable() {
			public void run() {
				String message = getString(R.string.meter_connected);
				notificationManager.notify(NOTIFICATION_ID, createNotification(message, true));
				Toast.makeText(MeterService.this, message, Toast.LENGTH_SHORT).show();

				synchronized (statusListeners) {
					for (CommunicationManagerListener listener : statusListeners) {
						listener.onConnected();
					}
				}
			}
		};
		uiHandler.post(run);
	}
	
    public void onCommDisconnected() {
        dispatchCommDisconnected();
    }
    
    private void dispatchCommDisconnected() {
        Runnable run = new Runnable() {
            public void run() {
                String message = getString(R.string.meter_disconnected);
                notificationManager.notify(NOTIFICATION_ID, createNotification(message, false));
                Toast.makeText(MeterService.this, message, Toast.LENGTH_SHORT).show();

                synchronized (statusListeners) {
                    for (CommunicationManagerListener listener : statusListeners) {
                        listener.onDisconnected();
                    }
                }
            }
        };
        uiHandler.post(run);
    }

    public void onCommFailed() {
        dispatchCommFailed();
    }
    
    private void dispatchCommFailed() {
        Runnable run = new Runnable() {
            public void run() {
//                String message = getString(R.string.meter_comm_failed);
            notificationManager.notify(NOTIFICATION_ID, createNotification(getString(
                    R.string.meter_disconnected), false));
                Toast.makeText(MeterService.this, getString(R.string.meter_comm_failed), 
                        Toast.LENGTH_SHORT).show();
            }
        };
        uiHandler.post(run);
    }

	public void dispatchCommSent() {
		Runnable run = new Runnable() {
			public void run() {
				synchronized (statusListeners) {
					for (CommunicationManagerListener listener : statusListeners) {
						listener.onDataSent();
					}
				}
			}
		};
		uiHandler.post(run);
	}

	public void dispatchCommRecieved() {
		Runnable run = new Runnable() {
			public void run() {
				synchronized (statusListeners) {
					for (CommunicationManagerListener listener : statusListeners) {
						listener.onDataRecieved();
					}
				}
			}
		};
		uiHandler.post(run);
	}

	public void dispatchCommError(final CommunicationErrorType type, String reason, Byte commandId) {
        if (MyDebug.LOG) {
            Log.e(TAG, "dispatchCommError: " + reason);
        }

        try {
            CommunicationParams communicationParams = communicationParamsDao.queryForDefault();

            if ((communicationParams.getCommunicationType() == CommunicationType.USB) &&
                (type == CommunicationErrorType.TIMEOUT_RESPONSE) &&
                (commandId != null) && (commandId == AlternateBreakCommand.COMMAND_ID)) {

                communicationManager.disconnect();

                // Display a toast if we get a timeout comm error after sending the alternate break 
                // command when using the USB connection
                Runnable run = new Runnable() {
                    public void run() {
                        notificationManager.notify(NOTIFICATION_ID, createNotification(getString(
                                R.string.meter_not_responding_title), false));
                        Toast.makeText(MeterService.this, getString(
                                R.string.meter_not_responding_message), Toast.LENGTH_LONG).show();
                    }
                };
                uiHandler.post(run);
            } else if (type == CommunicationErrorType.UNEXPECTED_RESPONSE &&
                    commandId != null && commandId == CurrentReadingResponse.RESPONSE_ID) {
                // If the message is an obs dataset, send an end interval reading command 
                endReading(new MyEndReadingCallback());

                if (MyDebug.LOG) {
                    Log.i(TAG, "dispatchCommError: handling unexpected response");
                    Log.d("EndObsRaceCond", "MeterService:dispatchCommError - Sending End Reading " +
                    		"command to meter");
                }
            } else {
                Runnable run = new Runnable() {
                    public void run() {
                        synchronized (statusListeners) {
                            for (CommunicationManagerListener listener : statusListeners) {
                                listener.onError(type);
                            }
                        }
                    }
                };
                uiHandler.post(run);
            }
        } catch (SQLException e) {
            if (MyDebug.LOG) {
                Log.e(TAG, "failed to retrieve default CommunicationsParams", e);
            }
            ErrorHandler errorHandler = ErrorHandler.getInstance();
            errorHandler.logError(Level.WARNING, "MeterService.dispatchCommError(): " +
            		"Failed to retrieve default CommunicationsParams - " + e,
            		R.string.communication_params_file_open_error_title,
                    R.string.communication_params_file_open_error_message);
//            stopSelf();
        }
	}
	
	// Test
	public void endReadingCallbackTest () {
	    MyEndReadingCallback test = new MyEndReadingCallback();
	    test.onFailed("test");
	}

    private class MyEndReadingCallback implements EndReadingCallback {
        @Override
        public void onSuccess() {
            if (MyDebug.LOG) {
                Log.d(TAG, "reading ended");
            }
            Runnable run = new Runnable() {
                public void run() {
                    String message = getString(R.string.ended_unrequested_reading);
                    notificationManager.notify(NOTIFICATION_ID, createNotification(message, false));
                    Toast.makeText(MeterService.this, message, Toast.LENGTH_SHORT).show();
                }
            };
            uiHandler.post(run);
        }

        @Override
        public void onFailed(final String reason) {
            if (MyDebug.LOG) {
                Log.d(TAG, "reading end failed. " + reason);
            }

            Runnable run = new Runnable() {
                public void run() {
                    String message = getString(R.string.failed_to_end_unrequested_reading);
                    notificationManager.notify(NOTIFICATION_ID, createNotification(message, false));
                    Toast.makeText(MeterService.this, message, Toast.LENGTH_SHORT).show();
                    
                    ErrorHandler errorHandler = ErrorHandler.getInstance();
                    errorHandler.logError(Level.WARNING, "MeterService$MyEndReadingCallback." +
                    		"onFailed(): Failed to end reading - " + reason,
                            R.string.failed_to_end_reading_title,
                            R.string.failed_to_end_reading_message);
                }
            };
            uiHandler.post(run);
        }
    }

    public void dispatchProcessorState(final ProcessorStateListener listener, 
            final ProcessorState state) {
		Runnable run = new Runnable() {
			public void run() {
				listener.onProcessorState(state);
			}
		};
		uiHandler.post(run);
	}

	public void dispatchProcessorStateError(final ProcessorStateListener listener, final String reason) {
		Runnable run = new Runnable() {
			public void run() {
				listener.onError(reason);
			}
		};
		uiHandler.post(run);
	}
	
    public void notifyUSBDeviceAttach() {
        if (MyDebug.LOG) {
            Log.d(TAG,"USB Device attached...");
        }
//        isUsbDeviceAttached = true;

        Runnable run = new Runnable() {
            public void run() {
                String message = getString(R.string.usb_cable_attached);
                notificationManager.notify(NOTIFICATION_ID, createNotification(message, false));
//                Toast.makeText(MeterService.this, message, Toast.LENGTH_SHORT).show();
            }
        };
        uiHandler.post(run);
        
        try {
            CommunicationParams communicationParams = communicationParamsDao.queryForDefault();
            if (communicationParams.getCommunicationType() == CommunicationType.USB) {
                communicationManager = new CommunicationManager(this, communicationParams);
                communicationManager.connect();
            }
        } catch (Exception e) {
            if (MyDebug.LOG) {
                Log.e(TAG, "failed to retrieve default CommunicationsParams", e);
            }
            stopSelf();
        }
    }

    public void usbDeviceConnect() {
        if (MyDebug.LOG) {
            Log.d(TAG,"USB Device connect...");
        }
        try {
            CommunicationParams communicationParams = communicationParamsDao.queryForDefault();
            if (communicationParams.getCommunicationType() == CommunicationType.USB) {
                communicationManager = new CommunicationManager(this, communicationParams);
                communicationManager.connect();
            }
        } catch (Exception e) {
            if (MyDebug.LOG) {
                Log.e(TAG, "failed to retrieve default CommunicationsParams", e);
            }
            stopSelf();
        }
    }

    public void notifyUSBDeviceDetach() {
        if (MyDebug.LOG) {
            Log.d(TAG,"USB Device detached...");
        }
//        isUsbDeviceAttached = false;

        try {
            CommunicationParams communicationParams = communicationParamsDao.queryForDefault();
            
            // Test
            boolean isTest = false;
            
            if (isTest) {
                throw new SQLException("SQLException test");
            } else {
               if ((communicationParams.getCommunicationType() == CommunicationType.USB)) {
                   communicationManager.disconnect();
               }
               Runnable run = new Runnable() {
                   public void run() {
                       String message = getString(R.string.usb_cable_detached);
                       notificationManager.notify(NOTIFICATION_ID, createNotification(message, false));
                       Toast.makeText(MeterService.this, message, Toast.LENGTH_SHORT).show();

                       synchronized (statusListeners) {
                           for (CommunicationManagerListener listener : statusListeners) {
                               listener.onDisconnected();
                           }
                       }
                   }
               };
               uiHandler.post(run);
           }
        } catch (SQLException e) {
            if (MyDebug.LOG) {
                Log.e(TAG, "failed to retrieve default CommunicationsParams", e);
            }
            ErrorHandler errorHandler = ErrorHandler.getInstance();
            errorHandler.logError(Level.WARNING, "MeterService.notifyUSBDeviceDetach(): " +
            		"Failed to retrieve default CommunicationsParams - " + e,
                    R.string.communication_params_file_open_error_title,
                    R.string.communication_params_file_open_error_message);
        }
    }

    public boolean isUsbDeviceAttached() {
        return communicationManager.isUsbDeviceAttached(this);
//        return isUsbDeviceAttached;
    }

    /**
     * USB broadcast receiver
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) 
        {
            MeterService meter = MeterService.getInstance();
            if (meter != null) {
                String action = intent.getAction();
                if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                    meter.notifyUSBDeviceDetach();

                } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                    meter.notifyUSBDeviceAttach();
                }
            }
        }   
    };

	private class SetDutyCycleResponseCallback implements ResponseCallback<EmptyResponse> {

		private SetDutyCycleCallback callback;

		public SetDutyCycleResponseCallback(SetDutyCycleCallback callback) {
			this.callback = callback;
		}

		public void onResponse(EmptyResponse response) {
			uiHandler.post(new Runnable() {
				public void run() {
					callback.onSuccess();
				}
			});
		}

		public void onFailed(final CommunicationErrorType type) {
			uiHandler.post(new Runnable() {
				public void run() {
					callback.onFailed(toUserMessage(type));
				}
			});
		}
	}
	
	private class GetDutyCycleResponseCallback implements ResponseCallback<GetPwmDutyCycleResponse> {

		private GetDutyCycleCallback callback;

		public GetDutyCycleResponseCallback(GetDutyCycleCallback callback) {
			this.callback = callback;
		}

		public void onResponse(final GetPwmDutyCycleResponse response) {
			uiHandler.post(new Runnable() {
				public void run() {
					callback.onSuccess(response.getDutyCycle());
				}
			});
		}

		public void onFailed(final CommunicationErrorType type) {
			uiHandler.post(new Runnable() {
				public void run() {
					callback.onFailed(toUserMessage(type));
				}
			});
		}
	}

	private class EndIntervalReadingResponseCallback implements ResponseCallback<EmptyResponse> {

		private EndReadingCallback callback;

		public EndIntervalReadingResponseCallback(EndReadingCallback callback) {
			this.callback = callback;
		}

		public void onResponse(EmptyResponse response) {
			readingProcessor = null;
			readingInProgress = false;

			uiHandler.post(new Runnable() {
				public void run() {
					callback.onSuccess();
				}
			});
		}

		public void onFailed(final CommunicationErrorType type) {
            readingProcessor = null;
            readingInProgress = false;

            uiHandler.post(new Runnable() {
				public void run() {
					callback.onFailed(toUserMessage(type));
				}
			});
		}
	}

	private class IntervalReadingResponseCallback implements ResponseCallback<CurrentReadingResponse> {
		
		private ReadingResponseCallback callback;
		private ProcessorStateListener listener;

		public IntervalReadingResponseCallback(ReadingResponseCallback callback, 
		        ProcessorStateListener listener) {
			this.callback = callback;
			this.listener = listener;
		}

		@Override
		public void onResponse(CurrentReadingResponse response) {
			try {
				// process the response
                if (MyDebug.LOG) {
                    Log.w(TAG, "IntervalReadingResponseCallback - Meter data received.");
                }
				readingProcessor.process(response);
				ProcessorState state = readingProcessor.getState();
				
				if ((state.getProcessorConfig().getObservationType() == ObservationType.SINGLE) ||
				        (state.getProcessorConfig().getObservationType() == ObservationType.CONTINUOUS)) {
				    
	                // send the computed duty cycle to the meter
	                setDutyCycle(state.getDutyCycleAs16Bits(), new SetDutyCycleCallback() {
	                    
	                    @Override
	                    public void onSuccess() {
	                        if (MyDebug.LOG) {
	                            Log.d(TAG, "IntervalReadingResponseCallback setDutyCycle success.");
	                        }
	                    }
	                    
	                    @Override
	                    public void onFailed(String reason) {
	                        if (MyDebug.LOG) {
	                            Log.e(TAG, "IntervalReadingResponseCallback setDutyCycle failed. " + 
	                                    reason);
	                        }
	                        ErrorHandler errorHandler = ErrorHandler.getInstance();
	                        errorHandler.logError(Level.WARNING, "MeterService$" +
	                                "IntervalReadingResponseCallback.onResponse()$setDutyCycle&" +
	                                "SetDutyCycleCallback.onFailed: " + reason,
	                                getString(R.string.set_duty_cycle_failed_title),
	                                getString(R.string.set_duty_cycle_failed_message));
	                    }
                    });
	            }
				
				// let listeners know of the new state
				dispatchProcessorState(listener, state);
			} catch (Exception e) {
                if (MyDebug.LOG) {
                    Log.e(TAG, "error processing current reading", e);
                }
				dispatchProcessorStateError(listener, getString(R.string.obs_processing_error));
			}
		}

		@Override
		public void onFailed(final CommunicationErrorType type) {
			readingInProgress = false;

			uiHandler.post(new Runnable() {
				public void run() {
					callback.onFailed(toUserMessage(type));
				}
			});
			
			dispatchProcessorStateError(listener, toUserMessage(type));   // will run on uiHandler
		}
	}
	
    public interface StartReadingCallback {

        public void onSuccess();

        public void onFailed(String reason);
    }

    public interface ReadingResponseCallback {

        public void onFailed(String reason);
    }

	public interface EndReadingCallback {

		public void onSuccess();

		public void onFailed(String reason);
	}
	
	public interface SetDutyCycleCallback {

		public void onSuccess();

		public void onFailed(String reason);
	}
	
	public interface GetDutyCycleCallback {

		public void onSuccess(double dutyCycle);

		public void onFailed(String reason);
	}

    public interface ProcessorStateListener {

        public void onProcessorState(ProcessorState processorState);

        public void onError(String reason);
    }
}
