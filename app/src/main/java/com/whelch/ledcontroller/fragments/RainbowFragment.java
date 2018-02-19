package com.whelch.ledcontroller.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.Switch;

import com.whelch.ledcontroller.MainActivity;
import com.whelch.ledcontroller.R;
import com.whelch.ledcontroller.model.StateType;
import com.whelch.ledcontroller.callbacks.RainbowCallback;
import com.whelch.ledcontroller.model.RainbowState;

public class RainbowFragment extends Fragment implements RainbowCallback, View.OnClickListener {

	private Switch rainbowSwitch;
	private Switch flowSwitch;
	private SeekBar repeatSeekBar;
	private NumberPicker durationPicker;
	
	private MainActivity activity;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.rainbow, container, false);
		activity = (MainActivity) getActivity();
		
		rainbowSwitch = ((Switch)rootView.findViewById(R.id.rainbowSwitch));
		
		repeatSeekBar = ((SeekBar)rootView.findViewById(R.id.rainbowRepeatSeekbar));
		
		flowSwitch = ((Switch)rootView.findViewById(R.id.rainbowFlowSwitch));
		
		durationPicker = (NumberPicker) rootView.findViewById(R.id.rainbowDurationPicker);
		durationPicker.setFormatter(value -> value + "s");
		durationPicker.setMinValue(1);
		durationPicker.setMaxValue(20);
		durationPicker.setWrapSelectorWheel(false);
		
		rootView.findViewById(R.id.rainbowSendButton).setOnClickListener(this);
		
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		onChange((RainbowState) activity.registerCallback(StateType.rainbow, this));
	}
	
	@Override
	public void onPause() {
		super.onPause();
		activity.unregisterCallback(StateType.rainbow, this);
	}
	
	@Override
	public void onChange(RainbowState state) {
		if (state.active != rainbowSwitch.isChecked()) {
			rainbowSwitch.setChecked(state.active);
		}
		if (state.repeat != repeatSeekBar.getProgress()) {
			repeatSeekBar.setProgress(state.repeat);
		}
		if(state.flow != flowSwitch.isChecked()) {
			flowSwitch.setChecked(state.flow);
		}
		if (state.flow && state.duration != durationPicker.getValue()) {
			durationPicker.setValue(state.duration);
		}
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.rainbowSendButton:
				activity.sendCommand(constructCommand());
				break;
		}
	}
	
	public byte[] constructCommand() {
		byte[] command = new byte[4];
		
		command[0] = (byte) 'r';
		command[1] = (byte) (rainbowSwitch.isChecked() ? '+' : '-');
		command[2] = (byte) repeatSeekBar.getProgress();
		command[3] = (byte) (flowSwitch.isChecked() ? durationPicker.getValue() : 0);
		
		return command;
	}
}
