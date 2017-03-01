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
import com.zlscorp.ultragrav.debug.MyDebug;
import com.zlscorp.ultragrav.file.ErrorHandler;
import com.zlscorp.ultragrav.model.CalibratedDialValue;
import com.zlscorp.ultragrav.model.CommunicationParams;
import com.zlscorp.ultragrav.model.FeedbackScaleParams;
import com.zlscorp.ultragrav.model.LevelCorrectionParams;
import com.zlscorp.ultragrav.model.MeterParams;
import com.zlscorp.ultragrav.model.SystemParams;
import com.zlscorp.ultragrav.model.FactoryParameters;
import com.zlscorp.ultragrav.type.CommunicationType;

public class SystemDatabaseHelper extends OrmLiteSqliteOpenHelper {
	
	private static final String TAG = "SystemDatabaseHelper";
	
    // name of the database file for your application
	private static final String DATABASE_NAME = "systemDatabase.db";
	// any time you make changes to your database objects, you may have to increase the database version
	private static final int DATABASE_VERSION = 1;
	
	// we do this so there is only one helper
	private static SystemDatabaseHelper instance = null;
	private static final AtomicInteger usageCounter = new AtomicInteger(0);
	
	public SystemDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
				TableUtils.createTableIfNotExists(connectionSource, LevelCorrectionParams.class);
				TableUtils.createTableIfNotExists(connectionSource, MeterParams.class);
				TableUtils.createTableIfNotExists(connectionSource, SystemParams.class);
                TableUtils.createTableIfNotExists(connectionSource, CommunicationParams.class);
                TableUtils.createTableIfNotExists(connectionSource, FeedbackScaleParams.class);
                TableUtils.createTableIfNotExists(connectionSource, FactoryParameters.class);
                TableUtils.createTableIfNotExists(connectionSource, CalibratedDialValue.class);
				
                // System Params
				SystemParams systemParams = new SystemParams();
				systemParams.setDefaultUse(true);
				systemParams.setUseNoncalibratedPoints(false);
				systemParams.setDialReading(2750);
				systemParams.setFeedbackGain(1.0);
				systemParams.setObservationPrecision(0.01);
				systemParams.setEnableStationSelect(true);
				systemParams.setUseCelsius(true);
				systemParams.setTestMode(false);
				systemParams.setSaveFrequencies(false);
				systemParams.setDataOutputRate(10);
				systemParams.setFilterTimeConstant(5);
                systemParams.setJustRestored(false);
				
				Dao<SystemParams, Long> systemParamsDao = getDao(SystemParams.class);
				systemParamsDao.create(systemParams);
				
                // Level Params
				LevelCorrectionParams levelCorrectionParams = new LevelCorrectionParams();
				levelCorrectionParams.setDefaultUse(true);
				levelCorrectionParams.setCrossLevel0(0);
				levelCorrectionParams.setCrossLevelA(0.0);
				levelCorrectionParams.setCrossLevelB(0.0);
				levelCorrectionParams.setCrossLevelC(0.0);
				levelCorrectionParams.setLongLevel0(0);
				levelCorrectionParams.setLongLevelA(0.0);
				levelCorrectionParams.setLongLevelB(0.0);
				levelCorrectionParams.setLongLevelC(0.0);
	            levelCorrectionParams.setJustRestored(false);
				
				Dao<LevelCorrectionParams, Long> levelCorrectionParamsDao = getDao(LevelCorrectionParams.class);
				levelCorrectionParamsDao.create(levelCorrectionParams);
				
                // Meter Params
				MeterParams meterParams = new MeterParams();
				meterParams.setDefaultUse(true);
                meterParams.setReadingLine(7000);
				meterParams.setBeamScale(-0.0005);
				meterParams.setFeedbackScale(0.0009);
                meterParams.setMinStop(6000);
				meterParams.setMaxStop(8000);
				meterParams.setGain(0.62);
				meterParams.setStopBoost(400);
				meterParams.setBoostFactor(3.5);
				meterParams.setSerialNumber("None");
				meterParams.setCalibrated(false);
				meterParams.setPlatesReversed(false);
				meterParams.setTemperature0(0);
				meterParams.setTemperatureA(0.0);
				meterParams.setTemperatureB(0.0);
				meterParams.setTemperatureC(0.0);
								
				Dao<MeterParams, Long> meterParamsDao = getDao(MeterParams.class);
				meterParamsDao.create(meterParams);

                // Feedback Scale Params - used in Private/Feedback tab
                FeedbackScaleParams feedbackScaleParams = new FeedbackScaleParams();
                feedbackScaleParams.setDefaultUse(true);
                feedbackScaleParams.setCcnmFactor(1.0);

                Dao<FeedbackScaleParams, Long> feedbackScaleParamsDao = getDao(FeedbackScaleParams.class);
                feedbackScaleParamsDao.create(feedbackScaleParams);

                // Factory Parameters
                FactoryParameters factoryParameters = new FactoryParameters();
                factoryParameters.setDefaultUse(true);
                factoryParameters.setFileCreated(false);

                Dao<FactoryParameters, Long> factoryParametersDao = getDao(FactoryParameters.class);
                factoryParametersDao.create(factoryParameters);

                // Communication Params
				CommunicationParams communicationParams = new CommunicationParams();
				communicationParams.setDefaultUse(true);
				communicationParams.setName("Mock ZLS Device");
				communicationParams.setCommunicationType(CommunicationType.MOCK);
				communicationParams.setAddress("00:00:00:00:00:00");
				
				Dao<CommunicationParams, Long> communicationParamsDao = getDao(CommunicationParams.class);
				communicationParamsDao.create(communicationParams);
			}
		} catch (SQLException e) {
            if (MyDebug.LOG) {
                Log.e(TAG, "Can't create database", e);
            }
            ErrorHandler errorHandler = ErrorHandler.getInstance();
            errorHandler.logError(Level.SEVERE, "SystemDatabaseHelper.onUpgrade(): " +
            		"Can't create database - " + e, 
            		0, 0);
		}
	}
	
	/**
	 * Get the helper, possibly constructing it if necessary. For each call to this method, there should be 1 and only 1
	 * call to {@link #close()}.
	 */
	public static synchronized SystemDatabaseHelper getHelper(Context context) {
		if (instance == null) {
			instance = new SystemDatabaseHelper(context);
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
