package com.zlscorp.ultragrav.persist;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.debug.MyDebug;
import com.zlscorp.ultragrav.file.ErrorHandler;
import com.zlscorp.ultragrav.model.Observation;
import com.zlscorp.ultragrav.model.Station;
import com.zlscorp.ultragrav.model.StationSeries;

public class UserDatabaseHelper extends OrmLiteSqliteOpenHelper {
	
	private static final String TAG = "UserDatabaseHelper";
	
	// name of the database file for your application
	private static final String DATABASE_NAME = "userDatabase.db";
	// any time you make changes to your database objects, you may have to increase the database version
	private static final int DATABASE_VERSION = 1;

	// we do this so there is only one helper
	private static UserDatabaseHelper instance = null;
	private static final AtomicInteger usageCounter = new AtomicInteger(0);
	
	private Context context;
	
	public UserDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		onUpgrade(db, 0, DATABASE_VERSION);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try {
			if (oldVersion < 1) {
				// create tables for v1
				TableUtils.createTableIfNotExists(connectionSource, StationSeries.class);
				TableUtils.createTableIfNotExists(connectionSource, Station.class);
				TableUtils.createTableIfNotExists(connectionSource, Observation.class);
				
				// prepopulate data
				Station defaultStation = new Station();
				defaultStation.setDefaultUse(true);
				defaultStation.setStationId(context.getString(R.string.default_name));
				defaultStation.setObserverId(context.getString(R.string.default_name));
				defaultStation.setLatitude(0.0);
				defaultStation.setLongitude(0.0);
				defaultStation.setElevation(0.0);
				defaultStation.setMeterHeight(0.0);
				defaultStation.setEarthTide(true);
				
				Station austinStation = new Station();
				austinStation.setDefaultUse(false);
				austinStation.setStationId(context.getString(R.string.austin));
				austinStation.setObserverId(context.getString(R.string.zls));
				austinStation.setLatitude(30.2866);
				austinStation.setLongitude(-97.7367);
				austinStation.setElevation(189.0);
				austinStation.setMeterHeight(0.0);
				austinStation.setEarthTide(true);
				
				Station beltonStation = new Station();
				beltonStation.setDefaultUse(false);
				beltonStation.setStationId(context.getString(R.string.belton));
				beltonStation.setObserverId(context.getString(R.string.zls));
				beltonStation.setLatitude(31.0557);
				beltonStation.setLongitude(-97.4642);
				beltonStation.setElevation(189.0);
				beltonStation.setMeterHeight(0.0);
				beltonStation.setEarthTide(true);
				
				Station ccnmStation = new Station();
				ccnmStation.setDefaultUse(false);
				ccnmStation.setStationId(context.getString(R.string.ccnm));
				ccnmStation.setObserverId(context.getString(R.string.zls));
				ccnmStation.setLatitude(32.9477);
				ccnmStation.setLongitude(-105.8335);
				ccnmStation.setElevation(2011.3);
				ccnmStation.setMeterHeight(0.0);
				ccnmStation.setEarthTide(true);
				
				Dao<Station, Long> stationDao = getDao(Station.class);
				stationDao.create(defaultStation);
				stationDao.create(austinStation);
				stationDao.create(beltonStation);
				stationDao.create(ccnmStation);
				
				// Test
//				throw new SQLException("test");
			}
		
		} catch (SQLException e) {
            if (MyDebug.LOG) {
                Log.e(TAG, "Can't create database", e);
            }
			ErrorHandler errorHandler = ErrorHandler.getInstance();
            errorHandler.logError(Level.SEVERE, "UserDatabaseHelper.onUpgrade(): " +
            		" - SQLException: Can't create database - " + e, 
            		0, 0);
		}
	}
	
	/**
	 * Get the helper, possibly constructing it if necessary. For each call to this method, there should be 1 and only 1
	 * call to {@link #close()}.
	 */
	public static synchronized UserDatabaseHelper getHelper(Context context) {
		if (instance == null) {
			instance = new UserDatabaseHelper(context);
		}
		usageCounter.incrementAndGet();
		return instance;
	}
	
	/**
	 * Close the database connections and clear any cached DAOs. For each call to {@link #getHelper(Context)}, there
	 * should be 1 and only 1 call to this method. If there were 3 calls to {@link #getHelper(Context)} then on the 3rd
	 * call to this method, the helper and the underlying database connections will be closed.
	 */
	@Override
	public void close() {
		if (usageCounter.decrementAndGet() == 0) {
			super.close();
			instance = null;
		}
	}
}
