package com.zlscorp.ultragrav.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.activity.fragment.StationEditFragment;
import com.zlscorp.ultragrav.model.Station;
import com.zlscorp.ultragrav.model.StationSeries;

public class StationEditActivity extends AbstractBaseActivity {
	
	private static String TAG = "StationEditActivity";

	@Override
	public String getActivityName() {
		return TAG;
	}
	
	@Override
	public String getHelpKey() {
		return "stationEdit";
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setTitle(getString(R.string.title_activity_station_edit)); 
		kind = 11;
        if (savedInstanceState == null) {
            Fragment newFragment = new StationEditFragment();
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
    public static Intent createIntent(Context callee, StationSeries stationSeries, Station station) {
    	Intent answer = new Intent(callee, StationEditActivity.class);
    	answer.putExtra(EXTRA_STATION_SERIES, stationSeries);
    	answer.putExtra(EXTRA_STATION, station);
		return answer;
    }
}
