package com.zlscorp.ultragrav.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import android.content.Context;

import com.google.inject.Inject;
import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.model.Station;
import com.zlscorp.ultragrav.persist.StationDao;
//import com.zlscorp.ultragrav.persist.StationSeriesDao;

public class StationExporter {
	
	@Inject
	private Context context;
	
    @Inject
    private StationDao stationDao;
    
//    @Inject
//    private StationSeriesDao stationSeriesDao;
    
    boolean writeHeader;
	
	public ExportResult export(String outputFilePath) {
		ExportResult result = new ExportResult();
		
		try {
			File outputFile = new File(outputFilePath);
			
			if (!outputFile.isFile() || !outputFile.canWrite()) {
				result.setSuccess(false);
				result.setErrorMessage(context.getString(R.string.no_file_write_permissions));
			} else {
				List<Station> stations = stationDao.queryForAllWithoutSeries();
				
				BufferedWriter outWriter = new BufferedWriter(new FileWriter(outputFile));
				CSVPrinter outCsv = new CSVPrinter(outWriter, CSVFormat.EXCEL);
				
                writeHeader = true;
				for (Station station : stations) {
					writeObservation(station, outCsv);
				}
				
				outCsv.flush();
				outWriter.close();
				result.setSuccess(true);
			}
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorMessage(context.getString(R.string.file_write_failed_message));
		}
		
		return result;
	}
	
	private void writeObservation(Station station, CSVPrinter outCsv) throws IOException {

	    String stationId = station.getStationId();
	    String observerId = station.getObserverId();
	    String latitude = new DecimalFormat("0.000000").format(station.getLatitude());
	    String longitude = new DecimalFormat("0.000000").format(station.getLongitude());
	    String elevation = new DecimalFormat("0.000").format(station.getElevation());
	    String height = new DecimalFormat("0.000").format(station.getMeterHeight());
	    String enableEarthtide = station.useEarthTide() ? "Yes" : "No";

	    if (writeHeader) {
	        writeHeader = false;
	        outCsv.println("Station ID", "Observer ID", "Latitude", "Longitude", "Elevation", 
	                "Meter Height", "Enable Earthtide");
	    }

	    outCsv.println(stationId, observerId, latitude, longitude, elevation, height, enableEarthtide);
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
