package com.zlscorp.ultragrav.activity;

import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.viewpagerindicator.TabPageIndicator;
import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.activity.fragment.CalculateBeamScaleFragment;
import com.zlscorp.ultragrav.activity.fragment.ReadMeterFragment;
import com.zlscorp.ultragrav.activity.fragment.StopsReadingLineFragment;
import com.zlscorp.ultragrav.activity.fragment.CalculateBeamScaleFragment.Mode;
import com.zlscorp.ultragrav.activity.fragment.ReadMeterFragment.ParentActivity;
import com.zlscorp.ultragrav.activity.widget.ListFragmentPagerAdapter;
import com.zlscorp.ultragrav.activity.widget.ListFragmentPagerAdapter.TabItem;

public class SetupActivity extends AbstractBaseActivity {

	private static final String TAG = "SetupActivity";

	@InjectView(R.id.indicator)
	private TabPageIndicator indicator;

	@InjectView(R.id.pager)
	private ViewPager pager;

	public static Intent createIntent(Context callee) {
		return new Intent(callee, SetupActivity.class);
	}

	@Override
	public String getActivityName() {
		return TAG;
	}

	@Override
	public String getHelpKey() {
		return "setup";
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_view_pager_tabs);
		kind = 10;
		getSupportActionBar().setTitle(getString(R.string.title_activity_setup)); 
		ListFragmentPagerAdapter adapter = new ListFragmentPagerAdapter(this,
				getSupportFragmentManager());
		adapter.add(new TabItem(getString(R.string.read_meter_tab),
				ReadMeterFragment.class, ReadMeterFragment
						.createArgs(ParentActivity.SETUP)));

		adapter.add(new TabItem(getString(R.string.set_stops_reading_line),
				StopsReadingLineFragment.class));
		adapter.add(new TabItem(getString(R.string.calculate_beam_scale),
				CalculateBeamScaleFragment.class, CalculateBeamScaleFragment
						.createArgs(Mode.BEAM_K)));
		pager.setAdapter(adapter);
		indicator.setViewPager(pager);
		indicator.setOnPageChangeListener(adapter);

		hasFragments = true;
	}
}
