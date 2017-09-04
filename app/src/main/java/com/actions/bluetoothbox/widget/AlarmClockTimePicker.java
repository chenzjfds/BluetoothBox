package com.actions.bluetoothbox.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.TimePicker;

public class AlarmClockTimePicker extends TimePicker {

	public AlarmClockTimePicker(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public AlarmClockTimePicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public AlarmClockTimePicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
			ViewParent p = getParent();
			if (p != null)
				p.requestDisallowInterceptTouchEvent(true);
		}

		return false;
	}
}
