package com.zlscorp.ultragrav.activity;

import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.debug.MyDebug;

public class NoteActivity extends AbstractBaseActivity {
	
	private static final String TAG = "NoteActivity";
	
	@InjectView(R.id.noteText)
	private TextView noteText;
	
	@Override
	public String getActivityName() {
		return TAG;
	}
	
	@Override
	public String getHelpKey() {
		return "note";
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        kind = 6;
        getSupportActionBar().setTitle(getString(R.string.title_activity_note)); 
        Intent intent = getIntent();
        
        String note = getNoteFromIntent(intent);
        if (note != null) {
        	noteText.setText(note);
        }

        hasFragments = false;
    }
    
    public void onSaveButtonClicked(View view) {
    	Intent result = new Intent();
    	String note = noteText.getText().toString();
    	result.putExtra(EXTRA_OBSERVATION_NOTE, note);
    	
    	if (getParent() == null) {
    		setResult(RESULT_OK, result);
    	} else {
    		getParent().setResult(RESULT_OK, result);
    	}
    	
    	finish();
    }
    
    public void onCancelButtonClicked(View view) {
    	setResult(RESULT_CANCELED);
    	finish();
    }
    
    public static String getNoteFromIntent(Intent intent) {
    	return intent.getExtras().getString(EXTRA_OBSERVATION_NOTE);
    }

    /**
     * Creates an Intent for this Activity.
     * @param callee
     * @return Intent
     */
    public static Intent createIntent(Context callee, String note) {
        if (MyDebug.LOG) {
            Log.d("NoteActivity", "creating note activity");
        }
    	Intent intent = new Intent(callee, NoteActivity.class);
    	intent.putExtra(EXTRA_OBSERVATION_NOTE, note);
		return intent;
    }
}
