package com.zlscorp.ultragrav.activity.fragment;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.google.inject.Inject;
import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.activity.AbstractBaseActivity;
import com.zlscorp.ultragrav.activity.FileSelectActivity;
import com.zlscorp.ultragrav.activity.StationDetailsActivity;
import com.zlscorp.ultragrav.activity.StationEditActivity;
import com.zlscorp.ultragrav.activity.StationSeriesEditActivity;
import com.zlscorp.ultragrav.activity.adapter.StationListAdapter;
import com.zlscorp.ultragrav.activity.adapter.StationListAdapter.OnStationClickListener;
import com.zlscorp.ultragrav.activity.adapter.StationListAdapter.OnStationConfigureClickListener;
import com.zlscorp.ultragrav.activity.widget.ListFragmentPagerAdapter.OnFragmentSelectedListener;
import com.zlscorp.ultragrav.debug.MyDebug;
import com.zlscorp.ultragrav.file.ErrorHandler;
import com.zlscorp.ultragrav.file.StationImporter.ImportResult;
import com.zlscorp.ultragrav.file.StationExporter.ExportResult;
import com.zlscorp.ultragrav.file.StationExporter;
import com.zlscorp.ultragrav.file.StationImporter;
import com.zlscorp.ultragrav.model.Station;
import com.zlscorp.ultragrav.model.StationSeries;
import com.zlscorp.ultragrav.persist.StationDao;
import com.zlscorp.ultragrav.persist.StationSeriesDao;
import com.zlscorp.ultragrav.type.ObservationType;

import roboguice.inject.InjectView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class StationListFragment extends AbstractBaseFragment implements OnFragmentSelectedListener {

	private static final String TAG = "StationListActivity";

	@Inject
	private StationSeriesDao stationSeriesDao;

	@Inject
	private StationDao stationDao;

	@Inject
	private StationListAdapter stationListAdapter;

    @Inject
    private StationExporter stationExporter;
    
    @Inject
    private StationImporter stationImporter;
    
    @InjectView(R.id.instructionsText)
    private TextView instructionsText;
    
	@InjectView(R.id.stationList)
	private ExpandableListView stationList;

	@InjectView(R.id.progressBar)
	private ProgressBar progressBar;

    @InjectView(R.id.readStationsButton)
    private Button readStationsButton;
    // Should we have option to append to or overwrite existing stations?
    // Check for duplicates within the imported file and between the imported file and existing stations.
    // If we are overwriting the existing stations, check to make sure the Default station still exists.
    
    @InjectView(R.id.writeStationsButton)
    private Button writeStationsButton;
    
	private ObservationType observationType;
	private String observationNote;
	// private List<StationSeries> series;

	private StationSeries selectedStationSeries;
	private Station selectedStation;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_station_list, container, false);
		setHasOptionsMenu(true);
		return v;
	}

	@Override
	public void setupView(View view, Bundle savedInstanceState) {

        // unpack the intent extras
		Bundle args = getArguments();
		if (args != null) {
		    // We got to this fragment on our way to an observation
			observationType = ObservationType.valueOf(getArguments().getString(EXTRA_OBSERVATION_TYPE));
			observationNote = (String) getArguments().getString(EXTRA_OBSERVATION_NOTE);
            instructionsText.setText(R.string.station_instr_for_obs);
		} else {
          // We got to this fragment from the Settings tabs
          instructionsText.setText(R.string.station_instr_settings);
		}
		
		MyOnStationClickListener clickListener = new MyOnStationClickListener();
		stationList.setOnChildClickListener(clickListener);
		stationList.setOnGroupClickListener(clickListener);
		stationListAdapter.setOnStationConfigureClickListener(new MyOnStationConfigureClickListener());
		stationList.setAdapter(stationListAdapter);
		
		setupButtons();
	}

    private void setupButtons() {
        writeStationsButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = FileSelectActivity.createIntent(getActivity(), true);
                startActivityForResult(intent, REQUEST_FILE_PATH_FOR_STATIONS);
            }
        });

        readStationsButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = FileSelectActivity.createIntent(getActivity(), false);
                intent.putExtra("com.zlscorp.ultragrav.fileName", "Stations.txt");
                startActivityForResult(intent, REQUEST_FILE_SELECT);
            }
        });
    }
    
    @Override
    public void onFragmentSelected() {
        AbstractBaseActivity.fragmentName = this.getClass().getSimpleName();
    }

    @Override
    public void onFragmentUnselected() {
    }

    @Override
	public void populateData() {
		new MyLoaderTask().execute();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		menu.add(R.string.new_text).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
				.setIcon(android.R.drawable.ic_menu_add)
				.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
                        Intent intent = StationEditActivity.createIntent(getActivity(), null, null);
                        startActivityForResult(intent, REQUEST_CREATE);
						return true;
					}
				});

		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_DISPLAY) {
			if (resultCode == RESULT_FINISH_OBSERVATION) {
				getActivity().setResult(RESULT_FINISH_OBSERVATION);
				getActivity().finish();
			} else if (resultCode == REQUEST_EDIT || requestCode == RESULT_DELETE) {
				new MyLoaderTask().execute();
			}
		} else if (requestCode == REQUEST_FILE_PATH_FOR_STATIONS) {
            if (resultCode == Activity.RESULT_OK) {
                String filePath = data.getStringExtra(FileSelectFragment.PATH_EXTRA);
                checkFilePermissions(filePath);
            }
        } else if (requestCode == REQUEST_FILE_SELECT) {
            if (resultCode == Activity.RESULT_OK) {
                doStationImport(data.getExtras().getString(FileSelectFragment.PATH_EXTRA));
            }
        }
	}

    private void doStationImport(String path) {

        MyStationImportTask task = new MyStationImportTask(path);
        task.execute();
    }

    private class MyStationImportTask extends AsyncTask<Void, Void, ImportResult> {

        private String path;

        public MyStationImportTask(String path) {
            super();
            this.path = path;
        }

        @Override
        protected ImportResult doInBackground(Void... params) {

            return stationImporter.importStation(path);
        }

        @Override
        protected void onPostExecute(ImportResult result) {

            if (result.isSuccess()) {
                new MyLoaderTask().execute();    // Load the new stations
                Toast.makeText(getActivity(), getString(R.string.file_read_complete), 
                        Toast.LENGTH_LONG).show();
            } else {
                ErrorHandler errorHandler = ErrorHandler.getInstance();
                errorHandler.logError(Level.WARNING, "StationListFragment$MyStationImportTask." +
                		"onPostExecute(): " + result.getErrorMessage(),
                        "Alert - Error reading station list",
                        result.getErrorMessage());
            }
        }
    }

    private void checkFilePermissions(final String path) {

        File file = new File(path);
        boolean fileCreated = false;
        
        try{
            file.getParentFile().mkdirs();
            fileCreated = file.createNewFile();
        } catch (IOException e) {
            ErrorHandler errorHandler = ErrorHandler.getInstance();
            errorHandler.logError(Level.WARNING, "StationListFragment.checkFilePermissions(): " +
                    "Can't create file - " + e,
                    R.string.file_write_failed_title,
                    R.string.file_write_failed_message);
            return;
        }
        
        if (!file.isFile()) {
            ErrorHandler errorHandler = ErrorHandler.getInstance();
            errorHandler.logError(Level.WARNING, "StationListFragment.checkFilePermissions(): " +
                    "Path is not a file - " + path,
                    R.string.path_not_file_title,
                    R.string.path_not_file_message);
            return;
        }
        if (!file.canWrite()) {
            ErrorHandler errorHandler = ErrorHandler.getInstance();
            errorHandler.logError(Level.WARNING, "StationListFragment.checkFilePermissions(): " +
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
                    MyStationExportTask task = new MyStationExportTask(path);
                    task.execute();
                }
            });
            
            builder.create().show();
            return;
        }
        
        MyStationExportTask task = new MyStationExportTask(path);
        task.execute();
    }
    
    private class MyStationExportTask extends AsyncTask<Void, Void, ExportResult> {
        
        private String path;

        public MyStationExportTask(String path) {
            super();
            this.path = path;
        }

        @Override
        protected ExportResult doInBackground(Void... params) {
            
            return stationExporter.export(path);
        }
        
        @Override
        protected void onPostExecute(ExportResult result) {
            
            if (result.isSuccess()) {
                Toast.makeText(getActivity(), getString(R.string.file_write_complete), 
                        Toast.LENGTH_LONG).show();
                
                // Tell the media scanner to scan file so it shows up on a connected computer.
                // This is accomplished in the constructor, so nothing else needs to be done 
                // with this object.
                @SuppressWarnings("unused")
                SingleMediaScanner mSms = new SingleMediaScanner(getActivity(), new File(path));
            } else {
                ErrorHandler errorHandler = ErrorHandler.getInstance();
                errorHandler.logError(Level.WARNING, "StationListFragment$MyStationExportTask." +
                		"onPostExecute(): " + result.getErrorMessage(),
                        "Alert - Error writing station file",
                        result.getErrorMessage());
            }
        }
    }
    
	private void showSeriesMenu() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		CharSequence[] items = new CharSequence[] { getString(R.string.create_station_for_series),
				getString(R.string.edit_series) };
		builder.setItems(items, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				switch (which) {
					case 0: {
						Intent intent = StationEditActivity.createIntent(getActivity(), selectedStationSeries,
								selectedStation);
						startActivityForResult(intent, REQUEST_CREATE);
						break;
					}
					case 1: {
						Intent intent = StationSeriesEditActivity.createIntent(getActivity(), 
						        selectedStationSeries);
						startActivityForResult(intent, REQUEST_CREATE);
						break;
					}
				}
			}
		});
		builder.show();
	}

	/**
	 * Creates an Intent for this Fragment.
	 * 
	 * @param callee
	 * @return Intent
	 */
	public static Bundle createArguments(Context callee, ObservationType observationType,
			String observationNote) {
		Bundle bundle = new Bundle();
		bundle.putString(EXTRA_OBSERVATION_TYPE, observationType.toString());
		bundle.putString(EXTRA_OBSERVATION_NOTE, observationNote);
		return bundle;
	}

	private class MyOnStationConfigureClickListener extends OnStationConfigureClickListener {

		public MyOnStationConfigureClickListener() {
			stationListAdapter.super();
		}

		@Override
		public void onStationSeriesConfigureClicked(StationSeries stationSeries) {
			selectedStationSeries = stationSeries;
			selectedStation = null;

			showSeriesMenu();
		}
	}

	private class MyOnStationClickListener extends OnStationClickListener {

		public MyOnStationClickListener() {
			stationListAdapter.super();
		}

		@Override
		public void onStationSelected(Station station) {
            if (MyDebug.LOG) {
                Log.d(TAG, "selection station");
            }
			selectedStationSeries = station.getStationSeries();
			selectedStation = station;

			Intent intent = StationDetailsActivity.createIntent(getActivity(), observationType, 
			        observationNote, selectedStationSeries, selectedStation);
			startActivityForResult(intent, REQUEST_DISPLAY);
		}

		@Override
		public void onStationSeriesSelected(StationSeries stationSeries) {
            if (MyDebug.LOG) {
                Log.d(TAG, "selection series");
            }
			selectedStationSeries = stationSeries;
			selectedStation = null;
		}
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

	private class MyLoaderTask extends AsyncTask<Void, Void, LoadResult> {

		private List<StationSeries> series;
		private List<Station> stations;

		@Override
		protected void onPreExecute() {
			setInProgress(true);
		}

		@Override
		protected LoadResult doInBackground(Void... params) {
		    LoadResult result = new LoadResult();
//		    Boolean isFailed = false;
		    
			try {
			    // Test
//			    boolean test = true;
//			    if (test) {
//			        throw new SQLException("test");
//			    }

				stations = stationDao.queryForAllWithoutSeries();
				series = stationSeriesDao.queryForAll();

				Collections.sort(series, new StationSeries.AlpabeticalComparator());
				Collections.sort(stations, new Station.AlpabeticalComparator());
                result.setSuccess(true);

			} catch (SQLException e) {
	            if (MyDebug.LOG) {
	                Log.e(TAG, "Unable to query StationSeries", e);
	            }
				result.setSuccess(false);
				result.setErrorMessage(e.toString());
//				isFailed = true;
			}
            return result;
//            return isFailed;
		}

		@Override
		protected void onPostExecute(LoadResult result) {
            setInProgress(false);
		    if (result.isSuccess()) {
	            stationListAdapter.setSeriesAndStations(series, stations);
	            stationListAdapter.notifyDataSetChanged();
		    } else {
	            ErrorHandler errorHandler = ErrorHandler.getInstance();
                errorHandler.logError(Level.WARNING, "StationListFragment$MyLoaderTask.onPostExecute(): " +
                        "Error reading either station or series station DB - " + result.getErrorMessage(),
                        R.string.station_file_read_error_title,
                        R.string.station_file_read_error_message);
		    }
		}

		@Override
		protected void onCancelled(LoadResult result) {
			setInProgress(false);
		}
	}

	private void setInProgress(boolean inProgress) {

        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
            stationList.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            stationList.setVisibility(View.VISIBLE);
        }
    }
	
    public static class LoadResult {
        private boolean success;
        private String errorMessage;
        
        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}
