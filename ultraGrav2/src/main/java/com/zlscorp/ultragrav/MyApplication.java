package com.zlscorp.ultragrav;

import android.app.Application;
import android.util.Log;

import java.util.logging.Level;

import com.zlscorp.ultragrav.debug.MyDebug;
import com.zlscorp.ultragrav.file.ErrorHandler;

public class MyApplication extends Application {
    
    private static final String TAG = "MyApplication";
	
	@Override
	public void onCreate() {
	    
		super.onCreate();
		
        if (MyDebug.LOG) {
            Log.d(TAG, "Starting ErrorHandler");
        }

        ErrorHandler errorHandler = ErrorHandler.getInstance(this);
        errorHandler.logError(Level.INFO, "MyApplication.onCreate() - Starting application.", 0, 0);
	}
}
