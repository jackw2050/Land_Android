package com.zlscorp.ultragrav.file;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
//import java.util.Scanner;

import android.content.Context;

import com.google.inject.Inject;
import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.model.Station;
import com.zlscorp.ultragrav.persist.StationDao;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;


public class StationImporter {
	
	@Inject
	private Context context;
	
	@Inject
	private StationDao stationDao;
	
	public ImportResult importStation(String inputFilePath) {
		
		ImportResult result = new ImportResult();
		
		int lineCount = 0;
		try {
			File inputFile = new File(inputFilePath);
			
			if (!inputFile.isFile() || !inputFile.canRead()) {
				result.setSuccess(false);
				result.setErrorMessage(context.getString(R.string.no_file_read_permissions));
			} else {
                List<Station> stations = new ArrayList<Station>();
                
                // These are the station info columns, in this order: 
                // Station ID , Observer ID , Latitude , Longitude , Elevation , Meter Height , Enable Earthtide
			    CSVParser parser = new CSVParser(new FileReader(inputFile), CSVFormat.EXCEL.withHeader());
			    for (CSVRecord record : parser) {
                    lineCount++;
                    // Don't import the Default station. We recreate it below.
			        if (!record.get(0).equals("default") && !record.get(0).equals("Default")) {

			            Station station = new Station();
	                    station.setDefaultUse(false);

	                    station.setStationId(record.get(0));
	                    station.setObserverId(record.get(1));
	                    station.setLatitude(Double.parseDouble(record.get(2)));
	                    station.setLongitude(Double.parseDouble(record.get(3)));
	                    station.setElevation(Double.parseDouble(record.get(4)));
	                    station.setMeterHeight(Double.parseDouble(record.get(5)));

	                    if (record.get(6).equals("Yes") || record.get(6).equals("yes")) {
	                        station.setEarthTide(true);
	                    } else {
	                        station.setEarthTide(false);
	                    }
	                    
	                    stations.add(station);
			        }
			    }
			    
//			    List<Station> stations = new ArrayList<Station>();
//				
//				Scanner scanner = new Scanner(inputFile).useDelimiter("\\r|\\n");     // was "\r?\n", which seemed to work for files with \n, but not for files with \r
				
//				while(scanner.hasNext()) {
//					lineCount++;
//					String line = scanner.next();
//					
////                    if (line.startsWith("#")) { // ignore comment lines
//                    if (lineCount == 1) { // ignore comment lines
//						continue;
//					}
//					
//					line = line.trim();
//					String[] stationValues = line.split(",");
//					
//                    // Don't import the Default station. We recreate it below.
//					if (stationValues[0].equals("Default") || stationValues[0].equals("default")) {
//                        continue;
//                    }
//
//                    Station station = new Station();
//	                station.setDefaultUse(false);
//
//					station.setStationId(stationValues[0]);
//					station.setObserverName(stationValues[1]);
//					station.setLatitude(Double.parseDouble(stationValues[2]));
//					station.setLongitude(Double.parseDouble(stationValues[3]));
//					station.setElevation(Double.parseDouble(stationValues[4]));
//					station.setMeterHeight(Double.parseDouble(stationValues[5]));
//                    
//                    if (stationValues[6].equals("Yes") || stationValues[6].equals("yes")) {
//                        station.setEarthTide(true);
//                    } else {
//                        station.setEarthTide(false);
//                    }
//					
//					stations.add(station);
//				}
				
//				scanner.close();

				// clear existing station
				stationDao.deleteAll();

                Station defaultStation = new Station();
                defaultStation.setDefaultUse(true);
                defaultStation.setStationId(context.getString(R.string.default_name));
                defaultStation.setObserverId(context.getString(R.string.default_name));
                defaultStation.setLatitude(0.0);
                defaultStation.setLongitude(0.0);
                defaultStation.setElevation(0.0);
                defaultStation.setMeterHeight(0.0);
                defaultStation.setEarthTide(true);
                stations.add(defaultStation);
                
				// save the new stations
				for (Station station : stations) {
				    stationDao.createOrUpdate(station);
				}

				result.setSuccess(true);
			}
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorMessage(context.getString(R.string.file_read_failed, lineCount));
		}
		
		return result;
	}
	
	
	public static class ImportResult {
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
