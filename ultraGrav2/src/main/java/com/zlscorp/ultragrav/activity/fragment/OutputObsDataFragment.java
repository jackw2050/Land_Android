package com.zlscorp.ultragrav.activity.fragment;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

import roboguice.inject.InjectView;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
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
import com.zlscorp.ultragrav.activity.FileSelectActivity;
import com.zlscorp.ultragrav.activity.widget.ListFragmentPagerAdapter.OnFragmentSelectedListener;
import com.zlscorp.ultragrav.debug.MyDebug;
import com.zlscorp.ultragrav.file.ErrorHandler;
import com.zlscorp.ultragrav.file.ObservationExporter;
import com.zlscorp.ultragrav.file.ObservationExporter.ExportResult;
import com.zlscorp.ultragrav.model.Observation;
import com.zlscorp.ultragrav.persist.ObservationDao;
import com.zlscorp.ultragrav.type.ObservationType;

public class OutputObsDataFragment extends AbstractBaseFragment implements OnFragmentSelectedListener {
	
	public static final String TAG = "OutputObsDataFragment";
	
	public static final int REQUEST_FILE_PATH_FOR_SINGLE = 1;
    public static final int REQUEST_FILE_PATH_FOR_CONTINUOUS_SHORT = 2;
    public static final int REQUEST_FILE_PATH_FOR_CONTINUOUS_LONG = 3;
	
    public static final int SHORT_RECORD = 1;
    public static final int LONG_RECORD = 2;

    @InjectView(R.id.singleSize)
	private TextView singleCountLabel;
	
	@InjectView(R.id.continuousSize)
	private TextView continuousCountLabel;

	@InjectView(R.id.saveSingleModeButton)
	private Button saveSingleButton;

    @InjectView(R.id.saveContinuousModeShortButton)
    private Button saveContinuousShortButton;

    @InjectView(R.id.saveContinuousModeLongButton)
    private Button saveContinuousLongButton;

	@InjectView(R.id.deleteSingleModeButton)
	private Button deleteSingleButton;

	@InjectView(R.id.deleteContinuousModeButton)
	private Button deleteContinuousButton;
	
	@Inject
	private ObservationDao observationDao;
	
	@Inject
	private ObservationExporter observationExporter;
	
	private MyObservationTypeCounts observationCountAsync;
	
    private ProgressDialog progressBarDialog;
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_output_data, container, false);

		return v;
	}
	
	@Override
	public void setupView(View view, Bundle savedInstanceState) {

	    AbstractBaseActivity.fragmentName = this.getClass().getSimpleName();

        setupButtons();
		
		observationCountAsync = new MyObservationTypeCounts();
		observationCountAsync.execute();
	}
	
    @Override
    public void onFragmentSelected() {
        if (MyDebug.LOG) {
            Log.d(TAG, "Tab selected");
        }
        AbstractBaseActivity.fragmentName = this.getClass().getSimpleName();
    }
    
    @Override
    public void onFragmentUnselected() {
        if (MyDebug.LOG) {
            Log.d(TAG, "Tab unselected");
        }
    }

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_FILE_PATH_FOR_SINGLE) {
			if (resultCode == Activity.RESULT_OK) {
				String filePath = data.getStringExtra(FileSelectFragment.PATH_EXTRA);
				checkFilePermissions(filePath, ObservationType.SINGLE, SHORT_RECORD);
			}
        } else if (requestCode == REQUEST_FILE_PATH_FOR_CONTINUOUS_SHORT) {
            if (resultCode == Activity.RESULT_OK) {
                String filePath = data.getStringExtra(FileSelectFragment.PATH_EXTRA);
                checkFilePermissions(filePath, ObservationType.CONTINUOUS, SHORT_RECORD);
            }
        } else if (requestCode == REQUEST_FILE_PATH_FOR_CONTINUOUS_LONG) {
            if (resultCode == Activity.RESULT_OK) {
                String filePath = data.getStringExtra(FileSelectFragment.PATH_EXTRA);
                checkFilePermissions(filePath, ObservationType.CONTINUOUS, LONG_RECORD);
            }
        }
	}

	private void setupButtons(){
		setupSaveSingleButton();
        setupSaveContinuousShortButton();
        setupSaveContinuousLongButton();
		setupDeleteSingleButton();
		setupDeleteContinuousButton();
	}

	private void setupSaveSingleButton(){
		saveSingleButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				saveSingleButtonClicked();
			}
		});
	}

    private void setupSaveContinuousShortButton(){
        saveContinuousShortButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                saveContinuousShortButtonClicked();
            }
        });
    }

    private void setupSaveContinuousLongButton(){
        saveContinuousLongButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                saveContinuousLongButtonClicked();
            }
        });
    }

	private void setupDeleteSingleButton(){
		deleteSingleButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				deleteSingleButtonClicked();
			}
		});
	}
	
	private void setupDeleteContinuousButton(){
		deleteContinuousButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				deleteContinuousButtonClicked();
			}
		});
	}
	
	public void saveSingleButtonClicked(){
        if (Integer.parseInt(singleCountLabel.getText().toString()) > 0) {
//            checkFilePermissions("/storage/emulated/0/Downloads/file1.csv", ObservationType.SINGLE, SHORT_RECORD);
            Intent intent = FileSelectActivity.createIntent(getActivity(), true);
            startActivityForResult(intent, REQUEST_FILE_PATH_FOR_SINGLE);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.no_single_obs_to_save_title);
            builder.setMessage(R.string.no_single_obs_to_save_message);
            builder.setPositiveButton(R.string.ok, null);
            builder.show();
        }
	}

    public void saveContinuousShortButtonClicked(){
        if (Integer.parseInt(continuousCountLabel.getText().toString()) > 0) {
            Intent intent = FileSelectActivity.createIntent(getActivity(), true);
            startActivityForResult(intent, REQUEST_FILE_PATH_FOR_CONTINUOUS_SHORT);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.no_continuous_obs_to_save_title);
            builder.setMessage(R.string.no_continuous_obs_to_save_message);
            builder.setPositiveButton(R.string.ok, null);
            builder.show();
        }
    }

    public void saveContinuousLongButtonClicked(){
        if (Integer.parseInt(continuousCountLabel.getText().toString()) > 0) {
            Intent intent = FileSelectActivity.createIntent(getActivity(), true);
            startActivityForResult(intent, REQUEST_FILE_PATH_FOR_CONTINUOUS_LONG);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.no_continuous_obs_to_save_title);
            builder.setMessage(R.string.no_continuous_obs_to_save_message);
            builder.setPositiveButton(R.string.ok, null);
            builder.show();
        }
    }

	public void deleteSingleButtonClicked(){
        if (Integer.parseInt(singleCountLabel.getText().toString()) > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.delete_single_mode_title);
            builder.setMessage(R.string.delete_single_mode_file_message);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        // Test
                        boolean test = false;
                        if (test) {
                            throw new SQLException("test"); 
                        }

                        observationDao.deleteObservationsForObservationTypeFull(ObservationType.SINGLE);

                        // Update the displayed record size
                        observationCountAsync = new MyObservationTypeCounts();
                        observationCountAsync.execute();
                        
                        Toast.makeText(getActivity(), getString(R.string.single_obs_delete_complete), Toast.LENGTH_LONG).show();
                    } catch(SQLException e) {
                        if (MyDebug.LOG) {
                            Log.e(TAG, "Failed to delete Single Mode Observations", e);
                        }
                        ErrorHandler errorHandler = ErrorHandler.getInstance();
                        errorHandler.logError(Level.WARNING, "OutputObsDataFragment.deleteSingleButtonClicked(): " +
                                "Can't delete single observations - " + e,
                                R.string.single_obs_delete_error_title,
                                R.string.single_obs_delete_error_message);
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            builder.show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.no_single_obs_to_delete_title);
            builder.setMessage(R.string.no_single_obs_to_delete_message);
            builder.setPositiveButton(R.string.ok, null);
            builder.show();
        }
	}

	public void deleteContinuousButtonClicked(){
        if (Integer.parseInt(continuousCountLabel.getText().toString()) > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.delete_continuous_mode_title);
            builder.setMessage(R.string.delete_continuous_mode_file_message);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        // Test
                        boolean test = false;
                        if (test) {
                            throw new SQLException("test"); 
                        }

                        observationDao.deleteObservationsForObservationTypeFull(ObservationType.CONTINUOUS);

                        // Update the displayed record size
                        observationCountAsync = new MyObservationTypeCounts();
                        observationCountAsync.execute();

                        Toast.makeText(getActivity(), getString(R.string.continuous_obs_delete_complete), 
                                Toast.LENGTH_LONG).show();
                    } catch(SQLException e) {
                        if (MyDebug.LOG) {
                            Log.e(TAG, "Failed to delete Continuous Mode Observations", e);
                        }
                        ErrorHandler errorHandler = ErrorHandler.getInstance();
                        errorHandler.logError(Level.WARNING, "OutputObsDataFragment.deleteContinuousButtonClicked(): " +
                                "Can't delete continuous observations - " + e,
                                R.string.continuous_obs_delete_error_title,
                                R.string.continuous_obs_delete_error_message);
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            builder.show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.no_continuous_obs_to_delete_title);
            builder.setMessage(R.string.no_continuous_obs_to_delete_message);
            builder.setPositiveButton(R.string.ok, null);
            builder.show();
        }
	}
	
	private void checkFilePermissions(final String path, final ObservationType observationType, final int recordLength) {

	    File file = new File(path);
		boolean fileCreated = false;
		
		try{
            // Test
            boolean test = false;
            if (test) {
                throw new IOException("test"); 
            }
            
			file.getParentFile().mkdirs();
			fileCreated = file.createNewFile();
		} catch (IOException e) {
            ErrorHandler errorHandler = ErrorHandler.getInstance();
            errorHandler.logError(Level.WARNING, "OutputObsDataFragment.checkFilePermissions(): " +
                    "Can't create file - " + e,
                    R.string.file_write_failed_title,
                    R.string.file_write_failed_message);
			return;
		}
		
        // Test
//        boolean test = true;

		if (!file.isFile()) {
//	      if (test) {
            ErrorHandler errorHandler = ErrorHandler.getInstance();
            errorHandler.logError(Level.WARNING, "OutputObsDataFragment.checkFilePermissions(): " +
                    "Path is not a file - " + path,
                    R.string.path_not_file_title,
                    R.string.path_not_file_message);
			return;
		}
		if (!file.canWrite()) {
//        if (test) {
            ErrorHandler errorHandler = ErrorHandler.getInstance();
            errorHandler.logError(Level.WARNING, "OutputObsDataFragment.checkFilePermissions(): " +
                    "Insufficient write permissions for file - " + file.toString(),
                    R.string.path_insufficient_write_permissions_title,
                    R.string.path_insufficient_write_permissions_message);
			return;
		}
		if (!fileCreated) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.allow_file_overwrite_title);
			builder.setMessage(R.string.allow_file_overwrite_message);
			builder.setNegativeButton(R.string.no, null);
			builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					MyObservationExportTask task = new MyObservationExportTask(path, observationType, 
					        recordLength);
					task.execute();
				}
			});
			
            builder.show();
			return;
		}
		
		MyObservationExportTask task = new MyObservationExportTask(path, observationType, recordLength);
		task.execute();
	}
	
	private class SingleMediaScanner implements MediaScannerConnectionClient {

	    private MediaScannerConnection mMs;
	    private File mFile;

	    public SingleMediaScanner(Context context, File f) {
	        mFile = f;
	        mMs = new MediaScannerConnection(context, this);
	        mMs.connect();
	    }

	    @Override
	    public void onMediaScannerConnected() {
	        mMs.scanFile(mFile.getAbsolutePath(), null);
	    }

	    @Override
	    public void onScanCompleted(String path, Uri uri) {
	        mMs.disconnect();
	    }
	}	

	private class MyObservationTypeCounts extends AsyncTask<Void, Void, Void> {
		
		private Long singleCount = null;
		private Long continuousCount = null;

		@Override
		protected Void doInBackground(Void... params) {
			
			try {
                // Test
                boolean test = false;
                if (test) {
                    throw new SQLException("test"); 
                }

				singleCount = observationDao.getObservationTypeCount(ObservationType.SINGLE);
				continuousCount = observationDao.getObservationTypeCount(ObservationType.CONTINUOUS);
			} catch(SQLException ex) {
                singleCount = null;
                continuousCount = null;
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {

		    String errorStr = null;
		    
            if (singleCount != null) {
                singleCountLabel.setText(Long.toString(singleCount));
            } else {
                errorStr = "singleCount is null.";
            }
            
            if (continuousCount != null) {
                continuousCountLabel.setText(Long.toString(continuousCount));
            } else {
                if (errorStr == null) {
                    errorStr = "continuousCount is null.";
                } else {
                    errorStr = "singleCount and continuousCount are null.";
                }
            }

            if (errorStr != null) {
                ErrorHandler errorHandler = ErrorHandler.getInstance();
                errorHandler.logError(Level.WARNING, "OutputObsDataFragment$MyObservationTypeCounts" +
                		".onPostExecute(): " + errorStr,
                        R.string.observation_count_read_error_title,
                        R.string.observation_count_read_error_message);
		    }
		}
	}
	
	private class MyObservationExportTask extends AsyncTask<Void, String, ExportResult> {
		
		private String path;
		private ObservationType observationType;
		private int recordLength;
		private boolean isCancelled;
		
		public MyObservationExportTask(String path, ObservationType observationType, int recordLength) {
			super();
			this.path = path;
			this.observationType = observationType;
			this.recordLength = recordLength;
			isCancelled = false;
		}

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBarDialog = new ProgressDialog(getActivity());
            progressBarDialog.setCancelable(false);
            progressBarDialog.setMessage("Exporting data ...");
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
		protected ExportResult doInBackground(Void... params) {
			
            ExportResult result = new ExportResult();
            
            try {
                List<Observation> observations = observationDao.getObservationsForObservationTypeFull(observationType);
                int obsSize = observations.size();
                progressBarDialog.setMax(obsSize);
                progressBarDialog.setProgressNumberFormat("%1d of %2d Records"); 
                
                if (observationExporter != null) {
                    result = observationExporter.exportSetup(path, observationType, recordLength);
                    
                    if (result.isSuccess()) {
                        for (int index = 0 ; index < obsSize ; index++) {
                            Observation observation = observations.get(index);
                            result = observationExporter.export(observation);

                            // Test
//                            if (((float) index/obsSize) > .5) {
//                                result.setSuccess(false);
//                                result.setErrorMessage("test");
//                            }

                            if (result.isSuccess()) {
                                if (index % 2 == 0) {      // Update every 2%
                                    publishProgress(Integer.toString(index));
                                    if (MyDebug.LOG) {
                                        Log.d(TAG, "Export progress = " + index + " of " + obsSize);
                                    }
                                }
                                
                                if (!progressBarDialog.isShowing()) {
                                    isCancelled = true;
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                        if (result.isSuccess()) {
                            result = observationExporter.exportWrapup();
                        }
                    }
                }
            } catch (SQLException e) {
                if (MyDebug.LOG) {
                    Log.e(TAG, "Failed to get Observations from database ", e);
                }
                result.setSuccess(false);
                result.setErrorMessage(getString(R.string.observation_file_read_error_message));
            }

            return result;
		}
		
        @Override
        protected void onProgressUpdate(String... obsDataString) {
//            setProgressBar(Integer.parseInt(obsDataString[1]));
            progressBarDialog.setProgress(Integer.parseInt(obsDataString[0]));
        }

		@Override
		protected void onPostExecute(ExportResult result) {
			
            if (progressBarDialog != null) {
                progressBarDialog.dismiss();
            }
			if (result.isSuccess()) {
                String msg;
			    if (isCancelled) {
	                msg = getString(R.string.file_write_cancelled);
			    } else {
                    msg = getString(R.string.file_write_complete);
			    }
                Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
                
                // Tell the media scanner to scan file so it shows up on a computer.
                // This is accomplished in the constructor, so nothing else needs to be done with this object.
                @SuppressWarnings("unused")
                SingleMediaScanner mSms = new SingleMediaScanner(getActivity(), new File(path));
			} else {
			    ErrorHandler errorHandler = ErrorHandler.getInstance();
			    errorHandler.logError(Level.WARNING, "OutputObsDataFragment$MyObservationExportTask.onPostExecute(): " +
			            "Data export failed because " + result.getErrorMessage(),
			            R.string.file_write_failed_title, 
			            R.string.file_write_failed_message);
			}
		}

		@Override
        protected void onCancelled(ExportResult result) {
            if (progressBarDialog != null) {
                progressBarDialog.dismiss();
            }
        }
	}
}
