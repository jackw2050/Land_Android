package com.zlscorp.ultragrav.activity.validate;

import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import com.google.inject.Inject;
import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.file.ErrorHandler;

public class Validator {

    @Inject
    private Context context;

    private Map<Object, Validatable> inputs = new LinkedHashMap<Object, Validatable>();

    private Set<Object> failedInputs = new HashSet<Object>();

    private Set<ValidationListener> listeners = new LinkedHashSet<ValidationListener>();

    public void addValidationListener(ValidationListener listener) {
        listeners.add(listener);
    }

    public void validateAsDate(EditText yearText, EditText monthText, EditText dayText, 
                               int minYear, int maxYear) {
        MyDateWatcher dateWatcher = new MyDateWatcher(yearText, monthText, dayText, minYear, maxYear);
        yearText.addTextChangedListener(dateWatcher);
        monthText.addTextChangedListener(dateWatcher);
        dayText.addTextChangedListener(dateWatcher);

        yearText.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputs.put(yearText, dateWatcher);

        monthText.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputs.put(monthText, dateWatcher);

        dayText.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputs.put(dayText, dateWatcher);
    }
    
    public void validateAsInteger(Integer value) {
    }

    public void validateAsInteger(EditText editText) {
        MyIntegerWatcher v = new MyIntegerWatcher(editText);
        editText.addTextChangedListener(v);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        inputs.put(editText, v);
    }

    public void validateAsInteger(EditText editText, int lower) {
        validateAsInteger(editText, lower, 65535, 1);
    }

    public void validateAsInteger(EditText editText, int lower, int upper) {
        validateAsInteger(editText, lower, upper, 1);
    }

    public void validateAsInteger(EditText editText, int lower, int upper, int mod) {
        MyIntegerWatcher v = new MyIntegerWatcher(editText, lower, upper, mod);
        editText.addTextChangedListener(v);
        int inputType = InputType.TYPE_CLASS_NUMBER;
        if (lower < 0) {
            inputType = inputType | InputType.TYPE_NUMBER_FLAG_SIGNED;
        }
        editText.setInputType(inputType);
        inputs.put(editText, v);
    }

    public void validateAsDouble(EditText editText) {
        MyDoubleWatcher v = new MyDoubleWatcher(editText);
        editText.addTextChangedListener(v);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | 
                              InputType.TYPE_NUMBER_FLAG_DECIMAL | 
                              InputType.TYPE_NUMBER_FLAG_SIGNED);
        inputs.put(editText, v);
    }

    public void validateAsDouble(EditText editText, double lower, double upper) {
        MyDoubleWatcher v = new MyDoubleWatcher(editText, lower, upper);
        editText.addTextChangedListener(v);
        int inputType = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
        if (lower < 0.0) {
            inputType = inputType | InputType.TYPE_NUMBER_FLAG_SIGNED;
        }
        editText.setInputType(inputType);
        inputs.put(editText, v);
    }

    public void validateAsDoubleSciNot(EditText editText) {
        MyDoubleWatcher v = new MyDoubleWatcher(editText);
        editText.addTextChangedListener(v);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL |
                              InputType.TYPE_NUMBER_FLAG_SIGNED);
        // InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL |
        // InputType.TYPE_NUMBER_FLAG_SIGNED);
        // InputType.TYPE_CLASS_TEXT
        // editText.setKeyListener(sciKeyListener);
        inputs.put(editText, v);
    }

    public void validateAsDoubleSciNot(EditText editText, double lower, double upper) {
        MyDoubleWatcher v = new MyDoubleWatcher(editText, lower, upper);
        editText.addTextChangedListener(v);
        int inputType = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL; // InputType.TYPE_CLASS_NUMBER |
                                                                                          // InputType.TYPE_NUMBER_FLAG_DECIMAL;
        if (lower < 0.0) {
            inputType = inputType | InputType.TYPE_NUMBER_FLAG_SIGNED;
        }
        editText.setInputType(inputType);
        inputs.put(editText, v);
    }

    public void validateString(EditText editText) {
        MyStringWatcher v = new MyStringWatcher(editText);
        editText.addTextChangedListener(v);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        inputs.put(editText, v);
    }

    public void validateString(EditText editText, int maxLength) {
        MyStringWatcher v = new MyStringWatcher(editText, maxLength);
        editText.addTextChangedListener(v);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        inputs.put(editText, v);
    }

    public void validateAll() {
        for (Validatable v : inputs.values()) {
            v.isValid();
        }
    }

    public boolean isValid(Object input) {
        Validatable v = inputs.get(input);
        
        // Test
        boolean test = false;
        if (test) {
            v = null;
        }
        
        if (v == null) {
            String viewName = context.getResources().getResourceEntryName(((TextView) input).getId());

            ErrorHandler errorHandler = ErrorHandler.getInstance();
            errorHandler.logError(Level.SEVERE, "Validator.isValid(): " +
            		"View not registered as a input - " + viewName,
            		R.string.unable_to_validate_field,
            		R.string.unable_to_validate_message);
            
            return false;
        } else {
            return v.isValid();
        }
    }

    public boolean isAllValid() {
        return failedInputs.size() == 0;
    }

    public void remove(EditText input) {
        Validatable v = inputs.remove(input);
        failedInputs.remove(input);

        // uninstall textWatcher if Validatable is one
        if (v instanceof TextWatcher) {
            TextWatcher tw = (TextWatcher) v;
            input.removeTextChangedListener(tw);
        }
    }

    private void displayValid(EditText editText) {

        failedInputs.remove(editText);
        editText.setError(null);
        dispatchValidationEvent(editText, true, isAllValid());
    }

    private void displayInvalid(EditText editText, String error) {

        failedInputs.add(editText);
        editText.setError(error);
        dispatchValidationEvent(editText, false, isAllValid());
    }

    public void dispatchValidationEvent(Object input, boolean inputValid, boolean allInputsValid) {
        for (ValidationListener l : listeners) {
            l.onValidation(input, inputValid, allInputsValid);
        }
    }

    private interface Validatable {

        public boolean isValid();
    }

    private class MyStringWatcher implements TextWatcher, Validatable {

        private EditText editText;
        private Integer maxLength = null;

        // Watcher for string with no max length
        public MyStringWatcher(EditText editText) {
            this.editText = editText;
            this.maxLength = 9999; // Arbitrary maxLength to make the
                                   // verification code nicer
        }

        // Watcher for string with a specified max length
        public MyStringWatcher(EditText editText, int maxLength) {
            this.editText = editText;
            this.maxLength = maxLength;
        }

        @Override
        public void afterTextChanged(Editable s) {
            isValid();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public boolean isValid() {

            int fieldLength = editText.getText().length();
            if (fieldLength > 0 && fieldLength <= maxLength) {
                displayValid(editText);
                return true;
            } else {
                if (maxLength == 9999) {
                    displayInvalid(editText, context.getString(R.string.invalidStringNotEmpty));
                } else {
                    displayInvalid(editText, context.getString(R.string.invalidStringWithMaxLength, maxLength));
                }
                return false;
            }
        }
    }

    private boolean isLeapYear(int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        return cal.getActualMaximum(Calendar.DAY_OF_YEAR) > 365;
    }

    private class MyDateWatcher implements TextWatcher, Validatable {
        int maxDay;
        int minYear;
        int maxYear;
        byte dateError;
        EditText yearText;
        EditText monthText;
        EditText dayText;

        public MyDateWatcher(EditText yearText, EditText monthText, EditText dayText, int minYear, int maxYear) {
            this.yearText = yearText;
            this.monthText = monthText;
            this.dayText = dayText;
            this.minYear = minYear;
            this.maxYear = maxYear;
        }

        @Override
        public boolean isValid() {
            boolean returnValue = false;
            
            if ((dateError = isValidDate()) == 0) {
                
                displayValid(yearText);
                displayValid(monthText);
                displayValid(dayText);
                returnValue = true;
                
            } else {
                String message;
                if ((dateError & ((byte) 1)) != 0) {
                    message = context.getString(R.string.invalidDateRange, minYear, maxYear);
                    displayInvalid(yearText, message);
                }
                if ((dateError & ((byte) 2)) != 0) {
                    message = context.getString(R.string.invalidDateRange, 1, 12);
                    displayInvalid(monthText, message);
                }
                if ((dateError & ((byte) 4)) != 0) {
                    message = context.getString(R.string.invalidDateRange, 1, maxDay);
                    displayInvalid(dayText, message);
                }
            }
            return returnValue;
        }

        /**
         * Returns - int Error: 0 = no error, 1 = bad year, 2 = bad month, 4 =
         * bad day
         */
        private byte isValidDate() {

            byte returnValue = 0;
            String yearString = yearText.getText().toString();
            String monthString = monthText.getText().toString();
            String dayString = dayText.getText().toString();

            Integer yearValue = null;
            Integer monthValue = null;
            Integer dayValue;

            if (yearString.length() == 0) {
                returnValue |= 1;
            } else {
                try {
                    yearValue = Integer.parseInt(yearString);
                    if (yearValue < minYear || yearValue > maxYear) {
                        returnValue |= 1;
                    }
                } catch (NumberFormatException e) {
                    returnValue |= 1;
                }
            }

            if (monthString.length() == 0) {
                returnValue |= 2;
            } else {
                try {
                    monthValue = Integer.parseInt(monthString);
                    if (monthValue < 1 || monthValue > 12) {
                        returnValue |= 2;
                    }
                } catch (NumberFormatException e) {
                    returnValue |= 2;
                }
            }
            
            // If the month value is good, evaluate to find the maxDay value
            if (monthValue != null) {
                if (monthValue == 4 || monthValue == 6 || monthValue == 9 || monthValue == 11) {
                    maxDay = 30;
                } else if (monthValue == 1 || monthValue == 3 || monthValue == 5 || monthValue == 7 ||
                        monthValue == 8 || monthValue == 10 || monthValue == 12) {
                    maxDay = 31;
                } else if (monthValue == 2) {
                    if (yearValue == null) {
                        maxDay = 28;
                    } else if (isLeapYear(yearValue)) {
                        maxDay = 29;
                    } else {
                        maxDay = 28;
                    }
                }
            } else {
                maxDay = 31;
            }
                
            if (dayString.length() == 0) {
                returnValue |= 4;
            } else {
                try {
                    dayValue = Integer.parseInt(dayString);
                    if (dayValue < 1 || dayValue > maxDay) {
                        returnValue |= 4;
                    }
                } catch (NumberFormatException e) {
                    returnValue |= 4;
                }
            }
            return returnValue;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            isValid();
        }

    }

    public static boolean isValidInteger (Integer value) {
        return isValidInteger(value, null, null, null);
    }

    public static boolean isValidInteger (Integer value, Integer lower) {
        return isValidInteger(value, lower, null, null);
    }

    public static boolean isValidInteger (Integer value, Integer lower, Integer upper) {
        return isValidInteger(value, lower, upper, null);
    }

    public static boolean isValidInteger (Integer value, Integer lower, Integer upper, Integer mod) {

        boolean result = true;
        
        if (value == null) {
            result = false;
        } else {
            if (lower != null && value < lower) {
                result = false;
            }
            if (upper != null && value > upper) {
                result = false;
            }
            if (mod != null && value % mod != 0) {
                result = false;
            }
        }
        return result;
    }

    public static boolean isValidDouble (Double value) {
        return isValidDouble(value, null, null);
    }
    
    public static boolean isValidDouble (Double value, Double lower) {
        return isValidDouble(value, lower, null);
    }
    
    public static boolean isValidDouble (Double value, Double lower, Double upper) {

        boolean result = true;
        
        if (value == null) {
            result = false;
        } else {
            if (lower != null && value < lower) {
                result = false;
            }
            if (upper != null && value > upper) {
                result = false;
            }
        }
        return result;
    }
    
    private class MyIntegerWatcher implements TextWatcher, Validatable {

        private Integer lower;
        private Integer upper;
        private Integer mod;
        private EditText editText;

        public MyIntegerWatcher(EditText editText) {
            this.editText = editText;
        }

        public MyIntegerWatcher(EditText editText, int lower, int upper, int mod) {
            this.editText = editText;
            this.lower = lower;
            this.upper = upper;
            this.mod = mod;
        }

        @Override
        public void afterTextChanged(Editable s) {
            isValid();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public boolean isValid() {
            if (isValidInteger()) {
                displayValid(editText);
                return true;
            } else {
                String message;
                if (lower != null && upper != null && mod != null) {
                    if (mod == 1) {
                        message = context.getString(R.string.invalidIntegerRange, lower, upper);
                    } else {
                        message = context.getString(R.string.invalidIntegerRangeAndMultiple, lower, upper,
                                mod);
                    }
                } else {
                    message = context.getString(R.string.invalidInteger);
                }
                displayInvalid(editText, message);
                return false;
            }
        }

        private boolean isValidInteger() {

            String text = editText.getText().toString();
            if (text.length() == 0) {
                return false;
            }
            try {
                Integer value = Integer.parseInt(text);

                if (lower != null && value < lower) {
                    return false;
                }
                if (upper != null && value > upper) {
                    return false;
                }
                if (mod != null && value % mod != 0) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }

            return true;
        }
    }

    private class MyDoubleWatcher implements TextWatcher, Validatable {

        private Double lower;
        private Double upper;
        private EditText editText;

        public MyDoubleWatcher(EditText editText) {
            this.editText = editText;
        }

        public MyDoubleWatcher(EditText editText, double lower, double upper) {
            this.editText = editText;
            this.lower = lower;
            this.upper = upper;
        }

        @Override
        public void afterTextChanged(Editable s) {
            isValid();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public boolean isValid() {
            if (isValidDouble()) {
                displayValid(editText);
                return true;
            } else {
                String message;
                if (lower != null && upper != null) {
                    message = context.getString(R.string.invalidDecimalRange, lower, upper);
                } else {
                    message = context.getString(R.string.invalidDecimal);
                }
                displayInvalid(editText, message);
                return false;
            }
        }

        private boolean isValidDouble() {

            String text = editText.getText().toString();
            if (text.length() == 0) {
                return false;
            }
            try {
                Double value = Double.parseDouble(text);
                if (lower != null && value < lower) {
                    return false;
                }
                if (upper != null && value > upper) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }

            return true;
        }
    }

    public interface ValidationListener {

        public void onValidation(Object input, boolean inputValid, boolean allInputsValid);
    }
}
