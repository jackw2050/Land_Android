package com.zlscorp.ultragrav.activity;

import java.io.Serializable;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.logging.Level;

import roboguice.inject.InjectView;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;
import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.debug.MyDebug;
import com.zlscorp.ultragrav.file.ErrorHandler;
import com.zlscorp.ultragrav.meter.MeterService;
import com.zlscorp.ultragrav.meter.MeterService.EndReadingCallback;
import com.zlscorp.ultragrav.meter.MeterService.ProcessorStateListener;
import com.zlscorp.ultragrav.meter.MeterService.ReadingResponseCallback;
import com.zlscorp.ultragrav.meter.MeterService.StartReadingCallback;
import com.zlscorp.ultragrav.meter.processor.ProcessorState;
import com.zlscorp.ultragrav.model.MeterParams;
import com.zlscorp.ultragrav.model.Station;
import com.zlscorp.ultragrav.model.StationSeries;
import com.zlscorp.ultragrav.model.SystemParams;
import com.zlscorp.ultragrav.persist.MeterParamsDao;
import com.zlscorp.ultragrav.persist.SystemParamsDao;
import com.zlscorp.ultragrav.type.ObservationType;

public class ObservationActivity extends AbstractBaseActivity {

	private static final String TAG = "ObservationActivity";

	private static final String STATE_MODEL = "model";

	@Inject
	private SystemParamsDao systemParamsDao;

	@Inject
	private MeterParamsDao meterParamsDao;

	@InjectView(R.id.elapsedTimeText)
	private TextView elapsedTimeText;

	@InjectView(R.id.dutyCycleLabel)
	private TextView dutyCycleLabel;

	@InjectView(R.id.dutyCycleText)
	private TextView dutyCycleText;

	@InjectView(R.id.dialReadingText)
	private TextView dialReadingText;

	@InjectView(R.id.observedGravityText)
	private TextView observedGravityText;

	@InjectView(R.id.standardDeviationLabel)
	private TextView standardDeviationLabel;

	@InjectView(R.id.standardDeviationText)
	private TextView standardDeviationText;

	@InjectView(R.id.beamErrorLabel)
	private TextView beamErrorLabel;

	@InjectView(R.id.beamErrorText)
	private TextView beamErrorText;

	@InjectView(R.id.feedbackCorrectionText)
	private TextView feedbackCorrectionText;

	@InjectView(R.id.earthtideView)
	private TextView earthtideView;

	@InjectView(R.id.earthtideText)
	private TextView earthtideText;

	@InjectView(R.id.levelCorrectionText)
	private TextView levelCorrectionText;

	@InjectView(R.id.tempCorrectionText)
	private TextView tempCorrectionText;

	@InjectView(R.id.dataOutputRateLabel)
	private TextView dataOutputRateLabel;

	@InjectView(R.id.dataOutputRateText)
	private TextView dataOutputRateText;

	@InjectView(R.id.filterTimeConstantLabel)
	private TextView filterTimeConstantLabel;

	@InjectView(R.id.filterTimeConstantText)
	private TextView filterTimeConstantText;

	@InjectView(R.id.timeText)
	private TextView timeText;

	@InjectView(R.id.beamFrequencyLabel)
	private TextView beamFrequencyLabel;

	@InjectView(R.id.beamFrequencyText)
	private TextView beamFrequencyText;

	@InjectView(R.id.crossLevelFrequencyLabel)
	private TextView crossLevelFrequencyLabel;

	@InjectView(R.id.crossLevelFrequencyText)
	private TextView crossLevelFrequencyText;

	@InjectView(R.id.longLevelFrequencyLabel)
	private TextView longLevelFrequencyLabel;

	@InjectView(R.id.longLevelFrequencyText)
	private TextView longLevelFrequencyText;

	@InjectView(R.id.temperatureFrequencyLabel)
	private TextView temperatureFrequencyLabel;

	@InjectView(R.id.temperatureFrequencyText)
	private TextView temperatureFrequencyText;

	@InjectView(R.id.temp0Label)
	private TextView temp0Label;

	@InjectView(R.id.temp0Text)
	private TextView temp0Text;

	@InjectView(R.id.tempBLabel)
	private TextView tempBLabel;

	@InjectView(R.id.tempBText)
	private TextView tempBText;

	@InjectView(R.id.levelWarningText)
	private TextView levelWarningText;

	@InjectView(R.id.tempWarningLabel)
	private TextView tempWarningLabel;

	@InjectView(R.id.tempWarningText)
	private TextView tempWarningText;

	@InjectView(R.id.filter10Button)
	private Button filter10Button;

	@InjectView(R.id.filter15Button)
	private Button filter15Button;

	@InjectView(R.id.saveButton)
	private Button saveButton;

	@InjectView(R.id.moreButton)
	private Button moreButton;

	@InjectView(R.id.doneButton)
	private Button doneButton;

    @InjectView(R.id.continueButton)
    private Button continueButton;

    //	boolean ok1 = false, ok2 = false;
	private enum SampleSize { FIVE, TEN, FIFTEEN };
	private SampleSize sampleSize = SampleSize.FIVE;

	// put all Activity state in MyModel to be saved automatically in
	// onSaveInstanceState
	private MyModel model;
	private Drawable defaultDrawable;
	private boolean isShowTimeOutAlert = true;
	private boolean isTestMode = false;
    private boolean isDCAlertRangeDisplayed;
    private boolean dontShowDCAlertAgain = false;
	// private boolean showEndReadingAlert;
	private boolean isMeterParamsAlertDisplayed;

	// Test
	private boolean isDutyCycleGoingUp;
	private Double dutyCycleAsPercentage;

	private Ringtone notificationSound;

	TextView txtStation;
	int lankind;
	TextView txt1, txt2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_observation);
		txtStation = (TextView) findViewById(R.id.textView2);
		txt1 = (TextView) findViewById(R.id.TextView01);
		txt2 = (TextView) findViewById(R.id.TextView02);
		defaultDrawable = doneButton.getBackground();
		lankind = pref.getInt("lan", 0);
		kind = 7;

		getSupportActionBar().setTitle(getString(R.string.title_activity_observation));

		if (savedInstanceState == null) {

			// unpack the intent extras
			model = new MyModel();
			model.observationType = (ObservationType) getIntent().getExtras()
					.get(EXTRA_OBSERVATION_TYPE);
			model.observationNote = (String) getIntent().getExtras().getString(
					EXTRA_OBSERVATION_NOTE);
			model.station = (Station) getIntent().getExtras().get(EXTRA_STATION);
			model.observationSaved = false;
			model.isObservationToSave = false;
			txtStation.setText(model.station.getStationId());
		} else {
			model = (MyModel) savedInstanceState.getSerializable(STATE_MODEL);
		}

		// Init these UI elements as not shown
		levelWarningText.setVisibility(View.GONE);
		tempWarningLabel.setVisibility(View.INVISIBLE);
		tempWarningText.setVisibility(View.INVISIBLE);

		if (model.observationType == ObservationType.SINGLE) {
			setTitle(R.string.title_activity_single_observation);
			earthtideView.setText(R.string.earthtide_correction);

			// Init these UI elements as not shown
			// These UI elements are not used in Single Mode
			dataOutputRateLabel.setVisibility(View.GONE);
			dataOutputRateText.setVisibility(View.GONE);
			filterTimeConstantLabel.setVisibility(View.GONE);
			filterTimeConstantText.setVisibility(View.GONE);
		} else if (model.observationType == ObservationType.CONTINUOUS) {
			setTitle(R.string.title_activity_continuous_observation);
			earthtideView.setText(R.string.earthtide);

			// These UI elements are not used in Continuous Mode
			standardDeviationLabel.setVisibility(View.GONE);
			standardDeviationText.setVisibility(View.GONE);
			txt1.setVisibility(View.GONE);
			txt2.setVisibility(View.GONE);
			beamErrorLabel.setVisibility(View.GONE);
			beamErrorText.setVisibility(View.GONE);

			saveButton.setVisibility(View.INVISIBLE);
			continueButton.setVisibility(View.INVISIBLE);
			moreButton.setVisibility(View.INVISIBLE);
			filter10Button.setVisibility(View.GONE);
			filter15Button.setVisibility(View.GONE);
		}

		// Check to see if the Test Mode checkbox is checked
		MeterParams meterParams = null;
		SystemParams systemParams = null;

		try {
			// Test
			boolean test = false;
			if (test) {
				throw new SQLException("test");
			}

			meterParams = meterParamsDao.queryForDefault();
			systemParams = systemParamsDao.queryForDefault();

			if (systemParams != null && systemParams.isTestMode() != null) {
				if (systemParams.isTestMode()) {
					isTestMode = true;
				} else {
					isTestMode = false;
					beamFrequencyLabel.setVisibility(View.GONE);
					beamFrequencyText.setVisibility(View.GONE);
					crossLevelFrequencyLabel.setVisibility(View.GONE);
					crossLevelFrequencyText.setVisibility(View.GONE);
					longLevelFrequencyLabel.setVisibility(View.GONE);
					longLevelFrequencyText.setVisibility(View.GONE);
					temperatureFrequencyLabel.setVisibility(View.GONE);
					temperatureFrequencyText.setVisibility(View.GONE);
					temp0Label.setVisibility(View.GONE);
					temp0Text.setVisibility(View.GONE);
					tempBLabel.setVisibility(View.GONE);
					tempBText.setVisibility(View.GONE);
				}
			}

			if (meterParams != null && isTestMode) {
				String str1 = Integer.toString(meterParams.getTemperature0());
				temp0Text.setText(convert(str1));
				String str2 = Double.toString(meterParams.getTemperatureB());
				if (lankind == 1)
					str2.replace(',', '.');
				tempBText.setText(str2);
			}
		} catch (SQLException e) {
			ErrorHandler errorHandler = ErrorHandler.getInstance();
			errorHandler
					.logError(
							Level.WARNING,
							"ObservationActivity.onCreate(): "
									+ "Could not open either meterParams or systemParams - "
									+ e,
							getString(R.string.params_file_open_error_title),
							getString(R.string.params_file_open_error_message),
							okOnClickListener);

		}

		filter10Button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onFilter10Button();
			}
		});

		filter15Button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onFilter15Button();
			}
		});

		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onSaveButton();
			}
		});

		moreButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onMoreButton();
            }
        });

		doneButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onDoneButton();
			}
		});

		// Force the screen to stay on during the reading
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		isDCAlertRangeDisplayed = false;
		// showEndReadingAlert = true;
		isMeterParamsAlertDisplayed = false;

		// Test
		isDutyCycleGoingUp = true;
		dutyCycleAsPercentage = 90.0;
	}

	//
	@Override
	protected void onResume() {
		super.onResume();

		updateDisplay();

		MeterService meter = MeterService.getInstance();

		if (meter != null) {
			if (!meter.isReadingInProgress()) {
				if (!meter.isConnected()) {
					showNoMeterConnectedAlert();
				} else {
					meter.startReading(model.observationType,
							model.observationNote, model.station,
							new MyStartReadingCallback(),
							new MyReadingResponseCallback(),
							new MyProcessorStateListener());

					// This next block was moved to
					// MyStartReadingCallback.onSuccess
					// model.observationSaved = false;
					// model.isObservationToSave = false;
				}
			} else {
				if (MyDebug.LOG) {
					Log.d(TAG, "observation in progress");
				}
				Toast.makeText(ObservationActivity.this,
						getString(R.string.observation_in_progress),
						Toast.LENGTH_SHORT).show();
			}
		} else {
			if (MyDebug.LOG) {
				Log.e(TAG, "No Meter Service started");
			}
			showNoMeterConnectedAlert();
		}

		Uri notification = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		notificationSound = RingtoneManager.getRingtone(
				getApplicationContext(), notification);
	}

	@Override
	protected void onStop() {
		super.onStop();

		// Make sure that we end the reading if that didn't happen for some
		// reason.
		MeterService meter = MeterService.getInstance();
		if (meter != null && meter.isReadingInProgress()) {
			meter.endReading(new MyEndReadingCallback(true));
		}

		notificationSound.stop();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(STATE_MODEL, model);
	}

	@Override
	public void onBackPressed() {
		// don't allow back button
		Toast.makeText(this, getString(R.string.press_done), Toast.LENGTH_SHORT)
				.show();
	}

	// The Save button is only visible/used in Single Mode Observation
	private void onSaveButton() {
		saveObservation();
	}

	private void saveObservation() {
		MeterService.SaveResult saveResult;
		MeterService meter = MeterService.getInstance();
		if (meter != null && meter.isConnected() && meter.isReadingInProgress()) {
			saveResult = meter.saveObservation();

			if (saveResult.isSuccess()) {
				// The Save button is only displayed in Single mode. Is this
				// check just for safety?
				if (model.observationType == ObservationType.SINGLE) {
					Toast.makeText(this, getString(R.string.observation_saved),
							Toast.LENGTH_SHORT).show();
					saveButton.setEnabled(false);
				}
				model.observationSaved = true;
				model.isObservationToSave = false;
			} else {
				if (MyDebug.LOG) {
					Log.d(TAG, "observation save failed");
				}

				// showEndReadingAlert = false;
				// meter.endReading(new MyEndReadingCallback());

				ErrorHandler errorHandler = ErrorHandler.getInstance();
				errorHandler
						.logError(
								Level.WARNING,
								"ObservationActivity.saveObservation(): "
										+ saveResult.getErrorMessage(),
								getString(R.string.observation_file_write_error_title),
								getString(R.string.observation_file_write_error_message),
								okOnClickListener);
			}
		} else {
			showNoMeterConnectedAlert();
		}
	}

	/*
	 * Date 2015-12-3 jvs - use one method to process the sample filter buttons
	 */
	private void processFilterButton(SampleSize mySampleSize) {
		MeterService meter = MeterService.getInstance();
		if (meter != null && meter.isConnected() && meter.isReadingInProgress()) {

			switch (mySampleSize) {
				case TEN:
					if (sampleSize == SampleSize.TEN) {
						// Sample size is currently 10, so reset it to 5
						sampleSize = SampleSize.FIVE;
						meter.setSampleSize(5);

						filter10Button.setTextColor(Color.BLACK);
						filter10Button.setBackgroundColor(Color.LTGRAY);
//						filter10Button.setBackgroundResource(android.R.drawable.btn_default);
					} else {
						// Sample size is not currently 10, so make it 10
						sampleSize = SampleSize.TEN;
						meter.setSampleSize(10);

						filter10Button.setTextColor(Color.WHITE);
						filter10Button.setBackgroundColor(Color.GRAY);

						filter15Button.setTextColor(Color.BLACK);
						filter15Button.setBackgroundColor(Color.LTGRAY);
//						filter15Button.setBackgroundResource(android.R.drawable.btn_default);
					}
					break;

				case FIFTEEN:
					if (sampleSize == SampleSize.FIFTEEN) {
						// Sample size is currently 15, so reset it to 5
						sampleSize = SampleSize.FIVE;
						meter.setSampleSize(5);

						filter15Button.setTextColor(Color.BLACK);
						filter15Button.setBackgroundColor(Color.LTGRAY);
//						filter15Button.setBackgroundResource(android.R.drawable.btn_default);
					} else {
						// Sample size is not currently 15, so make it 15
						sampleSize = SampleSize.FIFTEEN;
						meter.setSampleSize(15);

						filter10Button.setTextColor(Color.BLACK);
						filter10Button.setBackgroundColor(Color.LTGRAY);
//						filter10Button.setBackgroundResource(android.R.drawable.btn_default);

						filter15Button.setTextColor(Color.WHITE);
						filter15Button.setBackgroundColor(Color.GRAY);
					}
					break;
			}
		} else {
			AlertDialog.Builder dlg = new AlertDialog.Builder(this);
			dlg.setTitle(getString(R.string.app_name))
					.setMessage("Meter error!")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							}).show();
		}
	}

	private void onFilter10Button() {
		processFilterButton(SampleSize.TEN);
//		MeterService meter = MeterService.getInstance();
////		ok1 = !ok1;
////		if (!ok1) {
//		if (sampleSize == SampleSize.TEN) {
//			// 10 Sample Filter was on, turn it off
//			sampleSize = SampleSize.FIVE;
//
//			filter10Button.setTextColor(Color.BLACK);
//			filter10Button.setBackgroundResource(android.R.drawable.btn_default);
//		} else {
//			// 10 Sample Filter was off, turn it on
//			sampleSize = SampleSize.TEN;
//
//			filter10Button.setTextColor(Color.WHITE);
//			filter10Button.setBackgroundColor(Color.GRAY);
//			filter15Button.setBackgroundResource(android.R.drawable.btn_default);
//			filter15Button.setTextColor(Color.BLACK);
////			ok2 = false;
//		}
//
//		if (meter != null && meter.isConnected() && meter.isReadingInProgress()) {
//			if (ok1) {
//				// Set sample size to 10
//				meter.setSampleSize(5);  // Why is this 5? Shouldn't it be 10?
//			} else {
//				// Reset sample size to 5
//				meter.setSampleSize(5);
//			}
//		} else {
//			AlertDialog.Builder dlg = new AlertDialog.Builder(this);
//			dlg.setTitle(getString(R.string.app_name))
//					.setMessage("Meter error!")
//					.setPositiveButton("OK",
//							new DialogInterface.OnClickListener() {
//
//								@Override
//								public void onClick(DialogInterface dialog, int which) {
//									dialog.dismiss();
//								}
//							}).show();
//		}
	}

	/*
	 * Date 2015- 07-27 Make one button unable and disable at a time
	 */

	private void onFilter15Button() {
		processFilterButton(SampleSize.FIFTEEN);
//		MeterService meter = MeterService.getInstance();
//
//		ok2 = !ok2;
//		if (!ok2) {
//			filter15Button.setTextColor(Color.BLACK);
//			filter15Button.setBackgroundResource(android.R.drawable.btn_default);
//		} else {
//			filter15Button.setTextColor(Color.WHITE);
//			filter15Button.setBackgroundColor(Color.GRAY);
//			filter10Button.setBackgroundResource(android.R.drawable.btn_default);
//			filter10Button.setTextColor(Color.BLACK);
//			ok1 = false;
//		}
//
//		if (meter != null && meter.isConnected() && meter.isReadingInProgress()) {
//			if (ok2) {
//				meter.setSampleSize(8);  // Why is this 8? Shouldn't it be 15?
//			}                           // where is the else if to reset it back to 5?
//		} else {
//			AlertDialog.Builder dlg = new AlertDialog.Builder(this);
//			dlg.setTitle(getString(R.string.app_name))
//					.setMessage("Meter error!")
//					.setPositiveButton("OK",
//							new DialogInterface.OnClickListener() {
//
//								@Override
//								public void onClick(DialogInterface dialog, int which) {
//									dialog.dismiss();
//								}
//							}).show();
//		}
	}

	private void onDoneButton() {
		if ((model.observationType == ObservationType.SINGLE)
				&& model.isObservationToSave && !model.observationSaved) {
			showObservationNotSavedAlert();
		} else {
			finishObservation(true);
		}
	}

	private void onMoreButton() {
		MeterService meter = MeterService.getInstance();
		if (meter != null && meter.isConnected() && meter.isReadingInProgress()) {
			meter.resetStatistics();
			// this should only be cleared when we have a good observation
			// model.observationSaved = false;
			// probably at worst this is redundant
			saveButton.setEnabled(false);
			moreButton.setEnabled(false);
		} else {
			showNoMeterConnectedAlert();
		}
	}

	private void finishObservation(boolean success) {
		MeterService meter = MeterService.getInstance();
		if (meter != null) {
			if (meter.isReadingInProgress()) {
				meter.endReading(new MyEndReadingCallback(success));
			} else {
				showClampBeamAlert(false);
			}
		} else {
			showClampBeamAlert(false);
		}
	}

	String convert(String str) {
		if (lankind == 1)
			return str.replace(',', '.');
		else
			return str;
	}

	private void updateDisplay() {

		if (model.processorState != null) {
			ProcessorState processorState = model.processorState;

			dialReadingText.setText(convert(Integer.toString(processorState.getDialReading())));

			long elapsedTime = processorState.getElapsedTime();
			if (elapsedTime != 0) {
				int minsPart = (int) (elapsedTime / 60);
				int secondsPart = (int) (elapsedTime % 60);
				if (secondsPart < 10) {
					// TODO - check this lint string warning
					String str = minsPart + ":0" + secondsPart;
					elapsedTimeText.setText(str);
//					elapsedTimeText.setText(minsPart + ":0" + secondsPart);
				} else {
					String str = minsPart + ":" + secondsPart;
					elapsedTimeText.setText(str);
//					elapsedTimeText.setText(minsPart + ":" + secondsPart);
				}
			}

			boolean isDutyCycleTest = false;
			if (isDutyCycleTest) {
				if (isDutyCycleGoingUp && dutyCycleAsPercentage < 100.0) {
					dutyCycleAsPercentage++;
				} else {
					isDutyCycleGoingUp = false;
					if (dutyCycleAsPercentage > 90.0) {
						dutyCycleAsPercentage--;
					} else {
                        isDutyCycleGoingUp = true;
                    }
				}
			} else {
				dutyCycleAsPercentage = processorState.getDutyCycleAsPercentage();
			}

			if (dutyCycleAsPercentage <= MeterParams.DC_LOW_LIMIT
					|| dutyCycleAsPercentage >= MeterParams.DC_HIGH_LIMIT) {

				// The duty cycle is out the end of its limits, so let the user
				// know

				dutyCycleText.setText(convert(new DecimalFormat("0.0")
						.format(dutyCycleAsPercentage) + "\n"+getResources()
						.getString(R.string.out_of_range)));
				dutyCycleText.setTextColor(Color.RED);
				dutyCycleLabel.setTextColor(Color.RED);

			} else if (dutyCycleAsPercentage <= MeterParams.DC_NEAR_LOW_LIMIT
					|| dutyCycleAsPercentage >= MeterParams.DC_NEAR_HIGH_LIMIT) {

				// The duty cycle is near the end of its limits, so let the user
				// know

				dutyCycleText.setText(convert(new DecimalFormat("0.0").format(dutyCycleAsPercentage)
						+ "\n" + getResources().getString(R.string.near_end)
						+ "\n" + getResources().getString(R.string.of_range)));
				dutyCycleText.setTextColor(getResources().getColor(R.color.orange));
				dutyCycleLabel.setTextColor(getResources().getColor(R.color.orange));

			} else {

				// The duty cycle is within its limits, so chill out.

				dutyCycleText.setText(convert(new DecimalFormat("0.0")
						.format(dutyCycleAsPercentage)));
				dutyCycleText.setTextColor(Color.BLACK);
				dutyCycleLabel.setTextColor(Color.BLACK);
			}

			if (model.observationType == ObservationType.SINGLE
					&& processorState.isDisplayStatisticsData()) {
				standardDeviationText.setText(convert(new DecimalFormat("0.0000")
						.format(processorState.getStandardDeviation())));
				beamErrorText.setText(convert(new DecimalFormat("0.0000")
						.format(processorState.getBeamError())));
			}

			if (isTestMode) {
				beamFrequencyText.setText(convert(Integer.toString(processorState.getBeamFreq())));
				crossLevelFrequencyText.setText(convert(Integer
						.toString(processorState.getCrossLevelFreq())));
				longLevelFrequencyText.setText(convert(Integer
						.toString(processorState.getLongLevelFreq())));
				temperatureFrequencyText.setText(convert(Integer
						.toString(processorState.getTemperatureFrequency())));
			}

			// A new gravity observation data set is ready to display. It is not
			// necessarily a "good" observation.
			if (processorState.isDisplayGravityData()) {
				// This section applies to both Single and Continuous  observations
				dialReadingText.setText(convert(Integer.toString(processorState.getDialReading())));
				observedGravityText.setText(convert(new DecimalFormat("0.000")
						.format(processorState.getObservedGravity())));
				feedbackCorrectionText.setText(convert(new DecimalFormat("0.000")
						.format(processorState.getFeedbackCorrection())));
				/*
				 * earthtideText.setText(convert(new DecimalFormat("0.0000")
				 * .format(Math.abs(processorState.getEarthtide()))));
				 */

				/*
				 * Date - 2015-07-14 absolute value changed to actual value
				 */
				earthtideText.setText(convert(new DecimalFormat("0.0000")
						.format(processorState.getEarthtide())));
				levelCorrectionText.setText(convert(new DecimalFormat("0.000")
						.format(processorState.getLevelCorrection())));
				tempCorrectionText.setText(convert(new DecimalFormat("0.000")
						.format(processorState.getTempCorrection())));
				{
					Time readingTime = new Time("UTC");
					readingTime.set(processorState.getReadingTime());
					readingTime.isDst = 0;

					String readingMins = Integer.toString(readingTime.minute);

					if (readingMins.length() == 1) { // Add a leading 0 if value less than 10
						readingMins = "0" + readingMins;
					}

					String readingHrs = Integer.toString(readingTime.hour);
					if (readingHrs.length() == 1) { // Add a leading 0 if value less than 10
						readingHrs = "0" + readingHrs;
					}
					timeText.setText(readingHrs + ":" + readingMins);
				}

				model.isObservationToSave = true;
				// We just presented the user with a new gravity observation
				// data set and the opportunity to save it
				model.observationSaved = false;

				if (model.observationType == ObservationType.SINGLE) {
					saveButton.setEnabled(true);
					moreButton.setEnabled(true);
					if (notificationSound != null) {
						notificationSound.play();
					}
				} else if (model.observationType == ObservationType.CONTINUOUS) {
					dataOutputRateText.setText(convert(Integer
							.toString(processorState.getDataOutputRate())));
					filterTimeConstantText.setText(convert(Integer
							.toString(processorState.getFilterTimeConstant())));
					saveObservation();
				}
			}

			// Check duty cycle and let user know if it is out of limits (< 5%
			// or > 98%)
			MeterParams meterParams = null;
			try {
				// Test
				boolean test = false;
				if (test) {
					throw new SQLException("test");
				}
				meterParams = meterParamsDao.queryForDefault();

				// was processorState.getDutyCycleAsPercentage()
				if (dutyCycleAsPercentage <= MeterParams.DC_LOW_LIMIT
						&& !isDCAlertRangeDisplayed && !dontShowDCAlertAgain) {
					if (meterParams.isCalibrated()) {
						showDutyCycleRangeAlert(R.string.duty_cycle_low_calibrated_message);
					} else {
						showDutyCycleRangeAlert(R.string.duty_cycle_low_non_calibrated_message);
					}
				}

				if (dutyCycleAsPercentage >= MeterParams.DC_HIGH_LIMIT
						&& !isDCAlertRangeDisplayed && !dontShowDCAlertAgain) {
					if (meterParams.isCalibrated()) {
						showDutyCycleRangeAlert(R.string.duty_cycle_high_calibrated_message);
					} else {
						showDutyCycleRangeAlert(R.string.duty_cycle_high_non_calibrated_message);
					}
				}
			} catch (SQLException e) {

				if (!isMeterParamsAlertDisplayed) {
					isMeterParamsAlertDisplayed = true;

					ErrorHandler errorHandler = ErrorHandler.getInstance();
					errorHandler
							.logError(
									Level.WARNING,
									"OutputObsDataFragment.updateDisplay(): "
											+ "Could not open meterParams - "
											+ e,
									getString(R.string.meter_params_file_open_error_title),
									getString(R.string.meter_params_file_open_error_message),
									okOnClickListener);
				}
			}

			// If DC was out of limits, and is now back in, clear the flag.
			if (isDCAlertRangeDisplayed
					&& dutyCycleAsPercentage > MeterParams.DC_LOW_LIMIT
					&& dutyCycleAsPercentage < MeterParams.DC_HIGH_LIMIT) {
				isDCAlertRangeDisplayed = false;
			}

			if ( ((model.observationType == ObservationType.SINGLE) &&
					(processorState.getElapsedTime() < 185)) ||
					(model.observationType == ObservationType.CONTINUOUS) ) {
				if (processorState.isLevelCorrectionGood()) {
					levelWarningText.setVisibility(View.GONE);
				} else {
					levelWarningText.setVisibility(View.VISIBLE);
				}

				if (processorState.isTemperatureGood()) {
					if (!processorState.isGoodObservation()) {
						tempWarningLabel.setVisibility(View.INVISIBLE);
						tempWarningText.setVisibility(View.INVISIBLE);
					}
				} else {
					tempWarningText
							.setText(convert(Integer.toString(processorState
									.getTemperatureFrequency())));
					tempWarningText.setVisibility(View.VISIBLE);
					tempWarningLabel.setVisibility(View.VISIBLE);
				}
			} else {
				if (isShowTimeOutAlert) {
					showTimedOutAlert();
					isShowTimeOutAlert = false;
				}
			}

		} else {

			// TODO should probably set all text views to null

			saveButton.setEnabled(false);
			moreButton.setEnabled(false);
		}

	}

	// @Override
	// public void doneButtonOnClickListener() {
	// if (isThisASeries) { // this flag hasn't been created yet, might exist
	// // by another name
	// // display Confirm Series Confirmation Alert dialog
	// if (!isSeriesComplete) { // this flag hasn't been created yet,
	// // result of dialog
	// // TODO - do the same thing as if user pressed Continue button
	// }
	// } else {
	// // display Clamp Beam Alert dialog
	// if (!obsDataSaved) {
	// // TODO - display Valid Data Not Saved Alert dialog
	// if (userWantsToSaveData) { // this flag hasn't been created yet,
	// // result of dialog
	// saveObsData = true;
	// }
	// }
	// // TODO - close dbs
	// // stopMeterDataTransmit();
	// quitFlag = true;
	// }
	//
	// // TODO - close dbs
	// // stopMeterDataTransmit();
	// quitFlag = true;
	// if (!obsDataSaved) {
	// // TODO - display Valid Data Not Saved Alert dialog
	// if (userWantsToSaveData) { // this flag hasn't been created yet,
	// // result of dialog
	// saveObsData = true;
	// }
	// }
	// }
	//
	// public void continueButtonOnClickListener() {
	// if (!obsDataSaved) {
	// // TODO - display Valid Data Not Saved Alert dialog
	// if (userWantsToSaveData) { // this flag hasn't been created yet,
	// // result of dialog
	// saveObsData = true;
	// }
	// }
	// }

	public void showTimedOutAlert() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.observation_timed_out_title);
		builder.setMessage(R.string.observation_timed_out_message);
		builder.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.show();
	}

	public void showClampBeamAlert(final boolean success) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.clamp_alert_title);
		builder.setMessage(R.string.clamp_alert_message);
		builder.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						setResult(success ? RESULT_FINISH_OBSERVATION
								: RESULT_CANCELED);
						finish();
						dialog.dismiss();
					}
				});
		AlertDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}

	public void showNoMeterConnectedAlert() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.no_meter_connected_title);
		builder.setMessage(R.string.no_meter_connected_message);
		builder.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						setResult(RESULT_CANCELED);
						finish();
						dialog.dismiss();
					}
				});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public void showObservationNotSavedAlert() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.data_not_saved_alert_title);
		builder.setMessage(R.string.data_not_saved_alert_message);
		builder.setPositiveButton(R.string.yes,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						saveObservation();
						dialog.dismiss();
						finishObservation(true);
					}
				});
		builder.setNegativeButton(R.string.no,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						finishObservation(true);
					}
				});
		builder.create().show();
	}

	public void showDutyCycleRangeAlert(int message) {

        // This is for a checkbox in the DC out of range alertdialog
        View checkBoxView = View.inflate(this, R.layout.activity_observation_alertdialog_checkbox, null);
        final CheckBox dontShowAgainCheckbox = (CheckBox) checkBoxView.findViewById(R.id.dontShowAgainCheckbox);
        dontShowAgainCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (dontShowAgainCheckbox.isChecked()) {
                    dontShowDCAlertAgain = true;
                }
            }
        });
        dontShowAgainCheckbox.setText(R.string.dont_show_this_warning_again);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.duty_cycle_range_alert_title);
		builder.setMessage(message);
        builder.setView(checkBoxView);
		builder.setCancelable(false); // Don't allow the dialog to be dismissed
										// by touching outside of it.
		builder.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						// isDCAlertRangeDisplayed = false;
					}
				});
		builder.create().show();
		isDCAlertRangeDisplayed = true;
	}

	public void showBeforeNextReadingAlert() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.before_next_reading_alert_title);
		builder.setMessage(Html
				.fromHtml(getString(R.string.before_next_reading_alert_message)));
		builder.setPositiveButton(R.string.continue_text,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	/**
	 * A simple class to hold all activity state from onSaveInstanceState to
	 * onCreate
	 */
	private static class MyModel implements Serializable {
		private static final long serialVersionUID = 1L;

		private ObservationType observationType;
		private String observationNote;
		private Station station;

		private ProcessorState processorState;

		private boolean observationSaved;
		private boolean isObservationToSave;

	}

	private class MyStartReadingCallback implements StartReadingCallback {
		@Override
		public void onSuccess() {
			if (MyDebug.LOG) {
				Log.d(TAG, "observation stared");
			}
			// Toast.makeText(ObservationActivity.this,
			// getString(R.string.observation_started),
			// Toast.LENGTH_SHORT).show();

			model.observationSaved = false;
			model.isObservationToSave = false;
		}

		@Override
		public void onFailed(String reason) {
			if (MyDebug.LOG) {
				Log.d(TAG, reason);
			}
			// Toast.makeText(ObservationActivity.this, reason,
			// Toast.LENGTH_LONG).show();

			ErrorHandler errorHandler = ErrorHandler.getInstance();
			errorHandler.logError(Level.WARNING, "ObservatonActivity$"
					+ "MyStartReadingCallback.onFailed(): " + reason,
					getString(R.string.failed_to_start_reading_title),
					getString(R.string.failed_to_start_reading_message),
					okOnClickListener);

			// if (isActive()) {
			// finishObservation();
			// }
		}
	}

	private class MyReadingResponseCallback implements ReadingResponseCallback {

		@Override
		public void onFailed(String reason) {
			if (MyDebug.LOG) {
				Log.d(TAG, reason);
			}
			Toast.makeText(ObservationActivity.this, reason, Toast.LENGTH_LONG)
					.show();

			ErrorHandler errorHandler = ErrorHandler.getInstance();
			errorHandler
					.logError(
							Level.WARNING,
							"ObservatonActivity$"
									+ "MyReadingResponseCallback.onFailed(): Error during reading - "
									+ reason,
							getString(R.string.interrupted_reading_title),
							getString(R.string.interrupted_reading_message),
							okOnClickListener);
		}
	}

	MyOnClickListener okOnClickListener = new MyOnClickListener() {
		@Override
		public void onClick() {
			// if (isActive()) {
			finishObservation(false);
			// }
		}
	};

	MyOnClickListener endReadingOkOnClickListener = new MyOnClickListener() {
		@Override
		public void onClick() {
			showClampBeamAlert(false);
		}
	};

	private class MyProcessorStateListener implements ProcessorStateListener {
		@Override
		public void onProcessorState(ProcessorState processorState) {
			if (MyDebug.LOG) {
				Log.d(TAG, "processor state changed. state=" + processorState);
			}

			model.processorState = processorState;

			if (isActive()) {
				updateDisplay();
			}
		}

		@Override
		public void onError(String reason) {
			if (MyDebug.LOG) {
				Log.e(TAG, "processor state failed. " + reason);
			}

			model.processorState = null;

			if (isActive()) {
				updateDisplay();
			}
			Toast.makeText(ObservationActivity.this, reason, Toast.LENGTH_LONG)
					.show();
		}
	}

	private class MyEndReadingCallback implements EndReadingCallback {

		boolean success;

		MyEndReadingCallback(boolean success) {
			this.success = success;
		}

		@Override
		public void onSuccess() {
			if (MyDebug.LOG) {
				Log.d(TAG, "observation ended");
			}
			Toast.makeText(ObservationActivity.this,
					getString(R.string.observation_ended), Toast.LENGTH_SHORT)
					.show();

			// if (isActive() && showEndReadingAlert) {
			if (isActive()) {
				showClampBeamAlert(success); // was true
			}
		}

		@Override
		public void onFailed(String reason) {
			if (MyDebug.LOG) {
				Log.d(TAG, "reading end failed. " + reason);
			}

			if (isActive()) {
				ErrorHandler errorHandler = ErrorHandler.getInstance();
				errorHandler.logError(Level.WARNING,
						"ObservationActivity$MyEndReadingCallback.onFailed(): "
								+ "Failed to end reading - " + reason,
						getString(R.string.failed_to_end_reading_title),
						getString(R.string.failed_to_end_reading_message),
						okOnClickListener);
			}
		}
	}

	/**
	 * Creates an Intent for this Activity.
	 * 
	 * @param callee
	 * @return Intent
	 */
	public static Intent createIntent(Context callee,
			ObservationType observationType, String observationNote,
			StationSeries stationSeries, Station station) {
		Intent answer = new Intent(callee, ObservationActivity.class);
		answer.putExtra(EXTRA_OBSERVATION_TYPE, observationType);
		answer.putExtra(EXTRA_OBSERVATION_NOTE, observationNote);
		answer.putExtra(EXTRA_STATION, station);

		return answer;
	}

	@Override
	public String getActivityName() {
		return TAG;
	}

	@Override
	public String getHelpKey() {
		return null;
	}
}
