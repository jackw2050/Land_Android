package com.zlscorp.ultragrav.activity.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
//import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DecimalFormat;

import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.activity.fragment.AbstractBaseFragment.NumberPadCallback;

public class NumberPadDialog extends DialogFragment {

    private View keys[] = new View[19];
    private EditText masterText;
    private TextView currentText;
    private TextView fieldNameText;
    private String fieldName;
    
    private NumberPadCallback callback;

    private Button enterButton;
    private Button nextButton; 

    public void bindToEditText(EditText editText, String name) {
        this.masterText = editText;
        fieldName = name;
    }
    
    public void setCallback(NumberPadCallback callback) {
        this.callback = callback;
    }
    
    private void setEnterNextButtons(boolean value) {
        nextButton.setEnabled(value);
        enterButton.setEnabled(value);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_number_pad, container, false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        fieldNameText = (TextView) view.findViewById(R.id.field_name);
        fieldNameText.setText(fieldName);
        enterButton = (Button) view.findViewById(R.id.keypad_enter);
        nextButton = (Button) view.findViewById(R.id.keypad_next);

        // Test
//        masterText.setText("-5.4245E");       

        // Get current value of EditText field and validate it.
        currentText = (TextView) view.findViewById(R.id.keypad_output);
        currentText.setText(masterText.getText().toString());       

        if (isValidNumber(masterText.getText().toString())) {
//            nextButton.setEnabled(true);
            setEnterNextButtons(true);
        } else {
//            nextButton.setEnabled(false);
            setEnterNextButtons(false);
        }

        findKeys(view);
        setListeners();

        getDialog().setCanceledOnTouchOutside(false);

        return view;
    }

    /**
     * This method adds all the views to the array of keys.
     */
    private void findKeys(View view) {
        
        keys[0] = view.findViewById(R.id.keypad_1);
        keys[1] = view.findViewById(R.id.keypad_2);
        keys[2] = view.findViewById(R.id.keypad_3);
        keys[3] = view.findViewById(R.id.keypad_4);
        keys[4] = view.findViewById(R.id.keypad_5);
        keys[5] = view.findViewById(R.id.keypad_6);
        keys[6] = view.findViewById(R.id.keypad_7);
        keys[7] = view.findViewById(R.id.keypad_8);
        keys[8] = view.findViewById(R.id.keypad_9);
        keys[9] = view.findViewById(R.id.keypad_0);
        keys[10] = view.findViewById(R.id.keypad_decimal);
        keys[11] = view.findViewById(R.id.keypad_E);
        keys[12] = view.findViewById(R.id.keypad_minus);
        keys[13] = view.findViewById(R.id.keypad_plus);
        keys[14] = view.findViewById(R.id.keypad_backspace);
        keys[15] = view.findViewById(R.id.keypad_cancel);
        keys[16] = view.findViewById(R.id.keypad_clear);
        keys[17] = view.findViewById(R.id.keypad_next);
        keys[18] = view.findViewById(R.id.keypad_enter);
    }

    /**
     * This method sets an action to each button.
     */
    private void setListeners() {
        for (int i = 0; i < keys.length; i++) {
            keys[i].setOnClickListener(onClickHandler);
        }
    }

    /**
     * This method validates the number pulled from the calling EditText field.
     * The following form is allowed: <+/->d<.d><ddddddddd><E><+/->d<d>
     */
//    private CharSequence validateNumber(TextView editText) {
    private boolean isValidNumber(String string) {

//        int mLength = editText.getText().length();
        int mLength = string.length();
//        CharSequence result = "";
        char[] resultBuffer = new char[mLength];
        char currentChar;
        char lastCharEntered;
        Boolean badNum = false;
        Boolean completeNum = false;
        
        for (int index = 0 ; index < mLength ; index++) {
//            currentChar = editText.getText().charAt(index);
            currentChar = string.charAt(index);

            if (index > 0) {
                lastCharEntered = resultBuffer[index-1];
            } else {
                lastCharEntered = '\0';
            }

            switch (currentChar) {
                case '+':
                case '-':
                    // Only allow a +/- sign as the first character or right after the 'e'
                    if ((index == 0) || (lastCharEntered == 'E')) {
                        resultBuffer[index] = currentChar;
                        completeNum = false;
                    } else {
                        badNum = true;
                    }
                    break;

                case 'E':
                    // Only allow the 'E' after a digit, and only one of them.
                    if ((index > 0) && Character.isDigit(lastCharEntered) && 
                            (resultBuffer.toString().indexOf('E') == -1)) {
                        resultBuffer[index] = currentChar;
                        completeNum = false;
                    } else {
                        badNum = true;
                    }
                    break;

                case '.':
                    // Only allow a decimal point after the first digit.
                    if ((index == 1 || index == 2) && (Character.isDigit(lastCharEntered))) {
                        resultBuffer[index] = currentChar;
                        completeNum = false;
                    } else {
                        badNum = true;
                    }
                    break;

                default:
                    /* Key pressed = 0 - 9
                    * Only allow one digit before the decimal and up to 10 after it, 
                    * and a max of 2 after the 'e'.
                    */
                    
                    // Verify that a digit button was pressed
                    if (Character.isDigit(currentChar)) {            
                        String resultSoFar = new String(resultBuffer);

                        // Has 'E' been entered yet?
                        int ePosition = resultSoFar.indexOf('E');
                        int unsignedIndex = index;
                        if (ePosition == -1) {
                            // 'E' is not in the string
                            if ((index > 0) && (resultBuffer[0] == '+' || resultBuffer[0] == '-')) {
                                // If a sign is present, account for it
                                unsignedIndex = index - 1;           
                            }
                            // '0' is not allowed before the decimal
//                            if (((unsignedIndex == 0) && currentChar != '0') || 
//                            (unsignedIndex > 1 && unsignedIndex < 12)) {
                            if (unsignedIndex < 12) {
                                resultBuffer[index] = currentChar;
                                completeNum = true;
                            } else {
                                badNum = true;
                            }
                        } else {
                            // 'E' is in the string
                            char nextToLastChar = resultBuffer[index-2]; 
                            if ((lastCharEntered == 'E' || lastCharEntered == '+' || 
                                    lastCharEntered == '-')) {
                                resultBuffer[index] = currentChar;
                                completeNum = true;
                            } else if (Character.isDigit(lastCharEntered) && (nextToLastChar == 'E' || 
                                    nextToLastChar == '+' || nextToLastChar == '-')) {
                                resultBuffer[index] = currentChar;
                                completeNum = true;
                            } else {
                                badNum = true;
                            }
                        }
                    }
                    break;
            }

            if (badNum) {
                break;
            }
        }
//        if (completeNum && !badNum) {
//            result = new String(resultBuffer);
//        }
//        return result;
        return completeNum && !badNum;
    }


    /**
     * This handler defines all the actions of the buttons. The rules are written to force numbers 
     * that comply with normalized scientific notation. i.e. <+/->d<.d><ddddddddd>E<+/->d<d> The E 
     * button displays an "E". The Cancel button quits without making any changes. The Enter button 
     * closes the dialog and passes the entered value into the calling EditText. The Backspace button 
     * deletes the last character entered. The Clear button clears the dialog TextView.
     */
    private View.OnClickListener onClickHandler = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            int mLength = currentText.getText().length();
            char lastCharEntered;
            if (mLength > 0) {
                lastCharEntered = currentText.getText().charAt(mLength - 1);
            } else {
                lastCharEntered = '\0';
            }

            switch (v.getId()) {
                case R.id.keypad_cancel:
                    dismiss();
                    if (callback != null) {
                        callback.onCancelClicked();
                    }
                    break;

                case R.id.keypad_clear:
                    currentText.setText("");
//                    nextButton.setEnabled(true);
                    setEnterNextButtons(true);
                    break;

                case R.id.keypad_enter:
//                    if (currentText.getText().toString().length() > 0) {
//                        masterText.setText(new DecimalFormat("0.#########E0").
//                                format(Double.parseDouble(currentText.getText().toString())));
//                    } else {
//                        masterText.setText("");
//                    }
//
//                    dismiss();
//                    break;

                case R.id.keypad_next:
                    if (currentText.getText().toString().length() > 0) {
                        masterText.setText(new DecimalFormat("0.#########E0").
                                format(Double.parseDouble(currentText.getText().toString())).replace(",", "."));
                    } else {
                        masterText.setText("");
                    }

                    dismiss();
                    if (callback != null) {
                        if (v.getId() == R.id.keypad_next) {
                            // Next was clicked
                            callback.onNextClicked();
                        } else {
                            // Enter was clicked. Enter returns the number, then has the same 
                            // action as Cancel.
                            callback.onCancelClicked();
                        }
                    }
                    break;

                case R.id.keypad_backspace:
                    if (mLength > 0) {
                        currentText.setText(currentText.getText().subSequence(0, mLength - 1));
                        
                        if (isValidNumber(currentText.getText().toString())) {
//                            nextButton.setEnabled(true);
                            setEnterNextButtons(true);
                        } else {
//                            nextButton.setEnabled(false);
                            setEnterNextButtons(false);
                        }

//                        String validatedString = validateNumber(currentText).toString();
//                        String currentString = currentText.getText().toString();
//
//                        if (currentString.equals(validatedString)) {
//                            nextButton.setEnabled(true);
//                        } else {
//                            nextButton.setEnabled(false);
//                        }
                    }
                    break;

                case R.id.keypad_plus:
                case R.id.keypad_minus:
                    // Only allow a +/- sign as the first character or right after the 'E'
                    if ((mLength == 0) || (lastCharEntered == 'E')) {
                        currentText.append(((Button) v).getText());
                    }
                    break;

                case R.id.keypad_E:
                    // Only allow the 'E' after a digit, and only one of them.
                    if ((mLength > 0) && Character.isDigit(lastCharEntered) && 
                            (currentText.getText().toString().indexOf('E') == -1)) {
                        currentText.append(((Button) v).getText());
//                        nextButton.setEnabled(false);
                        setEnterNextButtons(false);
                    }
                    break;

                case R.id.keypad_decimal:
                    // Only allow a decimal point after the first digit.
                    if ((mLength == 1 || mLength == 2) && (Character.isDigit(lastCharEntered))) {
                        currentText.append(((Button) v).getText());
//                        nextButton.setEnabled(false);
                        setEnterNextButtons(false);
                    }
                    break;

                default:
                    /* Key pressed = 0 - 9
                    * Only allow one digit before the decimal and up to 10 after it, 
                    * and a max of 2 after the 'e'.
                    */ 
                    
                    // Verify that a digit button was pressed
                    if (Character.isDigit((((Button) v).getText()).charAt(0))) {         
                        // Has 'E' been entered yet?
                        int ePosition = currentText.getText().toString().indexOf('E');   
                        if (ePosition == -1) {
                            // 'E' is not in the string
                            if ((mLength > 0) && (currentText.getText().charAt(0) == '+' || 
                                    currentText.getText().charAt(0) == '-')) {
                                // If a sign is present, dec mLength to account for it
                                mLength--;                                               
                            }
                            // '0' is not allowed before the decimal
//                            if (((mLength == 0) && ((((Button) v).getText()).charAt(0) != '0')) || 
//                                    (mLength > 1 && mLength < 12)) {
                            if (mLength < 12) {
                                 currentText.append(((Button) v).getText());
//                                 nextButton.setEnabled(true);
                                 setEnterNextButtons(true);
                            }
                        } else {
                            // 'E' is in the string
                            char nextToLastChar = currentText.getText().charAt(mLength - 2); 
                            if ((lastCharEntered == 'E' || lastCharEntered == '+' || lastCharEntered == '-')) {
                                currentText.append(((Button) v).getText());
//                                nextButton.setEnabled(true);
                                setEnterNextButtons(true);
                            } else if (Character.isDigit(lastCharEntered) && (nextToLastChar == 'E' || 
                                    nextToLastChar == '+' || nextToLastChar == '-')) {
                                currentText.append(((Button) v).getText());
//                                nextButton.setEnabled(true);
                                setEnterNextButtons(true);
                            }
                        }
                    }
                    break;
            }
        }
    };
}
