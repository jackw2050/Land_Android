package com.zlscorp.ultragrav.display;

import com.zlscorp.ultragrav.model.Observation;
import com.zlscorp.ultragrav.type.ObservationType;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ObservationDisplayer {

    public static final String TAG = "ObservationDisplayer";
    
    private static final String DATE_FORMAT = "yyyy/MM/dd";          // This format is required by a popular gravity processing application.
    private static final String TIME_FORMAT = "HH:mm:ss";            // Ditto.

    private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.US);
    private SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT, Locale.US);
    
    private boolean writeHeader;
    private int recordLength;
    private boolean isShowFrequencies;
    
    public ObservationDisplayer(int recordLength, boolean isShowFrequencies) {
        writeHeader = true;
        this.recordLength = recordLength;
        this.isShowFrequencies = isShowFrequencies;
    }
    
    public DisplayResult display(Observation observation) {
        
        DisplayResult displayResult = new DisplayResult();

        String obsDataString = "";

        int SHORT_RECORD = 1;
        int LONG_RECORD = 2;

        String observedGravity = null;
        String feedbackCorrection = null;
        String earthtide = null;
        String levelCorrection = null;
        String temperatureCorrection = null;

        String dialReading = observation.getDial().toString();
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
//            dialReading = new DecimalFormat("0.000").format(observation.getDial());
            feedbackCorrection = new DecimalFormat("0.000").format(observation.getFeedbackCorrection());
            earthtide = new DecimalFormat("0.000").format(observation.getEarthtide());
            levelCorrection = new DecimalFormat("0.000").format(observation.getLevelCorrection());
            temperatureCorrection = new DecimalFormat("0.000").format(observation.getTemperatureCorrection());

            String beamError = new DecimalFormat("0.0000").format(observation.getBeamError());
            String standardDeviation = new DecimalFormat("0.0000").format(observation.getStandardDeviation());
            
            if (writeHeader) {
                writeHeader = false;
                obsDataString = "Single Mode Data\n";
                obsDataString = obsDataString.concat(
                        String.format("%-14s  %-8s  %-5s  %-10s  %-8s  %-5s  " +
                                      "%-4s  %-7s  %-6s  %-7s  %-8s  " +
                                      "%-8s  %-6s  %-9s  %-10s  %-11s  " +
                                      "%-12s  %-7s  %-9s  %-10s\n", 
                                      "Station ID", "Obs ID", "S/N", "Date", "Time", "ObsG", 
                                      "Dial", "FBK Cor", "ET Cor", "Lvl Cor", "Temp Cor", 
                                      "Beam Err", "Height", "Elevation", "Latitude", "Longitude", 
                                      "Elapsed Time", "Std Dev", "Temp Freq", "Note"));
            }
            
            obsDataString = obsDataString.concat(
                    String.format("%-14s  %-8s  %-5s  %-10s  %-8s  %-5s  " +
                                  "%-4s  %7s  %6s  %7s  %8s  " +
                                  "%8s  %-6s  %-9s  %10s  %11s  " +
                                  "%-12s  %-7s  %-9s  %-10s\n", 
                                  stationName, observerName, serialNumber, readingDate, readingTime, observedGravity, 
                                  dialReading, feedbackCorrection, earthtide, levelCorrection, temperatureCorrection, 
                                  beamError, height, elevation, latitude, longitude, 
                                  elapsedTime, standardDeviation, temperatureFrequency, note));

        } else if (observation.getObservationType() == ObservationType.CONTINUOUS) {
            observedGravity = new DecimalFormat("0.000000").format(observation.getObservedGravity());
//            dialReading = new DecimalFormat("0.000000").format(observation.getDial());
            feedbackCorrection = new DecimalFormat("0.000000").format(observation.getFeedbackCorrection());
            earthtide = new DecimalFormat("0.000000").format(observation.getEarthtide());
            levelCorrection = new DecimalFormat("0.000000").format(observation.getLevelCorrection());
            temperatureCorrection = new DecimalFormat("0.000000").format(observation.getTemperatureCorrection());
            
            if (recordLength == SHORT_RECORD) {
                if (isShowFrequencies) {
                    String beamFrequency = observation.getBeamFrequency().toString();
                    String crossLevelFrequency = observation.getCrossLevelFrequency().toString();
                    String longLevelFrequency = observation.getLongLevelFrequency().toString();

                    if (writeHeader) {
                        writeHeader = false;
                        obsDataString = "Short Continuous Mode Data with Frequencies\n";
                        obsDataString = obsDataString.concat(
                                String.format("%-10s  %-9s  %-10s  %-4s  %-10s  " +
                                              "%-9s  %-9s  %-9s  %-9s  " +
                                              "%-9s  %-10s  %-10s  %-10s\n", 
                                              "Date", "Time", "ObsG", "Dial", "FBK Cor", 
                                              "ET", "Lvl Cor", "Temp Cor", "Temp Freq", 
                                              "Beam Freq", "X Lvl Freq", "L Lvl Freq", "Note"));
                    }
                    
                    obsDataString = obsDataString.concat(
                            String.format("%-10s  %-9s  %10s  %-4s  %10s  " +
                                          "%9s  %-9s  %-9s  %-9s  " +
                                          "%-9s  %-10s  %-10s  %-10s\n", 
                                          readingDate, readingTime, observedGravity, dialReading, feedbackCorrection, 
                                          earthtide, levelCorrection, temperatureCorrection, temperatureFrequency, 
                                          beamFrequency, crossLevelFrequency, longLevelFrequency, note));
                } else {
                    if (writeHeader) {
                        writeHeader = false;
                        obsDataString = "Short Continuous Mode Data\n";
                        obsDataString = obsDataString.concat(
                                String.format("%-10s  %-9s  %-10s  %-4s  %-10s  " +
                                              "%-9s  %-9s  %-9s  %-10s\n", 
                                              "Date", "Time", "ObsG", "Dial", "FBK Cor", 
                                              "ET", "Lvl Cor", "Temp Cor", "Note"));
                    }
                    
                    obsDataString = obsDataString.concat(
                            String.format("%-10s  %-9s  %10s  %-4s  %10s  " +
                                          "%9s  %-9s  %-9s  %-10s\n", 
                                          readingDate, readingTime, observedGravity, dialReading, feedbackCorrection, 
                                          earthtide, levelCorrection, temperatureCorrection, note));
                }
            } else if (recordLength == LONG_RECORD) {
                String dataOutputRate = observation.getDataOutputRate().toString();
                String filterTimeConstant = observation.getFilterTimeConstant().toString();
                
                if (isShowFrequencies) {
                    String beamFrequency = observation.getBeamFrequency().toString();
                    String crossLevelFrequency = observation.getCrossLevelFrequency().toString();
                    String longLevelFrequency = observation.getLongLevelFrequency().toString();

                    if (writeHeader) {
                        writeHeader = false;
                        obsDataString = "Long Continuous Mode Data with Frequencies\n";
                        obsDataString = obsDataString.concat(
                                String.format("%-10s  %-9s  %-10s  %-4s  %-10s  " +
                                              "%-9s  %-9s  %-9s  %-14s  %-8s  " +
                                              "%-5s  %-6s  %-10s  %-10s  %-11s  %-12s  " +
                                              "%-16s  %-9s  %-9s  %-9s  " + 
                                              "%-10s  %-10s  %-10s\n", 
                                              "Date", "Time", "ObsG", "Dial", "FBK Cor", 
                                              "ET", "Lvl Cor", "Temp Cor", "Station ID", "Obs ID",
                                              "S/N", "Height", "Elevation", "Latitude", "Longitude", "Elapsed Time", 
                                              "Data Output Rate", "Filter TC", "Temp Freq", "Beam Freq", 
                                              "X Lvl Freq", "L Lvl Freq", "Note"));
                    }
                    
                    obsDataString = obsDataString.concat(
                            String.format("%-10s  %-9s  %10s  %-4s  %10s  " +
                                          "%9s  %9s  %9s  %-14s  %-8s  " +
                                          "%-5s  %-6s  %-10s  %10s  %11s  %-12s  " +
                                          "%-16s  %-9s  %-9s  %-9s  " + 
                                          "%-10s  %-10s  %-10s\n", 
                                          readingDate, readingTime, observedGravity, dialReading, feedbackCorrection, 
                                          earthtide, levelCorrection, temperatureCorrection, stationName, observerName, 
                                          serialNumber, height, elevation, latitude, longitude, elapsedTime, 
                                          dataOutputRate, filterTimeConstant, temperatureFrequency, beamFrequency, 
                                          crossLevelFrequency, longLevelFrequency, note));
                } else {
                    if (writeHeader) {
                        writeHeader = false;
                        obsDataString = "Long Continuous Mode Data\n";
                        obsDataString = obsDataString.concat(
                                String.format("%-10s  %-9s  %-10s  %-4s  %-10s  " +
                                              "%-9s  %-9s  %-9s  %-14s  %-8s  " +
                                              "%-5s  %-6s  %-10s  %-10s  %-11s  %-12s  " +
                                              "%-16s  %-9s  %-9s  %-10s\n", 
                                              "Date", "Time", "ObsG", "Dial", "FBK Cor", 
                                              "ET", "Lvl Cor", "Temp Cor", "Station ID", "Obs ID",
                                              "S/N", "Height", "Elevation", "Latitude", "Longitude", "Elapsed Time", 
                                              "Data Output Rate", "Filter TC", "Temp Freq", "Note"));
                    }
                    
                    obsDataString = obsDataString.concat(
                            String.format("%-10s  %-9s  %10s  %-4s  %10s  " +
                                          "%9s  %9s  %9s  %-14s  %-8s  " +
                                          "%-5s  %-6s  %-10s  %10s  %11s  %-12s  " +
                                          "%-16s  %-9s  %-9s  %-10s\n", 
                                          readingDate, readingTime, observedGravity, dialReading, feedbackCorrection, 
                                          earthtide, levelCorrection, temperatureCorrection, stationName, observerName, 
                                          serialNumber, height, elevation, latitude, longitude, elapsedTime, 
                                          dataOutputRate, filterTimeConstant, temperatureFrequency, note));
                }
            }
        }
        
        displayResult.setMessage(obsDataString);
        displayResult.setSuccess(true);
        return displayResult;
    }

    public static class DisplayResult {
        private boolean success;
        private String message;
        
        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
