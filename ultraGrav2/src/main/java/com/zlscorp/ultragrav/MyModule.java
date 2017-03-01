package com.zlscorp.ultragrav;

import java.sql.SQLException;
import java.util.logging.Level;

import android.content.Context;
import android.util.Log;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.j256.ormlite.dao.Dao;
import com.zlscorp.ultragrav.debug.MyDebug;
import com.zlscorp.ultragrav.file.ErrorHandler;
import com.zlscorp.ultragrav.model.CalibratedDialValue;
import com.zlscorp.ultragrav.model.CommunicationParams;
import com.zlscorp.ultragrav.model.FactoryParameters;
import com.zlscorp.ultragrav.model.FeedbackScaleParams;
import com.zlscorp.ultragrav.model.LevelCorrectionParams;
import com.zlscorp.ultragrav.model.MeterParams;
import com.zlscorp.ultragrav.model.Observation;
import com.zlscorp.ultragrav.model.Station;
import com.zlscorp.ultragrav.model.StationSeries;
import com.zlscorp.ultragrav.model.SystemParams;
import com.zlscorp.ultragrav.persist.CommunicationParamsDao;
import com.zlscorp.ultragrav.persist.FeedbackScaleParamsDao;
import com.zlscorp.ultragrav.persist.LevelCorrectionParamsDao;
import com.zlscorp.ultragrav.persist.MeterParamsDao;
import com.zlscorp.ultragrav.persist.FactoryParametersDao;
import com.zlscorp.ultragrav.persist.ObservationDao;
import com.zlscorp.ultragrav.persist.CalibratedDialValueDao;
import com.zlscorp.ultragrav.persist.StationDao;
import com.zlscorp.ultragrav.persist.StationSeriesDao;
import com.zlscorp.ultragrav.persist.SystemDatabaseHelper;
import com.zlscorp.ultragrav.persist.SystemParamsDao;
import com.zlscorp.ultragrav.persist.UserDatabaseHelper;

public class MyModule extends AbstractModule {

    private static final String TAG = "MyModule";

    private final Context context;
    private static ErrorHandler errorHandler;

    public MyModule(Context context) {
        this.context = context;
        if (MyDebug.LOG) {
            Log.d(TAG, "MyModule constructor");
        }
        errorHandler = ErrorHandler.getInstance();
        errorHandler.logError(Level.INFO, "MyModule.MyModule()", 0, 0);
    }

    /**
     * This configures the applications class to impl mappings for injected
     * things.
     */
    @Override
    protected void configure() {
        
        if (MyDebug.LOG) {
            Log.d(TAG, "Entered configure()");
        }

        bind(UserDatabaseHelper.class).toProvider(new UserDatabaseHelperProvider(context));

        bind(SystemDatabaseHelper.class).toProvider(new SystemDatabaseHelperProvider(context));

        bind(StationSeriesDao.class);
        bind(new TypeLiteral<Dao<StationSeries, Long>>() {
        }).toProvider(StationSeriesBaseDaoProvider.class);

        bind(StationDao.class);
        bind(new TypeLiteral<Dao<Station, Long>>() {
        }).toProvider(StationBaseDaoProvider.class);

        bind(ObservationDao.class);
        bind(new TypeLiteral<Dao<Observation, Long>>() {
        }).toProvider(ObservationBaseDaoProvider.class);

        bind(LevelCorrectionParamsDao.class);
        bind(new TypeLiteral<Dao<LevelCorrectionParams, Long>>() {
        }).toProvider(LevelCorrectionParamsBaseDaoProvider.class);

        bind(MeterParamsDao.class);
        bind(new TypeLiteral<Dao<MeterParams, Long>>() {
        }).toProvider(MeterParamsBaseDaoProvider.class);

        bind(FactoryParametersDao.class);
        bind(new TypeLiteral<Dao<FactoryParameters, Long>>() {
        }).toProvider(FactoryDefaultParamsBaseDaoProvider.class);

        bind(SystemParamsDao.class);
        bind(new TypeLiteral<Dao<SystemParams, Long>>() {
        }).toProvider(SystemParamsBaseDaoProvider.class);
        
        bind(FeedbackScaleParamsDao.class);
        bind(new TypeLiteral<Dao<FeedbackScaleParams, Long>>() {
        }).toProvider(FeedbackScaleParamsBaseDaoProvider.class);
        
        bind(CalibratedDialValueDao.class);
        bind(new TypeLiteral<Dao<CalibratedDialValue, Long>>() {
        }).toProvider(CalibratedDialValueBaseDaoProvider.class);

        bind(CommunicationParamsDao.class);
        bind(new TypeLiteral<Dao<CommunicationParams, Long>>() {
        }).toProvider(CommunicationParamsBaseDaoProvider.class);
    }

    private static class UserDatabaseHelperProvider implements Provider<UserDatabaseHelper> {

        private Context context;

        public UserDatabaseHelperProvider(Context context) {
            this.context = context;
        }

        @Override
        public UserDatabaseHelper get() {
            return UserDatabaseHelper.getHelper(context);
        }
    }

    private static class SystemDatabaseHelperProvider implements Provider<SystemDatabaseHelper> {

        private Context context;

        public SystemDatabaseHelperProvider(Context context) {
            this.context = context;
        }

        @Override
        public SystemDatabaseHelper get() {
            return SystemDatabaseHelper.getHelper(context);
        }
    }

    private static class StationSeriesBaseDaoProvider implements Provider<Dao<StationSeries, Long>> {

        @Inject
        private UserDatabaseHelper userDatabaseHelper;

        @Override
        public Dao<StationSeries, Long> get() {

            Dao<StationSeries, Long> result = null;
            boolean test = false;
            
            try {
                // Test
                if (test) {
                    throw new SQLException("test");
                } else {
                    result = userDatabaseHelper.getDao(StationSeries.class);
                }
            } catch (SQLException e) {
                if (MyDebug.LOG) {
                    Log.e(TAG, "Unable to create StationSeriesDao", e);
                }
                errorHandler.logError(Level.SEVERE, "MyModule$StationSeriesBaseDaoProvider.get(): " +
                		"Unable to create StationSeriesDao - " + e, 0, 0);
            }
            
            return result;
        }
    }

    private static class StationBaseDaoProvider implements Provider<Dao<Station, Long>> {

        @Inject
        private UserDatabaseHelper userDatabaseHelper;

        @Override
        public Dao<Station, Long> get() {
            
            Dao<Station, Long> result = null;
            
            try {
                result = userDatabaseHelper.getDao(Station.class);
            } catch (SQLException e) {
                if (MyDebug.LOG) {
                    Log.e(TAG, "Unable to create StationDao", e);
                }
                errorHandler.logError(Level.SEVERE, "MyModule$StationBaseDaoProvider.get(): " +
                		"Unable to create StationDao - " + e, 0, 0);
            }
            
            return result;
        }
    }

    private static class ObservationBaseDaoProvider implements Provider<Dao<Observation, Long>> {

        @Inject
        private UserDatabaseHelper userDatabaseHelper;

        @Override
        public Dao<Observation, Long> get() {
            
            Dao<Observation, Long> result = null;
            
            try {
                result = userDatabaseHelper.getDao(Observation.class);
            } catch (SQLException e) {
                if (MyDebug.LOG) {
                    Log.e(TAG, "Unable to create ObservationDao", e);
                }
                errorHandler.logError(Level.SEVERE, "MyModule$ObservationBaseDaoProvider.get(): " +
                		"Unable to create ObservationBaseDaoProvider - " + e, 0, 0);
            }
            
            return result;
        }
    }

    private static class FactoryDefaultParamsBaseDaoProvider implements Provider<Dao<FactoryParameters, Long>> {

        @Inject
        private SystemDatabaseHelper systemDatabaseHelper;

        @Override
        public Dao<FactoryParameters, Long> get() {
            
            Dao<FactoryParameters, Long> result = null;

            try {
                result = systemDatabaseHelper.getDao(FactoryParameters.class);
            } catch (SQLException e) {
                if (MyDebug.LOG) {
                    Log.e(TAG, "Unable to create FactoryParametersDao", e);
                }
                errorHandler.logError(Level.SEVERE, "MyModule$FactoryDefaultParamsBaseDaoProvider.get(): " +
                		"Unable to create FactoryDefaultParamsBaseDaoProvider - " + e, 0, 0);
            }
            
            return result;
        }
    }
    
    private static class MeterParamsBaseDaoProvider implements Provider<Dao<MeterParams, Long>> {

        @Inject
        private SystemDatabaseHelper systemDatabaseHelper;

        @Override
        public Dao<MeterParams, Long> get() {
            
            Dao<MeterParams, Long> result = null;

            try {
                result = systemDatabaseHelper.getDao(MeterParams.class);
            } catch (SQLException e) {
                if (MyDebug.LOG) {
                    Log.e(TAG, "Unable to create MeterParamsDao", e);
                }
                errorHandler.logError(Level.SEVERE, "MyModule$MeterParamsBaseDaoProvider.get(): " +
                		"Unable to create MeterParamsBaseDaoProvider - " + e, 0, 0);
            }
            
            return result;
        }
    }
    
    private static class LevelCorrectionParamsBaseDaoProvider implements
            Provider<Dao<LevelCorrectionParams, Long>> {

        @Inject
        private SystemDatabaseHelper systemDatabaseHelper;

        @Override
        public Dao<LevelCorrectionParams, Long> get() {
            
            Dao<LevelCorrectionParams, Long> result = null;

            try {
                result = systemDatabaseHelper.getDao(LevelCorrectionParams.class);
            } catch (SQLException e) {
                if (MyDebug.LOG) {
                    Log.e(TAG, "Unable to create LevelCorrectionParamsDao", e);
                }
                errorHandler.logError(Level.SEVERE, "MyModule$LevelCorrectionParamsBaseDaoProvider.get(): " +
                		"Unable to create LevelCorrectionParamsBaseDaoProvider - " + e, 0, 0);
            }
            
            return result;
        }
    }

    private static class SystemParamsBaseDaoProvider implements Provider<Dao<SystemParams, Long>> {

        @Inject
        private SystemDatabaseHelper systemDatabaseHelper;

        @Override
        public Dao<SystemParams, Long> get() {
            
            Dao<SystemParams, Long> result = null;

            try {
                result = systemDatabaseHelper.getDao(SystemParams.class);
            } catch (SQLException e) {
                if (MyDebug.LOG) {
                    Log.e(TAG, "Unable to create SystemParamsDao", e);
                }
                errorHandler.logError(Level.SEVERE, "MyModule$SystemParamsBaseDaoProvider.get(): " +
                        "Unable to create SystemParamsBaseDaoProvider - " + e, 0, 0);
            }
            
            return result;
        }
    }

    private static class FeedbackScaleParamsBaseDaoProvider implements Provider<Dao<FeedbackScaleParams, Long>> {

        @Inject
        private SystemDatabaseHelper systemDatabaseHelper;

        @Override
        public Dao<FeedbackScaleParams, Long> get() {
            
            Dao<FeedbackScaleParams, Long> result = null;

            try {
                result = systemDatabaseHelper.getDao(FeedbackScaleParams.class);
            } catch (SQLException e) {
                if (MyDebug.LOG) {
                    Log.e(TAG, "Unable to create FeedbackScaleParamsDao", e);
                }
                errorHandler.logError(Level.SEVERE, "MyModule$FeedbackScaleParamsBaseDaoProvider.get(): " +
                        "Unable to create FeedbackScaleParamsBaseDaoProvider - " + e, 0, 0);
            }
            
            return result;
        }
    }

    private static class CommunicationParamsBaseDaoProvider implements
            Provider<Dao<CommunicationParams, Long>> {

        @Inject
        private SystemDatabaseHelper systemDatabaseHelper;

        @Override
        public Dao<CommunicationParams, Long> get() {
            
            Dao<CommunicationParams, Long> result = null;

            try {
                result = systemDatabaseHelper.getDao(CommunicationParams.class);
            } catch (SQLException e) {
                if (MyDebug.LOG) {
                    Log.e(TAG, "Unable to create CommunicationParamsBaseDao", e);
                }
                errorHandler.logError(Level.SEVERE, "MyModule$CommunicationParamsBaseDaoProvider.get(): " +
                        "Unable to create CommunicationParamsBaseDaoProvider - " + e, 0, 0);
            }
            
            return result;
        }
    }
    
    private static class CalibratedDialValueBaseDaoProvider implements Provider<Dao<CalibratedDialValue, Long>> {

        @Inject
        private SystemDatabaseHelper systemDatabaseHelper;

        @Override
        public Dao<CalibratedDialValue, Long> get() {
            
            Dao<CalibratedDialValue, Long> result = null;

            try {
                result = systemDatabaseHelper.getDao(CalibratedDialValue.class);
            } catch (SQLException e) {
                if (MyDebug.LOG) {
                    Log.e(TAG, "Unable to create CalibratedDialValueDao", e);
                }
                errorHandler.logError(Level.SEVERE, "MyModule$CalibratedDialValueBaseDaoProvider.get(): " +
                        "Unable to create CalibratedDialValueBaseDaoProvider - " + e, 0, 0);
            }
            
            return result;
        }
    }

}
