package com.whelch.ledcontroller.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.Switch;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.whelch.ledcontroller.MainActivity;
import com.whelch.ledcontroller.R;

import java.nio.ByteBuffer;

public class MainFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, NumberPicker.OnValueChangeListener {
	
	private Switch leftRailSwitch;
	private Switch leftPostSwitch;
	private Switch rightRailSwitch;
	private Switch rightPostSwitch;
	private NumberPicker intensityNumberPicker;
	
	private byte red = (byte) 255;
	private byte green = (byte) 255;
	private byte blue = (byte) 255;
	private byte intensity = (byte) 255;
	
	private MainActivity activity;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.main, container, false);
		activity = (MainActivity) getActivity();
		
		rootView.findViewById(R.id.mainColorButton).setOnClickListener(this);
		rootView.findViewById(R.id.mainAllOnButton).setOnClickListener(this);
		rootView.findViewById(R.id.mainAllOffButton).setOnClickListener(this);
		rootView.findViewById(R.id.mainSendButton).setOnClickListener(this);
		
		leftRailSwitch = ((Switch)rootView.findViewById(R.id.mainLeftRailSwitch));
		leftRailSwitch.setOnCheckedChangeListener(this);
		
		leftPostSwitch = ((Switch)rootView.findViewById(R.id.mainLeftPostSwitch));
		leftPostSwitch.setOnCheckedChangeListener(this);
		
		rightRailSwitch = ((Switch)rootView.findViewById(R.id.mainRightRailSwitch));
		rightRailSwitch.setOnCheckedChangeListener(this);
		
		rightPostSwitch = ((Switch)rootView.findViewById(R.id.mainRightPostSwitch));
		rightPostSwitch.setOnCheckedChangeListener(this);
		
		intensityNumberPicker = (NumberPicker) rootView.findViewById(R.id.mainIntensityPicker);
		intensityNumberPicker.setMinValue(0);
		intensityNumberPicker.setMaxValue(255);
		intensityNumberPicker.setValue(intensity);
		intensityNumberPicker.setOnValueChangedListener(this);
		
		return rootView;
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.mainColorButton:
				ColorPickerDialogBuilder
						.with(activity, R.style.Theme_AppCompat)
						.setTitle("Choose Color")
						.initialColor((0xff << 24) | (red << 16) | (green << 8) | blue)
						.lightnessSliderOnly()
						.wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
						.density(32)
						.setPositiveButton("Send", (dialogInterface, selectedColor, allColors) -> {
							byte[] intBytes = ByteBuffer.allocate(4).putInt(selectedColor).array();
							red = intBytes[1];
							green = intBytes[2];
							blue = intBytes[3];
							
							byte[] command = new byte[4];
							
							command[0] = (byte) 'c';
							command[1] = red;
							command[2] = green;
							command[3] = blue;
							
							activity.sendCommand(command);
						})
						.setNegativeButton("Cancel", null)
						.build()
						.show();
				break;
			case R.id.mainAllOnButton:
				byte[] onCommand = new byte[2];
				
				onCommand[0] = (byte) 'a';
				onCommand[1] = '+';
				
				activity.sendCommand(onCommand);
				break;
			case R.id.mainAllOffButton:
				byte[] offCommand = new byte[2];
				
				offCommand[0] = (byte) 'a';
				offCommand[1] = '-';
				
				activity.sendCommand(offCommand);
				break;
			case R.id.mainSendButton:
				activity.sendCommand(constructCommand());
					break;
		}
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		byte[] command = new byte[3];
		
		command[0] = (byte) 't';
		
		switch(buttonView.getId()) {
			case R.id.mainLeftRailSwitch:
				command[1] = 0;
				break;
			case R.id.mainLeftPostSwitch:
				command[1] = 1;
				break;
			case R.id.mainRightRailSwitch:
				command[1] = 2;
				break;
			case R.id.mainRightPostSwitch:
				command[1] = 3;
				break;
		}
		command[2] = (byte) (isChecked ? '+' : '-');
		
		activity.sendCommand(command);
	}
	
	@Override
	public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
		switch (picker.getId()) {
			case R.id.mainIntensityPicker:
				intensity = (byte) newVal;
		}
	}
	
	private byte[] constructCommand() {
		byte[] command = new byte[2];
		
		command[0] = (byte) 'i';
		command[1] = intensity;
		
		return command;
	}
}
