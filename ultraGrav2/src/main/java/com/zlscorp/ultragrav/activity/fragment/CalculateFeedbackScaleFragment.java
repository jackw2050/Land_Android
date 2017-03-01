package com.zlscorp.ultragrav.activity.fragment;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.logging.Level;

import roboguice.inject.InjectView;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;
import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.activity.AbstractBaseActivity;
import com.zlscorp.ultragrav.activity.PrivateActivity;
import com.zlscorp.ultragrav.activity.validate.Validator;
import com.zlscorp.ultragrav.activity.validate.Validator.ValidationListener;
import com.zlscorp.ultragrav.activity.widget.ListFragmentPagerAdapter.OnFragmentSelectedListener;
import com.zlscorp.ultragrav.debug.MyDebug;
import com.zlscorp.ultragrav.file.ErrorHandler;
import com.zlscorp.ultragrav.meter.MeterService;
import com.zlscorp.ultragrav.meter.MeterService.EndReadingCallback;
import com.zlscorp.ultragrav.meter.MeterService.GetDutyCycleCallback;
import com.zlscorp.ultragrav.meter.MeterService.ProcessorStateListener;
import com.zlscorp.ultragrav.meter.MeterService.ReadingResponseCallback;
import com.zlscorp.ultragrav.meter.MeterService.SetDutyCycleCallback;
import com.zlscorp.ultragrav.meter.MeterService.StartReadingCallback;
import com.zlscorp.ultragrav.meter.processor.ProcessorState;
import com.zlscorp.ultragrav.model.FeedbackScaleParams;
import com.zlscorp.ultragrav.model.MeterParams;
import com.zlscorp.ultragrav.persist.FeedbackScaleParamsDao;
import com.zlscorp.ultragrav.persist.MeterParamsDao;
import com.zlscorp.ultragrav.type.ObservationType;
//import android.content.Context;

public class CalculateFeedbackScaleFragment extends AbstractBaseFragment 
                                            implements OnFragmentSelectedListener {

    private static String TAG = "CalculateFeedbackScaleFragment";

    @Inject
    private FeedbackScaleParamsDao feedbackScaleParamsDao;

    @Inject
    private MeterParamsDao meterParamsDao;

	@Inject
	private Validator validator;

    @InjectView(R.id.dummyLinearLayout)
    private LinearLayout dummyLinearLayout;

	@InjectView(R.id.dutyValPreset5Percent)
	private Button dutyValPreset5Percent;

	@InjectView(R.id.dutyValPreset50Percent)
	private Button dutyValPreset50Percent;

	@InjectView(R.id.dutyValPreset98Percent)
	private Button dutyValPreset98Percent;

	@InjectView(R.id.currentDutyCycleText)
	private TextView currentDutyCycleText;

	@InjectView(R.id.newDutyCycleText)
	private EditText newDutyCycleText;

	@InjectView(R.id.setDutyCycleButton)
	private Button setDutyCycleButton;

    @InjectView(R.id.beamFrequencyText)
    private TextView beamFrequencyText;

    @InjectView(R.id.freqMinus10Text)
    private EditText freqMinus10Text;

    @InjectView(R.id.freqMinus5Text)
    private EditText freqMinus5Text;

    @InjectView(R.id.freq0Text)
    private EditText freq0Text;

    @InjectView(R.id.freqPlus5Text)
    private EditText freqPlus5Text;

    @InjectView(R.id.freqPlus10Text)
    private EditText freqPlus10Text;

    @InjectView(R.id.fivePercentMinus10Text)
    private EditText fivePercentMinus10Text;

    @InjectView(R.id.fivePercentMinus5Text)
    private EditText fivePercentMinus5Text;

    @InjectView(R.id.fivePercent0Text)
    private EditText fivePercent0Text;

    @InjectView(R.id.fivePercentPlus5Text)
    private EditText fivePercentPlus5Text;

    @InjectView(R.id.fivePercentPlus10Text)
    private EditText fivePercentPlus10Text;

    @InjectView(R.id.nintyEightPercentMinus10Text)
    private EditText nintyEightPercentMinus10Text;

    @InjectView(R.id.nintyEightPercentMinus5Text)
    private EditText nintyEightPercentMinus5Text;

    @InjectView(R.id.nintyEightPercent0Text)
    private EditText nintyEightPercent0Text;

    @InjectView(R.id.nintyEightPercentPlus5Text)
    private EditText nintyEightPercentPlus5Text;

    @InjectView(R.id.nintyEightPercentPlus10Text)
    private EditText nintyEightPercentPlus10Text;

    @InjectView(R.id.deltaMinus10Text)
    private TextView deltaMinus10Text;

    @InjectView(R.id.deltaMinus5Text)
    private TextView deltaMinus5Text;

    @InjectView(R.id.delta0Text)
    private TextView delta0Text;

    @InjectView(R.id.deltaPlus5Text)
    private TextView deltaPlus5Text;

    @InjectView(R.id.deltaPlus10Text)
    private TextView deltaPlus10Text;

	@InjectView(R.id.c93Text)
	private TextView c93Text;

	@InjectView(R.id.feedbackScaleText)
	private TextView feedbackScaleText;

	@InjectView(R.id.ccnmFactorText)
	private EditText ccnmFactorText;

	@InjectView(R.id.finalFdkScaleText)
	private TextView finalFdkScaleText;

    @InjectView(R.id.startButton)
    private Button startButton;

    @InjectView(R.id.clearAllButton)
    private Button clearAllButton;

    @InjectView(R.id.acceptButton)
    private Button acceptButton;

    private TextView[] deltaTextArray  = new TextView[5];
    private EditText[][] feedbackScaleDataTextArray =  new EditText[3][5];

    private boolean wasStartedByMe;

    PrivateActivity parent;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_calculate_feedback_scale, container, false);
		parent = (PrivateActivity) getActivity();
		return v;
	}
    String convert(String str)
    {
    	int kind = parent.lankind;
    	if  (kind == 1)
    		return str.replace(',',  '.');
    	else 
    		return str;
    }
	@Override
    public void setupView(View view, Bundle savedInstanceState) {

        feedbackScaleDataTextArray[0][0] = freqMinus10Text;
        feedbackScaleDataTextArray[0][1] = freqMinus5Text;
        feedbackScaleDataTextArray[0][2] = freq0Text;
        feedbackScaleDataTextArray[0][3] = freqPlus5Text;
        feedbackScaleDataTextArray[0][4] = freqPlus10Text;
        
        feedbackScaleDataTextArray[1][0] = fivePercentMinus10Text;
        feedbackScaleDataTextArray[1][1] = fivePercentMinus5Text;
        feedbackScaleDataTextArray[1][2] = fivePercent0Text;
        feedbackScaleDataTextArray[1][3] = fivePercentPlus5Text;
        feedbackScaleDataTextArray[1][4] = fivePercentPlus10Text;
        
        feedbackScaleDataTextArray[2][0] = nintyEightPercentMinus10Text;
        feedbackScaleDataTextArray[2][1] = nintyEightPercentMinus5Text;
        feedbackScaleDataTextArray[2][2] = nintyEightPercent0Text;
        feedbackScaleDataTextArray[2][3] = nintyEightPercentPlus5Text;
        feedbackScaleDataTextArray[2][4] = nintyEightPercentPlus10Text;
        
        deltaTextArray[0] = deltaMinus10Text;
        deltaTextArray[1] = deltaMinus5Text;
        deltaTextArray[2] = delta0Text;
        deltaTextArray[3] = deltaPlus5Text;
        deltaTextArray[4] = deltaPlus10Text;

        validator.validateAsDouble(newDutyCycleText, MeterParams.DC_LOW_LIMIT, 
                MeterParams.DC_HIGH_LIMIT);
        
        for (int i = 0 ; i < 5 ; i++) {
            validator.validateAsInteger(feedbackScaleDataTextArray[0][i], 1000, 15000);
            validator.validateAsDouble(feedbackScaleDataTextArray[1][i], 1000.0, 10000.0);
            validator.validateAsDouble(feedbackScaleDataTextArray[2][i], 1000.0, 10000.0);
        }

        validator.validateAsDouble(ccnmFactorText, 0.0, 2.0);
//        validator.validateAsDouble(dutyCycleBufferText);

        validator.addValidationListener(new ValidationListener() {
            public void onValidation(Object input, boolean inputValid, boolean allInputsValid) {
                if (input.equals(newDutyCycleText)) {
                    setDutyCycleButton.setEnabled(inputValid);
                }
            }
        });
        validator.validateAll();

        dutyValPreset5Percent.setOnClickListener(new DutyCycle5OnClick());
        dutyValPreset50Percent.setOnClickListener(new DutyCycle50OnClick());
        dutyValPreset98Percent.setOnClickListener(new DutyCycle98OnClick());
        setDutyCycleButton.setOnClickListener(new SetDutyCycleOnClick());
        startButton.setOnClickListener(new StartOnClick());
        acceptButton.setOnClickListener(new AcceptOnClick());
        acceptButton.setEnabled(false);
        clearAllButton.setOnClickListener(new ClearAllOnClick());
        
        for (int column = 0 ; column < 5 ; column++) {
            DialCounterTextWatcher dialCounterTextWatcher = new DialCounterTextWatcher(column);
            feedbackScaleDataTextArray[1][column].addTextChangedListener(dialCounterTextWatcher);
            feedbackScaleDataTextArray[2][column].addTextChangedListener(dialCounterTextWatcher);
        }

        ccnmFactorText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {
                checkCcnmFactor();
            }
        });
        wasStartedByMe = false;
	}
	
    @Override
    public void onPause() {
        super.onPause();
        if (MyDebug.LOG) {
            Log.d(TAG, "Fragment Paused");
        }
        endReading();
    }
    
	public class DialCounterTextWatcher implements TextWatcher {
	    int index;
	    
	    public DialCounterTextWatcher (int index) {
	        this.index = index;
	    }
	    
        @Override
        public void afterTextChanged(Editable s) {
            checkDialValues(index);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
	}
	
	@Override
    public void onFragmentSelected() {
        if (MyDebug.LOG) {
            Log.d(TAG, "Tab selected");
        }

        // This keeps any real element from getting focus.
        dummyLinearLayout.requestFocus();

        // set focus on this field and position cursor at right end of line
//        newDutyCycleText.requestFocus();
//        newDutyCycleText.setSelection(newDutyCycleText.getText().toString().length());

        AbstractBaseActivity.fragmentName = this.getClass().getSimpleName();
    }

    @Override
    public void onFragmentUnselected() {
        if (MyDebug.LOG) {
            Log.d(TAG, "Tab unselected");
        }
        endReading();

        // Hide the soft keyboard before changing tabs. 
        InputMethodManager imm = (InputMethodManager) getActivity().
                getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    private void endReading() {
        // Stop the reading if it was started in this fragment.
        MeterService meter = MeterService.getInstance();
        if (meter != null && meter.isReadingInProgress() && wasStartedByMe) {
            if (MyDebug.LOG) {
                Log.d("EndObsRaceCond", "StopsReadingLineFragment - Sending End Reading command to meter");
            }
            meter.endReading(new MyEndReadingCallback());
            wasStartedByMe = false;
        }

        currentDutyCycleText.setText("");
        beamFrequencyText.setText("");
        startButton.setEnabled(true);
    }
    
    @Override
    public void populateData() {

        FeedbackScaleParams feedbackScaleParams = null;

        try {
            feedbackScaleParams = feedbackScaleParamsDao.queryForDefault();
        } catch (SQLException e) {
            ErrorHandler errorHandler = ErrorHandler.getInstance();
            errorHandler.logError(Level.WARNING, "CalculateFeedbackScaleFragment.populateData(): " +
                    "Can't open feedbackScaleParams - " + e,
                    R.string.feedback_scale_params_file_read_error_title,
                    R.string.feedback_scale_params_file_read_error_message);
        }

        if (feedbackScaleParams != null) {
            for (int column = 0 ; column < 5 ; column++) {
                if (feedbackScaleParams.getFrequencyArray(column) != null) {
                    feedbackScaleDataTextArray[0][column].setText(convert(feedbackScaleParams.
                            getFrequencyArray(column).toString()));
                }
                for (int row = 1 ; row < 3 ; row++) {
                    if (feedbackScaleParams.getDialCounterArray(row, column) != null) {
                        feedbackScaleDataTextArray[row][column].setText(convert(feedbackScaleParams.
                                getDialCounterArray(row, column).toString()));
                    }
                }
            }

            if (feedbackScaleParams.getCcnmFactor() != null) {
                ccnmFactorText.setText(convert(feedbackScaleParams.getCcnmFactor().toString()));
            }
        }

        if (AbstractBaseActivity.fragmentName.equals(this.getClass().getSimpleName())) {
            // This keeps any real element from getting focus.
            dummyLinearLayout.requestFocus();

            // set focus on this field and position cursor at right end of line
//            newDutyCycleText.requestFocus();
//            newDutyCycleText.setSelection(newDutyCycleText.getText().toString().length());
        }
    }
    
    private void checkDialValues (int column) {
        double fivePercentNum;
        double nintyEightPercentNum;
        double deltaNum;
        double c93Num;
        double feedbackScaleNum;

        if (validator.isValid(feedbackScaleDataTextArray[1][column]) && 
            validator.isValid(feedbackScaleDataTextArray[2][column])) {

            fivePercentNum = Double.parseDouble(feedbackScaleDataTextArray[1][column].getText().
                    toString());
            nintyEightPercentNum = Double.parseDouble(feedbackScaleDataTextArray[2][column].getText().
                    toString());

            deltaNum = fivePercentNum - nintyEightPercentNum;
            deltaTextArray[column].setText(convert(new DecimalFormat("0.00").format(deltaNum)));

            if (column == 2) {
                c93Num = deltaNum/.93;
                feedbackScaleNum = c93Num/65535;
                c93Text.setText(convert(new DecimalFormat("0.00000000").format(c93Num)));
                feedbackScaleText.setText(convert(new DecimalFormat("0.#########E0").format(feedbackScaleNum)));
                checkCcnmFactor();
            }
            
        } else {
            deltaTextArray[column].setText("");
            if (column == 2) {
                c93Text.setText("");
                feedbackScaleText.setText("");
                finalFdkScaleText.setText("");
                acceptButton.setEnabled(false);
            }
        }
    }

    private void checkCcnmFactor() {
        double ccnmFactorNum;
        double finalFeedbackScaleNum;
        
        if (validator.isValid(ccnmFactorText) && feedbackScaleText.getText().toString().length() > 0) {

            ccnmFactorNum = Double.parseDouble(ccnmFactorText.getText().toString());
            finalFeedbackScaleNum = Double.parseDouble(feedbackScaleText.getText().toString()) * 
                    ccnmFactorNum;
            finalFdkScaleText.setText(convert(new DecimalFormat("0.#########E0").format(finalFeedbackScaleNum)));

            acceptButton.setEnabled(true);

        } else {
            finalFdkScaleText.setText("");
            acceptButton.setEnabled(false);
        }
    }

    private class StartOnClick implements OnClickListener {

        // The StartReading button is used so the app doesn't start communicating with meter 
        // as soon as the Setup button is tapped. Set Stops and Reading Lines might not be the 
        // option that the user intends to use.
        @Override
        public void onClick(View v) {
            // Request current DC from meter and display value when received
            MeterService meter = MeterService.getInstance();
            if (meter != null) {
                if (meter.isConnected()) {
                    meter.getDutyCycle(new MyGetDutyCycleCallback());
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
    }

    private class DutyCycle5OnClick implements OnClickListener {

        @Override
        public void onClick(View v) {
            setDutyCycle(5);
        }
    }

    private class DutyCycle50OnClick implements OnClickListener {

        @Override
        public void onClick(View v) {
            setDutyCycle(50);
        }
    }

    private class DutyCycle98OnClick implements OnClickListener {

        @Override
        public void onClick(View v) {
            setDutyCycle(98);
        }
    }

    private class SetDutyCycleOnClick implements OnClickListener {

        @Override
        public void onClick(View v) {
            if (validator.isValid(newDutyCycleText)) {
                setDutyCycle(Double.parseDouble(newDutyCycleText.getText().toString()));
            }
        }
    }

    private class ClearAllOnClick implements OnClickListener {

        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.confirm_clear_all_alert_title);
            builder.setMessage(R.string.confirm_clear_all_alert_message);
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    clearAllData();
                }
            });
            builder.setNegativeButton(R.string.no, null);
            builder.show();
        }
    }

    private void clearAllData() {
        MeterParams meterParams = null;
        FeedbackScaleParams feedbackScaleParams = null;

        try {
            meterParams = meterParamsDao.queryForDefault();
        } catch (SQLException e) {
            ErrorHandler errorHandler = ErrorHandler.getInstance();
            errorHandler.logError(Level.WARNING, "CalculateFeedbackScaleFragment.clearAllData(): " +
                    "Can't open meterParams - " + e,
                    R.string.meter_params_file_open_error_title,
                    R.string.meter_params_file_open_error_message);
        }

        if (meterParams != null) {
            meterParams.setFeedbackScale(null);
            try {
                meterParamsDao.update(meterParams);
                Toast.makeText(getActivity(), getString(R.string.feedback_scale_value_cleared), 
                        Toast.LENGTH_SHORT).show();
            } catch (SQLException e) {
                ErrorHandler errorHandler = ErrorHandler.getInstance();
                errorHandler.logError(Level.WARNING, "CalculateFeedbackScaleFragment.clearAllData(): " +
                        "Can't update meterParams - " + e,
                        R.string.meter_params_file_update_error_title,
                        R.string.meter_params_file_update_error_message);
            }
        }
        
        for (int column = 0 ; column < 5 ; column++) {
            for (int row = 0 ; row < 3 ; row++) {
                feedbackScaleDataTextArray[row][column].setText("");
            }
            deltaTextArray[column].setText("");
        }
        c93Text.setText("");
        feedbackScaleText.setText("");
        ccnmFactorText.setText("");
        finalFdkScaleText.setText("");

        try {
            feedbackScaleParams = feedbackScaleParamsDao.queryForDefault();
        } catch (SQLException e) {
            ErrorHandler errorHandler = ErrorHandler.getInstance();
            errorHandler.logError(Level.WARNING, "CalculateFeedbackScaleFragment.clearAllData(): " +
                    "Can't open feedbackScaleParams - " + e,
                    R.string.feedback_scale_params_file_open_error_title,
                    R.string.feedback_scale_params_file_open_error_message);
        }

        if (feedbackScaleParams != null) {
            for (int column = 0 ; column < 5 ; column++) {
                feedbackScaleParams.setFrequencyArray(column, null);
                for (int row = 1 ; row < 3 ; row++) {
                    feedbackScaleParams.setDialCounterArray(row, column, null);
                }
            }

            feedbackScaleParams.setCcnmFactor(null);

            try {
                feedbackScaleParamsDao.update(feedbackScaleParams);
                Toast.makeText(getActivity(), getString(R.string.feedback_scale_params_cleared), 
                        Toast.LENGTH_SHORT).show();
            } catch (SQLException e) {
                ErrorHandler errorHandler = ErrorHandler.getInstance();
                errorHandler.logError(Level.WARNING, "CalculateFeedbackScaleFragment.clearAllData(): " +
                        "Can't update feedbackScaleParams - " + e,
                        R.string.feedback_scale_params_file_update_error_title,
                        R.string.feedback_scale_params_file_update_error_message);
            }
        }
    }

    private class AcceptOnClick implements OnClickListener {

        @Override
        public void onClick(View v) {
            MeterParams meterParams = null;
            try {
                meterParams = meterParamsDao.queryForDefault();
            } catch (SQLException e) {
                ErrorHandler errorHandler = ErrorHandler.getInstance();
                errorHandler.logError(Level.WARNING, "CalculateFeedbackScaleFragment$" +
                		"AcceptOnClick.onClick(): Can't open meterParams - " + e,
                        R.string.meter_params_file_open_error_title,
                        R.string.meter_params_file_open_error_message);
            }

            if (meterParams != null) {
                if ((validator.isValid(fivePercent0Text)) && (validator.isValid(nintyEightPercent0Text)) && 
                        (validator.isValid(ccnmFactorText))) {
                    meterParams.setFeedbackScale(Double.parseDouble(finalFdkScaleText.getText().
                            toString()));
                }
                try {
                    meterParamsDao.update(meterParams);
                    Toast.makeText(getActivity(), getString(R.string.feedback_scale_value_saved), 
                            Toast.LENGTH_SHORT).show();
                } catch (SQLException e) {
                    ErrorHandler errorHandler = ErrorHandler.getInstance();
                    errorHandler.logError(Level.WARNING, "CalculateFeedbackScaleFragment$" +
                            "AcceptOnClick.onClick(): Can't update meterParams - " + e,
                            R.string.meter_params_file_update_error_title,
                            R.string.meter_params_file_update_error_message);
                }
            }

            FeedbackScaleParams feedbackScaleParams = null;
            try {
                feedbackScaleParams = feedbackScaleParamsDao.queryForDefault();
            } catch (SQLException e) {
                ErrorHandler errorHandler = ErrorHandler.getInstance();
                errorHandler.logError(Level.WARNING, "CalculateFeedbackScaleFragment$" +
                        "AcceptOnClick.onClick(): Can't open feedbackScaleParams - " + e,
                        R.string.feedback_scale_params_file_open_error_title,
                        R.string.feedback_scale_params_file_open_error_message);
            }

            if (feedbackScaleParams != null) {
                for (int column = 0 ; column < 5 ; column++) {
                    if (validator.isValid(feedbackScaleDataTextArray[0][column])) {
                        feedbackScaleParams.setFrequencyArray(column, 
                                Integer.parseInt(feedbackScaleDataTextArray[0][column].
                                        getText().toString()));
                    }
                    for (int row = 1 ; row < 3 ; row++) {
                        if (validator.isValid(feedbackScaleDataTextArray[row][column])) {
                            feedbackScaleParams.setDialCounterArray(row, column, 
                                    Double.parseDouble(feedbackScaleDataTextArray[row][column].
                                            getText().toString()));
                        }
                    }
                }
                if (validator.isValid(ccnmFactorText)) {
                    feedbackScaleParams.setCcnmFactor(Double.parseDouble(ccnmFactorText.getText().
                            toString()));
                }
                try {
                    feedbackScaleParamsDao.update(feedbackScaleParams);
                    Toast.makeText(getActivity(), getString(R.string.feedback_scale_params_saved), 
                            Toast.LENGTH_SHORT).show();
                } catch (SQLException e) {
                    ErrorHandler errorHandler = ErrorHandler.getInstance();
                    errorHandler.logError(Level.WARNING, "CalculateFeedbackScaleFragment$" +
                    		"AcceptOnClick.onClick(): Can't update feedbackScaleParams - " + e,
                            R.string.feedback_scale_params_file_update_error_title,
                            R.string.feedback_scale_params_file_update_error_message);
                }
            }
        }
    }

    private void setDutyCycle(final double newDutyCycle) {
        // Entered as %, in range of 0 - 100 (sort of), 
        // then converted for meter to the range 65534 - 1.
        final int dutyCycle = (int) (655.34 * (100.0 - newDutyCycle));
        MeterService meter = MeterService.getInstance();
        if (meter != null && meter.isConnected()) {
            meter.setDutyCycle(dutyCycle, new SetDutyCycleCallback() {

                @Override
                public void onSuccess() {
                    Toast.makeText(getActivity(), getString(R.string.set_duty_cycle_success),
                            Toast.LENGTH_LONG).show();
                    currentDutyCycleText.setText(convert(new DecimalFormat("0.0").format(newDutyCycle)));
                    // position cursor at right end of line
                    newDutyCycleText.setSelection(newDutyCycleText.getText().length());   
                }

                @Override
                public void onFailed(String reason) {
                    ErrorHandler errorHandler = ErrorHandler.getInstance();
                    errorHandler.logError(Level.WARNING, "CalculateFeedbackScaleFragment." +
                    		"setDutyCycle()$SetDutyCycleCallback.onFailed(): " +
                    		"Can't set duty cycle - " + reason,
                            R.string.set_duty_cycle_failed_title,
                            R.string.set_duty_cycle_failed_message);
                }
            });
        } else {
            showNoMeterConnectedAlert();
        }
    }

    public void showNoMeterConnectedAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.no_meter_connected_title);
        builder.setMessage(R.string.no_meter_connected_message);
        builder.setPositiveButton(R.string.ok, null);
        builder.show();
    }
    
    private class MyStartReadingCallback implements StartReadingCallback {
        @Override
        public void onSuccess() {
            if (MyDebug.LOG) {
            Log.d(TAG, "reading stared");
            }
//            Toast.makeText(getActivity(), getString(R.string.observation_started), 
//                    Toast.LENGTH_SHORT).show();
            
            // Once the reading has begun, disable the Start Reading button.          
            startButton.setEnabled(false);            
            wasStartedByMe = true;

            // Force the screen to stay on during the reading
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        @Override
        public void onFailed(String reason) {
            if (MyDebug.LOG) {
                Log.d(TAG, reason);
            }
//            Toast.makeText(getActivity(), reason, Toast.LENGTH_LONG).show();
            
            ErrorHandler errorHandler = ErrorHandler.getInstance();
            errorHandler.logError(Level.WARNING, "CalculateFeedbackScaleFragment$" +
                    "MyStartReadingCallback.onFailed(): " + reason,
                    R.string.failed_to_start_reading_title,
                    R.string.failed_to_start_reading_message);
        }
    }

    private class MyReadingResponseCallback implements ReadingResponseCallback {

        @Override
        public void onFailed(String reason) {
            if (MyDebug.LOG) {
                Log.d(TAG, reason);
            }
            Toast.makeText(getActivity(), reason, Toast.LENGTH_LONG).show();
            
            ErrorHandler errorHandler = ErrorHandler.getInstance();
            errorHandler.logError(Level.WARNING, "CalculateFeedbackFragment$" +
                    "MyReadingResponseCallback.onFailed(): Error during reading - " + reason,
                    R.string.interrupted_reading_title,
                    R.string.interrupted_reading_message);
        }
    }
    
    private class MyProcessorStateListener implements ProcessorStateListener {
        @Override
        public void onProcessorState(ProcessorState processorState) {
            if (MyDebug.LOG) {
                Log.d(TAG, "processor state changed. state=" + processorState);
            }
            beamFrequencyText.setText(convert(Integer.toString(processorState.getBeamFreq())));
        }

        @Override
        public void onError(String reason) {
            Toast.makeText(getActivity(), reason, Toast.LENGTH_LONG).show();
            if (MyDebug.LOG) {
                Log.d(TAG, "processor state failed. " + reason);
            }
        }
    }

    private class MyGetDutyCycleCallback implements GetDutyCycleCallback {
        @Override
        public void onSuccess(double dutyCycle) {
            if (MyDebug.LOG) {
                Log.d(TAG, "Duty Cycle from Meter: " + dutyCycle);
            }
            currentDutyCycleText.setText(convert(new DecimalFormat("0.0").format(dutyCycle)));

            MeterService meter = MeterService.getInstance();
            if (meter != null && meter.isConnected()) {
                meter.startReading(ObservationType.READ_METER, null, null, 
                        new MyStartReadingCallback(), new MyReadingResponseCallback(),
                        new MyProcessorStateListener());

                // This next block was moved to MyStartReadingCallback.onSuccess
                // Once the reading has begun, disable the Start Reading button.          
//                startButton.setEnabled(false);            
//                wasStartedByMe = true;

                // Force the screen to stay on during the reading
//                getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }

        @Override
        public void onFailed(String reason) {
            if (MyDebug.LOG) {
                Log.d(TAG, "Get Duty Cycle Failed. " + reason);
            }

            ErrorHandler errorHandler = ErrorHandler.getInstance();
            errorHandler.logError(Level.WARNING, "CalculateFeedbackScaleFragment$" +
                    "MyGetDutyCycleCallback.onFailed(): Can't get duty cycle - " + reason,
                    R.string.get_duty_cycle_failed_title,
                    R.string.get_duty_cycle_failed_message);
        }
    }
    
    private class MyEndReadingCallback implements EndReadingCallback {
        @Override
        public void onSuccess() {
            if (MyDebug.LOG) {
                Log.d(TAG, "reading ended");
            }
            Toast.makeText(MeterService.getInstance(), getString(R.string.ended_reading_success),
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onFailed(String reason) {
            if (MyDebug.LOG) {
                Log.d(TAG, "reading end failed. " + reason);
            }
//            Toast.makeText(MeterService.getInstance(), getString(R.string.ended_reading_failed),
//                    Toast.LENGTH_LONG).show();
            
            ErrorHandler errorHandler = ErrorHandler.getInstance();
            errorHandler.logError(Level.WARNING, "CalculateFeedbackScaleFragment$" +
                    "MyEndReadingCallback.onFailed(): Can't end reading - " + reason,
                    R.string.failed_to_end_reading_title,
                    R.string.failed_to_end_reading_message);
        }
    }
}
