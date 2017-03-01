package com.zlscorp.ultragrav.activity;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.zlscorp.ultragrav.BuildConfig;
import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.debug.MyDebug;

/**
 * Created by john on 2/4/16.
 */
public class AboutActivity extends AbstractBaseActivity {

    public final String TAG = "DashboardActivity";

    private TextView versionNumberTV;

    @Override
    public String getActivityName() {
        return TAG;
    }

    @Override
    public String getHelpKey() {
        return "about";
    }

    @Override
    protected void onCreate(Bundle savedInstanceSate) {
        super.onCreate(savedInstanceSate);
        setContentView(R.layout.activity_about);

        versionNumberTV = (TextView) findViewById(R.id.versionNumber);

        String versionName = BuildConfig.VERSION_NAME;
        versionNumberTV.setText(versionName);

//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    public static Intent createIntent(Context callee) {
        if (MyDebug.LOG) {
            Log.d("AboutActivity", "creating about activity");
        }
        return new Intent(callee, AboutActivity.class);
    }

}
