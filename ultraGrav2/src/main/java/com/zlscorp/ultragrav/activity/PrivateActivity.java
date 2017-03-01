package com.zlscorp.ultragrav.activity;

import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.viewpagerindicator.TabPageIndicator;
import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.activity.fragment.CalculateBeamScaleFragment;
import com.zlscorp.ultragrav.activity.fragment.CalculateBeamScaleFragment.Mode;
import com.zlscorp.ultragrav.activity.fragment.CalculateFeedbackScaleFragment;
import com.zlscorp.ultragrav.activity.fragment.PrivateParamsFragment;
import com.zlscorp.ultragrav.activity.fragment.ReadMeterFragment;
import com.zlscorp.ultragrav.activity.fragment.ReadMeterFragment.ParentActivity;
import com.zlscorp.ultragrav.activity.fragment.StopsReadingLineFragment;
import com.zlscorp.ultragrav.activity.widget.ListFragmentPagerAdapter;
import com.zlscorp.ultragrav.activity.widget.ListFragmentPagerAdapter.TabItem;
import com.zlscorp.ultragrav.model.Station;
import com.zlscorp.ultragrav.model.StationSeries;

public class PrivateActivity extends AbstractBaseActivity {

    private static final String TAG = "PrivateActivity";

	@InjectView(R.id.indicator)
    private TabPageIndicator indicator;
	
	@InjectView(R.id.pager)
	private ViewPager pager;
	
	@Override
	public String getActivityName() {
		return TAG;
	}
	
	@Override
	public String getHelpKey() {
		return "privateActivity";
	}
	public int lankind = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_view_pager_tabs);
		getSupportActionBar().setTitle(getString(R.string.title_activity_private)); 
		kind = 9;
		lankind = pref.getInt("lan", 0);
		ListFragmentPagerAdapter adapter = new ListFragmentPagerAdapter(this, getSupportFragmentManager());
		adapter.add(new TabItem(getString(R.string.title_activity_private), PrivateParamsFragment.class));
        adapter.add(new TabItem(getString(R.string.read_meter_tab), ReadMeterFragment.class, 
                ReadMeterFragment.createArgs(ParentActivity.PRIVATE)));
		adapter.add(new TabItem(getString(R.string.set_stops_reading_line), StopsReadingLineFragment.class));
		adapter.add(new TabItem(getString(R.string.beam_k), CalculateBeamScaleFragment.class, 
		        CalculateBeamScaleFragment.createArgs(Mode.BEAM_K)));
		adapter.add(new TabItem(getString(R.string.ccjr_beam_k), CalculateBeamScaleFragment.class, 
		        CalculateBeamScaleFragment.createArgs(Mode.CCJR_BEAM_K)));
		adapter.add(new TabItem(getString(R.string.calculate_feedback_scale), CalculateFeedbackScaleFragment.class));

		pager.setAdapter(adapter);        
		indicator.setViewPager(pager);
		indicator.setOnPageChangeListener(adapter);

        hasFragments = true;
	}
	
    /**
     * Creates an Intent for this Activity.
     * @param callee
     * @return Intent
     */
    public static Intent createIntent(Context callee, StationSeries stationSeries, Station station) {
    	Intent answer = new Intent(callee, PrivateActivity.class);
    	return answer;
    }
}
