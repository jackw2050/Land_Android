package com.zlscorp.ultragrav.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import android.content.Context;

import com.google.inject.Inject;
import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.model.CalibratedDialValue;
import com.zlscorp.ultragrav.persist.CalibratedDialValueDao;


public class CalibrationImporter {
	
	private static final int EXPECTED_VALUE_COUNT = 133;
	
	@Inject
	private Context context;
	
	@Inject
	private CalibratedDialValueDao calibratedDialValueDao;
	
	
	public ImportResult importCalibration(String inputFilePath) {
		
		ImportResult result = new ImportResult();
		
		int lineCount = 0;
		try {
			File inputFile = new File(inputFilePath);
			
			if (!inputFile.isFile() || !inputFile.canRead()) {
				result.setSuccess(false);
				result.setErrorMessage(context.getString(R.string.no_file_read_permissions));
			} else {
				List<CalibratedDialValue> calibratedDialValues = new ArrayList<CalibratedDialValue>(EXPECTED_VALUE_COUNT);
				
				Scanner scanner = new Scanner(inputFile).useDelimiter("\r?\n");
				
				int index = 0;
				while(scanner.hasNext()) {
					lineCount++;
					String line = scanner.next();
					
					if (line.startsWith("#")) { // ignore comment lines
						continue;
					}
					
					line = line.trim();
					double value = Double.parseDouble(line);
					
					CalibratedDialValue calibratedDialValue = new CalibratedDialValue();
					calibratedDialValue.setIndex(index);
					calibratedDialValue.setDialValue(value);
					
					calibratedDialValues.add(calibratedDialValue);
					
					index++;
				}
				
				scanner.close();
				
				// check we got the expected count
				if (calibratedDialValues.size() != EXPECTED_VALUE_COUNT) {
					result.setSuccess(false);
					result.setErrorMessage(context.getString(R.string.calibration_import_expected_count));
				} else {
	                // clear existing calibration values
	                calibratedDialValueDao.deleteAll();
	                
	                // save the new calibration values
	                for (CalibratedDialValue calibratedDialValue : calibratedDialValues) {
	                    calibratedDialValueDao.createOrUpdate(calibratedDialValue);
	                }
	                
	                result.setSuccess(true);
				}
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
