package com.zlscorp.ultragrav.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import android.content.Context;

import com.google.inject.Inject;
import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.model.Observation;
import com.zlscorp.ultragrav.model.SystemParams;
import com.zlscorp.ultragrav.persist.SystemParamsDao;
import com.zlscorp.ultragrav.type.ObservationType;

public class ObservationExporter {
	
    private static final String DATE_FORMAT = "yyyy/MM/dd";     // It is required by a popular gravity processing application.
    private static final String TIME_FORMAT = "HH:mm:ss";       // It is required by a popular gravity processing application.

	@Inject
	private Context context;
	
    @Inject
    private SystemParamsDao systemParamsDao;

    private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.US);
    private SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT, Locale.US);
    boolean writeHeader;
    int recordLength;
    boolean saveFrequencies;
    int obsSize;
    BufferedWriter outWriter;
    CSVPrinter outCsv;
    SystemParams systemParams = null;

    public ExportResult exportSetup(String outputFilePath, ObservationType observationType, int recordLength) {
        ExportResult result = new ExportResult();

        this.recordLength = recordLength;
        writeHeader = true;

        File outputFile = new File(outputFilePath);
            
            boolean test = false;
//            if (test) {
            if (!outputFile.isFile() || !outputFile.canWrite()) {
                result.setSuccess(false);
                result.setErrorMessage(context.getString(R.string.no_file_write_permissions));
            } else {
                try {
                    // Test
//                    test = false;
//                    if (test) {
//                        throw new IOException("test"); 
//                    }

                    try {
                        // Test
                        if (test) {
                            throw new SQLException("test"); 
                        }

                        systemParams = systemParamsDao.queryForDefault();
                        saveFrequencies = systemParams.isSaveFrequencies();

                        outWriter = new BufferedWriter(new FileWriter(outputFile));
                        outCsv = new CSVPrinter(outWriter, CSVFormat.EXCEL);

                        result.setSuccess(true);

                    } catch (SQLException e) {
                        result.setSuccess(false);
                        result.setErrorMessage(context.getString(R.string.system_params_file_open_error_message) 
                                + " " + e);
                    }
                } catch (Exception e) {
                    result.setSuccess(false);
                    result.setErrorMessage(e.toString());
                }
            }

        return result;
    }
    
    public ExportResult exportWrapup() {
        ExportResult result = new ExportResult();

        try {
            outCsv.flush();
            outWriter.close();
            result.setSuccess(true);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMessage(context.getString(R.string.file_write_failed_message) + " " + e);
        }

        return result;
    }

    public ExportResult export(Observation observation) {
		ExportResult result = new ExportResult();

		try {
		    writeObservation(observation);
		    result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorMessage(context.getString(R.string.file_write_failed_message) + " " + e);
		}
		
		return result;
	}
	
	private void writeObservation(Observation observation) throws IOException {

	    int SHORT_RECORD = 1;
	    int LONG_RECORD = 2;

        String observedGravity = null;
        String dialReading = null;
        String feedbackCorrection = null;
        String earthtide = null;
        String levelCorrection = null;
        String temperatureCorrection = null;

        String stationName = observation.getStationId();
		String observerName = observation.getObserverId();
		String serialNumber = observation.getSerialNumber();

		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String readingDate = dateFormat.format(new Date(observation.getReadingTime()));
        String readingTime = timeFormat.format(new Date(observation.getReadingTime()));

        String height = new DecimalFormat("0.000").format(observation.getMeterHeight());
        String elevation = new DecimalFormat("0.000").format(observation.getElevation());
        String latitude = new DecimalFormat("0.000000").format(observation.getLatitude());
        String longitude = new DecimalFormat("0.000000").format(observation.getLongitude());

        String elapsedTime = observation.getElapsedTime().toString();
        String temperatureFrequency = observation.getTemperatureFrequency().toString();
        String note = observation.getNote();
        if (note == null) {
            note = "No notes.";
        }

        if (observation.getObservationType() == ObservationType.SINGLE) {
            // These values are saved with different precisions for Single and Continuous Observations  
            observedGravity = new DecimalFormat("0.000").format(observation.getObservedGravity());
            dialReading = new DecimalFormat("0.000").format(observation.getDial());
            feedbackCorrection = new DecimalFormat("0.000").format(observation.getFeedbackCorrection());
            earthtide = new DecimalFormat("0.000").format(observation.getEarthtide());
            levelCorrection = new DecimalFormat("0.000").format(observation.getLevelCorrection());
            temperatureCorrection = new DecimalFormat("0.000").format(observation.getTemperatureCorrection());

            String beamError = new DecimalFormat("0.0000").format(observation.getBeamError());
            String standardDeviation = new DecimalFormat("0.0000").format(observation.getStandardDeviation());
            
            if (writeHeader) {
                writeHeader = false;
                outCsv.println("Station ID", "Observer ID", "Serial Number", "Date", "Time", "ObsG", "Dial", 
                        "Feedback Correction", "Earthtide Correction", "Level Correction", "Temperature Correction", "Beam Error", "Height",
                        "Elevation", "Latitude", "Longitude", "Elapsed Time", "Standard Deviation", "Temperature Frequency", "Note");
            }
            
            outCsv.println(stationName, observerName, serialNumber, readingDate, readingTime, observedGravity, dialReading, 
                    feedbackCorrection, earthtide, levelCorrection, temperatureCorrection, beamError, height,
                    elevation, latitude, longitude, elapsedTime, standardDeviation, temperatureFrequency, note);

        } else if (observation.getObservationType() == ObservationType.CONTINUOUS) {
            observedGravity = new DecimalFormat("0.000000").format(observation.getObservedGravity());
            dialReading = new DecimalFormat("0.000000").format(observation.getDial());
            feedbackCorrection = new DecimalFormat("0.000000").format(observation.getFeedbackCorrection());
            earthtide = new DecimalFormat("0.000000").format(observation.getEarthtide()); 
            levelCorrection = new DecimalFormat("0.000000").format(observation.getLevelCorrection());
            temperatureCorrection = new DecimalFormat("0.000000").format(observation.getTemperatureCorrection());
            
            if (recordLength == SHORT_RECORD) {
                if (saveFrequencies) {
                    String beamFrequency = observation.getBeamFrequency().toString();
                    String crossLevelFrequency = observation.getCrossLevelFrequency().toString();
                    String longLevelFrequency = observation.getLongLevelFrequency().toString();

                    if (writeHeader) {
                        writeHeader = false;
                        outCsv.println("Date", "Time", "ObsG", "Dial", "Feedback Correction", "Earthtide Correction", 
                                "Level Correction", "Temperature Correction", "Temperature Frequency", "Beam Frequency", 
                                "Cross Level Frequency", "Long Level Frequency", "Note");
                    }
                    
                    outCsv.println(readingDate, readingTime, observedGravity, dialReading, feedbackCorrection, earthtide, 
                            levelCorrection, temperatureCorrection, temperatureFrequency, beamFrequency, 
                            crossLevelFrequency, longLevelFrequency, note);
                } else {
                    if (writeHeader) {
                        writeHeader = false;
                        outCsv.println("Date", "Time", "ObsG", "Dial", "Feedback Correction", "Earthtide Correction", 
                                "Level Correction", "Temperature Correction", "Note");
                    }
                    
                    outCsv.println(readingDate, readingTime, observedGravity, dialReading, feedbackCorrection, earthtide, 
                            levelCorrection, temperatureCorrection, note);
                }
            } else if (recordLength == LONG_RECORD) {
                String dataOutputRate = observation.getDataOutputRate().toString();
                String filterTimeConstant = observation.getFilterTimeConstant().toString();
                
                if (saveFrequencies) {
                    String beamFrequency = observation.getBeamFrequency().toString();
                    String crossLevelFrequency = observation.getCrossLevelFrequency().toString();
                    String longLevelFrequency = observation.getLongLevelFrequency().toString();

                    if (writeHeader) {
                        writeHeader = false;
                        outCsv.println("Date", "Time", "ObsG", "Dial", "Feedback Correction", "Earthtide Correction", 
                                "Level Correction", "Temperature Correction", "Station ID", "Observer ID", "Serial Number", "Height",
                                "Elevation", "Latitude", "Longitude", "Elapsed Time", "Data Output Rate", "Filter Time Constant", 
                                "Temperature Frequency", "Beam Frequency", "Cross Level Frequency", "Long Level Frequency", "Note");
                    }
                    
                    outCsv.println(readingDate, readingTime, observedGravity, dialReading, feedbackCorrection, earthtide, 
                            levelCorrection, temperatureCorrection, stationName, observerName, serialNumber, height,
                            elevation, latitude, longitude, elapsedTime, dataOutputRate, filterTimeConstant, temperatureFrequency,
                            beamFrequency, crossLevelFrequency, longLevelFrequency, note);
                } else {
                    if (writeHeader) {
                        writeHeader = false;
                        outCsv.println("Date", "Time", "ObsG", "Dial", "Feedback Correction", "Earthtide Correction", 
                                "Level Correction", "Temperature Correction", "Station ID", "Observer ID", "Serial Number", "Height",
                                "Elevation", "Latitude", "Longitude", "Elapsed Time", "Data Output Rate", "Filter Time Constant", 
                                "Temperature Frequency", "Note");
                    }
                    
                    outCsv.println(readingDate, readingTime, observedGravity, dialReading, feedbackCorrection, earthtide, 
                            levelCorrection, temperatureCorrection, stationName, observerName, serialNumber, height,
                            elevation, latitude, longitude, elapsedTime, dataOutputRate, filterTimeConstant, temperatureFrequency, note);
                }
            }
        }
	}
	
	public static class ExportResult {
		private boolean success;
		private String errorMessage;
		
		public boolean isSuccess() {
			return success;
		}

		public void setSuccess(boolean success) {
			this.success = success;
		}

		public String getErrorMessage() {
			return errorMessage;
		}

		public void setErrorMessage(String errorMessage) {
			this.errorMessage = errorMessage;
		}
	}
}
