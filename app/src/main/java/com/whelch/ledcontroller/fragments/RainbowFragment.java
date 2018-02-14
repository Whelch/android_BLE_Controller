package com.whelch.ledcontroller.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.Switch;

import com.whelch.ledcontroller.MainActivity;
import com.whelch.ledcontroller.R;

public class RainbowFragment extends Fragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener, NumberPicker.OnValueChangeListener, View.OnTouchListener {

	private Switch flowSwitch;
	private SeekBar repeatSeekBar;
	private NumberPicker durationPicker;
	
	private boolean active = false;
	private byte repeat = 1;
	private boolean flow = true;
	private byte duration = 3;
	
	private MainActivity activity;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.rainbow, container, false);
		activity = (MainActivity) getActivity();
		
		((Switch)rootView.findViewById(R.id.rainbowSwitch)).setOnCheckedChangeListener(this);
		rootView.findViewById(R.id.rainbowSendButton).setOnClickListener(this);
		
		repeatSeekBar = ((SeekBar)rootView.findViewById(R.id.rainbowRepeatSeekbar));
		repeatSeekBar.setProgress(repeat);
		repeatSeekBar.setOnSeekBarChangeListener(this);
		repeatSeekBar.setOnTouchListener(this);
		
		flowSwitch = ((Switch)rootView.findViewById(R.id.rainbowFlowSwitch));
		flowSwitch.setChecked(flow);
		flowSwitch.setOnCheckedChangeListener(this);
		flowSwitch.setOnTouchListener(this);
		
		durationPicker = (NumberPicker) rootView.findViewById(R.id.rainbowDurationPicker);
		durationPicker.setFormatter(value -> value + "s");
		durationPicker.setMinValue(1);
		durationPicker.setMaxValue(20);
		durationPicker.setValue(duration);
		durationPicker.setWrapSelectorWheel(false);
		durationPicker.setOnValueChangedListener(this);
		durationPicker.setOnTouchListener(this);
		
		return rootView;
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.rainbowSendButton:
				activity.sendCommand(constructCommand());
				break;
		}
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch(buttonView.getId()) {
			case R.id.rainbowSwitch:
				active = isChecked;
				break;
			case R.id.rainbowFlowSwitch:
				flow = isChecked;
				if (isChecked) {
					duration = (byte)durationPicker.getValue();
				} else {
					duration = 0;
				}
				break;
		}
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		switch(seekBar.getId()) {
			case R.id.rainbowRepeatSeekbar:
				repeat = (byte) progress;
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
		duration = (byte)newVal;
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (v.getId()) {
//			case R.id.rainbowFlowSwitch:
//				return !active;
//			case R.id.rainbowRepeatSeekbar:
//				return !active;
//			case R.id.rainbowDurationPicker:
//				return !(active && flow);
			default:
				return false;
		}
	}
	
	private byte[] constructCommand() {
		byte[] command = new byte[4];
		
		command[0] = (byte) 'r';
		command[1] = (byte) (active ? '+' : '-');
		command[2] = repeat;
		command[3] = duration;
		
		return command;
	}
}
