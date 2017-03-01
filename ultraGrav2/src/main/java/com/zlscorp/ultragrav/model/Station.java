package com.zlscorp.ultragrav.model;

import java.util.Comparator;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "station")
public class Station extends AbstractDomainObject {
	private static final long serialVersionUID = 1L;
	
	public static final String COLUMN_STATION_ID = "stationId";
	public static final String COLUMN_STATION_SERIES_ID = "stationSeriesId";
	public static final String COLUMN_OBSERVER_ID = "observerId";
	public static final String COLUMN_LONGITUDE = "longitude";
	public static final String COLUMN_LATITUDE = "latitude";
	public static final String COLUMN_ELEVATION = "elevation";
	public static final String COLUMN_METER_HEIGHT = "meterHeight";
	public static final String COLUMN_USE_EARTH_TIDE = "useEarthTide";
    public static final String DEFAULT_USE_COLUMN = "defaultUse";
	
	@DatabaseField(columnName=COLUMN_STATION_ID)
	private String stationId;
	
	@DatabaseField(columnName=COLUMN_STATION_SERIES_ID, canBeNull=true, foreign=true)
	private StationSeries stationSeries;
	
	@DatabaseField(columnName=COLUMN_OBSERVER_ID)
	private String observerId;
	
	@DatabaseField(columnName=COLUMN_LONGITUDE)
	private Double longitude;
	
	@DatabaseField(columnName=COLUMN_LATITUDE)
	private Double latitude;
	
	@DatabaseField(columnName=COLUMN_ELEVATION)
	private Double elevation;
	
	@DatabaseField(columnName=COLUMN_METER_HEIGHT)
	private Double meterHeight;
	
	@DatabaseField(columnName=COLUMN_USE_EARTH_TIDE)
	private Boolean useEarthTide;
	
    @DatabaseField(columnName=DEFAULT_USE_COLUMN)
    private Boolean defaultUse;

	
	public Station() {
	}

    public String getStationId() {
        return stationId;
    }

    public static int getStationIdMaxLength() {
        return 14;
    }

	public void setStationId(String stationId) {
		this.stationId = stationId;
	}

	public StationSeries getStationSeries() {
		return stationSeries;
	}

	public void setStationSeries(StationSeries stationSeries) {
		this.stationSeries = stationSeries;
	}

    public String getObserverId() {
        return observerId;
    }

    public static int getObserverIdMaxLength() {
        return 8;
    }

    public void setObserverId(String observerId) {
        this.observerId = observerId;
    }
    
	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getElevation() {
		return elevation;
	}

	public void setElevation(Double elevation) {
		this.elevation = elevation;
	}

	public Double getMeterHeight() {
		return meterHeight;
	}

	public void setMeterHeight(Double meterHeight) {
		this.meterHeight = meterHeight;
	}

	public Boolean useEarthTide() {
		return useEarthTide;
	}

	public void setEarthTide(Boolean earthTide) {
		this.useEarthTide = earthTide;
	}

	public Boolean getDefaultUse() {
		return defaultUse;
	}

	public void setDefaultUse(Boolean defaultUse) {
		this.defaultUse = defaultUse;
	}
	
	public static class AlpabeticalComparator implements Comparator<Station> {

		@Override
		public int compare(Station lhs, Station rhs) {
			if (lhs.getStationId()==null && rhs.getStationId()==null) {
				return lhs.getCreateDate().compareTo(rhs.getCreateDate());
			}
			if (lhs.getStationId()!=null && rhs.getStationId()==null) {
				return 1;
			}
			if (lhs.getStationId()==null && rhs.getStationId()!=null) {
				return -1;
			}
			return lhs.getStationId().compareTo(rhs.getStationId());
		}
	}
	
}
