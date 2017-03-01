package com.zlscorp.ultragrav.activity.fragment;

import java.sql.SQLException;

import roboguice.inject.InjectView;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

import com.google.inject.Inject;
import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.activity.AbstractBaseActivity;
import com.zlscorp.ultragrav.activity.validate.Validator;
import com.zlscorp.ultragrav.activity.validate.Validator.ValidationListener;
import com.zlscorp.ultragrav.model.StationSeries;
import com.zlscorp.ultragrav.persist.StationSeriesDao;

public class StationSeriesEditFragment extends AbstractBaseFragment {

//	private static String TAG = "StationSeriesEditFragment";
	
	@Inject
	private StationSeriesDao stationSeriesDao;
	
	@Inject
	private Validator validator;
	
	@InjectView(R.id.stationSeriesNameText)
	private EditText stationSeriesNameText;
	
	@InjectView(R.id.seriesAutoIncrementCheckBox)
	private CheckBox seriesAutoIncrementCheckBox;
	
	@InjectView(R.id.seriesStartingNumberCheckBox)
	private CheckBox seriesStartingNumberCheckBox;
	
	@InjectView(R.id.seriesStartingNumberText)
	private EditText seriesStartingNumberText;
	
	@InjectView(R.id.seriesCopyCheckBox)
	private CheckBox seriesCopyCheckBox;
	
	@InjectView(R.id.saveButton)
	private Button saveButton;
	
	@InjectView(R.id.cancelButton)
	private Button cancelButton;
	
	private boolean isCreate;
	private StationSeries series;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_station_series_edit, container, false);
        return v;
    }
	
	@Override
	public void setupView(View view, Bundle savedInstanceState) {

        AbstractBaseActivity.fragmentName = this.getClass().getSimpleName();

        Bundle extras = getActivity().getIntent().getExtras();
        series = (StationSeries)extras.get(EXTRA_STATION_SERIES);
        
        saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onSaveClicked(v);
			}
		});
        
        cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onCancelClicked(v);
			}
		});
        
        seriesStartingNumberCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				updateStartingNumberValidation();
			}
		});
        
        validator.validateString(stationSeriesNameText);
        
        validator.addValidationListener(new MyValidationListener());
	}
	
	private void updateStartingNumberValidation() {
		
		if(seriesStartingNumberCheckBox.isChecked()) {
			validator.validateAsInteger(seriesStartingNumberText);
			seriesStartingNumberText.setEnabled(true);
		} else {
			validator.remove(seriesStartingNumberText);
			seriesStartingNumberText.setError(null);
			seriesStartingNumberText.setText(null);
			seriesStartingNumberText.setEnabled(false);
		}
		
		validator.validateAll();
	}
	
	@Override
	public void populateData() {
		if (series==null) {
        	series = new StationSeries();
        	isCreate = true;
        	saveButton.setText(getString(R.string.create));
        	saveButton.setEnabled(false);
        } else {
        	isCreate = false;
        	saveButton.setEnabled(true);
        }
        
        stationSeriesNameText.setText(series.getName());
        if (series.getAutoIncrement()==null || series.getAutoIncrement()) {
        	seriesAutoIncrementCheckBox.setChecked(true);
        } else {
        	seriesAutoIncrementCheckBox.setChecked(false);
        }
        if (series.getStartingNumber() == null) {
        	seriesStartingNumberCheckBox.setChecked(false);
        	seriesStartingNumberText.setText(null);
        } else {
        	seriesStartingNumberCheckBox.setChecked(true);
        	seriesStartingNumberText.setText(series.getStartingNumber().toString());
        }
        updateStartingNumberValidation();
        if (series.getCopyInfo()==null || series.getCopyInfo()) {
        	seriesCopyCheckBox.setChecked(true);
        } else {
        	seriesCopyCheckBox.setChecked(false);
        }
	}
    
	@Override
	public void persistData() {
		// maybe save?
	}
	
    public void onSaveClicked(View view) {
    	
    	series.setName(stationSeriesNameText.getText().toString());
    	series.setAutoIncrement(seriesAutoIncrementCheckBox.isChecked());
    	if (seriesStartingNumberCheckBox.isChecked() && validator.isValid(seriesStartingNumberText)) {
    		series.setStartingNumber(Integer.parseInt(seriesStartingNumberText.getText().toString()));
    	} else {
    		series.setStartingNumber(null);
    	}
    	series.setCopyInfo(seriesCopyCheckBox.isChecked());
    	
    	try {
			stationSeriesDao.createOrUpdate(series);
			
			getActivity().setResult(Activity.RESULT_OK);
			getActivity().finish();
		} catch (SQLException e) {
			
			String msg = null;
			if (isCreate) {
				msg = "Failed to create series.";
			} else {
				msg = "Failed to update series.";
			}
			Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
		}
    }
    
    public void onCancelClicked(View view) {
    	
    	getActivity().setResult(Activity.RESULT_CANCELED);
    	getActivity().finish();
    }
    
//    public void onHelpClicked(View view) {
//    	
//        AbstractBaseActivity.fragmentName = TAG;
//
//    	Intent intent = HelpActivity.createIntent(getActivity(), "stationSeries", 
//    	        this.getClass().getSimpleName());
//    	startActivity(intent);
//    }

	private class MyValidationListener implements ValidationListener {

		@Override
		public void onValidation(Object input, boolean inputValid, boolean allInputsValid) {
			saveButton.setEnabled(allInputsValid);
		} 
	}
    
}
