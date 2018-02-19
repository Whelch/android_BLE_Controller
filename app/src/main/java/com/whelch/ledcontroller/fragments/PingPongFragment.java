package com.whelch.ledcontroller.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;

import com.whelch.ledcontroller.Easing;
import com.whelch.ledcontroller.MainActivity;
import com.whelch.ledcontroller.R;
import com.whelch.ledcontroller.callbacks.PingPongCallback;
import com.whelch.ledcontroller.model.PingPongState;
import com.whelch.ledcontroller.model.StateType;

public class PingPongFragment extends Fragment implements PingPongCallback, View.OnClickListener {
	
	private Switch pingPongSwitch;
	private SeekBar spreadSeekBar;
	private NumberPicker durationPicker;
	private Switch darkSwitch;
	private ArrayAdapter<Easing> easingAdapter;
	private Spinner easingSpinner;
	
	private MainActivity activity;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.ping_pong, container, false);
		activity = (MainActivity) getActivity();
		
		pingPongSwitch = ((Switch)rootView.findViewById(R.id.pingPongSwitch));
		rootView.findViewById(R.id.pingPongSendButton).setOnClickListener(this);
		
		spreadSeekBar = ((SeekBar)rootView.findViewById(R.id.pingPongSpreadSeekbar));
		
		durationPicker = (NumberPicker) rootView.findViewById(R.id.pingPongDurationPicker);
		durationPicker.setFormatter(value -> value + "s");
		durationPicker.setMinValue(1);
		durationPicker.setMaxValue(20);
		durationPicker.setWrapSelectorWheel(false);
		
		darkSwitch = ((Switch)rootView.findViewById(R.id.pingPongDarkSwitch));
		
		easingSpinner = ((Spinner)rootView.findViewById(R.id.pingPongEasingSpinner));
		easingAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, Easing.values());
		easingSpinner.setAdapter(easingAdapter);
		
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		onChange((PingPongState) activity.registerCallback(StateType.pingPong, this));
	}
	
	@Override
	public void onPause() {
		super.onPause();
		activity.unregisterCallback(StateType.pingPong, this);
	}
	
	@Override
	public void onChange(PingPongState state) {
		if (state.active != pingPongSwitch.isChecked()) {
			pingPongSwitch.setChecked(state.active);
		}
		if (state.spread != (spreadSeekBar.getProgress() + 1)) {
			spreadSeekBar.setProgress(state.spread - 1);
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
			case R.id.pingPongSendButton:
				activity.sendCommand(constructCommand());
				break;
		}
	}
	
	private byte[] constructCommand() {
		int extraBytes = 0;
		boolean dark = darkSwitch.isChecked();
		if (dark) {
			extraBytes++;
		}
		byte[] command = new byte[5 + extraBytes];
		
		command[0] = (byte) 'p';
		command[1] = (byte) (pingPongSwitch.isChecked() ? '+' : '-');
		command[2] = (byte) (spreadSeekBar.getProgress() + 1);
		command[3] = (byte) durationPicker.getValue();
		command[4] = easingAdapter.getItem(easingSpinner.getSelectedItemPosition()).value;
		
		int optionByte = 5;
		if (dark) {
			command[optionByte++] = 'd';
		}
		
		return command;
	}
}
