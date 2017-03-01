package com.zlscorp.ultragrav.activity.fragment;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.logging.Level;

import roboguice.inject.InjectView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.LinearLayout;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

import com.google.inject.Inject;
import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.activity.AbstractBaseActivity;
import com.zlscorp.ultragrav.activity.FileSelectActivity;
import com.zlscorp.ultragrav.activity.PrivateActivity;
import com.zlscorp.ultragrav.activity.validate.Validator;
import com.zlscorp.ultragrav.activity.widget.NumberPadEditText;
import com.zlscorp.ultragrav.activity.widget.ListFragmentPagerAdapter.OnFragmentSelectedListener;
import com.zlscorp.ultragrav.debug.MyDebug;
import com.zlscorp.ultragrav.file.CalibrationImporter;
import com.zlscorp.ultragrav.file.ErrorHandler;
import com.zlscorp.ultragrav.file.CalibrationImporter.ImportResult;
import com.zlscorp.ultragrav.model.FactoryParameters;
import com.zlscorp.ultragrav.model.LevelCorrectionParams;
import com.zlscorp.ultragrav.model.MeterParams;
import com.zlscorp.ultragrav.persist.FactoryParametersDao;
import com.zlscorp.ultragrav.persist.LevelCorrectionParamsDao;
import com.zlscorp.ultragrav.persist.MeterParamsDao;
import com.zlscorp.ultragrav.model.SystemParams;
import com.zlscorp.ultragrav.persist.SystemParamsDao;

public class PrivateParamsFragment extends AbstractBaseFragment implements OnFragmentSelectedListener {

    private static final String TAG = "PrivateParamsFragment";

    @Inject
    private MeterParamsDao meterParamsDao;

    @Inject
    private SystemParamsDao systemParamsDao;

    @Inject
    private LevelCorrectionParamsDao levelCorrectionParamsDao;

    @Inject
    private FactoryParametersDao factoryParametersDao;

    @Inject
    private Validator validator;

	@Inject
	private CalibrationImporter calibrationImporter;

    @InjectView(R.id.dummyLinearLayout)
    private LinearLayout dummyLinearLayout;

	@InjectView(R.id.meterSerialText)
	private EditText meterSerialText;

	@InjectView(R.id.gainText)
	private NumberPadEditText gainText;

	@InjectView(R.id.stopBoostText)
	private EditText stopBoostText;

	@InjectView(R.id.boostFactorText)
	private EditText boostFactorText;

	@InjectView(R.id.temp0Text)
	private EditText temp0Text;

	@InjectView(R.id.tempAText)
	private EditText tempAText;

	@InjectView(R.id.tempBText)
	private EditText tempBText;

	@InjectView(R.id.tempCText)
	private EditText tempCText;

	@InjectView(R.id.calibratedCheckBox)
	private CheckBox calibratedCheckBox;

	@InjectView(R.id.platesReversedCheckBox)
	private CheckBox platesReversedCheckBox;

    @InjectView(R.id.saveFrequenciesCheckBox)
    private CheckBox saveFrequenciesCheckBox;

    @InjectView(R.id.testModeCheckBox)
    private CheckBox testModeCheckBox;

    @InjectView(R.id.loadCalibrationTableButton)
    private Button loadCalibrationTableButton;

    @InjectView(R.id.createFactoryParamsButton)
    private Button createFactoryParamsButton;

    @InjectView(R.id.cancelButton)
    private Button cancelButton;

    @InjectView(R.id.saveButton)
    private Button saveButton;

    boolean persistData;

    int lankind = 0;
    PrivateActivity parent;
    SharedPreferences pref;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (MyDebug.LOG) {
            Log.d(TAG, "inflating fragment Private Params");
        }
        parent = (PrivateActivity) getActivity();
		View v = inflater.inflate(R.layout.fragment_private_params, container, false);
		pref = getActivity().getSharedPreferences("language", 0);
		lankind = pref.getInt("lan", 0);
		return v;
	}

	String convert(String str)
	{
		if (lankind == 1) 
			return str.replace(',', '.');
		else  
			return str;
	}
	@Override
	public void setupView(View view, Bundle savedInstanceState) {

        AbstractBaseActivity.fragmentName = this.getClass().getSimpleName();

        setupButtons();
		validator.validateString(meterSerialText);
		validator.validateAsDoubleSciNot(gainText);
		validator.validateAsInteger(stopBoostText);
		validator.validateAsDouble(boostFactorText);
		validator.validateAsInteger(temp0Text);
		validator.validateAsDouble(tempAText);
		validator.validateAsDouble(tempBText);
		validator.validateAsDouble(tempCText);
		
        setupNumberPadKeypad(gainText, getString(R.string.gain), dummyLinearLayout, true, true);

        setOnFocusChangedListeners(stopBoostText);
        setOnFocusChangedListeners(boostFactorText);
        setOnFocusChangedListeners(temp0Text);
        setOnFocusChangedListeners(tempAText);
        setOnFocusChangedListeners(tempBText);
        setOnFocusChangedListeners(tempCText);

        persistData = true;
        saveButton.setVisibility(View.INVISIBLE);     // This button is just used for visual spacing
        cancelButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (MyDebug.LOG) {
                    Log.d(TAG,"PersistData False");
                }
                persistData = false;
                getActivity().finish();
            }
        });
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

    @Override
    public void onFragmentSelected() {
        if (MyDebug.LOG) {
            Log.d(TAG, "Tab selected");
        }
        AbstractBaseActivity.fragmentName = this.getClass().getSimpleName();
        
        // This keeps any real element from getting focus.
        dummyLinearLayout.requestFocus();
        dummyLLHasFocus = false;
    }

    @Override
    public void onFragmentUnselected() {
        if (MyDebug.LOG) {
            Log.d(TAG, "Tab unselected");
        }
        
        // This keeps any real element from getting focus.
        dummyLinearLayout.requestFocus();
        dummyLLHasFocus = true;

        // Hide the soft keyboard before changing tabs. 
        InputMethodManager imm = (InputMethodManager) getActivity().
                getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

	private void setupButtons() {
	    
	    platesReversedCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MeterParams meterParams = null;
                Double gain = null;
                Double beamScale;
                String gainStr = gainText.getText().toString();

                try {
                    meterParams = meterParamsDao.queryForDefault();
                } catch (SQLException e) {
                    ErrorHandler errorHandler = ErrorHandler.getInstance();
                    errorHandler.logError(Level.WARNING, "PrivateParamsFragment.setupButtons$" +
                    		"platesReversedCheckBox.setOnCheckedChangeListener$OnCheckedChangeListener." +
                    		"onCheckedChange(): Can't open meterParams - " + e,
                            R.string.meter_params_file_open_error_title,
                            R.string.meter_params_file_open_error_message);
                }

                beamScale = meterParams.getBeamScale();

                try {
                    gain = Double.parseDouble(gainStr);
                    if (platesReversedCheckBox.isChecked()) {
                        if (gain > 0) {
                            gain = -gain;
                            gainText.setText(convert(new DecimalFormat("0.#########E0").format(gain)));
                        }
                    } else {
                        if (gain < 0) {
                            gain = -gain;
                            gainText.setText(convert(new DecimalFormat("0.#########E0").format(gain)));
                        }
                    }
                } catch (NumberFormatException e) {
                    if (MyDebug.LOG) {
                        Log.e(TAG, "Beam value not a double", e);
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.bad_gain_value_title);
                    builder.setMessage(R.string.bad_gain_value_message);
                    builder.setPositiveButton(R.string.ok, null);
                    builder.show();
                }

                if (platesReversedCheckBox.isChecked()) {
                    if ((beamScale != null) && (beamScale < 0)) {
                        meterParams.setBeamScale(-beamScale);
                    }
                } else {
                    if ((beamScale != null) && (beamScale > 0)) {
                        meterParams.setBeamScale(-beamScale);
                    }
                }

                try {
                    meterParamsDao.update(meterParams);
                } catch (SQLException e) {
                    ErrorHandler errorHandler = ErrorHandler.getInstance();
                    errorHandler.logError(Level.WARNING, "PrivateParamsFragment.setupButtons$" +
                            "platesReversedCheckBox.setOnCheckedChangeListener$OnCheckedChangeListener." +
                            "onCheckedChange(): Can't update meterParams - " + e,
                            R.string.meter_params_file_update_error_title,
                            R.string.meter_params_file_update_error_message);
                }
            }
        });

        loadCalibrationTableButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onLoadCalibrationTableClicked();
			}
		});
        
        createFactoryParamsButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                onCreateFactoryParamsClicked();
            }
        });
	}

    public void onCreateFactoryParamsClicked() {
        SystemParams systemParams = null;
        MeterParams meterParams = null;
        LevelCorrectionParams levelCorrectionParams = null;
        FactoryParameters factoryParameters = null;

        try {
            meterParams = meterParamsDao.queryForDefault();
            systemParams = systemParamsDao.queryForDefault();
            levelCorrectionParams = levelCorrectionParamsDao.queryForDefault();
            factoryParameters = factoryParametersDao.queryForDefault();
        } catch (SQLException e) {
            ErrorHandler errorHandler = ErrorHandler.getInstance();
            errorHandler.logError(Level.WARNING, "PrivateParamsFragment.onCreateFactoryParamsClicked(): " +
                    "Can't open either meterParams, systemParams, levelCorrectionParams, factoryParameters - " + e,
                    R.string.params_file_update_error_title,
                    R.string.params_file_update_error_message);
        }

        if (systemParams != null && meterParams != null && levelCorrectionParams != null && 
                factoryParameters != null) {

            if (Validator.isValidDouble(systemParams.getFeedbackGain(), SystemParams.FEEDBACK_GAIN_MIN,
                    SystemParams.FEEDBACK_GAIN_MAX)) {
                factoryParameters.setFeedbackGain(systemParams.getFeedbackGain());
            } else {
                displayNullValueAlert();
            }
            
            if (Validator.isValidDouble(systemParams.getObservationPrecision(), SystemParams.OBS_PRECISION_MIN,
                    SystemParams.OBS_PRECISION_MAX)) {
                factoryParameters.setObservationPrecision(systemParams.getObservationPrecision());
            } else {
                displayNullValueAlert();
            }
            
            if (Validator.isValidInteger(systemParams.getDataOutputRate(), SystemParams.DATA_OUTPUT_RATE_MIN)) {
                factoryParameters.setDataOutputRate(systemParams.getDataOutputRate());
            } else {
                displayNullValueAlert();
            }
            
            if (Validator.isValidInteger(systemParams.getFilterTimeConstant(), SystemParams.FILTER_TIME_CONST_MIN)) {
                factoryParameters.setFilterTimeConstant(systemParams.getFilterTimeConstant());
            } else {
                displayNullValueAlert();
            }
            
            // Meter Params
            if (Validator.isValidInteger(meterParams.getReadingLine())) {
                factoryParameters.setReadingLine(meterParams.getReadingLine());
            } else {
                displayNullValueAlert();
            }
            
            if (Validator.isValidDouble(meterParams.getBeamScale())) {
                factoryParameters.setBeamScale(meterParams.getBeamScale());
            } else {
                displayNullValueAlert();
            }
            
            if (Validator.isValidDouble(meterParams.getFeedbackScale())) {
                factoryParameters.setFeedbackScale(meterParams.getFeedbackScale());
            } else {
                displayNullValueAlert();
            }
            
            if (Validator.isValidInteger(meterParams.getMinStop())) {
                factoryParameters.setMinStop(meterParams.getMinStop());
            } else {
                displayNullValueAlert();
            }
            
            if (Validator.isValidInteger(meterParams.getMaxStop())) {
                factoryParameters.setMaxStop(meterParams.getMaxStop());
            } else {
                displayNullValueAlert();
            }
            
            // Long Level Params
            if (Validator.isValidInteger(levelCorrectionParams.getLongLevel0())) {
                factoryParameters.setLongLevel0(levelCorrectionParams.getLongLevel0());
            } else {
                displayNullValueAlert();
            }
            
            if (Validator.isValidDouble(levelCorrectionParams.getLongLevelA())) {
                factoryParameters.setLongLevelA(levelCorrectionParams.getLongLevelA());
            } else {
                displayNullValueAlert();
            }
            
            if (Validator.isValidDouble(levelCorrectionParams.getLongLevelB())) {
                factoryParameters.setLongLevelB(levelCorrectionParams.getLongLevelB());
            } else {
                displayNullValueAlert();
            }
            
            if (Validator.isValidDouble(levelCorrectionParams.getLongLevelC())) {
                factoryParameters.setLongLevelC(levelCorrectionParams.getLongLevelC());
            } else {
                displayNullValueAlert();
            }
            
            // Cross Level Params
            if (Validator.isValidInteger(levelCorrectionParams.getCrossLevel0())) {
                factoryParameters.setCrossLevel0(levelCorrectionParams.getCrossLevel0());
            } else {
                displayNullValueAlert();
            }
            
            if (Validator.isValidDouble(levelCorrectionParams.getCrossLevelA())) {
                factoryParameters.setCrossLevelA(levelCorrectionParams.getCrossLevelA());
            } else {
                displayNullValueAlert();
            }
            
            if (Validator.isValidDouble(levelCorrectionParams.getCrossLevelB())) {
                factoryParameters.setCrossLevelB(levelCorrectionParams.getCrossLevelB());
            } else {
                displayNullValueAlert();
            }
            
            if (Validator.isValidDouble(levelCorrectionParams.getCrossLevelC())) {
                factoryParameters.setCrossLevelC(levelCorrectionParams.getCrossLevelC());
            } else {
                displayNullValueAlert();
            }
            
            // Set flag to indicate that the file has been created.
            factoryParameters.setFileCreated(true);        

            try {
                factoryParametersDao.update(factoryParameters);
                Toast.makeText(getActivity(), getString(R.string.factory_parameter_file_created), Toast.LENGTH_LONG).show();
            } catch (SQLException e) {
                ErrorHandler errorHandler = ErrorHandler.getInstance();
                errorHandler.logError(Level.WARNING, "PrivateParamsFragment.onCreateFactoryParamsClicked(): " +
                        "Can't update factoryParameters - " + e,
                        R.string.factory_params_file_create_error_title,
                        R.string.factory_params_file_create_error_message);
            }
        }
    }

    private void displayNullValueAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.factory_param_null_error_title);
        builder.setMessage(R.string.factory_param_null_error_message);
        builder.setPositiveButton(R.string.ok, null);
        builder.show();
    }
    
    public void onLoadCalibrationTableClicked() {

        Intent intent = FileSelectActivity.createIntent(getActivity(), false);
        startActivityForResult(intent, REQUEST_FILE_SELECT);
    }

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_FILE_SELECT:
			if (resultCode == Activity.RESULT_OK) {
				doCalibrationImport(data.getExtras().getString(FileSelectFragment.PATH_EXTRA));
			}
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void populateData() {
        MeterParams meterParams = null;
        SystemParams systemParams = null;

        try {
            meterParams = meterParamsDao.queryForDefault();
            systemParams = systemParamsDao.queryForDefault();
		} catch (SQLException e) {
            ErrorHandler errorHandler = ErrorHandler.getInstance();
            errorHandler.logError(Level.WARNING, "PrivateParamsFragment.populateData(): " +
                    "Can't open either meterParams or systemParams - " + e,
                    R.string.params_file_open_error_title,
                    R.string.params_file_open_error_message);
		}

		if (meterParams != null) {
			if (meterParams.getSerialNumber() != null) {
				meterSerialText.setText(convert(meterParams.getSerialNumber()));
				
	            if (AbstractBaseActivity.fragmentName != null && 
	                    AbstractBaseActivity.fragmentName.equals(this.getClass().getSimpleName())) {
	                // This keeps any real element from getting focus.
	                dummyLinearLayout.requestFocus();
	                
	                // position cursor at right end of line
//	                meterSerialText.setSelection(meterSerialText.getText().toString().length());
//	                meterSerialText.requestFocus();
		        }
			}
			if (meterParams.getGain() != null) {
				gainText.setText(convert(new DecimalFormat("0.#########E0").format(meterParams.getGain())));
			}
			if (meterParams.getStopBoost() != null) {
				stopBoostText.setText(convert(meterParams.getStopBoost().toString()));
			}
			if (meterParams.getBoostFactor() != null) {
				boostFactorText.setText(convert(meterParams.getBoostFactor().toString()));
			}
			if (meterParams.getTemperature0() != null) {
				temp0Text.setText(convert(meterParams.getTemperature0().toString()));
			}
			if (meterParams.getTemperatureA() != null) {
			    tempAText.setText(convert(new DecimalFormat("0.00000").format(meterParams.getTemperatureA())));
			}
			if (meterParams.getTemperatureB() != null) {
                tempBText.setText(convert(new DecimalFormat("0.00000").format(meterParams.getTemperatureB())));
			}
			if (meterParams.getTemperatureC() != null) {
                tempCText.setText(convert(new DecimalFormat("0.00000").format(meterParams.getTemperatureC())));
			}
			if (meterParams.isCalibrated() != null) {
				calibratedCheckBox.setChecked(meterParams.isCalibrated());
			}
            if (meterParams.getPlatesReversed() != null) {
                platesReversedCheckBox.setChecked(meterParams.getPlatesReversed());
            }
		}

        if (systemParams != null) {
            if (systemParams.isSaveFrequencies() != null) {
                saveFrequenciesCheckBox.setChecked(systemParams.isSaveFrequencies());
            }
            if (systemParams.isTestMode() != null) {
                testModeCheckBox.setChecked(systemParams.isTestMode());
            }
        }
		
		validator.validateAll();
	}

	@Override
	public void persistData() {
		MeterParams meterParams = null;
        SystemParams systemParams = null;

        if (persistData) {
            if (MyDebug.LOG) {
                Log.d(TAG,"Persisting Data");
            }
            try {
                meterParams = meterParamsDao.queryForDefault();
                systemParams = systemParamsDao.queryForDefault();
            } catch (SQLException e) {
                ErrorHandler errorHandler = ErrorHandler.getInstance();
                errorHandler.logError(Level.WARNING, "PrivateParamsFragment.persistData(): " +
                        "Can't open either meterParams or systemParams - " + e,
                        R.string.params_file_open_error_title,
                        R.string.params_file_open_error_message);
            }

            if (meterParams != null) {
                if (validator.isValid(meterSerialText)) {
                    meterParams.setSerialNumber(meterSerialText.getText().toString());
                }
                if (validator.isValid(gainText)) {
                    meterParams.setGain(Double.parseDouble(gainText.getText().toString()));
                }
                if (validator.isValid(stopBoostText)) {
                    meterParams.setStopBoost(Integer.parseInt(stopBoostText.getText().toString()));
                }
                if (validator.isValid(boostFactorText)) {
                    meterParams.setBoostFactor(Double.parseDouble(boostFactorText.getText().toString()));
                }
                if (validator.isValid(temp0Text)) {
                    meterParams.setTemperature0(Integer.parseInt(temp0Text.getText().toString()));
                }
                if (validator.isValid(tempAText)) {
                    meterParams.setTemperatureA(Double.parseDouble(tempAText.getText().toString()));
                }
                if (validator.isValid(tempBText)) {
                    meterParams.setTemperatureB(Double.parseDouble(tempBText.getText().toString()));
                }
                if (validator.isValid(tempCText)) {
                    meterParams.setTemperatureC(Double.parseDouble(tempCText.getText().toString()));
                }
                meterParams.setCalibrated(calibratedCheckBox.isChecked());
                meterParams.setPlatesReversed(platesReversedCheckBox.isChecked());

                try {
                    meterParamsDao.update(meterParams);
                } catch (SQLException e) {
                    ErrorHandler errorHandler = ErrorHandler.getInstance();
                    errorHandler.logError(Level.WARNING, "PrivateParamsFragment.persistData(): " +
                            "Can't update meterParams - " + e,
                            R.string.meter_params_file_update_error_title,
                            R.string.meter_params_file_update_error_message);
                }
            }

            if (systemParams != null) {
                if (systemParams.isSaveFrequencies() != null) {
                    systemParams.setSaveFrequencies(saveFrequenciesCheckBox.isChecked());
                }
                if (systemParams.isTestMode() != null) {
                    systemParams.setTestMode(testModeCheckBox.isChecked());
                }
                try {
                    systemParamsDao.update(systemParams);
                } catch (SQLException e) {
                    ErrorHandler errorHandler = ErrorHandler.getInstance();
                    errorHandler.logError(Level.WARNING, "PrivateParamsFragment.persistData(): " +
                            "Can't update systemParams - " + e,
                            R.string.system_params_file_update_error_title,
                            R.string.system_params_file_update_error_message);
                }
            }
        }
	}

	private void doCalibrationImport(String path) {

		MyCalibrationImportTask task = new MyCalibrationImportTask(path);
		task.execute();
	}

	private class MyCalibrationImportTask extends AsyncTask<Void, Void, ImportResult> {

		private String path;

		public MyCalibrationImportTask(String path) {
			super();
			this.path = path;
		}

		@Override
		protected ImportResult doInBackground(Void... params) {

			return calibrationImporter.importCalibration(path);
		}

		@Override
		protected void onPostExecute(ImportResult result) {

			if (result.isSuccess()) {
                Toast.makeText(getActivity(), getString(R.string.calibration_import_complete), Toast.LENGTH_LONG).show();
			} else {
	            ErrorHandler errorHandler = ErrorHandler.getInstance();
                errorHandler.logError(Level.WARNING, "PrivateParamsFragment$MyCalibrationImportTask.onPostExecute(): " +
                        "Calibration table import error - " + result.getErrorMessage(),
                        R.string.calibration_table_import_error_title,
                        result.getErrorMessage());
			}
		}
	}
}
