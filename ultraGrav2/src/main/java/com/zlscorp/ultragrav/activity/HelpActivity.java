package com.zlscorp.ultragrav.activity;

import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.zlscorp.ultragrav.R;

public class HelpActivity extends AbstractBaseActivity {
	
	private static final String TAG = "HelpActivity";
    public static final String EXTRA_ACTIVITY_NAME = "activityName";
    public static final String EXTRA_FRAGMENT_NAME = "fragmentName";
	
    private String activityName;
    private String fragmentName;
	
	@InjectView(R.id.webView)
	private WebView webView;
	
	@Override
	public String getActivityName() {
		return TAG;
	}
	
	@Override
	public String getHelpKey() {
		return null;         // no help
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        kind = 5;
        getSupportActionBar().setTitle(getString(R.string.title_activity_help)); 
        WebSettings webSettings = webView.getSettings();
        webSettings.setDefaultFontSize(24);
        webSettings.setDefaultTextEncodingName("utf-8");
        activityName = getIntent().getExtras().getString(EXTRA_ACTIVITY_NAME);
        fragmentName = getIntent().getExtras().getString(EXTRA_FRAGMENT_NAME);
        
        String helpStr;
        int resID;
        
        if (fragmentName == null) {
            resID = getResources().getIdentifier(activityName, "string", getPackageName());
        } else {
            resID = getResources().getIdentifier(fragmentName, "string", getPackageName());
        }
        
        if (resID == 0) {
            helpStr = getString(R.string.cant_find_help_text);
        } else {
            helpStr = getString(resID);
        }
        webView.loadDataWithBaseURL(null, helpStr, "text/html", "utf-8", null);
      // webView.loadData(helpStr, "text/html", "UTF8");
    }

    /**
     * Creates an Intent for this Activity.
     * @param callee
     * @return Intent
     */
    public static Intent createIntent(Context callee, String activityName, String fragmentName) {
    	Intent intent = new Intent(callee, HelpActivity.class);
        intent.putExtra(EXTRA_ACTIVITY_NAME, activityName);
        intent.putExtra(EXTRA_FRAGMENT_NAME, fragmentName);
		return intent;
    }
}
