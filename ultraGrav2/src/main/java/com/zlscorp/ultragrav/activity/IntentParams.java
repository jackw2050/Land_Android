package com.zlscorp.ultragrav.activity;



public interface IntentParams {
	
	//public static final int RESULT_OK = see Activity
	//public static final int RESULT_CANCELED = see Activity
	public static final int RESULT_DELETE = 0x0000AA01;
	public static final int RESULT_FINISH_OBSERVATION = 0x0000AA02;
	
	public static final int REQUEST_NOTE = 0x0000EE01;
	public static final int REQUEST_CREATE = 0x0000EE02;
	public static final int REQUEST_DISPLAY = 0x0000EE03;
	public static final int REQUEST_EDIT = 0x0000EE04;
	public static final int REQUEST_BEGIN_OBSERVATION = 0x0000EE05;
	public static final int REQUEST_FILE_SELECT = 0x0000EE06;
    public static final int REQUEST_FILE_PATH_FOR_STATIONS = 0x0000EE07;
	
	public static final String EXTRA_OBSERVATION_TYPE = "observationType";
	public static final String EXTRA_OBSERVATION_NOTE = "observationNote";
	public static final String EXTRA_STATION = "station";
	public static final String EXTRA_STATION_SERIES = "stationSeries";
}
