package com.zlscorp.ultragrav.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.zlscorp.ultragrav.type.ObservationType;

@DatabaseTable(tableName = "observation")
public class Observation extends AbstractDomainObject {
	private static final long serialVersionUID = 1L;
	
	public static final String COLUMN_STATION_ID = "stationId";
    public static final String COLUMN_OBSERVER_ID = "observerId";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_ELEVATION = "elevation";
    public static final String COLUMN_METER_HEIGHT = "meterHeight";
    public static final String COLUMN_USE_EARTH_TIDE = "useEarthTide";
	public static final String COLUMN_OBSERVATION_TYPE = "observationType";
    public static final String COLUMN_SERIAL_NUMBER = "serialNumber";
	public static final String COLUMN_READING_TIME = "readingTime";
	public static final String COLUMN_OBSERVED_GRAVITY = "observedGraity";
	public static final String COLUMN_DIAL = "dial";
	public static final String COLUMN_FEEDBACK_CORRECTION = "feedbackCorrection";
	public static final String COLUMN_EARTH_TIDE = "earthtide";
	public static final String COLUMN_LEVEL_CORRECTION = "levelCorrection";
	public static final String COLUMN_TEMPERATURE_CORRECTION = "temperatureCorrection";
	public static final String COLUMN_BEAM_ERROR = "beamError";
	public static final String COLUMN_ELAPSED_TIME = "elapsedTime";
	public static final String COLUMN_DATA_OUTPUT_RATE = "dataOutputRate";
	public static final String COLUMN_FILTER_TIME_CONSTANT = "filterTimeConstant";
	public static final String COLUMN_STANDARD_DEVIATION = "standardDeviation";
    public static final String COLUMN_TEMPERATURE_FREQUENCY = "temperatureFrequency";
    public static final String COLUMN_BEAM_FREQUENCY = "beamFrequency";
    public static final String COLUMN_CROSS_LEVEL_FREQUENCY = "crossLevelFrequency";
    public static final String COLUMN_LONG_LEVEL_FREQUENCY = "longLevelFrequency";
	public static final String COLUMN_NOTE = "note";
	
//  N = New, as in not saved in Aceeca program
//	**Field********************Single Obs****Continuous Observation Data Record****
//    Station.Name             x             N	
//    Station.Observer         x             N
//    Meter S/N                x             N
//    Year (yyyy)              x             x
//    Month (mm)               x             x
//    Day (dd)                 x             x
//    Hour (hh)                x             x
//    Minute (mm)              x             x
//    Second (ss)              x             x
//	  Observed Gravity         x             x
//	  Dial (ST)                x             x
//	  Feedback (DC)            x             x
//	  Earthtide Correction     x             x
//    Level Correction         x             x
//    Temperature Correction   x             x
//    Beam Error (bavg)        x             
//    Station.Height           x             N
//    Station.Elevation        x             N
//    Station.Latitude         x             N
//    Station.Longitude        x             N
//	  Elapsed Time (seconds)   N             N
//	  Data Output Rate                       N
//	  Filter Time Constant                   N
//	  Standard Deviation       N             
//	  Temperature Frequency    N             N
//	  Note                     N             N
//	  Cross Level Frequency                  N*
//	  Long Level Frequency                   N*
//	  Beam Frequency                         N*
//
//    * = if Save Frequencies checkbox in ZLS Private activity is checked.
	
    @DatabaseField(columnName=COLUMN_STATION_ID, canBeNull=false)    // , foreign=true 
	private String stationId;
	
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
    
	@DatabaseField(columnName=COLUMN_OBSERVATION_TYPE, dataType=DataType.ENUM_STRING)
	private ObservationType observationType;

    @DatabaseField(columnName=COLUMN_SERIAL_NUMBER)
    private String serialNumber;
    
	@DatabaseField(columnName=COLUMN_READING_TIME)
	private Long readingTime;
	
	@DatabaseField(columnName=COLUMN_OBSERVED_GRAVITY)
	private Double observedGravity;
	
	@DatabaseField(columnName=COLUMN_DIAL)
	private Integer dial;
	
	@DatabaseField(columnName=COLUMN_FEEDBACK_CORRECTION)
	private Double feedbackCorrection;
	
	@DatabaseField(columnName=COLUMN_EARTH_TIDE)
	private Double earthtide;
	
	@DatabaseField(columnName=COLUMN_LEVEL_CORRECTION)
	private Double levelCorrection;
	
	@DatabaseField(columnName=COLUMN_TEMPERATURE_CORRECTION)
	private Double temperatureCorrection;
	
	@DatabaseField(columnName=COLUMN_BEAM_ERROR)
	private Double beamError;
	
	@DatabaseField(columnName=COLUMN_ELAPSED_TIME)
	private Long elapsedTime;
	
	@DatabaseField(columnName=COLUMN_DATA_OUTPUT_RATE)
	private Integer dataOutputRate;
	
	@DatabaseField(columnName=COLUMN_FILTER_TIME_CONSTANT)
	private Integer filterTimeConstant;
	
	@DatabaseField(columnName=COLUMN_STANDARD_DEVIATION)
	private Double standardDeviation;
	
    @DatabaseField(columnName=COLUMN_TEMPERATURE_FREQUENCY)
    private Integer temperatureFrequency;
    
    @DatabaseField(columnName=COLUMN_BEAM_FREQUENCY)
    private Integer beamFrequency;
    
    @DatabaseField(columnName=COLUMN_CROSS_LEVEL_FREQUENCY)
    private Integer crossLevelFrequency;
    
    @DatabaseField(columnName=COLUMN_LONG_LEVEL_FREQUENCY)
    private Integer longLevelFrequency;
    
	@DatabaseField(columnName=COLUMN_NOTE)
	private String note;
	
    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public String getObserverId() {
        return observerId;
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

    public Boolean getUseEarthTide() {
        return useEarthTide;
    }

    public void setUseEarthTide(Boolean useEarthTide) {
        this.useEarthTide = useEarthTide;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

	public ObservationType getObservationType() {
		return observationType;
	}

	public void setObservationType(ObservationType observationType) {
		this.observationType = observationType;
	}

	public Long getReadingTime() {
		return readingTime;
	}

	public void setReadingtime(Long readingTime) {
		this.readingTime = readingTime;
	}

	public Double getObservedGravity() {
		return observedGravity;
	}

	public void setObservedGravity(Double observedGravity) {
		this.observedGravity = observedGravity;
	}

	public Integer getDial() {
		return dial;
	}

	public void setDial(Integer dial) {
		this.dial = dial;
	}

	public Double getFeedbackCorrection() {
		return feedbackCorrection;
	}

	public void setFeedbackCorrection(Double feedbackCorrection) {
		this.feedbackCorrection = feedbackCorrection;
	}

	public Double getEarthtide() {
		return earthtide;
	}

	public void setEarthtide(Double earthtide) {
		this.earthtide = earthtide;
	}

	public Double getLevelCorrection() {
		return levelCorrection;
	}

	public void setLevelCorrection(Double levelCorrection) {
		this.levelCorrection = levelCorrection;
	}

	public Double getTemperatureCorrection() {
		return temperatureCorrection;
	}

	public void setTemperatureCorrection(Double temperatureCorrection) {
		this.temperatureCorrection = temperatureCorrection;
	}

	public Double getBeamError() {
		return beamError;
	}

	public void setBeamError(Double beamError) {
		this.beamError = beamError;
	}

	public Long getElapsedTime() {
		return elapsedTime;
	}

	public void setElapsedTime(Long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	public Integer getDataOutputRate() {
		return dataOutputRate;
	}

	public void setDataOutputRate(Integer dataOutputRate) {
		this.dataOutputRate = dataOutputRate;
	}

	public Integer getFilterTimeConstant() {
		return filterTimeConstant;
	}

	public void setFilterTimeConstant(Integer filterTimeConstant) {
		this.filterTimeConstant = filterTimeConstant;
	}

	public Double getStandardDeviation() {
		return standardDeviation;
	}

	public void setStandardDeviation(Double standardDeviation) {
		this.standardDeviation = standardDeviation;
	}

	public Integer getTemperatureFrequency() {
		return temperatureFrequency;
	}

	public void setTemperatureFrequency(Integer temperatureFrequency) {
		this.temperatureFrequency = temperatureFrequency;
	}

	public Integer getBeamFrequency() {
        return beamFrequency;
    }

    public void setBeamFrequency(Integer beamFrequency) {
        this.beamFrequency = beamFrequency;
    }

    public Integer getCrossLevelFrequency() {
        return crossLevelFrequency;
    }

    public void setCrossLevelFrequency(Integer crossLevelFrequency) {
        this.crossLevelFrequency = crossLevelFrequency;
    }

    public Integer getLongLevelFrequency() {
        return longLevelFrequency;
    }

    public void setLongLevelFrequency(Integer longLevelFrequency) {
        this.longLevelFrequency = longLevelFrequency;
    }

    public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
}
