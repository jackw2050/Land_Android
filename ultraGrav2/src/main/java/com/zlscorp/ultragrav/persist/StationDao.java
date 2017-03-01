package com.zlscorp.ultragrav.persist;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import com.google.inject.Inject;
import com.j256.ormlite.misc.TransactionManager;
import com.zlscorp.ultragrav.file.ErrorHandler;
import com.zlscorp.ultragrav.model.Station;

public class StationDao extends AbstractDao<Station> {

	@Inject
	private StationSeriesDao stationSeriesDao;
	
    public void deleteAll() throws SQLException {
        baseDao.delete(baseDao.deleteBuilder().prepare());
    }
    
	public List<Station> queryForAllWithoutSeries() throws SQLException {
		return baseDao.queryBuilder().where().isNull(Station.COLUMN_STATION_SERIES_ID).query();
	}
	
	public Station queryForDefaultUse() throws SQLException {
		return baseDao.queryForFirst(baseDao.queryBuilder().
		               where().eq(Station.DEFAULT_USE_COLUMN, true).prepare());
	}
	
	public void createOrUpdateWithSeriesInTransaction(final Station station, final TransactionCallback callback) {
	    
        // Test
//        boolean test = true;
//        if (test) {
//            callback.onFailed(new SQLException("test"));
//        }
		
		try {
			TransactionManager.callInTransaction(baseDao.getConnectionSource(), new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					
					try {
						if (station.getStationSeries()!=null) {
			    			stationSeriesDao.createOrUpdate(station.getStationSeries());
			    		}
						createOrUpdate(station);
						callback.onSuccess();
					} catch(SQLException e) {
                        if (callback != null) {
                            callback.onFailed(e);
                        } else {
                            ErrorHandler errorHandler = ErrorHandler.getInstance();
                            errorHandler.logError(Level.SEVERE, this.getClass().getSimpleName() + 
                                    ".createOrUpdateWithSeriesInTransaction(): " +
                                    "Inner SQLException and callback is null - " + e,
                                    0, 0);
                        }
					}
					
					return null;
				}
			});
		} catch (SQLException e) {
            if (callback != null) {
                callback.onFailed(e);
            } else {
                ErrorHandler errorHandler = ErrorHandler.getInstance();
                errorHandler.logError(Level.SEVERE, this.getClass().getSimpleName() +
                        ".createOrUpdateWithSeriesInTransaction(): " +
                        "Outer SQLException and callback is null - " + e,
                        0, 0);
            }
		}
	}
	
	public int refreshFull(Station obj) throws SQLException {
		int rows = super.refresh(obj);
		if (obj.getStationSeries() != null) {
			// force load the station series for the station
			stationSeriesDao.refresh(obj.getStationSeries());
		}
		return rows;
	}
}
