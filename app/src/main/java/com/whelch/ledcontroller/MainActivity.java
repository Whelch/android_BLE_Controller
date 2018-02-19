package com.whelch.ledcontroller;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.whelch.ledcontroller.callbacks.BreathingCallback;
import com.whelch.ledcontroller.model.BreathingState;
import com.whelch.ledcontroller.model.PingPongState;
import com.whelch.ledcontroller.model.StateType;
import com.whelch.ledcontroller.callbacks.PingPongCallback;
import com.whelch.ledcontroller.callbacks.RainbowCallback;
import com.whelch.ledcontroller.callbacks.StripCallback;
import com.whelch.ledcontroller.model.RainbowState;
import com.whelch.ledcontroller.model.StripState;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements View.OnClickListener {
	
	private static final String SEARCHING = "Searching";
	private static final String CONNECTED = "Connected";
	private static final String DISCONNECTED = "Disconnected";
	
	// Bluetooth LE UART instance.  This is defined in BluetoothLeUart.java.
	private BluetoothLeUart uart;
	
	private TextView statusText;
	private Button retryButton;
	private ViewPager viewPager;
	
	private List<StripCallback> stripCallbacks = new ArrayList();
	private List<RainbowCallback> rainbowCallbacks = new ArrayList();
	private List<PingPongCallback> pingPongCallbacks = new ArrayList();
	private List<BreathingCallback> breathingCallbacks = new ArrayList();
	
	private StripState stripState = new StripState(false, false, false, false);
	private RainbowState rainbowState = new RainbowState(false, (byte) 1, true, (byte) 3);
	private PingPongState pingPongState = new PingPongState(false, (byte) 4, (byte) 5, false, Easing.linear);
	private BreathingState breathingState = new BreathingState(false, (byte) 0, (byte) 255, (byte) 5, Easing.linear);
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.controller_layout);
		
		// Initialize UART.
		uart = new BluetoothLeUart(getApplicationContext(), this);
		
		statusText = (TextView) findViewById(R.id.connectionStatusText);
		
		retryButton = (Button) findViewById(R.id.connectionRetryButton);
		retryButton.setOnClickListener(this);
		
		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(new BedControllerPagerAdapter(getSupportFragmentManager()));
		viewPager.setPageTransformer(true, new ZoomOutPageTransformer());
	}
	
	// OnResume, called right before UI is displayed.  Connect to the bluetooth device.
	@Override
	protected void onResume() {
		super.onResume();
		Log.d("Bluetooth", "Scanning for devices ...");
		uart.connectFirstAvailable();
	}
	
	// OnStop, called right before the activity loses foreground focus.  Close the BTLE connection.
	@Override
	protected void onStop() {
		super.onStop();
		uart.disconnect();
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.connectionRetryButton:
				uart.connectFirstAvailable();
				break;
		}
	}
	
	/**
	 * Sends a command to the bluetooth device, if it's connected.
	 */
	public void sendCommand(byte[] command) {
		if(uart != null) {
			uart.send(command);
		}
	}
	
	public Object registerCallback(StateType type, Object callback) {
		switch(type) {
			case strip:
				stripCallbacks.add((StripCallback) callback);
				return stripState;
			case rainbow:
				rainbowCallbacks.add((RainbowCallback) callback);
				return rainbowState;
			case pingPong:
				pingPongCallbacks.add((PingPongCallback) callback);
				return pingPongState;
			case breathing:
				breathingCallbacks.add((BreathingCallback) callback);
				return breathingState;
			default:
				return null;
		}
	}
	
	public void unregisterCallback(StateType type, Object callback) {
		switch(type) {
			case strip:
				stripCallbacks.remove(callback);
				break;
			case rainbow:
				rainbowCallbacks.remove(callback);
				break;
			case pingPong:
				pingPongCallbacks.remove(callback);
				break;
			case breathing:
				breathingCallbacks.remove(callback);
				break;
		}
	}
	
	public void updateState(StateType type, Object state) {
		runOnUiThread(() -> {
			switch(type) {
				case strip:
					stripState = (StripState) state;
					stripCallbacks.forEach(callback -> callback.onChange(stripState));
					break;
				case rainbow:
					rainbowState = (RainbowState) state;
					rainbowCallbacks.forEach(callback -> callback.onChange(rainbowState));
					break;
				case pingPong:
					pingPongState = (PingPongState) state;
					pingPongCallbacks.forEach(callback -> callback.onChange(pingPongState));
					break;
				case breathing:
					breathingState = (BreathingState) state;
					breathingCallbacks.forEach(callback -> callback.onChange(breathingState));
					break;
			}
		});
	}
	
	public void setSearching() {
		runOnUiThread(() -> {
			statusText.setText(SEARCHING);
			statusText.setTextColor(getResources().getColor(R.color.searching));
			retryButton.setVisibility(View.GONE);
		});
	}
	
	public void setConnected() {
		runOnUiThread(() -> {
			statusText.setText(CONNECTED);
			statusText.setTextColor(getResources().getColor(R.color.connected));
		});
	}
	
	public void setDisconnected() {
		runOnUiThread(() -> {
			statusText.setText(DISCONNECTED);
			statusText.setTextColor(getResources().getColor(R.color.disconnected));
			retryButton.setVisibility(View.VISIBLE);
		});
	}
	
	
}
