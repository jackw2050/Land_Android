package com.zlscorp.ultragrav.model;

import java.util.Comparator;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "stationSeries")
public class StationSeries extends AbstractDomainObject {
	private static final long serialVersionUID = 1L;
	
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_AUTO_INCREMENT = "autoIncrement";
	public static final String COLUMN_STARTING_NUMBER = "startingNumber";
	public static final String COLUMN_COPY_INFO = "copyInfo";
	
	@DatabaseField(columnName=COLUMN_NAME)
	private String name;
	
	@ForeignCollectionField(eager=false)
	private ForeignCollection<Station> stations;
	
	@DatabaseField(columnName=COLUMN_AUTO_INCREMENT)
	private Boolean autoIncrement;
	
	@DatabaseField(columnName=COLUMN_STARTING_NUMBER)
	private Integer startingNumber;
	
	@DatabaseField(columnName=COLUMN_COPY_INFO)
	private Boolean copyInfo;
	
	public StationSeries() {
	}
	
	public ForeignCollection<Station> getStations() {
		return stations;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public Boolean getAutoIncrement() {
		return autoIncrement;
	}

	public void setAutoIncrement(Boolean autoIncrement) {
		this.autoIncrement = autoIncrement;
	}

	public Boolean getCopyInfo() {
		return copyInfo;
	}

	public void setCopyInfo(Boolean copyInfo) {
		this.copyInfo = copyInfo;
	}

	public Integer getStartingNumber() {
		return startingNumber;
	}

	public void setStartingNumber(Integer startingNumber) {
		this.startingNumber = startingNumber;
	}

	public static class AlpabeticalComparator implements Comparator<StationSeries> {

		@Override
		public int compare(StationSeries lhs, StationSeries rhs) {
			if (lhs.getName()==null && rhs.getName()==null) {
				return lhs.getCreateDate().compareTo(rhs.getCreateDate());
			}
			if (lhs.getName()!=null && rhs.getName()==null) {
				return 1;
			}
			if (lhs.getName()==null && rhs.getName()!=null) {
				return -1;
			}
			return lhs.getName().compareTo(rhs.getName());
		}
	}
	
}
