package com.zlscorp.ultragrav.activity;

import java.sql.SQLException;
import java.util.logging.Level;

import roboguice.inject.InjectView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import com.google.inject.Inject;
import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.activity.validate.Validator;
import com.zlscorp.ultragrav.activity.validate.Validator.ValidationListener;
import com.zlscorp.ultragrav.debug.MyDebug;
import com.zlscorp.ultragrav.file.ErrorHandler;
import com.zlscorp.ultragrav.model.Station;
import com.zlscorp.ultragrav.model.SystemParams;
import com.zlscorp.ultragrav.persist.StationDao;
import com.zlscorp.ultragrav.persist.SystemParamsDao;
import com.zlscorp.ultragrav.type.ObservationType;

public class DialReadingActivity extends AbstractBaseActivity {

	private static String TAG = "DialReadingActivity";

    private ObservationType observationType;
	private String observationNote;

	@Inject
	private StationDao stationDao;
	
	@Inject
	private Validator validator;

    @InjectView(R.id.dummyLinearLayout)
    private LinearLayout dummyLinearLayout;

	@InjectView(R.id.acceptButton)
	private Button acceptButton;

//	@InjectView(R.id.noteButton)
//	private Button noteButton;

	@InjectView(R.id.dialReadingText)
	private EditText dialReadingText;

	@InjectView(R.id.useNonCalibratedPointsCheckBox)
	private CheckBox useNonCalibratedPointsCheckBox;
	
    @Inject
    private SystemParamsDao systemParamsDao;

	private SystemParams systemParams;
	
	private Context context;

	@Override
	public String getActivityName() {
		return TAG;
	}

	@Override
	public String getHelpKey() {
		return "dialReading";
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dial_reading);
		kind = 3;
		getSupportActionBar().setTitle(getString(R.string.title_activity_dial_reading)); 
		observationType = (ObservationType) getIntent().getExtras().get(EXTRA_OBSERVATION_TYPE);
		
		try {
            // Test
            boolean test = false;
            if (test) {
                throw new SQLException("test");
            }
            
			systemParams = systemParamsDao.queryForDefault();
			
            if (systemParams.getUseNoncalibratedPoints() != null) {
				useNonCalibratedPointsCheckBox.setChecked(systemParams.getUseNoncalibratedPoints());
			}
			if (systemParams.getDialReading() != null) {
				dialReadingText.setText(systemParams.getDialReading().toString());
			}
			
            updateDialReadingValidator();

            if (systemParams.getDialReading() != null) {
                dialReadingText.setSelection(systemParams.getDialReading().toString().length());   // position cursor at right end of line
            }

            useNonCalibratedPointsCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					systemParams.setUseNoncalibratedPoints(isChecked);
					updateDialReadingValidator();
				}
			});
			
			validator.addValidationListener(new ValidationListener() {
				public void onValidation(Object input, boolean inputValid, boolean allInputsValid) {
					acceptButton.setEnabled(allInputsValid);
				}
			});
			
			validator.validateAll();
			
		} catch (SQLException e) {
		    ErrorHandler errorHandler = ErrorHandler.getInstance();
            errorHandler.logError(Level.WARNING, "DialReadingActivity.onCreate(): " +
            		"Could not open systemParams - " + e,
            		R.string.system_params_file_open_error_title, 
            		R.string.system_params_file_open_error_message);
            
            finish();
		 }

        hasFragments = false;
        context = this;
        
        // This keeps any real element from getting focus.
        dummyLinearLayout.requestFocus();
	}
	
	private void updateDialReadingValidator() {
		
		if (systemParams.getUseNoncalibratedPoints()==null || !systemParams.getUseNoncalibratedPoints()) {
			validator.validateAsInteger(dialReadingText, 250, 6850, 50);
		} else {
			validator.validateAsInteger(dialReadingText, 250, 6850);
		}
		
		validator.validateAll();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (requestCode == REQUEST_NOTE) {
			if (resultCode == RESULT_OK) {
				String note = NoteActivity.getNoteFromIntent(data);
				observationNote = note;
			}
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		systemParams.setUseNoncalibratedPoints(useNonCalibratedPointsCheckBox.isChecked());
		if (validator.isValid(dialReadingText)) {
			systemParams.setDialReading(Integer.parseInt(dialReadingText.getText().toString()));
		}
	}

	public void onNoteButtonClicked(View view) {
        if (MyDebug.LOG) {
            Log.d(TAG, "touched note button");
        }
		Intent intent = NoteActivity.createIntent(this, observationNote);
		startActivityForResult(intent, REQUEST_NOTE);
		
	}

	public void onAcceptButtonClicked(View view) {
        Station station = null;
        Intent intent;
        if (MyDebug.LOG) {
            Log.d(TAG, "touched accept button");
        }
		
        try {
            persistData();
            
            // If Station Select is checked, the next stop for both Single or 
            // Continuous Mode is Station List
            if (systemParams.getEnableStationSelect()) {
                intent = StationListActivity.createIntent(this, observationType, observationNote);
                finish();                        // remove self from back stack
                startActivity(intent);
            } else {
                // If Station Select is not checked, the next stop for Single Observation is 
                // Observation, and for Continuous Mode is Cont Mode Params 
                try {
                    // Test
                    boolean test = false;
                    if (test) {
                        throw new SQLException("test");
                    }

                    station = stationDao.queryForDefaultUse();

                    if (observationType == ObservationType.SINGLE) {
                        intent = ObservationActivity.createIntent(this, observationType, 
                                observationNote, null, station);
                    } else {                    // Continuous Mode
                        intent = ContinousSetupActivity.createIntent(this, observationType, 
                                observationNote, null, station);
                    }
                    finish();                    // remove self from back stack
                    startActivity(intent);

                } catch (SQLException e) {
                    if (MyDebug.LOG) {
                        Log.e(TAG, "failed to load default", e);
                    }
                    ErrorHandler errorHandler = ErrorHandler.getInstance();
                    errorHandler.logError(Level.WARNING, "DialReadingActivity.onAcceptButtonClicked(): " +
                            "Could not open station DB - " + e, 
                            getString(R.string.station_file_read_error_title),
                            getString(R.string.station_file_read_error_message),
                            okOnClickListener);
                }
            }
        } catch (Exception e) {
            if (MyDebug.LOG) {
                Log.e(TAG, "persistData failed", e);
            }
            
            ErrorHandler errorHandler = ErrorHandler.getInstance();
            errorHandler.logError(Level.WARNING, "DialReadingActivity.onAcceptButtonClicked(): " +
                    "Persist failed - " + e,
                    getString(R.string.activity_fatal_persist_title),
                    getString(R.string.activity_fatal_persist_message),
                    okOnClickListener);
        }
	}

    MyOnClickListener okOnClickListener = new MyOnClickListener() {
        
        @Override
        public void onClick() {
            Intent intent = DashboardActivity.createIntent(context);
            finish();        // remove self from back stack
            startActivity(intent);
        }
    };

    /**
     * Saves value of Use Non-Calibrated Points checkbox and Dial Reading.
     */
	public void persistData() throws Exception {

	    systemParams.setUseNoncalibratedPoints(useNonCalibratedPointsCheckBox.isChecked());

	    if (validator.isValid(dialReadingText)) {
	        systemParams.setDialReading(Integer.parseInt(dialReadingText.getText().toString()));
	    } else {
	        throw new Exception("Bad validation.");
	    }

	    // Test
	    boolean test = false;
	    if (test) {
	        throw new SQLException("test");
	    }

	    systemParamsDao.createOrUpdate(systemParams);
	}

	/**
	 * Creates an Intent for this Activity.
	 * 
	 * @param callee
	 * @param observationType
	 * @return Intent
	 */
	public static Intent createIntent(Context callee, ObservationType observationType) {
		Intent answer = new Intent(callee, DialReadingActivity.class);
		answer.putExtra(EXTRA_OBSERVATION_TYPE, observationType);

		return answer;
	}
}
