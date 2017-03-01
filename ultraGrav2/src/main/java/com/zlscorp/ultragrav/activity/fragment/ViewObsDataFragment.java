package com.zlscorp.ultragrav.activity.fragment;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

import roboguice.inject.InjectView;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;
import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.activity.AbstractBaseActivity;
import com.zlscorp.ultragrav.activity.widget.ListFragmentPagerAdapter.OnFragmentSelectedListener;
import com.zlscorp.ultragrav.model.Observation;
import com.zlscorp.ultragrav.model.SystemParams;
import com.zlscorp.ultragrav.persist.ObservationDao;
import com.zlscorp.ultragrav.persist.SystemParamsDao;
import com.zlscorp.ultragrav.type.ObservationType;
import com.zlscorp.ultragrav.debug.MyDebug;
import com.zlscorp.ultragrav.display.ObservationDisplayer;
import com.zlscorp.ultragrav.display.ObservationDisplayer.DisplayResult;
import com.zlscorp.ultragrav.file.ErrorHandler;

public class ViewObsDataFragment extends AbstractBaseFragment implements OnFragmentSelectedListener {
	
	public static final String TAG = "ViewDataFragment";
	
    public static final int SHORT_RECORD = 1;
    public static final int LONG_RECORD = 2;

    @Inject
    private SystemParamsDao systemParamsDao;

    private ProgressDialog progressBarDialog;
    
	@InjectView(R.id.viewSingleModeButton)
	private Button viewSingleButton;

    @InjectView(R.id.viewContinuousModeShortButton)
    private Button viewContinuousShortButton;

    @InjectView(R.id.viewContinuousModeLongButton)
    private Button viewContinuousLongButton;

    @InjectView(R.id.obsData)
    private TextView obsData;
    
	@Inject
	private ObservationDao observationDao;
	
    private MyObservationTypeCounts observationCountAsync;
    private Long singleObservationCount;
    private Long continuousObservationCount;
    private boolean areObservationCountsValid;
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_view_data, container, false);

		return v;
	}
	
	@Override
	public void setupView(View view, Bundle savedInstanceState) {
		setupButtons();
	}
	
    @Override
    public void onFragmentSelected() {
        if (MyDebug.LOG) {
            Log.d(TAG, "Tab selected");
        }
        observationCountAsync = new MyObservationTypeCounts();
        observationCountAsync.execute();
        AbstractBaseActivity.fragmentName = this.getClass().getSimpleName();
    }

    @Override
    public void onFragmentUnselected() {
        if (MyDebug.LOG) {
            Log.d(TAG, "Tab unselected");
        }
    }

	private void setupButtons(){
		setupViewSingleButton();
        setupViewContinuousShortButton();
        setupViewContinuousLongButton();
	}

	private void setupViewSingleButton(){
		viewSingleButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				viewSingleButtonClicked();
			}
		});
	}

    private void setupViewContinuousShortButton(){
        viewContinuousShortButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                viewContinuousShortButtonClicked();
            }
        });
    }

    private void setupViewContinuousLongButton(){
        viewContinuousLongButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                viewContinuousLongButtonClicked();
            }
        });
    }

	public void viewSingleButtonClicked() {
	    if (areObservationCountsValid) {
	        if (singleObservationCount > 0) {
	            MyObservationDisplayTask task = new MyObservationDisplayTask(ObservationType.SINGLE, SHORT_RECORD);
	            task.execute();
            } else {
                showAlertDialog(R.string.no_single_obs_to_view_title, R.string.no_single_obs_to_view_message);
            }
	    } else {
            showAlertDialog(R.string.obs_counts_not_valid_title, R.string.obs_counts_not_valid_message);
	    }
	}

    public void viewContinuousShortButtonClicked() {
        if (areObservationCountsValid) {
            if (continuousObservationCount > 0) {
                MyObservationDisplayTask task = new MyObservationDisplayTask(ObservationType.CONTINUOUS, SHORT_RECORD);
                task.execute();
            } else {
                showAlertDialog(R.string.no_cont_obs_to_view_title, R.string.no_cont_obs_to_view_message);
            }
        } else {
            showAlertDialog(R.string.obs_counts_not_valid_title, R.string.obs_counts_not_valid_message);
        }
    }

    public void viewContinuousLongButtonClicked() {
        if (areObservationCountsValid) {
            if (continuousObservationCount > 0) {
                MyObservationDisplayTask task = new MyObservationDisplayTask(ObservationType.CONTINUOUS, LONG_RECORD);
                task.execute();
            } else {
                showAlertDialog(R.string.no_cont_obs_to_view_title, R.string.no_cont_obs_to_view_message);
            }
        } else {
            showAlertDialog(R.string.obs_counts_not_valid_title, R.string.obs_counts_not_valid_message);
        }
    }
    
    private void showAlertDialog(int title, int message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.ok, null);
        builder.show();
    }

    private class MyObservationTypeCounts extends AsyncTask<Void, Void, DisplayResult> {

        private Long singleCount = null;
        private Long continuousCount = null;

        @Override
        protected DisplayResult doInBackground(Void... params) {
            DisplayResult result = new DisplayResult();
            
            try {
                // Test
                boolean test = false;
                if (test) {
                    throw new SQLException("test"); 
                }

                singleCount = observationDao.getObservationTypeCount(ObservationType.SINGLE);
                continuousCount = observationDao.getObservationTypeCount(ObservationType.CONTINUOUS);
                result.setSuccess(true);
            } catch(SQLException e) {
                if (MyDebug.LOG) {
                    Log.e(TAG, "failed to query observation counts", e);
                }

                result.setSuccess(false);
                result.setMessage(e.toString());
            }
            return result;
        }
        
        @Override
        protected void onPostExecute(DisplayResult result) {
            areObservationCountsValid = false;

            if (result.isSuccess()) {

                String errorStr = null;

                if (singleCount != null) {
                    singleObservationCount = singleCount;
                } else {
                    errorStr = " - singleCount is null.";
                }

                if (continuousCount != null) {
                    continuousObservationCount = continuousCount;
                } else {
                    if (errorStr == null) {
                        errorStr = " - continuousCount is null.";
                    } else {
                        errorStr = " - singleCount and continuousCount are null.";
                    }
                }

                if (errorStr == null) {
                    areObservationCountsValid = true;
                } else {
                    ErrorHandler errorHandler = ErrorHandler.getInstance();
                    errorHandler.logError(Level.WARNING, "ViewObsDataFragment$MyObservationTypeCounts." +
                    		"onPostExecute(): " + errorStr,
                            R.string.observation_count_read_error_title,
                            R.string.observation_count_read_error_message);
                }
            } else {
              ErrorHandler errorHandler = ErrorHandler.getInstance();
              errorHandler.logError(Level.WARNING, "ViewObsDataFragment$MyObservationTypeCounts." +
              		"onPostExecuted(): Could not get observation record counts - " + result.getMessage(),
                      R.string.observation_count_read_error_title,
                      R.string.observation_count_read_error_message);
            }
        }
    }

    private class MyObservationDisplayTask extends AsyncTask<Void, String, DisplayResult> {

        private ObservationType observationType;
        private int recordLength;
        private boolean isCancelled;
        
        public MyObservationDisplayTask(ObservationType observationType, int recordLength) {
            super();
            this.observationType = observationType;
            this.recordLength = recordLength;
            isCancelled = false;
        }

        @Override
        protected void onPreExecute() {
            obsData.setText("");
            progressBarDialog = new ProgressDialog(getActivity());
            progressBarDialog.setCancelable(false);
            progressBarDialog.setMessage("Loading data ...");
            progressBarDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressBarDialog.setProgress(0);
            progressBarDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    progressBarDialog.dismiss();
                }
            });
            progressBarDialog.show();        
        }

        @Override
        protected DisplayResult doInBackground(Void... params) {

//            int obsSize;
            Observation observation;
            SystemParams systemParams = null;
            DisplayResult result = new DisplayResult();
            ObservationDisplayer observationDisplayer = null;
            
            try {
                systemParams = systemParamsDao.queryForDefault();
                observationDisplayer = new ObservationDisplayer(recordLength, systemParams.isSaveFrequencies());
            } catch (SQLException e) {
                result.setSuccess(false);
                result.setMessage(getString(R.string.system_params_file_open_error_message));
            }
            
            try {
                List<Observation> observations = observationDao.getObservationsForObservationTypeFull(observationType);
                int obsSize = observations.size();
                progressBarDialog.setMax(obsSize);
                progressBarDialog.setProgressNumberFormat("%1d of %2d Records"); 

                if (observationDisplayer != null) {
                    for (int index = 0 ; index < obsSize ; index++) {
                        observation = observations.get(index);
                        result = observationDisplayer.display(observation);

                        // Test
//                        if (((float) index/obsSize) > .5) {
//                            result.setSuccess(false);
//                            result.setMessage("test");
//                        }

                        if (result.isSuccess()) {
//                            if (index % 2 == 0) {      // Update every other observation
                                publishProgress(result.getMessage(), Integer.toString(index));
                                if (MyDebug.LOG) {
                                    Log.d(TAG, "Display progress = " + index + " of " + obsSize);
                                }
//                            }

                            if (!progressBarDialog.isShowing()) {
                                isCancelled = true;
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                }
            } catch (SQLException e) {
                if (MyDebug.LOG) {
                    Log.e(TAG, "Failed to get Observations from database ", e);
                }
                result.setSuccess(false);
                result.setMessage(getString(R.string.observation_file_read_error_message));
            }

            return result;
        }
        
        @Override
        protected void onProgressUpdate(String... obsDataString) {
            obsData.append(obsDataString[0]);
            progressBarDialog.setProgress(Integer.parseInt(obsDataString[1]));
        }

        @Override
        protected void onPostExecute(DisplayResult result) {
            if (progressBarDialog != null) {
                progressBarDialog.dismiss();
            }

            if (result.isSuccess()) {
                if (isCancelled) {
                    Toast.makeText(getActivity(), 
                            getString(R.string.file_write_cancelled), 
                            Toast.LENGTH_LONG).show();
                }
            } else {
                ErrorHandler errorHandler = ErrorHandler.getInstance();
                errorHandler.logError(Level.WARNING, "ViewObsDataFragment$MyObservationDisplayTask." +
                		"onPostExecute(): Data display failed because " + result.getMessage(),
                        R.string.observation_file_read_error_title,
                        R.string.observation_file_read_error_message);
            }
        }

        @Override
        protected void onCancelled(DisplayResult result) {
            if (progressBarDialog != null) {
                progressBarDialog.dismiss();
            }
        }
    }
}
