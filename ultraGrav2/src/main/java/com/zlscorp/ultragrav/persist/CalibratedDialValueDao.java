package com.zlscorp.ultragrav.persist;

import java.sql.SQLException;
import java.util.List;

import com.zlscorp.ultragrav.model.CalibratedDialValue;

public class CalibratedDialValueDao extends AbstractDao<CalibratedDialValue> {

	
	public long getCount() throws SQLException {
		
		return baseDao.countOf(baseDao.queryBuilder().setCountOf(true).prepare());
	}
	
	public void deleteAll() throws SQLException {
		
		baseDao.delete(baseDao.deleteBuilder().prepare());
	}
	
	public List<CalibratedDialValue> queryAllCalibratedDialValues() throws SQLException {
		
		return baseDao.query(baseDao.queryBuilder()
				.orderBy(CalibratedDialValue.COLUMN_INDEX, true).prepare());
	}
}
