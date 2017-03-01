package com.zlscorp.ultragrav.activity.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

public class NumberPadEditText extends EditText {
	
	public NumberPadEditText(Context context) {
		super(context);
	}
	
	public NumberPadEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public NumberPadEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onCheckIsTextEditor() {
		return false;
	}

}
