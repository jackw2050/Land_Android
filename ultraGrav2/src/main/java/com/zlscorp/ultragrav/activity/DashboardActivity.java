package com.zlscorp.ultragrav.activity;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;

import roboguice.inject.InjectView;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StatFs;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.google.inject.Inject;
import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.debug.MyDebug;
import com.zlscorp.ultragrav.file.ErrorHandler;
import com.zlscorp.ultragrav.meter.MeterService;
import com.zlscorp.ultragrav.model.CalibratedDialValue;
import com.zlscorp.ultragrav.model.CommunicationParams;
import com.zlscorp.ultragrav.model.FeedbackScaleParams;
import com.zlscorp.ultragrav.model.LevelCorrectionParams;
import com.zlscorp.ultragrav.model.MeterParams;
import com.zlscorp.ultragrav.model.SystemParams;
import com.zlscorp.ultragrav.persist.CalibratedDialValueDao;
import com.zlscorp.ultragrav.persist.CommunicationParamsDao;
import com.zlscorp.ultragrav.persist.FeedbackScaleParamsDao;
import com.zlscorp.ultragrav.persist.LevelCorrectionParamsDao;
import com.zlscorp.ultragrav.persist.MeterParamsDao;
import com.zlscorp.ultragrav.persist.StationDao;
import com.zlscorp.ultragrav.persist.StationSeriesDao;
import com.zlscorp.ultragrav.persist.SystemParamsDao;
import com.zlscorp.ultragrav.type.ObservationType;
//import android.widget.Button;
//import android.widget.Toast;

public class DashboardActivity extends AbstractBaseActivity {

	public final String TAG = "DashboardActivity";
	
	@Inject
	private MeterParamsDao meterParamsDao;
	
	@Inject 
	private CommunicationParamsDao communicationParamsDao;
	
	@Inject
	private LevelCorrectionParamsDao levelCorrectionParamsDao;
	
    @Inject
    private SystemParamsDao systemParamsDao;
    
    @Inject
    private FeedbackScaleParamsDao feedbackScaleParamsDao;
    
	@Inject
	private StationDao stationDao;
	
	@Inject
	private StationSeriesDao stationSeriesDao;

    @Inject
    private CalibratedDialValueDao calibratedDialValueDao;
    
	@InjectView(R.id.singleButton)
	private Button singleButton;
//	
//	@InjectView(R.id.continuousButton)
//	private Button continuousButton;
//	
//	@InjectView(R.id.settingsButton)
//	private Button settingsButton;
//	
//	@InjectView(R.id.setupButton)
//	private Button setupButton;
//	
//	@InjectView(R.id.optionsButton)
//	private Button optionsButton;

	@InjectView(R.id.meterNameView)
	private TextView meterNameView;
	
    @InjectView(R.id.calibTableView)
    private TextView calibTableView;
    
    @InjectView(R.id.dateAndTimeView)
    private TextView dateAndTimeView;
    
    // For future use. The current meter does not report it's temperature or battery level
    @InjectView(R.id.tempView)
    private TextView tempView;
    
    @InjectView(R.id.batteryView)
    private TextView batteryView;

    public Calendar cal;
    private BroadcastReceiver timeChangedReceiver;

    @Override
	public String getActivityName() {
		return TAG;
	}

	@Override
	public String getHelpKey() {
        return "dashboard";
		
	}
	int lankind;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        getSupportActionBar().setTitle(getString(R.string.title_activity_dashboard)); 
        lankind = pref.getInt("lan", 0);
		kind = 0;
//        Toast.makeText(this, "" + kind, Toast.LENGTH_SHORT).show();
        ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(false);
//		 in.putExtra("change", 1);

		if (getIntent().hasExtra("change"))
			
		{
			Intent in = getIntent();
//		     finish();
		     startActivity(in);
		}

        int kin = pref.getInt("lan", 0);
       
//        changeLanguage(kind);
		// Create a new Error Handler object in case the application was stopped 
		// but not destroyed, in which case, MyApplication.onCreate will not be executed.
		ErrorHandler.getInstance(this.getApplicationContext());
		
        Intent intent = MeterService.createIntent(this);
    	startService(intent);
        if (MyDebug.LOG) {
            Log.d(TAG, "Started MeterService.");
        }
        
        // Add the app version number to the dashboard activity title
        try {
            String appVersion = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).
                    versionName;
            setTitle(getTitle() + " " + "V" + appVersion);
        } catch (NameNotFoundException e) {
            if (MyDebug.LOG) {
                Log.v(TAG, e.getMessage());
            }
            ErrorHandler errorHandler = ErrorHandler.getInstance();
            errorHandler.logError(Level.WARNING, "DashboardActivity.onCreate(): " +
                    "Could not read the app version - " + e, 
                    R.string.can_not_read_app_version_title, 
                    R.string.can_not_read_app_version_message);
        }
        
    	// For future use. The current meter does not report it's temperature or battery level
        tempView.setVisibility(View.INVISIBLE);
        batteryView.setVisibility(View.INVISIBLE);

        timeChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (action.equals(Intent.ACTION_TIME_CHANGED) ||
                    action.equals(Intent.ACTION_DATE_CHANGED) ||
                    action.equals(Intent.ACTION_TIMEZONE_CHANGED) ||
                    action.equals(Intent.ACTION_LOCALE_CHANGED) ||
                    action.equals(Intent.ACTION_TIME_TICK)) {
                    // TODO - Include DST?
                    displayTime();
                }
            }
        };

        IntentFilter timeFilter = new IntentFilter();
        timeFilter.addAction(Intent.ACTION_TIME_CHANGED);
        timeFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        timeFilter.addAction(Intent.ACTION_TIME_TICK);

        registerReceiver(timeChangedReceiver, timeFilter);

        hasFragments = false;
	}

	public void displayTime () {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        String month = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US);
//        String day = cal.getDisplayName(Calendar.DATE, Calendar.SHORT, Locale.US);
//        String year = cal.getDisplayName(Calendar.YEAR, Calendar.SHORT, Locale.US);
        
        String date = Integer.toString(cal.get(Calendar.DATE));
        String year = Integer.toString(cal.get(Calendar.YEAR));

        String currentMins = Integer.toString(cal.get(Calendar.MINUTE));
        if (currentMins.length() == 1) {            // Add a leading 0 if value less than 10
            currentMins = "0" + currentMins;
        }
        String currentHrs = Integer.toString(cal.get(Calendar.HOUR_OF_DAY));
        if (currentHrs.length() == 1) {             // Add a leading 0 if value less than 10
            currentHrs = "0" + currentHrs;
        }
        dateAndTimeView.setText(month + " " + date + ", " + year + " " + currentHrs + 
                ":" + currentMins + " UTC");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (lankind != pref.getInt("lan", 0))
		{
			finish();
			startActivity(getIntent());
			lankind = pref.getInt("lan", 0);
			
		}
        kind = 0;
		MyDataLoader task = new MyDataLoader();
        task.execute();
        displayTime();
        	
        // Check for the presence of a calibration table
        try {
            List<CalibratedDialValue> calibratedDialValues = calibratedDialValueDao.
                    queryAllCalibratedDialValues();
            if (calibratedDialValues == null) {
                // Calibration table not installed  
                if (MyDebug.LOG) {
                    Log.i(TAG, "Calibration table is NULL");
                }
            } else {
                int tableSize = calibratedDialValues.size();
                if (MyDebug.LOG) {
                    Log.i(TAG, "Calibration table size is " + tableSize);
                }
                if (tableSize == 0) {
                    calibTableView.setText(R.string.no_calib_Table_title);
                } else if (tableSize == 133) {
                    calibTableView.setVisibility(View.GONE);
                } else {
                    ErrorHandler errorHandler = ErrorHandler.getInstance();
                    errorHandler.logError(Level.WARNING, "DashboardActivity.onResume(): " +
                            "Calibration table has wrong number of records: " + tableSize, 
                            R.string.calib_table_size_error_title, 
                            R.string.calib_table_size_error_message);
                }
            }
        } catch (Exception e) {
            if (MyDebug.LOG) {
                Log.e(TAG, "Calibration table read error", e);
            }
            ErrorHandler errorHandler = ErrorHandler.getInstance();
            errorHandler.logError(Level.WARNING, "DashboardActivity.onResume(): " +
                    "Could not read calibration table DB - " + e, 
                    R.string.calib_table_db_read_error_title, 
                    R.string.calib_table_db_read_error_message);
        }
	}
	
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(timeChangedReceiver);
    }	
    boolean ok = false;
    public void onSingleButtonClicked(View v) {
    	
        MeterService meter = MeterService.getInstance();
        if (meter != null) {
            if (meter.isConnected()) {
                Intent intent = DialReadingActivity.createIntent(this, ObservationType.SINGLE);
                startActivity(intent);
//                finish();
            } else {
                showNoMeterConnectedAlert();
            }
        } else {
            if (MyDebug.LOG) {
                Log.e(TAG, "No Meter Service started");
            }
            showNoMeterConnectedAlert();
        }
//    	Intent in = new Intent(this, ObservationActivity.class);
//    	startActivity(in);
    }

    public void onContinuousButtonClicked(View v) {
        MeterService meter = MeterService.getInstance();
        if (meter != null) {
            if (meter.isConnected()) {
                Intent intent = DialReadingActivity.createIntent(this, ObservationType.CONTINUOUS);
                startActivity(intent);
//                finish();
            } else {
                showNoMeterConnectedAlert();
            }
        } else {
            if (MyDebug.LOG) {
                Log.e(TAG, "No Meter Service started");
            }
            showNoMeterConnectedAlert();
        }
    }
    
    public void onSettingsButtonClicked(View v) {
    	
    	Intent intent = SettingsActivity.createIntent(this);
    	startActivity(intent);
//    	finish();
    }
    
    public void onSetupButtonClicked(View v) {
        MeterService meter = MeterService.getInstance();
        if (meter != null) {
            if (meter.isConnected()) {
                Intent intent = SetupActivity.createIntent(this);
                startActivity(intent);
//                finish();
            } else {
                showNoMeterConnectedAlert();
            }
        } else {
            if (MyDebug.LOG) {
                Log.e(TAG, "No Meter Service started");
            }
            showNoMeterConnectedAlert();
        }
    }
    
    public void onOptionsButtonClicked(View v) {
    	
    	Intent intent = OptionsActivity.createIntent(this);
    	startActivity(intent);
//    	finish();
    }
    
    public void onExitButtonClicked(View v) {

        Intent intent = MeterService.createIntent(this);
        stopService(intent);
        
        LocalBroadcastManager.getInstance(this).registerReceiver(meterEventReceiver, 
                new IntentFilter(MeterService.METER_SERVICE_EVENT));

        if (MyDebug.LOG) {
            Log.d(TAG, "Exit clicked");
        }
    }

//    public void OnResume()
    
    private BroadcastReceiver meterEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String message = intent.getStringExtra("message");
            if (MyDebug.LOG) {
                Log.d("receiver", "Got message: " + message);
            }

            if (message.equals(MeterService.METER_SERVICE_CREATED)) {
            } else if  (message.equals(MeterService.METER_SERVICE_DESTROYED)) {
                if (MyDebug.LOG) {
                    Log.d(TAG, "MeterService destroyed.");
                }
                ErrorHandler errorHandler = ErrorHandler.getInstance();
                if (errorHandler != null) {
                    errorHandler.logError(Level.INFO, "DashboardActivity$meterEventReceiver." +
                    		"onReceive(): Exiting application.");

                    errorHandler.onDestroy();
                }
                
                finish();
            }
        }
    };
        
    public void showNoMeterConnectedAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.no_meter_connected_title);
        builder.setMessage(R.string.no_meter_connected_message);
        builder.setPositiveButton(R.string.ok, null);
        builder.show();
    }
    
    public AlertDialog createDiskSpaceAlert(double percentAvailable){
    	double percentFull = 100.0 - percentAvailable;
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(R.string.disk_space_warning_title);
    	builder.setMessage(String.format(getString(R.string.disk_space_warning_message), percentFull));
    	builder.setPositiveButton(R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		return builder.create();
	}
    
    public void onLogoClicked(View v) {
        if (MyDebug.LOG) {
            Log.d(TAG, "Logo clicked");
        }
        
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.enter_password);
		final EditText input = new EditText(this);
	    input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
		builder.setView(input);

		builder.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String password = input.getText().toString();
				if (verifyPassword(password)) {
			        Intent intent = new Intent(DashboardActivity.this, PrivateActivity.class);
					startActivity(intent);
//					finish();
				} else {
					showIncorrectPasswordAlert();
				}
			}
		});
		builder.show();
	}
	
	private void showIncorrectPasswordAlert(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.incorrect_password_title);
		builder.setMessage(R.string.incorrect_password_message);
		builder.setPositiveButton(R.string.ok, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.show();
	}
	
	private boolean verifyPassword(String password) {
	    String mPassword = "ykr127";       // was alt159
	    return mPassword.equals(password);
	}

	/**
	 * Creates an Intent for this Activity.
	 * @param callee
	 * @return Intent
	 */
	public static Intent createIntent(Context callee) {
        if (MyDebug.LOG) {
            Log.d("DashBoardActivity", "creating dashboard activity");
        }
		return new Intent(callee, DashboardActivity.class);
	}
	
    private class MyDataLoader extends AsyncTask<Void, Void, LoaderResult> {
		
		private double percentAvailableDisk;
		
		private MeterParams meterParams;
		private CommunicationParams communicationParams;
		private LevelCorrectionParams levelCorrectionParams;
		private FeedbackScaleParams feedbackScaleParams;
		private SystemParams systemParams;
		private boolean stationSuccess;
		private boolean stationSeriesSuccess;
		
		@Override
		protected LoaderResult doInBackground(Void... params) {
			LoaderResult result = new LoaderResult();
			result.setSuccess(true);
			
			StatFs fs = new StatFs(getFilesDir().getAbsolutePath());
	    	percentAvailableDisk = ((double)fs.getAvailableBlocks() / (double)fs.getBlockCount()) * 100.0;
			
            // Test
//	    	boolean test = true;

            try {
                // Test
//                if (test) {
//                    throw new SQLException("test");
//                } else {
                    meterParams = meterParamsDao.queryForDefault();
//                }
			} catch(SQLException e) {
	            if (MyDebug.LOG) {
	                Log.e(TAG, "MeterParams failed to load", e);
	            }
				result.setSuccess(false);
				result.setMessage("Can't open meterParams " + e);
			}
			
			try {
                // Test
//			    throw new SQLException("test");
				communicationParams = communicationParamsDao.queryForDefault();
			} catch(SQLException e) {
	            if (MyDebug.LOG) {
	                Log.e(TAG, "CommunicationParams failed to load", e);
	            }
                result.setSuccess(false);
                result.setMessage("Can't open communicationParams " + e);
			}
			
			try {
                // Test
//              throw new SQLException("test");
				levelCorrectionParams = levelCorrectionParamsDao.queryForDefault();
			} catch(SQLException e) {
	            if (MyDebug.LOG) {
	                Log.e(TAG, "LevelCorrectionParams failed to load", e);
	            }
                result.setSuccess(false);
                result.setMessage("Can't open levelCorrectionParams " + e);
			}
			
            try {
                systemParams = systemParamsDao.queryForDefault();
            } catch(SQLException e) {
                if (MyDebug.LOG) {
                    Log.e(TAG, "SystemParams failed to load", e);
                }
                result.setSuccess(false);
                result.setMessage("Can't open systemParams " + e);
            }
            
            try {
                feedbackScaleParams = feedbackScaleParamsDao.queryForDefault();
            } catch(SQLException e) {
                if (MyDebug.LOG) {
                    Log.e(TAG, "FeedbackScaleParams failed to load", e);
                }
                result.setSuccess(false);
                result.setMessage("Can't open feedbackScaleParams " + e);
           }
            
			try {
				stationDao.queryForId(0L); // bogus id, just fishing for an exception
				stationSuccess = true;
			} catch(SQLException e) {
	            if (MyDebug.LOG) {
	                Log.e(TAG, "Station failed to load", e);
	            }
                result.setSuccess(false);
                result.setMessage("Can't open station DB " + e);
			}
			
			try {
				stationSeriesDao.queryForId(0L);  // bogus id, just fishing an for exception
				stationSeriesSuccess = true;
			} catch(SQLException e) {
	            if (MyDebug.LOG) {
	                Log.e(TAG, "StationSeries failed to load", e);
	            }
                result.setSuccess(false);
                result.setMessage("Can't open stationSeries DB " + e);
			}
			
			return result;
		}
		
		@Override
		protected void onPostExecute(LoaderResult result) {
		    
		    if (result.isSuccess()) {
	            if (meterParams == null
	                    || communicationParams == null
	                    || feedbackScaleParams == null
	                    || levelCorrectionParams == null
	                    || systemParams == null
	                    || !stationSuccess
	                    || !stationSeriesSuccess ) {
	                
	                ErrorHandler errorHandler = ErrorHandler.getInstance();
	                errorHandler.logError(Level.SEVERE, "DashboardActivity$MyDataLoader.onPostExecute(): " +
	                        "At least one of the DBs is null.", 
	                        R.string.configuration_load_failed_title, 
	                        R.string.configuration_load_failed_message);
	            } else {
	                if (meterParams.isCalibrated() == null || meterParams.isCalibrated() == false) {
	                    meterNameView.setText(getString(R.string.meter_type_noncalibrated, 
	                            meterParams.getSerialNumber()));
	                } else {
	                    meterNameView.setText(getString(R.string.meter_type_calibrated, 
	                            meterParams.getSerialNumber()));
	                }
	            }
		    } else {
                ErrorHandler errorHandler = ErrorHandler.getInstance();
                errorHandler.logError(Level.SEVERE, "DashboardActivity$MyDataLoader.onPostExecute(): " +
                        result.getMessage(), 
                        R.string.configuration_load_failed_title, 
                        R.string.configuration_load_failed_message);
		    }
			
			if (percentAvailableDisk < 10.0) {
	    		createDiskSpaceAlert(percentAvailableDisk).show();
	    	}
		}
	}

    public static class LoaderResult {
        private boolean success;
        private String message;
        
        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
