package com.zlscorp.ultragrav.activity.fragment;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.logging.Level;

import roboguice.inject.InjectView;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.inject.Inject;
import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.activity.AbstractBaseActivity;
import com.zlscorp.ultragrav.activity.validate.Validator;
import com.zlscorp.ultragrav.activity.widget.NumberPadEditText;
import com.zlscorp.ultragrav.activity.widget.ListFragmentPagerAdapter.OnFragmentSelectedListener;
import com.zlscorp.ultragrav.debug.MyDebug;
import com.zlscorp.ultragrav.file.ErrorHandler;
import com.zlscorp.ultragrav.model.FactoryParameters;
import com.zlscorp.ultragrav.model.LevelCorrectionParams;
import com.zlscorp.ultragrav.model.MeterParams;
import com.zlscorp.ultragrav.model.SystemParams;
import com.zlscorp.ultragrav.persist.FactoryParametersDao;
import com.zlscorp.ultragrav.persist.LevelCorrectionParamsDao;
import com.zlscorp.ultragrav.persist.MeterParamsDao;
import com.zlscorp.ultragrav.persist.SystemParamsDao;

public class MeterParamsFragment extends AbstractBaseFragment implements
		OnFragmentSelectedListener {

	private static String TAG = "MeterParamsFragment";

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

	@InjectView(R.id.dummyLinearLayout)
	private LinearLayout dummyLinearLayout;

	@InjectView(R.id.readingLineText)
	private EditText readingLineText;

	@InjectView(R.id.beamScaleText)
	private NumberPadEditText beamScaleText;

	@InjectView(R.id.feedbackScaleText)
	private NumberPadEditText feedbackScaleText;

	@InjectView(R.id.minimumStopText)
	private EditText minimumStopText;

	@InjectView(R.id.maximumStopText)
	private EditText maximumStopText;

	@InjectView(R.id.cancelButton)
	private Button cancelButton;

	@InjectView(R.id.restoreButton)
	private Button restoreButton;

	private MeterParams meterParams;
	private boolean persistData;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_meter_params, container,
				false);
		return v;
	}

	@Override
	public void setupView(View view, Bundle savedInstanceState) {

		validator.validateAsInteger(readingLineText);
		validator.validateAsDoubleSciNot(beamScaleText);
		validator.validateAsDoubleSciNot(feedbackScaleText);
		validator.validateAsInteger(minimumStopText);
		validator.validateAsInteger(maximumStopText);

		setupNumberPadKeypad(beamScaleText, getString(R.string.beam_scale),
				dummyLinearLayout, false, true);
		setupNumberPadKeypad(feedbackScaleText,
				getString(R.string.feedback_scale), dummyLinearLayout, true,
				false);

		readingLineText.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus && readingLineText.getText().toString() != null) {
					// position cursor at right end of line
					readingLineText.setSelection(readingLineText.getText()
							.toString().length());
				}
			}
		});

		setOnFocusChangedListeners(minimumStopText);
		setOnFocusChangedListeners(maximumStopText);

		persistData = true;
		restoreButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onRestoreFactoryParamsClicked();
			}
		});

		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (MyDebug.LOG) {
					Log.d(TAG, "PersistData False");
				}
				persistData = false;
				getActivity().finish();
			}
		});

		try {
			FactoryParameters factoryParameters = factoryParametersDao
					.queryForDefault();

			if ((factoryParameters == null)
					|| !factoryParameters.isFileCreated()) {
				restoreButton.setEnabled(false);
			}
		} catch (SQLException e) {
			restoreButton.setEnabled(false);

			ErrorHandler errorHandler = ErrorHandler.getInstance();
			errorHandler.logError(Level.INFO,
					"MeterParamsFragment.setupView(): "
							+ "factoryParameters error - " + e);
		}
	}

	private void setOnFocusChangedListeners(final EditText editText) {

		editText.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus && editText.getText().toString() != null) {
					// position cursor at right end of line
					editText.setSelection(editText.getText().toString()
							.length());
				}
			}
		});
	}

	@Override
	public void onFragmentSelected() {
		// This keeps any real element from getting focus.
		dummyLinearLayout.requestFocus();
		dummyLLHasFocus = false;

		AbstractBaseActivity.fragmentName = this.getClass().getSimpleName();
	}

	@Override
	public void onFragmentUnselected() {
		// This keeps any real element from getting focus.
		dummyLinearLayout.requestFocus();
		dummyLLHasFocus = true;

		// Hide the soft keyboard before changing tabs.
		InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
	}

	public void onRestoreFactoryParamsClicked() {
		SystemParams systemParams = null;
		MeterParams meterParams = null;
		LevelCorrectionParams levelCorrectionParams = null;
		FactoryParameters factoryParameters = null;

		if (MyDebug.LOG) {
			Log.d(TAG, "Restore button pressed");
		}

		boolean test = false;

		try {
			// Test
			if (test) {
				factoryParameters = null;
				throw new SQLException("test");
			} else {
				factoryParameters = factoryParametersDao.queryForDefault();
			}
		} catch (SQLException e) {
			ErrorHandler errorHandler = ErrorHandler.getInstance();
			errorHandler.logError(Level.WARNING,
					"MeterParamsFragment.onRestoreFactoryParamsClicked(): "
							+ "Can't open factoryParameters - " + e,
					R.string.factory_params_file_open_error_title,
					R.string.factory_params_file_open_error_message);
			return;
		}
		if ((factoryParameters != null) && factoryParameters.isFileCreated()) {
			try {
				meterParams = meterParamsDao.queryForDefault();
				systemParams = systemParamsDao.queryForDefault();
				levelCorrectionParams = levelCorrectionParamsDao
						.queryForDefault();
			} catch (SQLException e) {
				ErrorHandler errorHandler = ErrorHandler.getInstance();
				errorHandler
						.logError(
								Level.WARNING,
								"MeterParamsFragment.onRestoreFactoryParamsClicked(): "
										+ "Can't open one of meterParams, systemParams or levelCorrectionParams - "
										+ e,
								R.string.params_file_open_error_title,
								R.string.params_file_open_error_message);
				return;
			}
			if (systemParams != null && meterParams != null
					&& levelCorrectionParams != null) {
				systemParams.setFeedbackGain(factoryParameters
						.getFeedbackGain());
				systemParams.setObservationPrecision(factoryParameters
						.getObservationPrecision());
				systemParams.setDataOutputRate(factoryParameters
						.getDataOutputRate());
				systemParams.setFilterTimeConstant(factoryParameters
						.getFilterTimeConstant());

				meterParams.setReadingLine(factoryParameters.getReadingLine());
				meterParams.setBeamScale(factoryParameters.getBeamScale());
				meterParams.setFeedbackScale(factoryParameters
						.getFeedbackScale());
				meterParams.setMinStop(factoryParameters.getMinStop());
				meterParams.setMaxStop(factoryParameters.getMaxStop());

				levelCorrectionParams.setLongLevel0(factoryParameters
						.getLongLevel0());
				levelCorrectionParams.setLongLevelA(factoryParameters
						.getLongLevelA());
				levelCorrectionParams.setLongLevelB(factoryParameters
						.getLongLevelB());
				levelCorrectionParams.setLongLevelC(factoryParameters
						.getLongLevelC());

				levelCorrectionParams.setCrossLevel0(factoryParameters
						.getCrossLevel0());
				levelCorrectionParams.setCrossLevelA(factoryParameters
						.getCrossLevelA());
				levelCorrectionParams.setCrossLevelB(factoryParameters
						.getCrossLevelB());
				levelCorrectionParams.setCrossLevelC(factoryParameters
						.getCrossLevelC());

				// try {
				readingLineText
						.setText(meterParams.getReadingLine().toString());

				

				beamScaleText.setText(meterParams.getBeamScale().toString());
				feedbackScaleText.setText(meterParams.getFeedbackScale()
						.toString());
				minimumStopText.setText(meterParams.getMinStop().toString());
				maximumStopText.setText(meterParams.getMaxStop().toString());
				// } catch (Exception e) {
				// Log.e(TAG,
				// "Error writing factory parameter value to Meter Parameter screen.");
				// }

				// This keeps these restored values from being overwritten by
				// their respective persist methods.
				levelCorrectionParams.setJustRestored(true);
				systemParams.setJustRestored(true);

				try {
					systemParamsDao.createOrUpdate(systemParams);
					meterParamsDao.createOrUpdate(meterParams);
					levelCorrectionParamsDao
							.createOrUpdate(levelCorrectionParams);
					Toast.makeText(getActivity(),
							getString(R.string.factory_params_restored),
							Toast.LENGTH_LONG).show();
				} catch (SQLException e) {
					ErrorHandler errorHandler = ErrorHandler.getInstance();
					errorHandler
							.logError(
									Level.WARNING,
									"MeterParamsFragment.onRestoreFactoryParamsClicked(): "
											+ "Can't update one of meterParams, systemParams or levelCorrectionParams - "
											+ e,
									R.string.params_file_update_error_title,
									R.string.params_file_update_error_message);
				}
			}
		} else {
			ErrorHandler errorHandler = ErrorHandler.getInstance();
			errorHandler.logError(Level.INFO,
					"MeterParamsFragment.onRestoreFactoryParamsClicked(): "
							+ "factoryParameters does not exist.",
					R.string.factory_params_file_does_not_exist_title,
					R.string.factory_params_file_does_not_exist_message);
		}
	}

	@Override
	public void populateData() {
		try {
			meterParams = meterParamsDao.queryForDefault();
		} catch (SQLException e) {
			ErrorHandler errorHandler = ErrorHandler.getInstance();
			errorHandler.logError(Level.SEVERE,
					"MeterParamsFragment.populateData(): "
							+ "Can't open meterParams - " + e,
					R.string.meter_params_file_open_error_title,
					R.string.meter_params_file_open_error_message);
		}

		if (meterParams == null) {
			meterParams = new MeterParams();
			meterParams.setDefaultUse(true);
		}

		if (meterParams.getReadingLine() != null) {
			readingLineText.setText(meterParams.getReadingLine().toString());
		}
		if (meterParams.getBeamScale() != null) {

		/**	String languageToLoad  = "ES_es" +
					"";
		     Locale locale1 = new Locale(languageToLoad); 
			
			if (getActivity().  getApplicationContext().getResources().getConfiguration().locale == locale1) {
				
			}
			
			
			
			
			
		     Locale.setDefault(locale);
		     Configuration config = new Configuration();
		     config.locale = locale;
		     getApplicationContext().getResources().updateConfiguration(config,getApplicationContext().getResources().getDisplayMetrics());
			*/
			

			beamScaleText.setText(new DecimalFormat("0.#########E0")
					.format(meterParams.getBeamScale()).replace(",", "."));
		}
		if (meterParams.getFeedbackScale() != null) {

			

			feedbackScaleText.setText(new DecimalFormat("0.#########E0")
					.format(meterParams.getFeedbackScale()).replace(",", "."));
		}
		if (meterParams.getMinStop() != null) {
			minimumStopText.setText(meterParams.getMinStop().toString());
		}
		if (meterParams.getMaxStop() != null) {
			maximumStopText.setText(meterParams.getMaxStop().toString());
		}

		validator.validateAll();
	}

	@Override
	public void persistData() {
		if (persistData) {
			try {
				meterParams = meterParamsDao.queryForDefault();
				// Test
				// throw new SQLException("test");
			} catch (SQLException e) {
				ErrorHandler errorHandler = ErrorHandler.getInstance();
				errorHandler.logError(Level.SEVERE,
						"MeterParamsFragment.persistData(): "
								+ "Can't open meterParams - " + e,
						R.string.meter_params_file_open_error_title,
						R.string.meter_params_file_open_error_message);
			}

			if (MyDebug.LOG) {
				Log.d(TAG, "Persisting Data");
			}
			if (validator.isValid(readingLineText)) {
				meterParams.setReadingLine(Integer.parseInt(readingLineText
						.getText().toString()));
			}
			if (validator.isValid(beamScaleText)) {


		

				meterParams.setBeamScale(Double.parseDouble(beamScaleText
						.getText().toString()));
			}
			if (validator.isValid(feedbackScaleText)) {
				
				meterParams.setFeedbackScale(Double
						.parseDouble(feedbackScaleText.getText().toString()));
			}
			if (validator.isValid(minimumStopText)) {
				meterParams.setMinStop(Integer.parseInt(minimumStopText
						.getText().toString()));
			}
			if (validator.isValid(maximumStopText)) {
				meterParams.setMaxStop(Integer.parseInt(maximumStopText
						.getText().toString()));
			}

			try {
				meterParamsDao.createOrUpdate(meterParams);
				// Test
				// throw new SQLException("test");
			} catch (SQLException e) {
				ErrorHandler errorHandler = ErrorHandler.getInstance();
				errorHandler.logError(Level.SEVERE,
						"MeterParamsFragment.persistData(): "
								+ "Can't update meterParams - " + e,
						R.string.meter_params_file_update_error_title,
						R.string.meter_params_file_update_error_message);
			}
		}
	}
}
