package com.zlscorp.ultragrav.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.activity.fragment.StationSeriesEditFragment;
import com.zlscorp.ultragrav.model.StationSeries;

public class StationSeriesEditActivity extends AbstractBaseActivity {
	
	private static String TAG = "StationSeriesEditActivity";
	
	@Override
	public String getActivityName() {
		return TAG;
	}
	
	@Override
	public String getHelpKey() {
		return "stationSeries";
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setTitle(getString(R.string.title_activity_station_series_edit)); 
		kind = 13;
        if (savedInstanceState == null) {
            Fragment newFragment = new StationSeriesEditFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(android.R.id.content, newFragment).commit();
        }

        hasFragments = true;
	}

    /**
     * Creates an Intent for this Activity.
     * @param callee
     * @return Intent
     */
    public static Intent createIntent(Context callee, StationSeries stationSeries) {
    	Intent answer = new Intent(callee, StationSeriesEditActivity.class);
    	answer.putExtra(EXTRA_STATION_SERIES, stationSeries);
		return answer;
    }
}
