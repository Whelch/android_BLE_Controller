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
import com.whelch.ledcontroller.callbacks.BreathingCallback;
import com.whelch.ledcontroller.model.BreathingState;
import com.whelch.ledcontroller.model.PingPongState;
import com.whelch.ledcontroller.model.StateType;

public class BreathingFragment extends Fragment implements BreathingCallback, View.OnClickListener {
	
	private Switch breathingSwitch;
	private NumberPicker minIntensityPicker;
	private NumberPicker maxIntensityPicker;
	private NumberPicker durationPicker;
	private ArrayAdapter<Easing> easingAdapter;
	private Spinner easingSpinner;
	
	private MainActivity activity;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.breathing, container, false);
		activity = (MainActivity) getActivity();
		
		breathingSwitch = ((Switch)rootView.findViewById(R.id.breathingSwitch));
		rootView.findViewById(R.id.breathingSendButton).setOnClickListener(this);
		
		minIntensityPicker = (NumberPicker) rootView.findViewById(R.id.breathingMinIntensityPicker);
		minIntensityPicker.setMinValue(0);
		minIntensityPicker.setMaxValue(255);
		
		maxIntensityPicker = (NumberPicker) rootView.findViewById(R.id.breathingMaxIntensityPicker);
		maxIntensityPicker.setMinValue(0);
		maxIntensityPicker.setMaxValue(255);
		
		durationPicker = (NumberPicker) rootView.findViewById(R.id.breathingDurationPicker);
		durationPicker.setFormatter(value -> value + "s");
		durationPicker.setMinValue(1);
		durationPicker.setMaxValue(20);
		durationPicker.setWrapSelectorWheel(false);
		
		easingSpinner = ((Spinner)rootView.findViewById(R.id.breathingEasingSpinner));
		easingAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, Easing.values());
		easingSpinner.setAdapter(easingAdapter);
		
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		onChange((BreathingState) activity.registerCallback(StateType.breathing, this));
	}
	
	@Override
	public void onPause() {
		super.onPause();
		activity.unregisterCallback(StateType.breathing, this);
	}
	
	@Override
	public void onChange(BreathingState state) {
		if (state.active != breathingSwitch.isChecked()) {
			breathingSwitch.setChecked(state.active);
		}
		if (state.minIntensity != (minIntensityPicker.getValue())) {
			minIntensityPicker.setValue(state.minIntensity);
		}
		if (state.maxIntensity != (maxIntensityPicker.getValue())) {
			maxIntensityPicker.setValue(state.maxIntensity);
		}
		if(state.duration != durationPicker.getValue()) {
			durationPicker.setValue(state.duration);
		}
		if (state.easing.value != easingAdapter.getItem(easingSpinner.getSelectedItemPosition()).value) {
			easingSpinner.setSelection(state.easing.value);
		}
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.breathingSendButton:
				activity.sendCommand(constructCommand());
				break;
		}
	}
	
	
	private byte[] constructCommand() {
		byte[] command = new byte[6];
		
		command[0] = (byte) 'b';
		command[1] = (byte) (breathingSwitch.isChecked() ? '+' : '-');
		command[2] = (byte) minIntensityPicker.getValue();
		command[3] = (byte) maxIntensityPicker.getValue();
		command[4] = (byte) durationPicker.getValue();
		command[5] = easingAdapter.getItem(easingSpinner.getSelectedItemPosition()).value;
		
		return command;
	}
}
