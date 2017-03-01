package com.zlscorp.ultragrav.activity;

import roboguice.inject.InjectView;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.logging.Level;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.google.inject.Inject;
import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.debug.MyDebug;
import com.zlscorp.ultragrav.file.ErrorHandler;
import com.zlscorp.ultragrav.model.Station;
import com.zlscorp.ultragrav.model.StationSeries;
import com.zlscorp.ultragrav.persist.StationDao;
import com.zlscorp.ultragrav.persist.StationSeriesDao;
import com.zlscorp.ultragrav.type.ObservationType;

public class StationDetailsActivity extends AbstractBaseActivity {

    private static String TAG = "StationDetailsActivity";

    @InjectView(R.id.stationIdText)
    private TextView stationIdText;

    @InjectView(R.id.observerIdText)
    private TextView observerIdText;

    @InjectView(R.id.latitudeText)
    private TextView latitudeText;

    @InjectView(R.id.longitudeText)
    private TextView longitudeText;

    @InjectView(R.id.elevationText)
    private TextView elevationText;

    @InjectView(R.id.meterHeightText)
    private TextView meterHeightText;

    @InjectView(R.id.enableEarthtideText)
    private TextView enableEarthText;

    @InjectView(R.id.acceptButton)
    private Button acceptButton;

    @Inject
    private StationSeriesDao stationSeriesDao;

    @Inject
    private StationDao stationDao;

    private ObservationType observationType;
    private String observationNote;
    private StationSeries stationSeries;
    private Station station;

    @Override
    public String getActivityName() {
        return TAG;
    }

    @Override
    public String getHelpKey() {
        return "stationDetails";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_details);
        getSupportActionBar().setTitle(getString(R.string.title_activity_station_details)); 
        Bundle extras = getIntent().getExtras();
        kind = 12;
        station = (Station) extras.get(EXTRA_STATION);
        stationSeries = (StationSeries) extras.get(EXTRA_STATION_SERIES);
        observationType = (ObservationType) extras.get(EXTRA_OBSERVATION_TYPE);
        observationNote = (String) extras.getString(EXTRA_OBSERVATION_NOTE);

        // Don't allow user to start an observation if there is no observationType, 
        // i.e. the user is accessing the stations through the Settings/Stations tab
        if (observationType == null) {
            acceptButton.setVisibility(View.GONE);
        }

        updateView();

        hasFragments = false;
    }

    private void updateView() {
        String useEarthtide = getString(R.string.no);

        updateTextView(stationIdText, station.getStationId());
        updateTextView(observerIdText, station.getObserverId());
        updateTextView(latitudeText, station.getLatitude());
        updateTextView(longitudeText, station.getLongitude());
        updateTextView(elevationText, station.getElevation());
        updateTextView(meterHeightText, station.getMeterHeight());
        if (station.useEarthTide()) {
            useEarthtide = getString(R.string.yes);
        }
        updateTextView(enableEarthText, useEarthtide);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Add the edit button to the menu
        menu.add(Menu.NONE, 0, 25, R.string.edit).
          setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                onEditClicked(item);
                return true;
            }
        }).
        setIcon(android.R.drawable.ic_menu_edit).
        setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        // If this is not the Default station, add the delete button to the menu
        if (station.getDefaultUse() == null || !station.getDefaultUse()) {
            menu.add(Menu.NONE, 0, 50, R.string.delete).
              setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    onDeleteClicked(item);
                    return true;
                }
            }).
            setIcon(android.R.drawable.ic_menu_delete).
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return super.onCreateOptionsMenu(menu);
    }

    public void onAcceptClicked(View view) {
        Intent intent;

        if (MyDebug.LOG) {
            Log.d(TAG, "begin clicked");
        }
        
        if (observationType == ObservationType.SINGLE) {
            intent = ObservationActivity.createIntent(this, observationType, observationNote, 
                    stationSeries, station);
        } else {
            intent = ContinousSetupActivity.createIntent(this, observationType, observationNote, 
                    stationSeries, station);
        }
        startActivityForResult(intent, IntentParams.REQUEST_BEGIN_OBSERVATION);
    }

    public void onEditClicked(MenuItem item) {
        if (MyDebug.LOG) {
            Log.d(TAG, "edit clicked");
        }

        Intent intent = StationEditActivity.createIntent(this, stationSeries, station);
        startActivityForResult(intent, REQUEST_EDIT);
    }

    public void onDeleteClicked(MenuItem item) {
        if (MyDebug.LOG) {
            Log.d(TAG, "delete clicked");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_station_title);
        builder.setMessage(getString(R.string.delete_station_message, station.getStationId()));
        builder.setPositiveButton(R.string.delete, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    stationDao.delete(station);
                    if (stationSeries.getStations().refreshCollection() == 0) {
                        stationSeriesDao.delete(stationSeries);
                    }
                } catch (Exception e) {
                    if (MyDebug.LOG) {
                        Log.e(TAG, "Delete failed", e);
                    }
                    ErrorHandler errorHandler = ErrorHandler.getInstance();
                    errorHandler.logError(Level.WARNING, "StationDetailsActivity.onDeleteClicked()$" +
                            "setPositiveButton$OnClickListener.onClick(): Error while deleting " +
                            "station - " + e,
                            R.string.station_file_delete_error_title,
                            R.string.station_file_delete_error_message);
                }

                setResult(RESULT_DELETE);
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_BEGIN_OBSERVATION) {
            if (resultCode == RESULT_FINISH_OBSERVATION || resultCode == RESULT_CANCELED) {
                setResult(RESULT_FINISH_OBSERVATION);
                finish();
            }
        } else if (requestCode == REQUEST_EDIT) {
            if (resultCode == RESULT_OK) {
                station = (Station) data.getSerializableExtra(EXTRA_STATION);
                stationSeries = (StationSeries) data.getSerializableExtra(EXTRA_STATION_SERIES);
                updateView();
            }
        }
    }

    /**
     * Creates an Intent for this Activity.
     * 
     * @param callee
     * @return Intent
     */
    public static Intent createIntent(Context callee, ObservationType observationType,
            String observationNote, StationSeries stationSeries, Station station) {
        Intent answer = new Intent(callee, StationDetailsActivity.class);
        answer.putExtra(EXTRA_OBSERVATION_TYPE, observationType);
        answer.putExtra(EXTRA_OBSERVATION_NOTE, observationNote);
        answer.putExtra(EXTRA_STATION_SERIES, stationSeries);
        answer.putExtra(EXTRA_STATION, station);
        return answer;
    }

}
