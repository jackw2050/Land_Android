package com.zlscorp.ultragrav.activity;

import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.viewpagerindicator.TabPageIndicator;
import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.activity.fragment.LevelParamsFragment;
import com.zlscorp.ultragrav.activity.fragment.MeterParamsFragment;
import com.zlscorp.ultragrav.activity.fragment.StationListFragment;
import com.zlscorp.ultragrav.activity.fragment.SystemParamsFragment;
import com.zlscorp.ultragrav.activity.widget.ListFragmentPagerAdapter;
import com.zlscorp.ultragrav.activity.widget.ListFragmentPagerAdapter.TabItem;

public class SettingsActivity extends AbstractBaseActivity {
	
    private static final String TAG = "SettingsActivity";

    @InjectView(R.id.indicator)
    private TabPageIndicator indicator;
	
	@InjectView(R.id.pager)
	private ViewPager pager;
    
    public static Intent createIntent(Context context) {
    	return new Intent(context, SettingsActivity.class);
    }
    
	@Override
	public String getActivityName() {
        return TAG;
	}
	
	@Override
	public String getHelpKey() { 
		return "settings";
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_pager_tabs);
        kind = 9;
        getSupportActionBar().setTitle(getString(R.string.title_activity_settings)); 
        ListFragmentPagerAdapter adapter = new ListFragmentPagerAdapter(this, getSupportFragmentManager());
        adapter.add(new TabItem(getString(R.string.system), SystemParamsFragment.class));
        adapter.add(new TabItem(getString(R.string.meter), MeterParamsFragment.class));
        adapter.add(new TabItem(getString(R.string.level), LevelParamsFragment.class));
        adapter.add(new TabItem(getString(R.string.stations), StationListFragment.class));
      

        pager.setAdapter(adapter);        
        indicator.setViewPager(pager);
        indicator.setOnPageChangeListener(adapter);

        hasFragments = true;
    }
}
