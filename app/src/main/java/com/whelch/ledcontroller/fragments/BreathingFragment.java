package com.whelch.ledcontroller.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Switch;

import com.whelch.ledcontroller.Easing;
import com.whelch.ledcontroller.MainActivity;
import com.whelch.ledcontroller.R;

public class BreathingFragment extends Fragment implements CompoundButton.OnCheckedChangeListener, View.OnTouchListener, NumberPicker.OnValueChangeListener, View.OnClickListener, AdapterView.OnItemSelectedListener {
	
	private NumberPicker minIntensityPicker;
	private NumberPicker maxIntensityPicker;
	private NumberPicker durationPicker;
	private Spinner easingSpinner;
	
	private boolean active = false;
	private byte minIntensity = 0;
	private byte maxIntensity = (byte)255;
	private byte duration = 5;
	private byte easing = 0;
	
	private MainActivity activity;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.breathing, container, false);
		activity = (MainActivity) getActivity();
		
		((Switch)rootView.findViewById(R.id.breathingSwitch)).setOnCheckedChangeListener(this);
		rootView.findViewById(R.id.breathingSendButton).setOnClickListener(this);
		
		minIntensityPicker = (NumberPicker) rootView.findViewById(R.id.breathingMinIntensityPicker);
		minIntensityPicker.setMinValue(0);
		minIntensityPicker.setMaxValue(255);
		minIntensityPicker.setValue(minIntensity);
		minIntensityPicker.setOnValueChangedListener(this);
		minIntensityPicker.setOnTouchListener(this);
		
		maxIntensityPicker = (NumberPicker) rootView.findViewById(R.id.breathingMaxIntensityPicker);
		maxIntensityPicker.setMinValue(0);
		maxIntensityPicker.setMaxValue(255);
		maxIntensityPicker.setValue(maxIntensity);
		maxIntensityPicker.setOnValueChangedListener(this);
		maxIntensityPicker.setOnTouchListener(this);
		
		durationPicker = (NumberPicker) rootView.findViewById(R.id.breathingDurationPicker);
		durationPicker.setFormatter(value -> value + "s");
		durationPicker.setMinValue(1);
		durationPicker.setMaxValue(20);
		durationPicker.setValue(duration);
		durationPicker.setWrapSelectorWheel(false);
		durationPicker.setOnValueChangedListener(this);
		durationPicker.setOnTouchListener(this);
		
		easingSpinner = ((Spinner)rootView.findViewById(R.id.breathingEasingSpinner));
		easingSpinner.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, Easing.values()));
		easingSpinner.setSelection(easing);
		easingSpinner.setOnItemSelectedListener(this);
		easingSpinner.setOnTouchListener(this);
		
		return rootView;
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.breathingSendButton:
				activity.sendCommand(constructCommand());
				break;
		}
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch(buttonView.getId()) {
			case R.id.breathingSwitch:
				active = isChecked;
				break;
		}
	}
	
	@Override
	public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
		switch(picker.getId()) {
			case R.id.breathingDurationPicker:
				duration = (byte) newVal;
				break;
			case R.id.breathingMinIntensityPicker:
				minIntensity = (byte) newVal;
				break;
			case R.id.breathingMaxIntensityPicker:
				maxIntensity = (byte) newVal;
				break;
		}
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		switch (parent.getId()) {
			case R.id.breathingEasingSpinner:
				easing = ((Easing) parent.getItemAtPosition(position)).value;
		}
	}
	
	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (v.getId()) {
//			case R.id.breathingMinIntensityPicker:
//				return !active;
//			case R.id.breathingMaxIntensityPicker:
//				return !active;
//			case R.id.breathingDurationPicker:
//				return !active;
//			case R.id.breathingEasingSpinner:
//				return !active;
			default:
				return false;
		}
	}
	
	private byte[] constructCommand() {
		byte[] command = new byte[6];
		
		command[0] = (byte) 'b';
		command[1] = (byte) (active ? '+' : '-');
		command[2] = minIntensity;
		command[3] = maxIntensity;
		command[4] = duration;
		command[5] = easing;
		
		return command;
	}
}
