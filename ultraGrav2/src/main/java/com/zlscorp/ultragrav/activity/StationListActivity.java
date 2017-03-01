package com.zlscorp.ultragrav.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.activity.fragment.StationListFragment;
import com.zlscorp.ultragrav.type.ObservationType;

public class StationListActivity extends AbstractBaseActivity {
	
	private static String TAG = "StationListActivity";

	@Override
	public String getActivityName() {
		return TAG;
	}
	
	@Override
	public String getHelpKey() {
		return "stationList";
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setTitle(getString(R.string.title_activity_station_list)); 
		kind = 12;
        if (savedInstanceState == null) {
            Fragment newFragment = new StationListFragment();
            newFragment.setArguments(getIntent().getExtras());
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
    public static Intent createIntent(Context callee, ObservationType observationType, String observationNote) {
    	Intent answer = new Intent(callee, StationListActivity.class);
    	answer.putExtra(EXTRA_OBSERVATION_TYPE, observationType.toString());
    	answer.putExtra(EXTRA_OBSERVATION_NOTE, observationNote);
		return answer;
    }
}
