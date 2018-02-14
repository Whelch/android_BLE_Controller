package com.whelch.ledcontroller.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.whelch.ledcontroller.MainActivity;
import com.whelch.ledcontroller.R;

public class ColorFragment extends Fragment implements OnColorSelectedListener {
	
	private ColorPickerView colorPickerView;
	
	private int color;
	
	private MainActivity activity;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.color, container, false);
		activity = (MainActivity) getActivity();
		
		colorPickerView = (ColorPickerView) rootView.findViewById(R.id.colorPicker);
		colorPickerView.addOnColorSelectedListener(this);
		
		return rootView;
	}
	
	private byte[] constructCommand() {
		byte[] command = new byte[6];
		
//		command[0] = (byte) 'b';
//		command[1] = (byte) (active ? '+' : '-');
//		command[2] = minIntensity;
//		command[3] = maxIntensity;
//		command[4] = duration;
//		command[5] = easing;
		
		return command;
	}
	
	@Override
	public void onColorSelected(int i) {
		color = i;
	}
}
