package com.whelch.ledcontroller.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.whelch.ledcontroller.MainActivity;
import com.whelch.ledcontroller.R;

public class RainbowFragment extends Fragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener {

	private boolean active = false; //+ for turn on, - for turn off
	private int repeat = 1;
	private boolean flow = true;
	private int duration = 4000;
	
	private TextView durationTextView;
	private MainActivity activity;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.rainbow, container, false);
		activity = (MainActivity) getActivity();
		
		durationTextView = (TextView) rootView.findViewById(R.id.rainbowDurationText);
		
		rootView.findViewById(R.id.rainbowSendButton).setOnClickListener(this);
		
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
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		switch(seekBar.getId()) {
			case R.id.rainbowRepeatSeekbar:
				repeat = progress;
				break;
			case R.id.rainbowDurationSeekBar:
				duration = progress * 1000;
				durationTextView.setText("Duration " + progress);
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
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch(buttonView.getId()) {
			case R.id.rainbowFlowSwitch:
				flow = isChecked;
				break;
			case R.id.rainbowSwitch:
				active = isChecked;
				break;
		}
	}
	
	private String constructCommand() {
		return "r " + repeat + " " + duration;
	}
}
