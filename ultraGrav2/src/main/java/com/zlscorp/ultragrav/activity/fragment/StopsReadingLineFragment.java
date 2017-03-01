package com.zlscorp.ultragrav.activity.fragment;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.logging.Level;

import roboguice.inject.InjectView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
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
import com.zlscorp.ultragrav.model.MeterParams;
import com.zlscorp.ultragrav.persist.MeterParamsDao;
import com.zlscorp.ultragrav.type.ObservationType;

public class StopsReadingLineFragment extends AbstractBaseFragment implements OnFragmentSelectedListener {

    private static String TAG = "StopsReadingLineFragment";

    @Inject
    private MeterParamsDao meterParamsDao;

    @Inject
    private Validator validator;

    @InjectView(R.id.dummyLinearLayout)
    private LinearLayout dummyLinearLayout;

    @InjectView(R.id.dutyValPreset10Percent)
    private Button dutyValPreset10Percent;

    @InjectView(R.id.dutyValPreset50Percent)
    private Button dutyValPreset50Percent;

    @InjectView(R.id.dutyValPreset90Percent)
    private Button dutyValPreset90Percent;

    @InjectView(R.id.currentDutyCycleText)
    private TextView currentDutyCycleText;

    @InjectView(R.id.newDutyCycleText)
    private EditText newDutyCycleText;

    @InjectView(R.id.setDutyCycleButton)
    private Button setDutyCycleButton;

    @InjectView(R.id.beamFrequencyText)
    private TextView beamFrequencyText;

    @InjectView(R.id.maxStopText)
    private EditText maxStopText;

    @InjectView(R.id.minStopText)
    private EditText minStopText;

    @InjectView(R.id.readingLineText)
    private TextView readingLineText;

    @InjectView(R.id.startButton)
    private Button startButton;

    @InjectView(R.id.acceptButton)
    private Button acceptButton;

    private boolean wasStartedByMe;
    SharedPreferences pref;
//    PrivateActivity parent;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (MyDebug.LOG) {
            Log.d(TAG, "inflating fragment Set Stops and Reading Line");
        }
//        parent = (PrivateActivity)getActivity();
        pref = getActivity().getSharedPreferences("language", 0);
        View v = inflater.inflate(R.layout.fragment_stops_reading_line, container, false);
        return v;
    }

    String convert(String str)
    {
    	int kind = pref.getInt("lan", 0);
    	if (kind == 1)
    		return str.replace(',', '.');
    	else
    		return str;
    }
    @Override
    public void setupView(View view, Bundle savedInstanceState) {

        validator.validateAsDouble(newDutyCycleText, MeterParams.DC_LOW_LIMIT, 
                MeterParams.DC_HIGH_LIMIT);
        validator.validateAsInteger(maxStopText, 1000, 15000);
        validator.validateAsInteger(minStopText, 1000, 15000);

        validator.addValidationListener(new ValidationListener() {
            public void onValidation(Object input, boolean inputValid, boolean allInputsValid) {
                if (input.equals(newDutyCycleText)) {
                    setDutyCycleButton.setEnabled(inputValid);
                }
            }
        });
        validator.validateAll();

        dutyValPreset10Percent.setOnClickListener(new DutyCycle10OnClick());
        dutyValPreset50Percent.setOnClickListener(new DutyCycle50OnClick());
        dutyValPreset90Percent.setOnClickListener(new DutyCycle90OnClick());
        setDutyCycleButton.setOnClickListener(new SetDutyCycleOnClick());
        startButton.setOnClickListener(new StartOnClick());
        acceptButton.setOnClickListener(new AcceptOnClick());
        acceptButton.setEnabled(false);
        
        maxStopText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {
                checkBeamFreqValues();
            }
        });

        minStopText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {
                checkBeamFreqValues();
            }
        });

        maxStopText.setOnFocusChangeListener(new OnFocusChangeListener() {          
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && maxStopText.getText().toString() != null) {
                    maxStopText.setSelection(maxStopText.getText().toString().length());   // position cursor at right end of line
                }
            }
        });

        minStopText.setOnFocusChangeListener(new OnFocusChangeListener() {          
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && minStopText.getText().toString() != null) {
                    minStopText.setSelection(minStopText.getText().toString().length());   // position cursor at right end of line
                }
            }
        });

        newDutyCycleText.setOnFocusChangeListener(new OnFocusChangeListener() {          
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && newDutyCycleText.getText().toString() != null) {
                    newDutyCycleText.setSelection(newDutyCycleText.getText().toString().length());   // position cursor at right end of line
                }
            }
        });
        wasStartedByMe = false;
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
        // Stop the reading.
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
    
    private void checkBeamFreqValues() {
        if (validator.isValid(maxStopText) && validator.isValid(minStopText)) {
            calculateReadingLine();
            acceptButton.setEnabled(true);
        } else {
            readingLineText.setText("");
            acceptButton.setEnabled(false);
        }
    }

    private void calculateReadingLine() {
        Integer maxStopInt = null;
        Integer minStopInt = null;
        
        maxStopInt = Integer.parseInt(maxStopText.getText().toString());
//        String maxStopString = maxStopText.getText().toString();
//        if (maxStopString.length() > 0) {
//            try {
//                maxStopInt = Integer.parseInt(maxStopString);
//            } catch (Exception e) {
//                if (MyDebug.LOG) {
//                    Log.e(TAG, "maxStopString is not an integer.", e);
//                }
//            }
//        } else {
//            return;
//        }

        minStopInt = Integer.parseInt(minStopText.getText().toString());
//        String minStopString = minStopText.getText().toString();
//        if (minStopString.length() > 0) {
//            try {
//                minStopInt = Integer.parseInt(minStopString);
//            } catch (Exception e) {
//                if (MyDebug.LOG) {
//                    Log.e(TAG, "minStopString is not an integer.", e);
//                }
//            }
//        } else {
//            return;
//        }

        if ((maxStopInt != null) && (minStopInt != null)) {
            readingLineText.setText(Integer.toString((maxStopInt + minStopInt)/2));
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        endReading();
    }
    
    @Override
    public void populateData() {
        MeterParams meterParams = null;
        
        try {
            // Test
//            boolean test = false;
//            if (test) {
//                throw new SQLException("test");
//            }
            
            meterParams = meterParamsDao.queryForDefault();
        } catch (SQLException e) {
            ErrorHandler errorHandler = ErrorHandler.getInstance();
            errorHandler.logError(Level.WARNING, "StopsReadingLineFragment.populateData(): " +
                    "Can't open meterParams - " + e,
                    R.string.meter_params_file_open_error_title,
                    R.string.meter_params_file_open_error_message);
        }

        if (meterParams != null) {
            if (meterParams.getMaxStop() != null) {
                maxStopText.setText(convert(meterParams.getMaxStop().toString()));
            }
            if (meterParams.getMinStop() != null) {
                minStopText.setText(convert(meterParams.getMinStop().toString()));
            }
            if (meterParams.getReadingLine() != null) {
                readingLineText.setText(convert(meterParams.getReadingLine().toString()));
            }
        }

        if (AbstractBaseActivity.fragmentName != null && 
                AbstractBaseActivity.fragmentName.equals(this.getClass().getSimpleName())) {
            // This keeps any real element from getting focus.
            dummyLinearLayout.requestFocus();

            // set focus on this field and position cursor at right end of line
//            newDutyCycleText.requestFocus();
//            newDutyCycleText.setSelection(newDutyCycleText.getText().toString().length());
        }
    }

    @Override
    public void persistData() {
        // The data is only saved when the Accept button is pressed,
        // not when the activity is paused.
    }

    private class StartOnClick implements OnClickListener {

        // The StartReading button is used so the app doesn't start communicating with meter as soon as the Setup button
        // is tapped. Set Stops and Reading Lines might not be the option that the user intends to use.
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

    private class DutyCycle10OnClick implements OnClickListener {

        @Override
        public void onClick(View v) {
            setDutyCycle(10);
        }
    }

    private class DutyCycle50OnClick implements OnClickListener {

        @Override
        public void onClick(View v) {
            setDutyCycle(50);
        }
    }

    private class DutyCycle90OnClick implements OnClickListener {

        @Override
        public void onClick(View v) {
            setDutyCycle(90);
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

    private class AcceptOnClick implements OnClickListener {

        @Override
        public void onClick(View v) {
            MeterParams meterParams = null;
            
            try {
                // Test
//                boolean test = false;
//                if (test) {
//                    throw new SQLException("test");
//                }
                
                meterParams = meterParamsDao.queryForDefault();
            } catch (SQLException e) {
                ErrorHandler errorHandler = ErrorHandler.getInstance();
                errorHandler.logError(Level.WARNING, "StopsReadingLineFragment$AcceptOnClick.onClick(): " +
                        "Can't open meterParams - " + e, 
                        R.string.meter_params_file_open_error_title,
                        R.string.meter_params_file_open_error_message);
            }

            if (meterParams != null) {
                if ((validator.isValid(maxStopText)) && (validator.isValid(minStopText))) {
                    meterParams.setMaxStop(Integer.parseInt(maxStopText.getText().toString()));
                    meterParams.setMinStop(Integer.parseInt(minStopText.getText().toString()));
                    meterParams.setReadingLine(Integer.parseInt(readingLineText.getText().toString()));
                }
                try {
                    // Test
//                    boolean test = false;
//                    if (test) {
//                        throw new SQLException("test");
//                    }
                    
                    meterParamsDao.update(meterParams);
                    Toast.makeText(getActivity(), getString(R.string.stops_reading_line_saved), Toast.LENGTH_SHORT).show();
                } catch (SQLException e) {
                    ErrorHandler errorHandler = ErrorHandler.getInstance();
                    errorHandler.logError(Level.WARNING, "StopsReadingLineFragment$AcceptOnClick.onClick(): " +
                            "Can't update meterParams - " + e,
                            R.string.meter_params_file_update_error_title,
                            R.string.meter_params_file_update_error_message);
                }
            }
        }
    }

    private void setDutyCycle(final double newDutyCycle) {
        // Entered as %, in range of 0 - 100 (sort of), then converted for meter to the range 65534 - 1.
        final int dutyCycle = (int) (655.34 * (100.0 - newDutyCycle));
        MeterService meter = MeterService.getInstance();
        if (meter != null && meter.isConnected()) {
            meter.setDutyCycle(dutyCycle, new SetDutyCycleCallback() {

                @Override
                public void onSuccess() {
                    Toast.makeText(getActivity(), getString(R.string.set_duty_cycle_success),
                            Toast.LENGTH_LONG).show();
                    currentDutyCycleText.setText(convert(new DecimalFormat("0.0").format(newDutyCycle)));
                    newDutyCycleText.setSelection(newDutyCycleText.getText().length());   // position cursor at right end of line
                }

                @Override
                public void onFailed(String reason) {
                    ErrorHandler errorHandler = ErrorHandler.getInstance();
                    errorHandler.logError(Level.WARNING, "StopsReadingLineFragment.setDutyCycle()$" +
                            "SetDutyCycleCallback.onFailed(): Can't set duty cycle - " + reason,
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
                Log.d(TAG, "observation stared");
            }
//            Toast.makeText(getActivity(), getString(R.string.observation_started), Toast.LENGTH_SHORT).show();

            // Once a reading has been started, disable the button.
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
            errorHandler.logError(Level.WARNING, "StopsReadingLineFragment$" +
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
            errorHandler.logError(Level.WARNING, "StopsReadingLineFragment$" +
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
            beamFrequencyText.setText(Integer.toString(processorState.getBeamFreq()));
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
//            Toast.makeText(getActivity(), "Got DC from Meter", Toast.LENGTH_SHORT).show();
            currentDutyCycleText.setText(convert(new DecimalFormat("0.0").format(dutyCycle)));

            MeterService meter = MeterService.getInstance();
            if (meter != null && meter.isConnected()) {
                meter.startReading(ObservationType.READ_METER, null, null, new MyStartReadingCallback(),
                        new MyReadingResponseCallback(), new MyProcessorStateListener());

                // This next block was moved to MyStartReadingCallback.onSuccess
                // Once a reading has been started, disable the button.
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
            errorHandler.logError(Level.SEVERE, "StopsReadingLineFragment$" +
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
            errorHandler.logError(Level.WARNING, "StopsReadingLineFragment$" +
                    "MyEndReadingCallback.onFailed(): Can't end reading - " + reason,
                    R.string.failed_to_end_reading_title,
                    R.string.failed_to_end_reading_message);
        }
    }
}
