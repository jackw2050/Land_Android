package com.zlscorp.ultragrav.activity.fragment;

//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
//import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;

import roboguice.inject.InjectView;
//import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.os.Environment;
import android.provider.Settings;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
//import android.widget.Toast;

import com.google.inject.Inject;
import com.zlscorp.ultragrav.R;
//import com.zlscorp.ultragrav.activity.FileSelectActivity;
import com.zlscorp.ultragrav.activity.AbstractBaseActivity;
import com.zlscorp.ultragrav.activity.validate.Validator;
import com.zlscorp.ultragrav.activity.widget.ListFragmentPagerAdapter.OnFragmentSelectedListener;
import com.zlscorp.ultragrav.debug.MyDebug;
import com.zlscorp.ultragrav.file.ErrorHandler;
//import com.zlscorp.ultragrav.file.ObservationExporter;
//import com.zlscorp.ultragrav.file.ObservationExporter.ExportResult;
import com.zlscorp.ultragrav.meter.processor.Earthtide;
//import com.zlscorp.ultragrav.persist.ObservationDao;
//import com.zlscorp.ultragrav.type.ObservationType;

public class EarthtideFragment extends AbstractBaseFragment implements OnFragmentSelectedListener, 
      LocationListener {
	
	public static final String TAG = "EarthtideFragment";
	
//	public static final int REQUEST_FILE_PATH_FOR_SINGLE = 1;
//	public static final int REQUEST_FILE_PATH_FOR_CONTINOUS = 2;
	
    @Inject
    private Validator validator;

	@InjectView(R.id.monthText)
	private EditText monthText;
	
	@InjectView(R.id.dayText)
	private EditText dayText;

    @InjectView(R.id.yearText)
    private EditText yearText;

    @InjectView(R.id.latitudeText)
    private EditText latitudeText;

    @InjectView(R.id.longitudeText)
    private EditText longitudeText;

    @InjectView(R.id.earthtideSampleText)
    private TextView earthtideSampleText;

    @InjectView(R.id.elevationText)
    private EditText elevationText;

	@InjectView(R.id.calculateButton)
	private Button calculateButton;

    @InjectView(R.id.gpsButton)
    private Button gpsButton;

	@InjectView(R.id.saveEarthtideDataButton)
	private Button saveEarthtideDataButton;

    @InjectView(R.id.dummyLinearLayout)
    private LinearLayout dummyLinearLayout;

    private CalculateEarthtideSample earthtideCalcAsync;

    private boolean isGpsEnabled;

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_earthtide, container, false);

		return v;
	}
	
	@Override
	public void setupView(View view, Bundle savedInstanceState) {

	    Calendar cal = GregorianCalendar.getInstance();
	    cal.setTimeInMillis(System.currentTimeMillis());
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));

	    yearText.setText(Integer.toString(cal.get(Calendar.YEAR)));
	    monthText.setText(Integer.toString(cal.get(Calendar.MONTH) + 1));
	    dayText.setText(Integer.toString(cal.get(Calendar.DATE)));
	    elevationText.setText("0");

	    setupCalculateButton();
        setupGpsButton();
        setupSaveEarthtideDataButton();
        saveEarthtideDataButton.setEnabled(false);         // TODO - Delete after save code written

        validator.validateAsDouble(latitudeText, -90.0, 90.0);
        validator.validateAsDouble(longitudeText, -180.0, 180.0);
        validator.validateAsDouble(elevationText);
      //  validator.validateAsDate(yearText, monthText, dayText, 1900, 2525);
        
        setOnFocusChangedListeners(monthText);
        setOnFocusChangedListeners(dayText);
        setOnFocusChangedListeners(latitudeText);
        setOnFocusChangedListeners(longitudeText);
        setOnFocusChangedListeners(elevationText);
        
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
    public void onFragmentSelected() {
        if (MyDebug.LOG) {
            Log.d(TAG, "Tab selected");
        }
        
        // This keeps any real element from getting focus.
        dummyLinearLayout.requestFocus();

        AbstractBaseActivity.fragmentName = this.getClass().getSimpleName();
        getGpsCoordinates();
    }

    @Override
    public void onFragmentUnselected() {
        if (MyDebug.LOG) {
            Log.d(TAG, "Tab unselected");
        }

        // Hide the soft keyboard before changing tabs. 
        InputMethodManager imm = (InputMethodManager) getActivity().
                getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

//	@Override
//	public void onActivityResult(int requestCode, int resultCode, Intent data) {
//		if (requestCode == REQUEST_FILE_PATH_FOR_SINGLE) {
//			if (resultCode == Activity.RESULT_OK) {
//				String filePath = data.getStringExtra(FileSelectFragment.PATH_EXTRA);
//				checkFilePermissions(filePath, ObservationType.SINGLE);
//			}
//		} else if (requestCode == REQUEST_FILE_PATH_FOR_CONTINOUS) {
//			if (resultCode == Activity.RESULT_OK) {
//				String filePath = data.getStringExtra(FileSelectFragment.PATH_EXTRA);
//				checkFilePermissions(filePath, ObservationType.CONTINUOUS);
//			}
//		}
//	}

	private void setupCalculateButton(){
	    calculateButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
			    calculateButtonClicked();
			}
		});
	}

    private void setupGpsButton(){
        gpsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onGetGpsClicked(v);
            }
        });
    }

    private void setupSaveEarthtideDataButton(){
        saveEarthtideDataButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                saveEarthtideDataButtonClicked();
            }
        });
    }

	public void calculateButtonClicked() {
	    if (validator.isAllValid()) {
	        earthtideCalcAsync = new CalculateEarthtideSample();
	        earthtideCalcAsync.execute();
	    } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.invalid_fields_title);
            builder.setMessage(R.string.invalid_fields_message);
            builder.setPositiveButton(R.string.ok, null);
            builder.show();
	    }
	}

	public void saveEarthtideDataButtonClicked(){
	}

//	private void checkFilePermissions(final String path, final ObservationType observationType) {
//		File file = new File(path);
//		boolean fileCreated = false;
//		try{
//			file.getParentFile().mkdirs();
//			fileCreated = file.createNewFile();
//		} catch (IOException e) {
//			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//            builder.setTitle(R.string.file_write_failed_title);
//            builder.setMessage(R.string.file_write_failed_message);
//			builder.setPositiveButton(R.string.ok, null);
//			builder.show();
//			return;
//		}
//		
//		if (!file.isFile()) {
//			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//            builder.setTitle(R.string.path_not_file_title);
//            builder.setMessage(R.string.path_not_file_message);
//			builder.setPositiveButton(R.string.ok, null);
//            builder.show();
//			return;
//		}
//		if (!file.canWrite()) {
//			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//            builder.setTitle(R.string.path_insufficient_write_permissions_title);
//            builder.setMessage(R.string.path_insufficient_write_permissions_message);
//			builder.setPositiveButton(R.string.ok, null);
//            builder.show();
//			return;
//		}
//		if (!fileCreated) {
//			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//            builder.setTitle(R.string.allow_file_overwrite_title);
//            builder.setMessage(R.string.allow_file_overwrite_message);
//			builder.setNegativeButton(R.string.no, null);
//			builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					MyObservationExportTask task = new MyObservationExportTask(path, observationType);
//					task.execute();
//				}
//			});
//			
//            builder.show();
//			return;
//		}
//		
//		MyObservationExportTask task = new MyObservationExportTask(path, observationType);
//		task.execute();
//	}
	
	private class CalculateEarthtideSample extends AsyncTask<Void, Void, Void> {
		
        private Earthtide earthtide = new Earthtide();
        private Location readingLocation = new Location("ultragrav");
        private String[] earthtideResults = new String[26];
        private String yearStr;
        private String monthStr;
        private String dayStr;
        
        CalculateEarthtideSample() {
            yearStr = yearText.getText().toString();
            monthStr = monthText.getText().toString();
            dayStr = dayText.getText().toString();
            String str1 = latitudeText.getText().toString();
            String str2 = longitudeText.getText().toString();
            String str3 = elevationText.getText().toString();
            if (str1.equals("") || str1 == null) str1 = "0.0";
            if (str2.equals("") || str1 == null) str2 = "0.0";
            if (str3.equals("") || str1 == null) str3 = "0.0";
            readingLocation.setLatitude(Double.parseDouble(str1));
            readingLocation.setLongitude(Double.parseDouble(str2));
            readingLocation.setAltitude(Double.parseDouble(str3));
        }

		@Override
		protected Void doInBackground(Void... params) {
			
//	        File sdCard = Environment.getExternalStorageDirectory();
//	        File dir = new File (sdCard.getAbsolutePath() + "/Download");
//	        dir.mkdirs();
//	        File file = new File(dir, "filename");
//
//	        FileOutputStream f = new FileOutputStream(file);
		    
		    int year = Integer.parseInt(yearStr);
		    int month = Integer.parseInt(monthStr);
		    int day = Integer.parseInt(dayStr);

	        Time time = new Time("UTC");
	        // Month is entered on the screen in the range 1-12 and stored in the range 0-11.
	        time.set(0, 0, 0, day, month-1, year);
	        time.isDst = 0;

	        int hour;
	        int[] result = new int[6];

            earthtideResults[0] = "Earthtide Report for latitude " + readingLocation.getLatitude() + 
                                  " and longitude " + readingLocation.getLongitude();
            earthtideResults[1] = String.format("\n%s %3s %3s %3s %6s %6s %6s %6s %6s %6s\n", 
                    "Year", "Mo", "Dy", "Hr", "+0", "+10", "+20", "+30", "+40", "+50");

            for (hour = 0; hour < 24 ; hour++) {
//	            cal.set(Calendar.HOUR_OF_DAY, hour);
	            time.hour = hour;
	            for (int i = 0; i < 6; i++) {
//	                cal.set(Calendar.MINUTE, i*10);
//                  result[i] = (int) (1000 * earthtide.calculate(cal.getTimeInMillis(), readingLocation));

	                time.minute = i*10;
                    result[i] = (int) (1000 * earthtide.calculate(time.toMillis(true), readingLocation));
	            }
                earthtideResults[hour+2] = String.format("%d %3d %3d %3d %6d %6d %6d %6d %6d %6d\n", 
                        year, month, day, hour, result[0], result[1], result[2], result[3], result[4], 
                        result[5]);
	        }
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			earthtideSampleText.setText("");
		    for (int i = 0; i < earthtideResults.length; i++) {
//		        try {
	                earthtideSampleText.append(earthtideResults[i]);
//		        } catch (Exception e){
//		            if (MyDebug.LOG) {
//		                Log.e(TAG, "Earthtide array blowed up", e);
//		            }
//		        }
		    }
		}
	}
    public void onGetGpsClicked(View view) {
        getGpsCoordinates();
    }

    public void getGpsCoordinates() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(
                Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, (long) 1000 * 5, 
                (float) 0.0, this);
        Location location;

        isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (isGpsEnabled) {
            try {
                // How do we know these values are valid?
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); 
                latitudeText.setText(new DecimalFormat("0.000000").format(location.getLatitude()).replace(",", "."));
                longitudeText.setText(new DecimalFormat("0.000000").format(location.getLongitude()).replace(",", "."));
            } catch (Exception e) {
                if (MyDebug.LOG) {
                    Log.e(TAG, "Error getting GPS location", e);
                }
                ErrorHandler errorHandler = ErrorHandler.getInstance();
                errorHandler.logError(Level.WARNING, "EarthtideFragment.onGetGpsClicked(): " +
                        "Error getting GPS location." + e,
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
    
//	private class MyObservationExportTask extends AsyncTask<Void, Void, ExportResult> {
//		private String path;
//		private ObservationType observationType;
//		
//		public MyObservationExportTask(String path, ObservationType observationType) {
//			super();
//			this.path = path;
//			this.observationType = observationType;
//		}
//
//		@Override
//		protected ExportResult doInBackground(Void... params) {
//			return observationExporter.export(path, observationType, 1);
//		}
//		
//		@Override
//		protected void onPostExecute(ExportResult result) {
//			if (!result.isSuccess()) {
//				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                builder.setTitle(R.string.file_write_failed_title);
//				builder.setMessage(result.getErrorMessage());
//				builder.setPositiveButton(R.string.ok, null);
//				builder.create().show();
//			} else {
//				Toast.makeText(getActivity(), getString(R.string.file_write_complete), Toast.LENGTH_LONG).
//				      show();
//			}
//		}
//	}

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
}
