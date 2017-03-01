package com.zlscorp.ultragrav.activity.fragment;

import roboguice.inject.InjectView;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
//import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.logging.Level;

import com.google.inject.Inject;
import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.activity.AbstractBaseActivity;
import com.zlscorp.ultragrav.activity.PrivateActivity;
//import com.zlscorp.ultragrav.activity.SetupActivity;
import com.zlscorp.ultragrav.activity.validate.Validator;
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
import com.zlscorp.ultragrav.type.ObservationType;

public class ReadMeterFragment extends AbstractBaseFragment implements OnFragmentSelectedListener {

    private static String TAG = "ReadMeterFragment";

    private static final String ACTIVITY_KEY = "parentActivity";

    @Inject
    private Validator validator;

    @InjectView(R.id.dummyLinearLayout)
    private LinearLayout dummyLinearLayout;

    @InjectView(R.id.elapsedTimeText)
    private TextView elapsedTimeText;

    @InjectView(R.id.meterFrequency)
    private TextView meterFrequency;

    @InjectView(R.id.newDutyCycle)
    private EditText newDutyCycle;

    @InjectView(R.id.crossLevelFrequency)
    private TextView crossLevelFrequency;

    @InjectView(R.id.longLevelFrequency)
    private TextView longLevelFrequency;

    @InjectView(R.id.temperatureValue)
    private TextView temperatureValue;

    @InjectView(R.id.currentDutyCycle)
    private TextView currentDutyCycle;

    @InjectView(R.id.setButton)
    Button setButton;

    @InjectView(R.id.startReadingButton)
    Button startReadingButton;

    private boolean wasStartedByMe;              // A reading, that is
    
    private ParentActivity parentActivity;
    
    public static Bundle createArgs(ParentActivity parentActivity) {
        Bundle args = new Bundle();
        args.putSerializable(ACTIVITY_KEY, parentActivity);
        return args;
    }

    public static enum ParentActivity {
        SETUP,
        PRIVATE;
    }
    SharedPreferences pref;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = getActivity().getSharedPreferences("language", 0);
        Bundle args = getArguments();
        parentActivity = (ParentActivity) args.getSerializable(ACTIVITY_KEY);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_read_meter, container, false);
//        parent = (PrivateActivity)getActivity();
        return v;
    }

    @Override
    public void setupView(View view, Bundle savedInstanceState) {

        validator.validateAsDouble(newDutyCycle, MeterParams.DC_LOW_LIMIT, 
                MeterParams.DC_HIGH_LIMIT);

        setButton.setOnClickListener(new SetDutyCycleOnClick());
        startReadingButton.setOnClickListener(new StartReadingOnClick());
        
        newDutyCycle.setOnFocusChangeListener(new OnFocusChangeListener() {          
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && newDutyCycle.getText().toString() != null) {
                    newDutyCycle.setSelection(newDutyCycle.getText().toString().length());   // position cursor at right end of line
                }
            }
        });
        
        wasStartedByMe = false;
    }

    @Override
    public void onResume() {
        if (MyDebug.LOG) {
            Log.d(TAG, "onResume");
        }

        super.onResume();

        if (parentActivity == ParentActivity.SETUP) {
            AbstractBaseActivity.fragmentName = this.getClass().getSimpleName();

            // This keeps any real element from getting focus.
            dummyLinearLayout.requestFocus();
            
            // set focus on this field and position cursor at right end of line
//            newDutyCycle.requestFocus();
//            newDutyCycle.setSelection(newDutyCycle.getText().toString().length());
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        endReading();
    }
    
	@Override
	public void onFragmentSelected() {
        if (MyDebug.LOG) {
            Log.d(TAG, "Tab selected");
        }

        // This keeps any real element from getting focus.
        dummyLinearLayout.requestFocus();

        // set focus on this field and position cursor at right end of line
//		newDutyCycle.requestFocus();
//        newDutyCycle.setSelection(newDutyCycle.getText().toString().length());

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
        // Stop the reading
        MeterService meter = MeterService.getInstance();
        if (meter != null && meter.isReadingInProgress() && wasStartedByMe) {
            if (MyDebug.LOG) {
                Log.d("EndObsRaceCond", "StopsReadingLineFragment - Sending End Reading command to meter");
            }
            meter.endReading(new ReadMeterEndReadingCallback());
            wasStartedByMe = false;
        }

        elapsedTimeText.setText("");
        meterFrequency.setText("");
        crossLevelFrequency.setText("");
        longLevelFrequency.setText("");
        temperatureValue.setText("");
        currentDutyCycle.setText("");
        
        startReadingButton.setEnabled(true);
        
        // Test
//        meter.endReadingCallbackTest();
    }
//    PrivateActivity parent;
    private class StartReadingOnClick implements OnClickListener {

        // The StartReading button is used so the fragment doesn't start communicating with 
        // the meter as soon as the Meter Setup button is tapped.
        // Read Meter might not be the option that the user intends to use.
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
    
    public void showNoMeterConnectedAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.no_meter_connected_title);
        builder.setMessage(R.string.no_meter_connected_message);
        builder.setPositiveButton(R.string.ok, null);
        builder.show();
    }
    
    String convert(String str)
    {
    	int kind = pref.getInt("lan", 0);
    	if (kind == 1)
    		return str.replace(',', '.');
    	else 
    		return str;
    }
    private class SetDutyCycleOnClick implements OnClickListener {

        @Override
        public void onClick(View v) {

            if (validator.isValid(newDutyCycle)) {
                // Entered as %, in range of 0 - 100 (sort of), then converted 
                // for meter to the range 65534 - 1.
                final int dutyCycle = (int) (655.34 * (100.0 - Double.parseDouble(
                        newDutyCycle.getText().toString())));
                MeterService meter = MeterService.getInstance();
                if (meter != null) {
                    if (meter.isConnected()) {
                        meter.setDutyCycle(dutyCycle, new SetDutyCycleCallback() {

                            @Override
                            public void onSuccess() {
                                Toast.makeText(getActivity(), getString(R.string.set_duty_cycle_success),
                                        Toast.LENGTH_LONG).show();
                                currentDutyCycle.setText(convert(new DecimalFormat("0.0").format(
                                        Double.parseDouble(newDutyCycle.getText().toString()))));
                                // position cursor at right end of line
                                newDutyCycle.setSelection((newDutyCycle.getText().length()));
//                                    newDutyCycle.setText("");
                            }

                            @Override
                            public void onFailed(String reason) {
                                ErrorHandler errorHandler = ErrorHandler.getInstance();
                                errorHandler.logError(Level.WARNING, "ReadMeterFragment$" +
                                		"SetDutyCycleOnClick.onClick(): Can't set duty cycle - " + reason,
                                        R.string.set_duty_cycle_failed_title,
                                        R.string.set_duty_cycle_failed_message);
                            }
                        });
                    } else {
                        showNoMeterConnectedAlert();
                    }
                }
            }
        }
    }

    private class MyStartReadingCallback implements StartReadingCallback {
        @Override
        public void onSuccess() {
            if (MyDebug.LOG) {
                Log.d(TAG, "reading started");
            }

            startReadingButton.setEnabled(false);     // Once a reading has been started, disable the button.
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
            errorHandler.logError(Level.WARNING, "ReadMeterFragment$" +
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
            errorHandler.logError(Level.WARNING, "ReadMeterFragment$" +
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
            long elapsedTime = processorState.getElapsedTime();
            if (elapsedTime != 0) {
                int minsPart = (int) (elapsedTime / 60);
                int secondsPart = (int) (elapsedTime % 60);
                if (secondsPart < 10) {
                    elapsedTimeText.setText(minsPart + ":0" + secondsPart);
                } else {
                    elapsedTimeText.setText(minsPart + ":" + secondsPart);
                }
            }
            meterFrequency.setText(convert(Integer.toString(processorState.getBeamFreq())));
            crossLevelFrequency.setText(convert(Integer.toString(processorState.getCrossLevelFreq())));
            longLevelFrequency.setText(convert(Integer.toString(processorState.getLongLevelFreq())));
            temperatureValue.setText(convert(Integer.toString(processorState.getTemperatureFrequency())));
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
            currentDutyCycle.setText(convert(new DecimalFormat("0.0").format(dutyCycle)));

            MeterService meter = MeterService.getInstance();
            if (meter != null && meter.isConnected()) {
                meter.startReading(ObservationType.READ_METER, null, null, 
                        new MyStartReadingCallback(), new MyReadingResponseCallback(),
                        new MyProcessorStateListener());
                
                // This next block was moved to MyStartReadingCallback.onSuccess
                // Once a reading has been started, disable the button.
//                startReadingButton.setEnabled(false);     
//                wasStartedByMe = true;
//                
//                // Force the screen to stay on during the reading
//                getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }

        @Override
        public void onFailed(String reason) {
            if (MyDebug.LOG) {
                Log.d(TAG, "Get Duty Cycle Failed. " + reason);
            }

            ErrorHandler errorHandler = ErrorHandler.getInstance();
            errorHandler.logError(Level.WARNING, "ReadMeterFragment$" +
                    "MyGetDutyCycleCallback.onFailed(): Can't get duty cycle - " + reason,
                    R.string.get_duty_cycle_failed_title,
                    R.string.get_duty_cycle_failed_message);
        }
    }

    private class ReadMeterEndReadingCallback implements EndReadingCallback {
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
            errorHandler.logError(Level.WARNING, "ReadMeterFragment$" +
                    "MyEndReadingCallback.onFailed(): Can't end reading - " + reason,
                    R.string.failed_to_end_reading_title,
                    R.string.failed_to_end_reading_message);
        }
    }
}
