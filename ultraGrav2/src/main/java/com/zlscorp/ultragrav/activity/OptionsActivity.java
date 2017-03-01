package com.zlscorp.ultragrav.activity;

import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.viewpagerindicator.TabPageIndicator;
import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.activity.fragment.EarthtideFragment;
import com.zlscorp.ultragrav.activity.fragment.OutputObsDataFragment;
import com.zlscorp.ultragrav.activity.fragment.ViewObsDataFragment;
import com.zlscorp.ultragrav.activity.widget.ListFragmentPagerAdapter;
import com.zlscorp.ultragrav.activity.widget.ListFragmentPagerAdapter.TabItem;

public class OptionsActivity extends AbstractBaseActivity {
	
    private static final String TAG = "OptionsActivity";

    @InjectView(R.id.indicator)
    private TabPageIndicator indicator;
	
	@InjectView(R.id.pager)
	private ViewPager pager;
	
	public static Intent createIntent (Context callee) {
		return new Intent(callee, OptionsActivity.class);
	}
    
	@Override
	public String getActivityName() {
		return TAG;
	}
	
	@Override
	public String getHelpKey() {
		return "options";
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_pager_tabs);
        getSupportActionBar().setTitle(getString(R.string.title_activity_options)); 
        kind = 8;
        ListFragmentPagerAdapter adapter = new ListFragmentPagerAdapter(this, getSupportFragmentManager());
        adapter.add(new TabItem(getString(R.string.output_data_tab), OutputObsDataFragment.class));
        adapter.add(new TabItem(getString(R.string.view_data_tab), ViewObsDataFragment.class));
        adapter.add(new TabItem(getString(R.string.earthtide_tab), EarthtideFragment.class));

        pager.setAdapter(adapter);
        indicator.setViewPager(pager);
        indicator.setOnPageChangeListener(adapter);

        hasFragments = true;
    }
}
