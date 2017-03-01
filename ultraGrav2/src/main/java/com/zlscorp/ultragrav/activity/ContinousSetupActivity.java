package com.zlscorp.ultragrav.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.sql.SQLException;
import java.util.logging.Level;

import com.google.inject.Inject;
import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.activity.validate.Validator;
import com.zlscorp.ultragrav.activity.validate.Validator.ValidationListener;
import com.zlscorp.ultragrav.debug.MyDebug;
import com.zlscorp.ultragrav.file.ErrorHandler;
import com.zlscorp.ultragrav.model.Station;
import com.zlscorp.ultragrav.model.StationSeries;
import com.zlscorp.ultragrav.model.SystemParams;
import com.zlscorp.ultragrav.persist.SystemParamsDao;
import com.zlscorp.ultragrav.type.ObservationType;

import roboguice.inject.InjectView;

public class ContinousSetupActivity extends AbstractBaseActivity {

    private static String TAG = "ContinousSetupActivity";

    @Inject
    private SystemParamsDao systemParamsDao;
    
    @Inject
    private Validator validator;

    private SystemParams systemParams;

    private String observationNote;
    private StationSeries stationSeries;
    private Station station;
    
    @InjectView(R.id.dummyLinearLayout)
    private LinearLayout dummyLinearLayout;

    @InjectView(R.id.dataOutputRateText)
    private EditText dataOutputRateText;

    @InjectView(R.id.filterTimeConstantText)
    private EditText filterTimeConstantText;

    @InjectView(R.id.acceptButton)
    private Button acceptButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_continous_setup);
        getSupportActionBar().setTitle(getString(R.string.title_activity_continous_setup));
        kind = 1;
        // unpack the intent extras
        observationNote = (String) getIntent().getExtras().getString(EXTRA_OBSERVATION_NOTE);
        stationSeries = (StationSeries) getIntent().getExtras().get(EXTRA_STATION_SERIES);
        station = (Station) getIntent().getExtras().get(EXTRA_STATION);

        try {
            systemParams = systemParamsDao.queryForDefault();
            
            // Test
            boolean test = false;
            if (test) {
                throw new SQLException("test");
            }
            
            if (systemParams.getDataOutputRate() != null) {
                dataOutputRateText.setText(systemParams.getDataOutputRate().toString());
            }
            if (systemParams.getFilterTimeConstant() != null) {
                filterTimeConstantText.setText(systemParams.getFilterTimeConstant().toString());
            }
            
            updateContinuousParamsValidator();
            
            validator.addValidationListener(new ValidationListener() {
                public void onValidation(Object input, boolean inputValid, boolean allInputsValid) {
                    acceptButton.setEnabled(allInputsValid);
                }
            });
            
            validator.validateAll();
            
            // position cursor at right end of line
            dataOutputRateText.setSelection(dataOutputRateText.getText().toString().length());   

        } catch (SQLException e) {
            if (MyDebug.LOG) {
                Log.d(TAG, "SystemParams query failed.");
            }
            ErrorHandler errorHandler = ErrorHandler.getInstance();
            errorHandler.logError(Level.WARNING, "ContinuousSetupActivity.onCreate(): " +
            		"Can't open systemParams " + e,
            		R.string.system_params_file_open_error_title,
            		R.string.system_params_file_open_error_message);

            setResult(RESULT_CANCELED);
            finish();
        }

        hasFragments = false;
        
        // This keeps any real element from getting focus.
        dummyLinearLayout.requestFocus();
        
        setOnFocusChangedListeners(filterTimeConstantText);
    }
    
    private void setOnFocusChangedListeners(final EditText editText) {

        editText.setOnFocusChangeListener(new OnFocusChangeListener() {          
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && editText.getText().toString() != null) {
                    // position cursor at right end of line
                    editText.setSelection(editText.getText().toString().length());   
                }
            }
        });
    }

    private void updateContinuousParamsValidator() {

        validator.validateAsInteger(dataOutputRateText, SystemParams.DATA_OUTPUT_RATE_MIN);
        validator.validateAsInteger(filterTimeConstantText, SystemParams.FILTER_TIME_CONST_MIN);
        
        validator.validateAll();
    }

	@Override
	public String getHelpKey() {
		return "continousSetup";
	}
    
	public void onAcceptButtonClicked(View view) {

	    try {
	        persistData();
	        Intent intent = ObservationActivity.createIntent(this, ObservationType.CONTINUOUS, 
	                observationNote, stationSeries, station);

	        startActivityForResult(intent, IntentParams.REQUEST_BEGIN_OBSERVATION);
	    } catch (Exception e) {
            if (MyDebug.LOG) {
                Log.e(TAG, "persistData failed", e);
            }

	        ErrorHandler errorHandler = ErrorHandler.getInstance();
	        errorHandler.logError(Level.WARNING, "ContinuousSetupActivity.onAcceptButtonClicked(): " +
	                "Persist failed - " + e,
	                getString(R.string.activity_fatal_persist_title),
	                getString(R.string.activity_fatal_persist_message),
	                okOnClickListener);
	    }
	}

    MyOnClickListener okOnClickListener = new MyOnClickListener() {
        
        @Override
        public void onClick() {
            setResult(RESULT_CANCELED);
            finish();
        }
    };

    public void persistData() throws Exception {

        if (validator.isValid(dataOutputRateText)) {
            systemParams.setDataOutputRate(Integer.parseInt(dataOutputRateText.getText().toString()));
        }

        if (validator.isValid(filterTimeConstantText)) {
            systemParams.setFilterTimeConstant(Integer.parseInt(filterTimeConstantText.getText().toString()));
        }

        // Test
        boolean test = false;
        if (test) {
            throw new SQLException("test");
        }

        systemParamsDao.createOrUpdate(systemParams);
    }

	@Override
	public String getActivityName() {
        return TAG;
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_BEGIN_OBSERVATION) {
            if (resultCode == RESULT_FINISH_OBSERVATION || resultCode == RESULT_CANCELED) {
                setResult(RESULT_FINISH_OBSERVATION);
                finish();
            }
        }
    }

    /**
     * Creates an Intent for this Activity.
     * @param callee
     * @return Intent
     */
    public static Intent createIntent(Context callee, ObservationType observationType, String observationNote, 
            StationSeries stationSeries, Station station) {
        Intent answer = new Intent(callee, ContinousSetupActivity.class);
        answer.putExtra(EXTRA_OBSERVATION_TYPE, observationType);
        answer.putExtra(EXTRA_OBSERVATION_NOTE, observationNote);
        answer.putExtra(EXTRA_STATION, station);
        return answer;
    }
}
