package com.zlscorp.ultragrav.persist;

import java.sql.SQLException;
import java.util.List;

import com.google.inject.Inject;
import com.zlscorp.ultragrav.model.Observation;
import com.zlscorp.ultragrav.type.ObservationType;

public class ObservationDao extends AbstractDao<Observation> {
	
    @Inject
    private ObservationDao observationDao;
    
//	@Inject
//	private StationDao stationDao;
	
	
	public long getObservationTypeCount(ObservationType observationType) throws SQLException {
		
		return baseDao.countOf(baseDao.queryBuilder().setCountOf(true)
				.where().eq(Observation.COLUMN_OBSERVATION_TYPE, observationType).prepare());
	}
	
    public void deleteObservationsForObservationTypeFull(ObservationType observationType) throws SQLException {
        List<Observation> observations = observationDao.getObservationsForObservationTypeFull(observationType);

        for (Observation observation : observations) {
            delete(observation);
        }
    }

    public List<Observation> getObservationsForObservationTypeFull(ObservationType observationType) throws SQLException {

        List<Observation> observations = baseDao.query(baseDao.queryBuilder()
                .where().eq(Observation.COLUMN_OBSERVATION_TYPE, observationType).prepare());
//		for (Observation observation : observations) {
//			// force load the station for for the observation
//			if (observation.getStation() != null) {
//				stationDao.refreshFull(observation.getStation());
//			}
//		}
		
        return observations;
	}
}
