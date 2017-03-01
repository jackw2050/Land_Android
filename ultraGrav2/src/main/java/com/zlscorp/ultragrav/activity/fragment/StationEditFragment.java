package com.zlscorp.ultragrav.activity.fragment;

import roboguice.inject.InjectView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
//import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.logging.Level;

import com.google.inject.Inject;
import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.activity.AbstractBaseActivity;
import com.zlscorp.ultragrav.activity.validate.Validator;
import com.zlscorp.ultragrav.activity.validate.Validator.ValidationListener;
import com.zlscorp.ultragrav.debug.MyDebug;
import com.zlscorp.ultragrav.file.ErrorHandler;
import com.zlscorp.ultragrav.model.Station;
import com.zlscorp.ultragrav.model.StationSeries;
import com.zlscorp.ultragrav.persist.AbstractDao.TransactionCallback;
import com.zlscorp.ultragrav.persist.StationDao;

public class StationEditFragment extends AbstractBaseFragment implements LocationListener {

    private static String TAG = "StationEditFragment";

//    @Inject
//    private StationSeriesDao stationSeriesDao;

    @Inject
    private StationDao stationDao;

    @Inject
    private Validator validator;

    @InjectView(R.id.dummyLinearLayout)
    private LinearLayout dummyLinearLayout;

    @InjectView(R.id.stationIdText)
    private EditText stationIdText;

    @InjectView(R.id.observerIdText)
    private EditText observerIdText;

    @InjectView(R.id.latitudeText)
    private EditText latitudeText;

    @InjectView(R.id.longitudeText)
    private EditText longitudeText;

    @InjectView(R.id.elevationText)
    private EditText elevationText;

    @InjectView(R.id.meterHeightText)
    private EditText meterHeightText;

    @InjectView(R.id.gpsButton)
    private Button gpsButton;

    @InjectView(R.id.earthTideCheckBox)
    private CheckBox earthTideCheckBox;

    @InjectView(R.id.saveButton)
    private Button saveButton;

    @InjectView(R.id.cancelButton)
    private Button cancelButton;

    private boolean isCreate;
    private StationSeries series;
    private Station station;
    private boolean isGpsEnabled;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_station_edit, container, false);
        return v;
    }

    @Override
    public void setupView(View view, Bundle savedInstanceState) {

        AbstractBaseActivity.fragmentName = this.getClass().getSimpleName();

        Bundle extras = getActivity().getIntent().getExtras();
        station = (Station) extras.get(EXTRA_STATION);
        series = (StationSeries) extras.get(EXTRA_STATION_SERIES);

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

        gpsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onGetGpsClicked(v);
            }
        });

        validator.validateString(stationIdText, Station.getStationIdMaxLength());
        validator.validateString(observerIdText, Station.getObserverIdMaxLength());
        validator.validateAsDouble(latitudeText, -90.0, 90.0);
        validator.validateAsDouble(longitudeText, -180.0, 180.0);
        validator.validateAsDouble(elevationText);
        validator.validateAsDouble(meterHeightText);

        validator.addValidationListener(new MyValidationListener());

        setOnFocusChangedListeners(observerIdText);
        setOnFocusChangedListeners(latitudeText);
        setOnFocusChangedListeners(longitudeText);
        setOnFocusChangedListeners(elevationText);
        setOnFocusChangedListeners(meterHeightText);

        // This keeps any real element from getting focus.
        dummyLinearLayout.requestFocus();
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
    public void populateData() {
        if (station == null) {
            station = new Station();
            isCreate = true;
            getActivity().setTitle(R.string.title_activity_station_create);
            saveButton.setText(getString(R.string.create));
            saveButton.setEnabled(false);
        } else {
            isCreate = false;
            saveButton.setEnabled(true);
        }

        stationIdText.setText(station.getStationId());
        observerIdText.setText(station.getObserverId());
        if (station.getLatitude() != null) {
            latitudeText.setText(station.getLatitude().toString());
        }
        if (station.getLongitude() != null) {
            longitudeText.setText(station.getLongitude().toString());
        }
        if (station.getElevation() != null) {
            elevationText.setText(station.getElevation().toString());
        }
        if (station.getMeterHeight() != null) {
            meterHeightText.setText(station.getMeterHeight().toString());
        }
        if (station.useEarthTide() == null || station.useEarthTide()) {
            earthTideCheckBox.setChecked(true);
        } else {
            earthTideCheckBox.setChecked(false);
        }
    }

    @Override
    public void persistData() {
        // only save when Save button is clicked
    }

    public void onSaveClicked(View view) {

        station.setStationSeries(series);
        if (validator.isValid(stationIdText)) {
            station.setStationId(stationIdText.getText().toString());
        }
        if (validator.isValid(observerIdText)) {
            station.setObserverId(observerIdText.getText().toString());
        }
        if (validator.isValid(latitudeText)) {
            station.setLatitude(Double.parseDouble(latitudeText.getText().toString()));
        }
        if (validator.isValid(longitudeText)) {
            station.setLongitude(Double.parseDouble(longitudeText.getText().toString()));
        }
        if (validator.isValid(elevationText)) {
            station.setElevation(Double.parseDouble(elevationText.getText().toString()));
        }
        if (validator.isValid(meterHeightText)) {
            station.setMeterHeight(Double.parseDouble(meterHeightText.getText().toString()));
        }
        station.setEarthTide(earthTideCheckBox.isChecked());

        stationDao.createOrUpdateWithSeriesInTransaction(station, new TransactionCallback() {

            @Override
            public void onSuccess() {
                Intent intent = new Intent();
                intent.putExtra(EXTRA_STATION, station);
                intent.putExtra(EXTRA_STATION_SERIES, series);
                getActivity().setResult(Activity.RESULT_OK, intent);
                getActivity().finish();
            }

            @Override
            public void onFailed(Exception e) {
                ErrorHandler errorHandler = ErrorHandler.getInstance();
//                String msg = null;
                if (isCreate) {
//                    msg = "Failed to create station - ";
                    errorHandler.logError(Level.WARNING, "StationEditFragment.onSaveClicked()$" +
                            "TransactionCallback.onFailed(): Failed to create station - " + e,
                            R.string.station_file_create_error_title,
                            R.string.station_file_create_error_message);
                } else {
//                    msg = "Failed to update station - ";
                    errorHandler.logError(Level.WARNING, "StationEditFragment.onSaveClicked()$" +
                            "TransactionCallback.onFailed(): Failed to update station - " + e,
                            R.string.station_file_update_error_title,
                            R.string.station_file_update_error_message);
                }
//                Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onGetGpsClicked(View view) {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, (long) 1000 * 5, (float) 0.0, this);
        Location location;

        isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (isGpsEnabled) {
            try {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); // How do we know these values are valid?
                latitudeText.setText(new DecimalFormat("0.000000").format(location.getLatitude()));
                longitudeText.setText(new DecimalFormat("0.000000").format(location.getLongitude()));
            } catch (Exception e) {
                if (MyDebug.LOG) {
                    Log.e(TAG, "Error getting GPS location", e);
                }
                ErrorHandler errorHandler = ErrorHandler.getInstance();
                errorHandler.logError(Level.WARNING, "StationEditFragment.onGetGpsClicked(): " +
                        "Error getting GPS location - " + e,
                        R.string.bad_gps_location_title,
                        R.string.bad_gps_location_message);
            }
        } else {
            // Display a dialog to ask the user to enable GPS
            showSettingsAlert();
        }
    }

    public void showSettingsAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.gps_disabled_title);
        builder.setMessage(R.string.gps_disabled_message);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                getActivity().startActivity(intent);
            }
        });
  
        // on pressing cancel button
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            }
        });
        builder.show();
    }
    
    @Override
    public void onLocationChanged(Location location) {
//        latitude = location.getLatitude();
//        longitude = location.getLongitude();
//        Log.d(TAG,"New GPS data: Latitude: " + latitude +  ", Longitude: " + longitude);
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (MyDebug.LOG) {
            Log.d(TAG,"GPS disabled");
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (MyDebug.LOG) {
            Log.d(TAG,"GPS enabled");
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void onCancelClicked(View view) {

        getActivity().setResult(Activity.RESULT_CANCELED);
        getActivity().finish();
    }

    private class MyValidationListener implements ValidationListener {

        @Override
        public void onValidation(Object input, boolean inputValid, boolean allInputsValid) {
            saveButton.setEnabled(allInputsValid);
        }
    }
}
