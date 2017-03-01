package com.zlscorp.ultragrav.activity.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentActivity;
//import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.logging.Level;

import com.google.inject.Inject;
//import com.viewpagerindicator.TabPageIndicator;
import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.activity.AbstractBaseActivity;
import com.zlscorp.ultragrav.activity.PrivateActivity;
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
import com.zlscorp.ultragrav.persist.MeterParamsDao;
import com.zlscorp.ultragrav.type.ObservationType;

import roboguice.inject.InjectView;

public class CalculateBeamScaleFragment extends AbstractBaseFragment implements OnFragmentSelectedListener {

    private static final String TAG = "CalculateBeamScaleFragment";
    
    private static final String MODE_KEY = "mode";

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

    @InjectView(R.id.dutyCycleText)
    private TextView dutyCycleText;

    @InjectView(R.id.beamFrequencyText)
    private TextView beamFrequencyText;

    @InjectView(R.id.baseDialReadingValue)
    private EditText baseDialReadingValue;

    @InjectView(R.id.baseDrMinus250Label)
    private TextView baseDrMinus250Label;

    @InjectView(R.id.baseDrMinus250Text)
    private TextView baseDrMinus250Text;

    @InjectView(R.id.baseDrPlus250Label)
    private TextView baseDrPlus250Label;

    @InjectView(R.id.baseDrPlus250Text)
    private TextView baseDrPlus250Text;

    @InjectView(R.id.minus250BeamFreqLabel)
    private TextView minus250BeamFreqLabel;

    @InjectView(R.id.minus250BeamFreqValue)
    private EditText minus250BeamFreqValue;

    @InjectView(R.id.plus250BeamFreqLabel)
    private TextView plus250BeamFreqLabel;

    @InjectView(R.id.plus250BeamFreqValue)
    private EditText plus250BeamFreqValue;

    @InjectView(R.id.deltaFreqText)
    private TextView deltaFreqText;

    @InjectView(R.id.beamScaleText)
    private TextView beamScaleText;

    @InjectView(R.id.startButton)
    private Button startButton;

    @InjectView(R.id.acceptButton)
    private Button acceptButton;

//    @InjectView(R.id.indicator)
//    private TabPageIndicator indicator;
    
//    PrivateActivity parent;
    private Mode mode;
    private boolean wasStartedByMe;
    
    public static Bundle createArgs(Mode mode) {
    	Bundle args = new Bundle();
    	args.putSerializable(MODE_KEY, mode);
    	return args;
    }

    public static enum Mode {
        BEAM_K,
        CCJR_BEAM_K;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	Bundle args = getArguments();
    	mode = (Mode) args.getSerializable(MODE_KEY);
    }
    SharedPreferences pref;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (MyDebug.LOG) {
            Log.d(TAG, "inflating fragment Calculate Beam Scale");
        }
        View v = inflater.inflate(R.layout.fragment_calculate_beam_scale, container, false);
        pref = getActivity().getSharedPreferences("language", 0);
//        parent = (PrivateActivity) getActivity();
        return v;
    }

    String convert(String str)
    {
    	int kind = pref.getInt("lan", 0);
    	if  (kind == 1)
    		return str.replace(',',  '.');
    	else 
    		return str;
    }
    @Override
    public void setupView(View view, Bundle savedInstanceState) {
        validator.validateAsDouble(baseDialReadingValue);
        validator.validateAsInteger(minus250BeamFreqValue, 1000, 15000);
        validator.validateAsInteger(plus250BeamFreqValue, 1000, 15000);

//        dutyValPreset10Percent.setOnClickListener(new DutyCycle10OnClick());
//        dutyValPreset90Percent.setOnClickListener(new DutyCycle90OnClick());
        
        dutyValPreset50Percent.setOnClickListener(new DutyCycle50OnClick());

        //These two buttons are just used for visual spacing
        dutyValPreset10Percent.setVisibility(View.INVISIBLE);
        dutyValPreset90Percent.setVisibility(View.INVISIBLE);

        startButton.setOnClickListener(new StartOnClick());
        acceptButton.setOnClickListener(new AcceptOnClick());
        acceptButton.setEnabled(false);
        
        baseDialReadingValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (validator.isValid(baseDialReadingValue)) {
                    double baseDialReading = Double.parseDouble(baseDialReadingValue.getText().toString());
                    if (mode == Mode.BEAM_K) {
                        baseDrMinus250Text.setText(convert(Double.toString(baseDialReading - .25)));
                        baseDrPlus250Text.setText(convert(Double.toString(baseDialReading + .25)));
                    } else {
                        baseDrMinus250Text.setText(convert(Double.toString(baseDialReading - .5)));
                        baseDrPlus250Text.setText(convert(Double.toString(baseDialReading + .5)));
                    }
                }
            }
        });

        minus250BeamFreqValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkBeamFreqValues();
            }
        });

        plus250BeamFreqValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkBeamFreqValues();
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
    
    @Override
    public void onFragmentSelected() {
        if (MyDebug.LOG) {
            Log.d(TAG, "Tab selected");
        }
        if (mode == Mode.BEAM_K) {
            AbstractBaseActivity.fragmentName = this.getClass().getSimpleName();

            baseDrMinus250Label.setText(R.string.base_dr_minus_250);
            baseDrPlus250Label.setText(R.string.base_dr_plus_250);
            minus250BeamFreqLabel.setText(R.string.minus_250_beam_freq);
            plus250BeamFreqLabel.setText(R.string.plus_250_beam_freq);
        } else {
            AbstractBaseActivity.fragmentName = "CCJR" + this.getClass().getSimpleName();

            baseDrMinus250Label.setText(R.string.base_dr_minus_500);
            baseDrPlus250Label.setText(R.string.base_dr_plus_500);
            minus250BeamFreqLabel.setText(R.string.minus_500_beam_freq);
            plus250BeamFreqLabel.setText(R.string.plus_500_beam_freq);
        }
        
        // This keeps any real element from getting focus.
        dummyLinearLayout.requestFocus();
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

        dutyCycleText.setText("");
        beamFrequencyText.setText("");
        startButton.setEnabled(true);
    }
    
    private void checkBeamFreqValues() {
        if (isGoodDeltaFreq()) {
            acceptButton.setEnabled(true);
        } else {
            deltaFreqText.setText("");
            beamScaleText.setText("");
            acceptButton.setEnabled(false);
        }
    }

    private boolean isGoodDeltaFreq() {
        int minus250BeamFreqInt;
        int plus250BeamFreqInt;

        if (validator.isValid(minus250BeamFreqValue) && validator.isValid(plus250BeamFreqValue)) {
            minus250BeamFreqInt = Integer.parseInt(minus250BeamFreqValue.getText().toString());
            plus250BeamFreqInt = Integer.parseInt(plus250BeamFreqValue.getText().toString());

            int deltaFreqInt = minus250BeamFreqInt - plus250BeamFreqInt;
            deltaFreqText.setText(Integer.toString(deltaFreqInt));

            // Beam Scale = .5 / deltaFreqInt
            beamScaleText.setText(convert(new DecimalFormat("0.#########E0").format(.5 / deltaFreqInt)));

            return true;
            
        } else {
            return false;
        }
    }

    private class DutyCycle50OnClick implements OnClickListener {

        @Override
        public void onClick(View v) {
            setDutyCycle(50);
        }
    }

    private void setDutyCycle(final int newDutyCycle) {
        // Entered as %, in range of 0 - 100 (sort of), then converted for meter to the range 65534 - 1.
        final int dutyCycle = (int) (655.34 * (100 - newDutyCycle));
        MeterService meter = MeterService.getInstance();
        if (meter != null && meter.isConnected()) {
            meter.setDutyCycle(dutyCycle, new SetDutyCycleCallback() {

                @Override
                public void onSuccess() {
                    Toast.makeText(getActivity(), getString(R.string.set_duty_cycle_success),
                            Toast.LENGTH_LONG).show();
                    dutyCycleText.setText(convert(new DecimalFormat("0.0").format((double) newDutyCycle)));
                }

                @Override
                public void onFailed(String reason) {
                    ErrorHandler errorHandler = ErrorHandler.getInstance();
                    errorHandler.logError(Level.WARNING, "CalculateBeamScaleFragment.setDutyCycle()$" +
                    		"SetDutyCycleCallback.onFailed(): Can't set duty cycle - " + reason,
                            R.string.set_duty_cycle_failed_title,
                            R.string.set_duty_cycle_failed_message);
                }
            });
        } else {
            showNoMeterConnectedAlert();
        }
    }

    private class StartOnClick implements OnClickListener {

        // The StartReading button is used so the app doesn't start communicating with meter as soon as the
        // activity/fragment is displayed.
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

    private class AcceptOnClick implements OnClickListener {

        @Override
        public void onClick(View v) {
            MeterParams meterParams = null;
            try {
                // Test
                // This is a test . . .
//              throw new SQLException("test");
              
                meterParams = meterParamsDao.queryForDefault();

            } catch (SQLException e) {
                ErrorHandler errorHandler = ErrorHandler.getInstance();
                errorHandler.logError(Level.WARNING, "CalculateBeamScaleFragment$AcceptOnClick.onClick(): " +
                        "Can't open meterParams - " + e,
                        R.string.beam_scale_save_error_title,
                        R.string.meter_params_file_open_error_message);
            }

            if (meterParams != null) {
                meterParams.setBeamScale(Double.parseDouble(beamScaleText.getText().toString()));

                try {
                    //Test
//                    throw new SQLException("test");
                  
                    meterParamsDao.update(meterParams);
                    Toast.makeText(getActivity(), getString(R.string.beam_scale_saved), 
                            Toast.LENGTH_SHORT).show();
                } catch (SQLException e) {
                    ErrorHandler errorHandler = ErrorHandler.getInstance();
                    errorHandler.logError(Level.WARNING, "CalculateBeamScaleFragment$" +
                    		"AcceptOnClick.onClick(): Can't update meterParams - " + e,
                            R.string.beam_scale_save_error_title,
                            R.string.meter_params_file_update_error_message);
                }
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
    
    private class MyStartReadingCallback implements StartReadingCallback {
        @Override
        public void onSuccess() {
            if (MyDebug.LOG) {
                Log.d(TAG, "reading stared");
            }
//            Toast.makeText(getActivity(), getString(R.string.observation_started), Toast.LENGTH_SHORT).show();

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
            errorHandler.logError(Level.WARNING, "CalculateBeamScaleFragment$" +
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
            errorHandler.logError(Level.WARNING, "CalculateBeamScaleFragment$" +
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
            dutyCycleText.setText(convert(new DecimalFormat("0.0").format(dutyCycle)));

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
            errorHandler.logError(Level.WARNING, "CalculateBeamScaleFragment$" +
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
            errorHandler.logError(Level.WARNING, "CalculateBeamScaleFragment$" +
                    "MyEndReadingCallback.onFailed(): Can't end reading - " + reason,
                    R.string.failed_to_end_reading_title,
                    R.string.failed_to_end_reading_message);
        }
    }
}
