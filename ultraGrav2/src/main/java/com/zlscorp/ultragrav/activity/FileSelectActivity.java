package com.zlscorp.ultragrav.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.activity.fragment.FileSelectFragment;

public class FileSelectActivity extends AbstractBaseActivity {
	
	private static String TAG = "FileSelectActivity";

	@Override
	public String getActivityName() {
		return TAG;
	}
	
	@Override
	public String getHelpKey() {
		return "fileSelect";
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setTitle(getString(R.string.title_activity_select_file)); 
		kind = 4;
        if (savedInstanceState == null) {
            Fragment newFragment = new FileSelectFragment();
            newFragment.setArguments(getIntent().getExtras());
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(android.R.id.content, newFragment, FileSelectFragment.TAG).commit();
        }

        hasFragments = true;
	}
	
    /**
     * Creates an Intent for this Activity.
     * @param callee
     * @return Intent
     */
    public static Intent createIntent(Context callee, boolean allowCreate) {
    	Intent answer = new Intent(callee, FileSelectActivity.class);
    	answer.putExtra(FileSelectFragment.ALLOW_CREATE_EXTRA, allowCreate);
		return answer;
    }
}