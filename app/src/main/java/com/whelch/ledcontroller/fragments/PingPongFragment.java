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
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;

import com.whelch.ledcontroller.Easing;
import com.whelch.ledcontroller.MainActivity;
import com.whelch.ledcontroller.R;

public class PingPongFragment extends Fragment implements CompoundButton.OnCheckedChangeListener, View.OnTouchListener, NumberPicker.OnValueChangeListener, SeekBar.OnSeekBarChangeListener, View.OnClickListener, AdapterView.OnItemSelectedListener {
	
	private SeekBar spreadSeekBar;
	private NumberPicker durationPicker;
	private Switch darkSwitch;
	private Spinner easingSpinner;
	
	private boolean active = false;
	private byte spread = 4;
	private byte duration = 5;
	private boolean dark = false;
	private byte easing = 0;
	
	private MainActivity activity;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.ping_pong, container, false);
		activity = (MainActivity) getActivity();
		
		((Switch)rootView.findViewById(R.id.pingPongSwitch)).setOnCheckedChangeListener(this);
		rootView.findViewById(R.id.pingPongSendButton).setOnClickListener(this);
		
		spreadSeekBar = ((SeekBar)rootView.findViewById(R.id.pingPongSpreadSeekbar));
		spreadSeekBar.setProgress(spread);
		spreadSeekBar.setOnSeekBarChangeListener(this);
		spreadSeekBar.setOnTouchListener(this);
		
		durationPicker = (NumberPicker) rootView.findViewById(R.id.pingPongDurationPicker);
		durationPicker.setFormatter(value -> value + "s");
		durationPicker.setMinValue(1);
		durationPicker.setMaxValue(20);
		durationPicker.setValue(duration);
		durationPicker.setWrapSelectorWheel(false);
		durationPicker.setOnValueChangedListener(this);
		durationPicker.setOnTouchListener(this);
		
		darkSwitch = ((Switch)rootView.findViewById(R.id.pingPongDarkSwitch));
		darkSwitch.setChecked(dark);
		darkSwitch.setOnCheckedChangeListener(this);
		darkSwitch.setOnTouchListener(this);
		
		easingSpinner = ((Spinner)rootView.findViewById(R.id.pingPongEasingSpinner));
		easingSpinner.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, Easing.values()));
		easingSpinner.setSelection(easing);
		easingSpinner.setOnItemSelectedListener(this);
		easingSpinner.setOnTouchListener(this);
		
		return rootView;
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.pingPongSendButton:
				activity.sendCommand(constructCommand());
				break;
		}
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch(buttonView.getId()) {
			case R.id.pingPongSwitch:
				active = isChecked;
				break;
			case R.id.pingPongDarkSwitch:
				dark = isChecked;
				break;
		}
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		switch(seekBar.getId()) {
			case R.id.pingPongSpreadSeekbar:
				spread = (byte) (progress+1);
				break;
		}
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	
	}
	
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	
	}
	
	@Override
	public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
		switch (picker.getId()) {
			case R.id.pingPongDurationPicker:
				duration = (byte)newVal;
				break;
		}
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			switch (parent.getId()) {
				case R.id.pingPongEasingSpinner:
					easing = ((Easing) parent.getItemAtPosition(position)).value;
			}
	}
	
	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (v.getId()) {
//			case R.id.pingPongDarkSwitch:
//				return !active;
//			case R.id.pingPongSpreadSeekbar:
//				return !active;
//			case R.id.pingPongDurationPicker:
//				return !active;
			default:
				return false;
		}
	}
	
	private byte[] constructCommand() {
		int extraBytes = 0;
		if (dark) {
			extraBytes++;
		}
		byte[] command = new byte[5 + extraBytes];
		
		command[0] = (byte) 'p';
		command[1] = (byte) (active ? '+' : '-');
		command[2] = spread;
		command[3] = duration;
		command[4] = easing;
		
		int optionByte = 5;
		if (dark) {
			command[optionByte++] = 'd';
		}
		
		return command;
	}
}
