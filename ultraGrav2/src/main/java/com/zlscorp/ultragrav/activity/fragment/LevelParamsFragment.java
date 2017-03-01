package com.zlscorp.ultragrav.activity.fragment;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.logging.Level;

import roboguice.inject.InjectView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.inject.Inject;
import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.activity.AbstractBaseActivity;
import com.zlscorp.ultragrav.activity.validate.Validator;
import com.zlscorp.ultragrav.activity.widget.ListFragmentPagerAdapter.OnFragmentSelectedListener;
import com.zlscorp.ultragrav.activity.widget.NumberPadEditText;
import com.zlscorp.ultragrav.debug.MyDebug;
import com.zlscorp.ultragrav.file.ErrorHandler;
import com.zlscorp.ultragrav.model.LevelCorrectionParams;
import com.zlscorp.ultragrav.persist.LevelCorrectionParamsDao;

public class LevelParamsFragment extends AbstractBaseFragment implements OnFragmentSelectedListener {
	
    private static String TAG = "LevelParamsFragment";

	@Inject
	private LevelCorrectionParamsDao levelCorrectionParamsDao;
	
	@Inject
	private Validator validator;
	
    @InjectView(R.id.dummyLinearLayout)
    private LinearLayout dummyLinearLayout;

	@InjectView(R.id.longLevel0Text)
	private EditText longLevel0Text;
	
	@InjectView(R.id.longLevelAText)
	private NumberPadEditText longLevelAText;
	
	@InjectView(R.id.longLevelBText)
	private NumberPadEditText longLevelBText;
	
	@InjectView(R.id.longLevelCText)
	private NumberPadEditText longLevelCText;
	
	@InjectView(R.id.crossLevel0Text)
	private EditText crossLevel0Text;
	
	@InjectView(R.id.crossLevelAText)
	private NumberPadEditText crossLevelAText;
	
	@InjectView(R.id.crossLevelBText)
	private NumberPadEditText crossLevelBText;
	
	@InjectView(R.id.crossLevelCText)
	private NumberPadEditText crossLevelCText;
	
    @InjectView(R.id.cancelButton)
    private Button cancelButton;

    @InjectView(R.id.saveButton)
    private Button saveButton;

	private LevelCorrectionParams levelCorrectionParams;
    private boolean persistData;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View v = inflater.inflate(R.layout.fragment_level_params, container, false);
    	return v;
    }
    
    @Override
    public void setupView(View view, Bundle savedInstanceState) {
    	
        validator.validateAsInteger(longLevel0Text);
    	validator.validateAsDoubleSciNot(longLevelAText);
    	validator.validateAsDoubleSciNot(longLevelBText);
    	validator.validateAsDoubleSciNot(longLevelCText);
    	validator.validateAsInteger(crossLevel0Text);
    	validator.validateAsDoubleSciNot(crossLevelAText);
    	validator.validateAsDoubleSciNot(crossLevelBText);
    	validator.validateAsDoubleSciNot(crossLevelCText);
    	
    	setupNumberPadKeypad(longLevelAText, getString(R.string.long_level_a), dummyLinearLayout, 
    	        false, false);
    	setupNumberPadKeypad(longLevelBText, getString(R.string.long_level_b), dummyLinearLayout, 
    	        false, false);
    	setupNumberPadKeypad(longLevelCText, getString(R.string.long_level_c), dummyLinearLayout, 
    	        true, false);
    	setupNumberPadKeypad(crossLevelAText, getString(R.string.cross_level_a), dummyLinearLayout, 
    	        false, false);
    	setupNumberPadKeypad(crossLevelBText, getString(R.string.cross_level_b), dummyLinearLayout, 
    	        false, false);
    	setupNumberPadKeypad(crossLevelCText, getString(R.string.cross_level_c), dummyLinearLayout, 
    	        false, true);
    	
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
    
	@Override
	public void populateData() {
		try {
	    	levelCorrectionParams = levelCorrectionParamsDao.queryForDefault();
    	} catch(SQLException e) {
            ErrorHandler errorHandler = ErrorHandler.getInstance();
            errorHandler.logError(Level.WARNING, "LevelParamsFragment.populateData(): " +
                    "Can't open levelCorrectionParams - " + e,
                    R.string.level_correction_params_file_open_error_title,
                    R.string.level_correction_params_file_open_error_message);
    	}
    	
    	if (levelCorrectionParams == null) {
    		levelCorrectionParams = new LevelCorrectionParams();
    		levelCorrectionParams.setDefaultUse(true);
    	}
    	
    	if (levelCorrectionParams.getLongLevel0() != null) {
            longLevel0Text.setText(levelCorrectionParams.getLongLevel0().toString());
    	}
    	if (levelCorrectionParams.getLongLevelA() != null) {
    		longLevelAText.setText(new DecimalFormat("0.#########E0").format(levelCorrectionParams.getLongLevelA()));
    	}
    	if (levelCorrectionParams.getLongLevelB() != null) {
            longLevelBText.setText(new DecimalFormat("0.#########E0").format(levelCorrectionParams.getLongLevelB()));
    	}
    	if (levelCorrectionParams.getLongLevelC() != null) {
            longLevelCText.setText(new DecimalFormat("0.#########E0").format(levelCorrectionParams.getLongLevelC()));
    	}
    	if (levelCorrectionParams.getCrossLevel0() != null) {
    		crossLevel0Text.setText(levelCorrectionParams.getCrossLevel0().toString());
    	}
    	if (levelCorrectionParams.getCrossLevelA() != null) {
    		crossLevelAText.setText(new DecimalFormat("0.#########E0").format(levelCorrectionParams.getCrossLevelA()));
    	}
    	if (levelCorrectionParams.getCrossLevelB() != null) {
            crossLevelBText.setText(new DecimalFormat("0.#########E0").format(levelCorrectionParams.getCrossLevelB()));
    	}
    	if (levelCorrectionParams.getCrossLevelC() != null) {
            crossLevelCText.setText(new DecimalFormat("0.#########E0").format(levelCorrectionParams.getCrossLevelC()));
    	}
    	
    	validator.validateAll();
	}
	
	@Override
	public void persistData() {
	    if (persistData) {                                  // If Cancel button was not tapped
	        try {
	            levelCorrectionParams = levelCorrectionParamsDao.queryForDefault();
                // Test
//	            throw new SQLException("test");
	        } catch(SQLException e) {
	            ErrorHandler errorHandler = ErrorHandler.getInstance();
	            errorHandler.logError(Level.WARNING, "LevelParamsFragment.persistData(): " +
	                    "Can't open levelCorrectionParams - " + e,
	                    R.string.level_correction_params_file_open_error_title,
	                    R.string.level_correction_params_file_open_error_message);
	        }
	        if (!(levelCorrectionParams.justRestored())) {  // If we didn't just restore data from the factory parameter file
	            if (MyDebug.LOG) {
	                Log.d(TAG,"Persisting Data");
	            }
	            if (validator.isValid(longLevel0Text)) {
	                levelCorrectionParams.setLongLevel0(Integer.parseInt(longLevel0Text.getText().toString()));
	            }
	            if (validator.isValid(longLevelAText)) {
	                levelCorrectionParams.setLongLevelA(Double.parseDouble(longLevelAText.getText().toString()));
	            }
	            if (validator.isValid(longLevelBText)) {
	                levelCorrectionParams.setLongLevelB(Double.parseDouble(longLevelBText.getText().toString()));
	            }
	            if (validator.isValid(longLevelCText)) {
	                levelCorrectionParams.setLongLevelC(Double.parseDouble(longLevelCText.getText().toString()));
	            }
	            if (validator.isValid(crossLevel0Text)) {
	                levelCorrectionParams.setCrossLevel0(Integer.parseInt(crossLevel0Text.getText().toString()));
	            }
	            if (validator.isValid(crossLevelAText)) {
	                levelCorrectionParams.setCrossLevelA(Double.parseDouble(crossLevelAText.getText().toString()));
	            }
	            if (validator.isValid(crossLevelBText)) {
	                levelCorrectionParams.setCrossLevelB(Double.parseDouble(crossLevelBText.getText().toString()));
	            }
	            if (validator.isValid(crossLevelCText)) {
	                levelCorrectionParams.setCrossLevelC(Double.parseDouble(crossLevelCText.getText().toString()));
	            }

	        } else {
	            levelCorrectionParams.setJustRestored(false);
	        }
	        try{
	            levelCorrectionParamsDao.createOrUpdate(levelCorrectionParams);
                // Test
//                throw new SQLException("test");
	        } catch(SQLException e) {
	            ErrorHandler errorHandler = ErrorHandler.getInstance();
                errorHandler.logError(Level.WARNING, "LevelParamsFragment.persistData(): " +
                        "Can't update levelCorrectionParams - " + e,
                        R.string.level_correction_params_file_update_error_title,
                        R.string.level_correction_params_file_update_error_message);
	        }
	    }
	}

    @Override
    public void onFragmentSelected() {
        AbstractBaseActivity.fragmentName = this.getClass().getSimpleName();

        try {
            levelCorrectionParams = levelCorrectionParamsDao.queryForDefault();
        } catch(SQLException e) {
            ErrorHandler errorHandler = ErrorHandler.getInstance();
            errorHandler.logError(Level.WARNING, "LevelParamsFragment.onFragmentSelected(): " +
                    "Can't open levelCorrectionParams - " + e,
                    R.string.level_correction_params_file_open_error_title,
                    R.string.level_correction_params_file_open_error_message);
        }
        // If we just restored data from the factory parameter file
        // populate the fields with the freshly restored params
        if (levelCorrectionParams.justRestored()) {    
            levelCorrectionParams.setJustRestored(false);
            try{
                levelCorrectionParamsDao.createOrUpdate(levelCorrectionParams);
            } catch(SQLException e) {
                ErrorHandler errorHandler = ErrorHandler.getInstance();
                errorHandler.logError(Level.WARNING, "LevelParamsFragment.onFragmentSelected(): " +
                        "Can't update levelCorrectionParams - " + e,
                        R.string.level_correction_params_file_update_error_title,
                        R.string.level_correction_params_file_update_error_message);
            }
            populateData();
        }
        
        // This keeps any real element from getting focus.
        dummyLinearLayout.requestFocus();
        dummyLLHasFocus = false;
    }

    @Override
    public void onFragmentUnselected() {
        // This keeps any real element from getting focus.
        dummyLinearLayout.requestFocus();
        dummyLLHasFocus = true;

        // Hide the soft keyboard before changing tabs. 
        InputMethodManager imm = (InputMethodManager) getActivity().
                getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }
}
